/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.api.servlets.mobile;

import com.logistimo.AppFactory;
import com.logistimo.api.servlets.JsonRestServlet;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;
import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;

import org.apache.commons.lang.StringUtils;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.MessageService;
import com.logistimo.config.models.CapabilityConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.entities.entity.IUserToKiosk;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.api.util.GsonUtil;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.proto.RelationshipInput;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.proto.SetupDataInput;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.api.util.RESTUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SetupDataServlet extends JsonRestServlet {
  private static final XLog xLogger = XLog.getLog(SetupDataServlet.class);

  private static ITaskService taskService = AppFactory.get().getTaskService();

  // Send the user's name/password over SMS
  private static void notifyPasswordToUser(String userId, String password, Long domainId,
                                           String sendingUserId) {
    xLogger.fine("Entered notifyPasswordToUser");
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      IDomain d = ds.getDomain(domainId);
      // Get the params
      String
          message =
          "Welcome to " + d.getName() + ". You may login anytime using your user ID '" + userId
              + "' and password '" + password + "'.";
      Map<String, String> params = new HashMap<String, String>();
      params.put("message", message);
      params.put("userids", userId);
      params.put("sendinguserid", sendingUserId);
      params.put("domainid", domainId.toString());
      taskService.schedule(taskService.QUEUE_MESSAGE, "/task/communicator", params, null,
          taskService.METHOD_POST, domainId, sendingUserId, "PASSWORD_CHANGE");
    } catch (Exception e) {
      xLogger.warn(
          "{0} when notifying login credentials to user {1} (registered via mobile) in domain {2}: {3}",
          e.getClass().getName(), userId, domainId, e.getMessage());
    }

    xLogger.fine("Exiting notifyPasswordToUser");
  }

  // Update a route tag for a kiosk, if needed
  // NOTE: userId is that of user doing the registration/update of entity
  private static void updateRouteTag(Long domainId, String userId, Long kioskId, String routeTag,
                                     Integer routeIndex, Hashtable<String, String> kiosk,
                                     boolean isAdd) {
    xLogger.fine("Entered updateRouteTag");
    // First determine if this is a managed entity or related entity
    String customers = kiosk.get(JsonTagsZ.CUSTOMERS);
    String vendors = kiosk.get(JsonTagsZ.VENDORS);
    boolean hasCustomers = (customers != null && !customers.isEmpty());
    boolean hasVendors = (vendors != null && !vendors.isEmpty());
    boolean isRelatedEntity = (hasCustomers || hasVendors);
    boolean isManagedEntity = !isRelatedEntity; // Changed to true
    if (isAdd
        && isRelatedEntity) { // in case of Add of a related entity, check if it is ALSO a managed entity for this user
      String usersSemiColonSeparated = kiosk.get(JsonTagsZ.USERS);
      if (usersSemiColonSeparated != null && !usersSemiColonSeparated.isEmpty()) {
        String[] users = usersSemiColonSeparated.split(";");
        xLogger.fine("userId: {0}", userId);
        for (int i = 0; i < users.length; i++) {
          if (isManagedEntity) {
            break;
          }
          isManagedEntity = (userId.equals(users[i]));
        }
      }
    }
    xLogger.fine(
        "isManagedEntity: {0}, isRelatedEntity: {1}, isAdd: {2}, routeTag: {3}, routeIndex: {4}, kioskId: {5}",
        isManagedEntity, isRelatedEntity, isAdd, routeTag, routeIndex, kioskId);
    // Update related entity tag/index and
    xLogger.fine("Now setting route info...");
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      // If managed entity, set the managed entity route info.
      if (isManagedEntity) {
        as.setManagedEntityRouteInfo(domainId, userId, kioskId, routeTag, routeIndex);
      }
      // If related entity, set the link type. Obtain the list of linked kiosk ids for this entity.
      // TODO: Obtain the lkid list. We have only lk name list here. How to get lkids???
      if (isRelatedEntity) {
        String linkType = null;
        List<String> lkNamesList = null;
        if (hasCustomers) {
          // Set link type to vendors
          linkType = IKioskLink.TYPE_VENDOR;
          lkNamesList = getList(customers);
        } else if (hasVendors) {
          // Set link type to vendors
          linkType = IKioskLink.TYPE_CUSTOMER;
          lkNamesList = getList(vendors);
        }
        // Loop through lkNamesList
        Iterator<String> lkNamesListIter = lkNamesList.iterator();
        Long lkid = null;
        while (lkNamesListIter.hasNext()) {
          String lkName = lkNamesListIter.next();
          if (lkName != null && !lkName.isEmpty()) {
            try {
              IKiosk k = as.getKioskByName(domainId, lkName);
              if (k != null) {
                lkid = k.getKioskId();
                // Set the related entity info
                as.setRelatedEntityRouteInfo(domainId, lkid, linkType, kioskId, routeTag,
                    routeIndex);
              } else {
                xLogger.warn("Could not get linked kiosk {0}", lkName);
              }
            } catch (Exception e) {
              xLogger.warn(
                  "{0} while setting route info for related entity for kioskId: {1}, LinkType: {2}, Linked Kiosk Id: {3}. Message: {4}",
                  e.getClass().getName(), kioskId, linkType, lkid, e.getMessage());
            }
          }
        }
      }

    } catch (Exception e) {
      xLogger.warn("{0} when updating route tag {1} for kiosk {2}: {3}", e.getClass().getName(),
          routeTag, kioskId, e.getMessage());
    }

    xLogger.fine("Exiting updateRouteTag");
  }

  // Get a list of strings from  semi colon separated values
  private static List<String> getList(String ssv) {
    List<String> list = null;
    if (ssv != null && !ssv.isEmpty()) {
      String[] itemsArray = ssv.split(";");
      if (itemsArray == null || itemsArray.length == 0) {
        itemsArray = new String[1];
        itemsArray[0] = ssv;
      }
      list = new ArrayList<String>();
      for (int i = 0; i < itemsArray.length; i++) {
        list.add(itemsArray[i]);
      }
    }
    return list;
  }

  // Check whether permissions exist
  private static boolean hasSetupPermission(Long domainId, String role, String action, String type,
                                            SetupDataInput sdInput) {
    xLogger.fine("Entered hasSetupPermission");
    // Determine whether add/edit of kiosk/user
    boolean
        isAdd =
        (BulkUploadMgr.OP_ADD.equals(action) ? true : false); // if not isAdd, then it is edit
    // Get the capabilities config.
    DomainConfig dc = DomainConfig.getInstance(domainId);
    CapabilityConfig cc = dc.getCapabilityByRole(role);
    if (!isAdd) { // edit, check permission, first by role and, if not by role, by general configuration that applies to all
      boolean
          isCapabilityDisabled =
          (cc != null ? cc.isCapabilityDisabled(CapabilityConfig.CAPABILITY_EDITPROFILE)
              : dc.isCapabilityDisabled(CapabilityConfig.CAPABILITY_EDITPROFILE));
      if (isCapabilityDisabled) {
        return false;
      }
    } else if (RestConstantsZ.TYPE_KIOSK.equals(
        type)) { // add kiosk, check permission as to which type of kiosk can be added by this role (NOTE: users can be added only along with kiosk; if that changes in future, adding users has to be checked separately)
      List<String>
          creatableEntities =
          (cc != null ? cc.getCreatableEntityTypes() : dc.getCreatableEntityTypes());
      if (creatableEntities == null || creatableEntities.isEmpty()) {
        return false;
      }
      // Check the type of entity being added: if there are customer/vendor relationship then entity being added is a vendor/customer; otherwise, managed entity
      Hashtable<String, String> kioskData = sdInput.getKiosk();

      String value = null;
      boolean
          isEntityVendor =
          ((value = kioskData.get(JsonTagsZ.CUSTOMERS)) != null && (!value.isEmpty()));
      if (isEntityVendor && !creatableEntities.contains(CapabilityConfig.TYPE_VENDOR)) {
        return false;
      }
      boolean
          isEntityCustomer =
          ((value = kioskData.get(JsonTagsZ.VENDORS)) != null && (!value.isEmpty()));
      if (isEntityVendor && !creatableEntities.contains(CapabilityConfig.TYPE_CUSTOMER)) {
        return false;
      }
      if (!creatableEntities.contains(CapabilityConfig.TYPE_MANAGEDENTITY)) {
        return false;
      }
    }
    return true;
  }

  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entering processGet");
    processPost(req, resp, backendMessages, messages);
    xLogger.fine("Exiting processGet");
  }

  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Inside processPost");
    // Get the post parameters
    String action = req.getParameter(RestConstantsZ.ACTION);
    if (RestConstantsZ.ACTION_CREATEUSERKIOSK.equalsIgnoreCase(action)
        || RestConstantsZ.ACTION_UPDATEUSERKIOSK.equalsIgnoreCase(action)) {
      setupData(req, resp, backendMessages, messages, action);
    } else if (RestConstantsZ.ACTION_UPDATEPASSWORD.equalsIgnoreCase(action)) {
      updatePassword(req, resp, backendMessages, messages, action);
    } else if (RestConstantsZ.ACTION_RESETPASSWORD.equalsIgnoreCase(action)) {
      resetPassword(req, resp, backendMessages, messages, action);
    } else if (RestConstantsZ.ACTION_REMOVE.equalsIgnoreCase(action)) {
      remove(req, resp, backendMessages, messages, action);
    } else if (RestConstantsZ.ACTION_CREATERELATIONSHIP.equalsIgnoreCase(action)
        || RestConstantsZ.ACTION_UPDATERELATIONSHIP.equalsIgnoreCase(action)
        || RestConstantsZ.ACTION_REMOVERELATIONSHIP.equalsIgnoreCase(action)) {
      manageRelationship(req, resp, backendMessages, messages, action);
    } else if (RestConstantsZ.ACTION_GETRELATEDENTITIES.equalsIgnoreCase(action)) {
      getRelatedEntities(req, resp, backendMessages, messages);
    } else {
      throw new ServiceException("Invalid action: " + action);
    }
    xLogger.fine("Out of processPost");
  }

  @SuppressWarnings("unchecked")
  private void setupData(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages, String action)
      throws IOException, ServiceException {
    xLogger.fine("Entering setupData");
    boolean status = true;
    Long domainId = null;
    String role = null;
    String errMsg = null; // service error
    Vector<String>
        dataErrors =
        new Vector<String>(); // data errors - Vector of error messages used by SetupDataOutput
    String localeStr = Constants.LANG_DEFAULT;
    String kioskId = null;
    String uId = null;
    int statusCode = HttpServletResponse.SC_OK;

    // Get the request parameters
    // Get the type
    String type = req.getParameter(RestConstantsZ.TRANS_TYPE);
    xLogger.fine("type: {0}", type);
    if (type == null || type.isEmpty()) {
      errMsg = "Invalid parameter transaction type: " + type;
      status = false;
      sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
      return;
    } else if (!(RestConstantsZ.TYPE_USER.equalsIgnoreCase(type) || RestConstantsZ.TYPE_KIOSK
        .equalsIgnoreCase(type) || RestConstantsZ.TYPE_USERKIOSK.equalsIgnoreCase(type))) {
      errMsg = "Invalid parameter transaction type: " + type;
      status = false;
      sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
      return;
    }

    // Get the json input string
    String jsonString = req.getParameter(RestConstantsZ.JSON_STRING);
    if (jsonString == null || jsonString.isEmpty()) {
      errMsg = "Invalid parameter json string while setting up data: " + jsonString;
      status = false;
      sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
      return;
    }

    // user id and password of the caller
    // Authenticate the user
    String userId = req.getParameter(RestConstantsZ.USER_ID);
    String password = req.getParameter(RestConstantsZ.PASSWORD);
    boolean
        notifyPassword =
        (req.getParameter("np")
            != null); // sent when registered user, and his userId/password are to be SMSed to him/her

		/* NOT NEEDED: User ID and password coming via BasicAuth header now
                if ( userId == null || userId.isEmpty() || password == null || password.isEmpty() ) {
			status = false;
			errMsg = "Invalid user name or password";
			sendSetupDataError( resp, uId, kioskId, dataErrors, localeStr, errMsg );
			return;
		}
		*/
    // Authenticate
    try {
      IUserAccount u = RESTUtil.authenticate(userId, password, null, req, resp);
      if (userId == null) // can be the case if BasicAuth was used
      {
        userId = u.getUserId();
      }
      domainId = u.getDomainId();
      role = u.getRole();
      localeStr = u.getLanguage();
                        /* Removing this restriction here and moving under hasSetupPermissions (see below) - Aug 22, 2015 (arun)
                        if ( SecurityManager.compareRoles( role, IUserAccount.ROLE_SERVICEMANAGER ) < 0 ) {
				xLogger.warn( "{0} does not have permission to perform this operation. Role = {1}", userId, role );
				errMsg = "You do not have permission to perform this operation";
				status = false;
			}
			*/
    } catch (ServiceException e) {
      xLogger.severe("Authentication of caller failed: Exception: {0}, Msg: {1} ",
          e.getClass().getName(), e.getMessage());
      errMsg = e.getMessage();
      status = false;
    } catch (UnauthorizedException e) {
      errMsg = e.getMessage();
      status = false;
      statusCode = HttpServletResponse.SC_UNAUTHORIZED;
    }

    if (!status) {
      sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
      return;
    }

    // Only if the caller is authenticated, proceed
    BulkUploadMgr.EntityContainer uec, eec = null;
    // Create a SetupDataInput object
    SetupDataInput setupDataInput = new SetupDataInput();
    Hashtable<String, String> user = null;
    try {
      //	setupDataInput.fromJSONString( jsonString );
      setupDataInput = GsonUtil.setupDataInputFromJson(jsonString);
      // Set the bulkUploadOp to add/edit depending on the action parameter
      String bulkUploadOp = BulkUploadMgr.OP_ADD;
      if (RestConstantsZ.ACTION_CREATEUSERKIOSK.equalsIgnoreCase(action)) {
        bulkUploadOp = BulkUploadMgr.OP_ADD;
      } else if (RestConstantsZ.ACTION_UPDATEUSERKIOSK.equalsIgnoreCase(action)) {
        bulkUploadOp = BulkUploadMgr.OP_EDIT;
      }

      // Check whether permission exists to perform this operation
      if (!hasSetupPermission(domainId, role, bulkUploadOp, type, setupDataInput)) {
        xLogger.warn("{0} does not have permission to perform this operation. Role = {1}", userId,
            role);
        errMsg = "You do not have permission to perform this operation";
        sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
        return;
      }

      String userCSV = null;
      Hashtable<String, String> kiosk = null;
      String kioskCSV = null;
      if (RestConstantsZ.TYPE_USERKIOSK.equalsIgnoreCase(type)) {
        UsersService as = Services.getService(UsersServiceImpl.class);
        EntitiesService es = Services.getService(EntitiesServiceImpl.class);
        user = setupDataInput.getUser();
        userCSV = getUserCSV(user, bulkUploadOp);
        if (userCSV != null) {
          try {
            as.getUserAccount(userCSV.split(CharacterConstants.COMMA)[1]);
            errMsg = "User with ID " + userId + " already exists. Cannot add this user again.";
            sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
            return;
          } catch (ObjectNotFoundException e) {
            // ignore
          }
        }

        kiosk = setupDataInput.getKiosk();
        kioskCSV = getKioskCSV(kiosk, bulkUploadOp);
        if (kioskCSV != null) {
          String
              kioskName =
              kioskCSV.split(CharacterConstants.COMMA)[1]
                  .replace(CharacterConstants.DOUBLE_QUOTES, CharacterConstants.EMPTY);
          IKiosk k = es.getKioskByName(domainId, kioskName);
          if (k != null) {
            errMsg =
                "Cannot add " + backendMessages.getString("kiosk.lowercase") + " " + "'" + kioskName
                    + "'. It already exists.";
            sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
            return;
          }
        }
      }

      // If the type parameter is u or uk then get the user info from the setupDataInput
      if (RestConstantsZ.TYPE_USER.equalsIgnoreCase(type) || RestConstantsZ.TYPE_USERKIOSK
          .equalsIgnoreCase(type)) {
        if (RestConstantsZ.TYPE_USER.equalsIgnoreCase(type)) {
          // Create a csv formatted string from the SetupData
          // First get the user info from the SetupDataInput
          user = setupDataInput.getUser();
          // get the userCSV as required by the BulkUploadMgr.processEntity() method
          userCSV = getUserCSV(user, bulkUploadOp);
        }

        if (userCSV == null || userCSV.isEmpty()) {
          status = false;
          dataErrors.add("No valid data passed");
          sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
          return;
        } else {
          uec = BulkUploadMgr.processEntity(BulkUploadMgr.TYPE_USERS, userCSV, domainId, userId);
          if (uec != null) {
            xLogger.fine("user entityId: {0}", uec.entityId);
            if (uec.hasErrors()) {
              dataErrors.addAll(uec.messages);
              status = false;
              sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
              return;
            } else {
              uId = (String) uec.entityId;
            }
          }
        }
      }

      // If the type is k or uk, then get kiosk info from setupDataInput object
      if (RestConstantsZ.TYPE_KIOSK.equalsIgnoreCase(type) || RestConstantsZ.TYPE_USERKIOSK
          .equalsIgnoreCase(type)) {
        if (RestConstantsZ.TYPE_KIOSK.equalsIgnoreCase(type)) {
          kiosk = setupDataInput.getKiosk();
          kioskCSV = getKioskCSV(kiosk, bulkUploadOp);
        }
        if (kioskCSV == null || kioskCSV.isEmpty()) {
          status = false;
          dataErrors.add("No valid data is passed");
          sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
          return;
        } else {
          // Call BulkUploadManager.processEntity() to create user and kiosk objects
          eec = BulkUploadMgr.processEntity(BulkUploadMgr.TYPE_KIOSKS, kioskCSV, domainId, userId);
          if (eec != null) {
            xLogger.fine("kiosk entityId: {0}", eec.entityId);
            if (eec.hasErrors()) {
              dataErrors.addAll(eec.messages);
              status = false;
              sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
              return;
            } else {
              kioskId = ((Long) eec.entityId).toString();
              // If geo-accuracy/geo-error were sent, update the kiosk with the same
              String geoAccuracy = kiosk.get(JsonTagsZ.GEO_ACCURACY);
              Object
                  geoErrorObj =
                  kiosk.get(
                      JsonTagsZ.GEO_ERROR_CODE); // at times, this has come as integer; so a safety check to cast it to String
              String geoError = null;
              if (geoErrorObj != null) {
                geoError = String.valueOf(geoErrorObj);
              }
              if ((geoAccuracy != null && !geoAccuracy.isEmpty()) || (geoError != null && !geoError
                  .isEmpty())) {
                // Update kiosk with metadata not taken by bulk upload yet (geo-accuracy, geo-error)
                PersistenceManager pm = PMF.get().getPersistenceManager();
                try {
                  IKiosk k = JDOUtils.getObjectById(IKiosk.class, eec.entityId, pm);
                  // Update kiosk with the above values (BulkUploadMgr.processEntity() does not handle these two values as of now)
                  k.setGeoError(geoError);
                  k.setGeoAccuracy(Double.parseDouble(geoAccuracy));
                } catch (Exception e) {
                  xLogger.warn(
                      "{0} when trying to update kiosk {1} with geo-accuracy {2}/geo-error {3}: {4}",
                      e.getClass().getName(), kioskId, geoAccuracy, geoError, e.getMessage());
                } finally {
                  pm.close();
                }
              }
              // Update route tag, if any
              String routeTag = kiosk.get(JsonTagsZ.ROUTE_TAG);
              String routeIndex = kiosk.get(JsonTagsZ.ROUTE_INDEX);
              Integer ri = null;
              if (routeTag != null && routeTag.isEmpty()) {
                routeTag = null;
              }
              boolean isAdd = (bulkUploadOp.equals(BulkUploadMgr.OP_ADD));
              if (routeIndex != null && !routeIndex.isEmpty()) {
                try {
                  ri = Integer.valueOf(routeIndex);
                } catch (Exception e) {
                  xLogger.warn(
                      "{0} while trying to update route tag for user {1}. kioskId: {2}, Message: {3}",
                      e.getClass().getName(), userId, kioskId, e.getMessage());
                }
              } else if (isAdd || (routeTag == null)) {
                ri = new Integer(IUserToKiosk.DEFAULT_ROUTE_INDEX);
              }
              // Update route tag info.
              updateRouteTag(domainId, userId, ((Long) eec.entityId), routeTag, ri, kiosk, isAdd);
            }
          }
        }
      }
    } catch (Exception e) {
      xLogger.severe("SetupDataServlet Protocol Exception: {0} : {1}", e.getClass().getName(),
          e.getMessage());
      errMsg = backendMessages.getString("error.systemerror");
      status = false;
      sendSetupDataError(resp, uId, kioskId, dataErrors, localeStr, errMsg, statusCode);
      return;
    }
    // Status is true
    // Create a SetupDataOutput object
    try {
      //SetupDataOutput setupDataOutput = new SetupDataOutput( status, uId, kioskId, errMsg, dataErrors, localeStr, RESTUtil.VERSION_01 );
      //sendJsonResponse( resp, HttpServletResponse.SC_OK, setupDataOutput.toJSONString() );
      String
          setupDataOutput =
          GsonUtil.setupDataOutputToJson(status, uId, kioskId, errMsg, dataErrors, localeStr,
              RESTUtil.VERSION_01);
      sendJsonResponse(resp, HttpServletResponse.SC_OK, setupDataOutput);

      // If user Id is not null, and password notification is required, then do so
      if (uId != null && notifyPassword && user != null) {
        notifyPasswordToUser((String) user.get(JsonTagsZ.USER_ID),
            (String) user.get(JsonTagsZ.PASSWORD), domainId, userId);
      }
    } catch (Exception e) {
      xLogger
          .severe("SetupDataServlet Protocol Exception: Class: {0} : {1}", e.getClass().getName(),
              e.getMessage());
      resp.setStatus(500);
    }
    xLogger.fine("Exiting setupData");
  }

  private void updatePassword(HttpServletRequest req, HttpServletResponse resp,
                              ResourceBundle backendMessages, ResourceBundle messages,
                              String action) throws IOException, ServiceException {
    xLogger.fine("Entering updatePassword");
    boolean status = true;
    Locale locale = new Locale(Constants.LANG_DEFAULT);
    String errMsg = null;
    String token = req.getHeader(Constants.TOKEN);
    IUserAccount u = validateCaller(req, resp, backendMessages, messages, action, null);

    if (u == null) {
      xLogger.severe("Failed to validate caller.");
      return;
    }

    // Check if the user is authorized to perform the update password operation.
    String role = u.getRole();
    // Obtain the locale of the caller
    locale = u.getLocale();
    if (SecurityUtil.compareRoles(role, SecurityConstants.ROLE_KIOSKOWNER) < 0) {
      xLogger
          .warn("{0} does not have permission to perform this operation. Role = {1}", u.getUserId(),
              role);
      errMsg = "You do not have permission to perform this operation";
      status = false;
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    // If the caller is authorized, proceed

    // Create AccountsService object
    UsersService as = Services.getService(UsersServiceImpl.class, locale);
    String userId = req.getParameter(RestConstantsZ.ENDUSER_ID);
    String password = req.getParameter(RestConstantsZ.OLD_PASSWORD);
    String upPassword = req.getParameter(RestConstantsZ.UPDATED_PASSWORD);
    // If end user id is null or empty, set error message and return
    if (userId == null || userId.isEmpty()) {
      status = false;
      errMsg = "Invalid user name of the end user whose password is to be updated";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    // If oldPassword is null or empty, set the error message and return
    if (password == null || password.isEmpty()) {
      status = false;
      errMsg = "Invalid old password";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    // If updatedPassword is null or empty, set error message and return
    if (upPassword == null || upPassword.isEmpty()) {
      status = false;
      errMsg = "Invalid new/updated password";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    if (as == null) {
      // Failed to create AccountsService. Set the status to false and set the errMsg appropriately. And return.
      status = false;
      errMsg = "Failed to create AccountsService";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    try {
      as.changePassword(userId, password, upPassword);
    } catch (ServiceException e) {
      xLogger.severe("ServiceException while changing password for user {0}, Msg: {1}", userId,
          e.getMessage());
      errMsg = e.getMessage();
      status = false;
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    // Successfully updated password.
    // Create a BasicOutput object
    try {
      xLogger.fine("status: {0}, errMsg: {1}", status, errMsg);
      //	BasicOutput updatePasswordOutput = new BasicOutput( status, errMsg, null, locale.toString(), RESTUtil.VERSION_01 );
      //	sendJsonResponse( resp, HttpServletResponse.SC_OK, updatePasswordOutput.toJSONString() );
      String
          updatePasswordOutput =
          GsonUtil.basicOutputToJson(status, errMsg, null, locale.toString(), RESTUtil.VERSION_01);
      sendJsonResponse(resp, HttpServletResponse.SC_OK, updatePasswordOutput);

    } catch (Exception e) {
      xLogger
          .severe("SetupDataServlet Protocol Exception: Class: {0} : {1}", e.getClass().getName(),
              e.getMessage());
      resp.setStatus(500);
    }

    xLogger.fine("Exiting updatePassword");
  }

  private void resetPassword(HttpServletRequest req, HttpServletResponse resp,
                             ResourceBundle backendMessages, ResourceBundle messages, String action)
      throws IOException, ServiceException {
    xLogger.fine("Entering resetPassword");
    boolean status = true;
    Locale locale = new Locale(Constants.LANG_DEFAULT);
    String errMsg = null;
    Long domainId = null;
    String userId = null;
    String country = Constants.COUNTRY_DEFAULT;

    // Read the request parameters. If uid, p are both given, then it's the admin trying to reset a user's password
    userId = req.getParameter(RestConstantsZ.USER_ID);
    String password = req.getParameter(RestConstantsZ.PASSWORD);

    if ((userId != null && !userId.isEmpty()) || (password != null && !password.isEmpty())) {
      IUserAccount u = validateCaller(req, resp, backendMessages, messages, action, null);
      if (u == null) {
        xLogger.severe("Failed to validate caller.");
        return;
      }
      // Check if the user is authorized to reset the password of another user
      // Check if the caller is authorized to perform reset password operation. Only a Domain Owner or a Super user can reset the password for some user
      String role = u.getRole();
      // If the caller is authenticated, proceed
      // Obtain the locale of the caller
      locale = u.getLocale();
      domainId = u.getDomainId(); // Domain id of the caller
      country = u.getCountry(); // Country of the caller
      if (SecurityUtil.compareRoles(role, SecurityConstants.ROLE_DOMAINOWNER) < 0) {
        xLogger.warn("{0} does not have permission to perform this operation. Role = {1}",
            u.getUserId(), role);
        errMsg = "You do not have permission to perform this operation";
        status = false;
        sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
        return;
      }
    }

    // Create AccountsService object
    UsersService as = Services.getService(UsersServiceImpl.class, locale);
    if (as == null) {
      // Failed to create AccountsService. Set the status to false and set the errMsg appropriately. And return.
      status = false;
      errMsg = "Failed to create AccountsService";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    String endUserId = req.getParameter(RestConstantsZ.ENDUSER_ID);
    String newPassword = req.getParameter(RestConstantsZ.UPDATED_PASSWORD);
    String notification = req.getParameter(RestConstantsZ.NOTIFICATION);

    // Check if endUserId is null or empty
    if (endUserId == null || endUserId.isEmpty()) {
      status = false;
      errMsg = "Invalid user name of the end user whose password is to be reset";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }
    // Check if newPassword is null or empty
    if (newPassword == null || newPassword.isEmpty()) {
      status = false;
      errMsg = "Invalid new password";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }
    // Check if the end user exists in the system
    IUserAccount eu = null;
    try {
      eu = as.getUserAccount(endUserId);
      if (eu == null) {
        status = false;
        errMsg = "Invalid User ID. User does not exist in the system.";
        sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
        return;
      }
      locale = eu.getLocale();
      country = eu.getCountry(); // Country of the user
    } catch (ObjectNotFoundException e) {
      status = false;
      errMsg = "Invalid User ID. User does not exist in the system.";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    // Proceed to reset the password
    if (domainId == null || (domainId != null && domainId.equals(eu.getDomainId()))) {
      try {
        as.changePassword(endUserId, null, newPassword);
      } catch (ServiceException e) {
        xLogger
            .severe("ServiceException while resetting password for user {0}, Msg: {1}", endUserId,
                e.getMessage());
        errMsg = e.getMessage();
        status = false;
        sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
        return;
      }
    } else {
      xLogger.severe(
          "Caller Domain ID: {0}, End User Domain ID: {1}, Domain IDs are different. Cannot reset password.",
          domainId, eu.getDomainId());
      errMsg = "You do not have permission to reset the password of a user in another domain";
      status = false;
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    xLogger.fine("Notification type is: " + notification);
    // Only if notification parameter is sent, send the notification
    if (notification != null && !notification.isEmpty()) {
      String sendtype = MessageService.SMS;
      if (notification.equalsIgnoreCase(RestConstantsZ.TYPE_EMAIL)) {
        // Get the send type
        sendtype = MessageService.EMAIL;
      } else if (notification.equalsIgnoreCase(RestConstantsZ.TYPE_SMS)) {
        // Get the send type
        sendtype = MessageService.SMS;
      }
      String msg = "Your password has been reset. Your new password is: " + newPassword;
      String logMsg = backendMessages.getString("password.reset.success.log");
      // Send message to user
      try {
        IUserAccount endUserAccount = as.getUserAccount(endUserId);
        MessageService
            ms =
            MessageService.getInstance(sendtype, country, true, domainId, userId, null);
        ms.send(endUserAccount, msg, MessageService.NORMAL, "Password updated", null, logMsg);
      } catch (MessageHandlingException e) {
        xLogger.severe("{0} while sending message during reset password for user {1}. Message: {2}",
            e.getClass().getName(), endUserId, e.getMessage());
        errMsg = e.getMessage();
        status = false;
        sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
        return;
      } catch (ObjectNotFoundException onfe) {
        xLogger.severe(
            "{0} while getting user account for user {1} during reset password. Message: {2}",
            onfe.getClass().getName(), endUserId, onfe.getMessage());
        errMsg = onfe.getMessage();
        status = false;
        sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
        return;
      }
    }

    // Send Success output
    // Create the BasicOutput object
    try {
      xLogger.fine("status: {0}, endUserId: {1}, password: {2}, errMsg: {3}", status, endUserId,
          newPassword, errMsg);
      //BasicOutput basicOutput = new BasicOutput( status, errMsg, null, locale.toString(), RESTUtil.VERSION_01 );
      //sendJsonResponse( resp, HttpServletResponse.SC_OK, basicOutput.toJSONString() );
      String
          basicOutput =
          GsonUtil.basicOutputToJson(status, errMsg, null, locale.toString(), RESTUtil.VERSION_01);
      sendJsonResponse(resp, HttpServletResponse.SC_OK, basicOutput);

    } catch (Exception e) {
      xLogger
          .severe("SetupDataServlet Protocol Exception: Class: {0} : {1}", e.getClass().getName(),
              e.getMessage());
      resp.setStatus(500);
    }
    xLogger.fine("Exiting resetPassword");
  }

  private void remove(HttpServletRequest req, HttpServletResponse resp,
                      ResourceBundle backendMessages, ResourceBundle messages, String action)
      throws IOException, ServiceException {
    xLogger.fine("Entering remove");
    boolean status = true;
    Locale locale = null;
    String errMsg = null;
    Long domainId = null;

    IUserAccount u = validateCaller(req, resp, backendMessages, messages, action, null);
    if (u == null) {
      xLogger.severe("Failed to validate caller.");
      return;
    }
    // If the caller is authenticated, proceed
    // Obtain the locale of the caller
    locale = u.getLocale();
    // Get the domainId for the user
    domainId = u.getDomainId();
    // Check if the user is authorized to perform remove operation. Only a Domain Owner or a Super User can remove users/kiosks/materials from his domain
    String role = u.getRole();
    if (SecurityUtil.compareRoles(role, SecurityConstants.ROLE_DOMAINOWNER) < 0) {
      xLogger
          .warn("{0} does not have permission to perform this operation. Role = {1}", u.getUserId(),
              role);
      errMsg = "You do not have permission to perform this operation";
      status = false;
    }

    if (status == false) {
      // Authorization failed. Return
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    // Read the request parameters
    String endUserIds = req.getParameter(RestConstantsZ.ENDUSER_IDS);
    String kioskIds = req.getParameter(RestConstantsZ.KIOSK_IDS);
    String materialIds = req.getParameter(RestConstantsZ.MATERIAL_IDS);

    // Read the request parameters
    if ((endUserIds == null || endUserIds.isEmpty()) && (kioskIds == null || kioskIds.isEmpty())
        && (materialIds == null || materialIds.isEmpty())) {
      status = false;
      errMsg = "Nothing to remove. Please specify the ids of users/kiosk/materials to remove";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }
    EntitiesService as = null;
    UsersService us = null;
    MaterialCatalogService ms = null;

    // Get the AccountsService and MaterialService instances
    as = Services.getService(EntitiesServiceImpl.class, locale);
    us = Services.getService(UsersServiceImpl.class, locale);
    ms = Services.getService(MaterialCatalogServiceImpl.class, locale);

    // If status is false or as is null, send error message and return
    if (as == null || ms == null) {
      xLogger.severe("Either status is false or as is null or ms is null");
      status = false;
      errMsg = "Failed to create AccountsService or MaterialCatalogService";
      sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
      return;
    }

    // Proceed with removal
    // Kiosk(s) - Remove kiosks first. Because users cannot be deleted without deleting the associted kiosks.
    if (kioskIds != null && !kioskIds.isEmpty()) {
      xLogger.fine("Deleting kiosks: {0}", kioskIds);
      // Remove Kiosk(s)
      // Form a list of kiosk ids from the comma separated kiosk id values
      List<String> kioskIdsStrList = StringUtil.getList(kioskIds);
      List<Long> kioskIdsList = null;
      // Convert the list of Strings to list of Long
      if (kioskIdsStrList != null && !kioskIdsStrList.isEmpty()) {
        kioskIdsList = new ArrayList<Long>();
        Iterator<String> kioskIdsIter = kioskIdsStrList.iterator();
        while (kioskIdsIter.hasNext()) {
          String kioskIdStr = kioskIdsIter.next();
          try {
            kioskIdsList.add(Long.valueOf(kioskIdStr));
          } catch (NumberFormatException nfe) {
            errMsg = "NumberFormatException while deleting kiosks. Invalid kiosk Id " + kioskIdStr;
            status = false;
            sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
            return;
          }
        }
        try {
          as.deleteKiosks(domainId, kioskIdsList, u.getUserId());
        } catch (ServiceException e) {
          xLogger.severe("ServiceException while deleting kiosks {0} for domain {1}, Msg: {2}",
              kioskIdsList, domainId, e.getMessage());
          errMsg = e.getMessage();
          status = false;
          sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
          return;
        }
      }
    } else if (endUserIds != null && !endUserIds.isEmpty()) { // User(s)
      // Remove User(s)
      // Form a list of user ids from the comma separated user id values
      List<String> endUserIdsList = StringUtil.getList(endUserIds);
      xLogger.fine("Deleting users: {0}", endUserIdsList);
      try {
        us.deleteAccounts(domainId, endUserIdsList, null);
      } catch (ServiceException e) {
        xLogger.severe("ServiceException while deleting users {0} for domain {1}, Msg: {2}",
            endUserIds, domainId, e.getMessage());
        errMsg = e.getMessage();
        status = false;
        sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
        return;
      }
    } else if (materialIds != null && !materialIds.isEmpty()) { // material(s)
      xLogger.fine("Deleting materials: {0}", materialIds);
      // Remove Material(s)
      // Form a list of material ids from the comma separated kiosk id values
      List<String> materialIdsStrList = StringUtil.getList(materialIds);
      List<Long> materialIdsList = null;
      // Convert the list of Strings to list of Long
      if (materialIdsStrList != null && !materialIdsStrList.isEmpty()) {
        materialIdsList = new ArrayList<Long>();
        Iterator<String> materialIdsIter = materialIdsStrList.iterator();
        while (materialIdsIter.hasNext()) {
          String materialIdStr = materialIdsIter.next();
          try {
            materialIdsList.add(Long.valueOf(materialIdStr));
          } catch (NumberFormatException nfe) {
            errMsg = "Error while deleting materials.";
            status = false;
            sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
            return;
          }
        }
        try {
          ms.deleteMaterials(domainId, materialIdsList);
        } catch (ServiceException e) {
          xLogger.severe("ServiceException while deleting materials {0} for domain {1}, Msg: {2}",
              materialIdsList, domainId, e.getMessage());
          errMsg = e.getMessage();
          status = false;
          sendBasicError(resp, locale.toString(), errMsg, null, HttpServletResponse.SC_OK);
          return;
        }
      }
    }

    // If remove users/kiosks/materials has succeeded
    if (status) {
      // Create the RemoveOutput object
      try {
        xLogger.fine("status: {0}, errMsg: {1}", status, errMsg);
        //BasicOutput removeOutput = new BasicOutput( status, errMsg, null, locale.toString(), RESTUtil.VERSION_01 );
        //sendJsonResponse( resp, HttpServletResponse.SC_OK, removeOutput.toJSONString() );
        String
            removeOutput =
            GsonUtil
                .basicOutputToJson(status, errMsg, null, locale.toString(), RESTUtil.VERSION_01);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, removeOutput);

      } catch (Exception e) {
        xLogger
            .severe("SetupDataServlet Protocol Exception: Class: {0} : {1}", e.getClass().getName(),
                e.getMessage());
        resp.setStatus(500);
      }
    }
    xLogger.fine("Exiting remove");
  }

  private String getUserCSV(Hashtable<String, String> ht, String operType) {
    xLogger.fine("ht = {0}", ht);
    String csv = null;
    if (ht == null || ht.isEmpty()) {
      xLogger.severe("User hashtable is null");
      return null;
    }
    String value = null;
    csv = operType + "," + ht.get(JsonTagsZ.USER_ID);
    String password = ht.get(JsonTagsZ.PASSWORD);
    if (password == null) {
      password = "";
    }
    csv +=
        "," + password + "," + password + "," + ht.get(JsonTagsZ.ROLE) + ",\"" + ht
            .get(JsonTagsZ.FIRST_NAME) + "\",";
    // Last name is optional
    if ((value = ht.get(JsonTagsZ.LAST_NAME)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";

    csv += ht.get(JsonTagsZ.MOBILE) + ",";

    // Email is optional if role is UserAccount.ROLE_KIOSKOWNER
    if ((value = ht.get(JsonTagsZ.EMAIL)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";

    csv +=
        ht.get(JsonTagsZ.COUNTRY) + "," + ht.get(JsonTagsZ.LANGUAGE) + "," + ht
            .get(JsonTagsZ.TIMEZONE) + ",";

    // Gender is optional
    if ((value = ht.get(JsonTagsZ.GENDER)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";

    // Age is optional
    if ((value = ht.get(JsonTagsZ.AGE)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";

    // Landline is optional
    if ((value = ht.get(JsonTagsZ.LANDLINE)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";

    // Landline is optional
    if ((value = ht.get(JsonTagsZ.STATE)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";

    // District is optional
    if ((value = ht.get(JsonTagsZ.DISTRICT)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";

    // Taluk is optional
    if ((value = ht.get(JsonTagsZ.TALUK)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";

    // City is optional
    if ((value = ht.get(JsonTagsZ.CITY)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";

    // Street address is optional
    if ((value = ht.get(JsonTagsZ.STREET_ADDRESS)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";

    // Pin code is optional
    if ((value = ht.get(JsonTagsZ.PINCODE)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";

    return csv;
  }

  private String getKioskCSV(Hashtable<String, String> ht, String operType) {
    String csv = null;
    if (ht == null || ht.isEmpty()) {
      xLogger.severe("Kiosk hashtable is null");
      return null;
    }
    String value = null;
    csv = operType + ",\"" + ht.get(JsonTagsZ.NAME) + "\",";
    csv += ht.get(JsonTagsZ.USERS) + ",";
    csv += ht.get(JsonTagsZ.COUNTRY) + ",";
    csv += '"' + ht.get(JsonTagsZ.STATE) + "\",";
    csv += '"' + ht.get(JsonTagsZ.CITY) + "\",";
    // Latitude is optional
    if ((value = ht.get(JsonTagsZ.LATITUDE)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // Longitude is optional
    if ((value = ht.get(JsonTagsZ.LONGITUDE)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // District/County is optional
    if ((value = ht.get(JsonTagsZ.DISTRICT)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";
    // Taluk is optional
    if ((value = ht.get(JsonTagsZ.TALUK)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";
    // Street address is optional
    if ((value = ht.get(JsonTagsZ.STREET_ADDRESS)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";
    // Pin code is optional
    if ((value = ht.get(JsonTagsZ.PINCODE)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // Currency is optional
    if ((value = ht.get(JsonTagsZ.CURRENCY)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // Tax is optional
    if ((value = ht.get(JsonTagsZ.TAX)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // Tax ID is optional
    if ((value = ht.get(JsonTagsZ.TAX_ID)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // Inventory policy is optional
    if ((value = ht.get(JsonTagsZ.INVENTORY_POLICY)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // Service level is optional
    if ((value = ht.get(JsonTagsZ.SERVICE_LEVEL)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // New entity/kiosk name is optional
    if ((value = ht.get(JsonTagsZ.NEW_NAME)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";
    // Add all materials is optional
    if ((value = ht.get(JsonTagsZ.ADD_ALL_MATERIALS)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // materials is optional
    if ((value = ht.get(JsonTagsZ.MATERIALS)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // inital stock count is optional
    if ((value = ht.get(JsonTagsZ.QUANTITY)) != null && (!value.isEmpty())) {
      csv += value;
    }
    csv += ",";
    // Customers is optional
    if ((value = ht.get(JsonTagsZ.CUSTOMERS)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";
    // Vendors is optional
    if ((value = ht.get(JsonTagsZ.VENDORS)) != null && (!value.isEmpty())) {
      csv += '"' + value + '"';
    }
    csv += ",";
    return csv;
  }

  private void sendSetupDataError(HttpServletResponse resp, String uId, String kioskId,
                                  Vector<String> dataErrors, String locale, String errMsg,
                                  int statusCode) throws IOException {
    xLogger.fine("Entering sendSetupDataError");
    // Create a SetupDataOutput object
    try {
      //	SetupDataOutput setupDataOutput = new SetupDataOutput( false, uId, kioskId, errMsg, dataErrors, locale, RESTUtil.VERSION_01 );
      //	sendJsonResponse( resp, statusCode, setupDataOutput.toJSONString() );
      String
          setupDataOutput =
          GsonUtil.setupDataOutputToJson(false, uId, kioskId, errMsg, dataErrors, locale,
              RESTUtil.VERSION_01);
      sendJsonResponse(resp, statusCode, setupDataOutput);
    } catch (Exception e) {
      xLogger
          .severe("SetupDataServlet Protocol Exception: Class: {0} : {1}", e.getClass().getName(),
              e.getMessage());
      resp.setStatus(500);
    }
    xLogger.fine("Exiting sendSetupDataError");
  }

  private void manageRelationship(HttpServletRequest req, HttpServletResponse resp,
                                  ResourceBundle backendMessages, ResourceBundle messages,
                                  String action) throws IOException, ServiceException {
    xLogger.fine("Entering manageRelationship");
    boolean status = true;
    Locale locale = null;
    String errMsg = null;
    Vector<String> errMsgs = new Vector<String>();
    ///String country = Constants.COUNTRY_DEFAULT;
    ///String language = Constants.LANG_DEFAULT;
    IUserAccount u = validateCaller(req, resp, backendMessages, messages, action, errMsgs);
    if (u == null) {
      xLogger.severe("Failed to validate caller.");
      return;
    }
    // If the caller is authenticated, proceed
    // Obtain the locale of the caller
                /* Removed by Arun - 9/2/2013 - no longer using session for REST calls
                SecureUserDetails sUser = SecurityManager.getUserDetails( req.getSession() );
		String suserName = null;
		if ( sUser != null ) {
			suserName = sUser.getUsername();
			locale = sUser.getLocale();
		} else {
			country = Constants.COUNTRY_DEFAULT;
			language = Constants.LANG_DEFAULT;
			locale = new Locale( language, country );
		}
		*/
    String suserName = u.getUserId();
    locale = u.getLocale();
    Long domainId = u.getDomainId();
    // Create AccountsService object
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);

    // Get the json input string
    String jsonString = req.getParameter(RestConstantsZ.JSON_STRING);
    if (jsonString == null || jsonString.isEmpty()) {
      errMsg = "Invalid parameter json string while managing relationship";
      status = false;
      sendBasicError(resp, locale.toString(), errMsg, errMsgs, HttpServletResponse.SC_OK);
      return;
    }

    // Create the RelationshipInput
    RelationshipInput ri = new RelationshipInput();
    try {
      //ri.fromJSONString( jsonString );
      ri = GsonUtil.relationshipInputFromJson(jsonString);
      Long
          kioskId =
          Long.valueOf(
              ri.getKioskId()); // KioskId - id of the kiosk for which relationship is being created/removed/updated
      String type = ri.getLinkType(); // Type of relationship as specified
      List<IKioskLink>
          kskLnks =
          getKioskLinks(resp, suserName, ri, locale, as, errMsgs,
              domainId); // Get KioskLink objects based on String ids of the linked kiosks

      List<String> kskLnkIds = null; // List of KioskLink object Ids ( keys )
      @SuppressWarnings("unchecked")
      List<String>
          lnkKidsRm =
          ri.getLinkedKioskIdsRm(); // This is used only in the case of update relationship. It is a list of String ids that represent kiosk ids of the linked kiosks

      if (kskLnks == null || kskLnks.isEmpty()) {
        xLogger.severe("Linked Kiosk Ids is not specified");
        status = false;
        errMsg = "Linked Kiosk Ids is not specified";
        sendBasicError(resp, locale.toString(), errMsg, errMsgs, HttpServletResponse.SC_OK);
        return;
      }

      // Iterate through the linked kiosks. And form an array list of KioskLink ids
      Iterator<IKioskLink> kskLnksIter = kskLnks.iterator();
      while (kskLnksIter.hasNext()) {
        if (kskLnkIds == null) {
          kskLnkIds = new ArrayList<String>();
        }
        IKioskLink kskLnk = kskLnksIter.next();
        if (kskLnk != null) {
          if (RestConstantsZ.ACTION_REMOVERELATIONSHIP.equalsIgnoreCase(action)) {
            if (as.hasKioskLink(kskLnk.getKioskId(), kskLnk.getLinkType(),
                kskLnk.getLinkedKioskId())) {
              kskLnkIds.add(kskLnk.getId()); // Add the kiosk link id to the list of kiosk link ids
            } else {
              errMsgs.add("Error: KioskLink " + kskLnk.getId() + " does not exist");
            }
          } else {
            kskLnkIds.add(kskLnk.getId()); // Add the kiosk link id to the list of kiosk link ids
          }
        }
      }
      // If the action is remove or update, first remove the existing kiosk links
      if (RestConstantsZ.ACTION_REMOVERELATIONSHIP.equalsIgnoreCase(action)) {
        removeRelationship(req, resp, backendMessages, messages, as, kskLnkIds, errMsgs, domainId);
      } else if (RestConstantsZ.ACTION_CREATERELATIONSHIP.equalsIgnoreCase(action)) {
        // If action is create, then create new kiosk links
        createRelationship(req, resp, backendMessages, messages, as, kskLnks, domainId);
      } else if (RestConstantsZ.ACTION_UPDATERELATIONSHIP.equalsIgnoreCase(action)) {
        // Remove the links if and then create new links. specified
        List<String> kskLnkIdsToBeRemoved = null;
        if (lnkKidsRm != null && !lnkKidsRm
            .isEmpty()) { // If the lnkKidsRm is specified, get the KioskLinkId (key)
          // Iterate through kskLnkIdsRm
          Iterator<String> lnkKidsRmIter = lnkKidsRm.iterator();
          while (lnkKidsRmIter.hasNext()) {
            if (kskLnkIdsToBeRemoved == null) {
              kskLnkIdsToBeRemoved = new ArrayList<String>();
            }
            kskLnkIdsToBeRemoved
                .add(JDOUtils.createKioskLinkId(kioskId, type, Long.valueOf(lnkKidsRmIter.next())));
          }

        } else {
          // Get the existing kiosk links of type for the kiosk
          Results results = as.getKioskLinks(kioskId, type, null, null, null);
          @SuppressWarnings("unchecked")
          List<IKioskLink> kskLnksToBeRemoved = results.getResults();
          if (kskLnksToBeRemoved == null || kskLnksToBeRemoved.isEmpty()) {
            throw new ServiceException("Links to be removed is null");
          }
          Iterator<IKioskLink> kskLnksToBeRemovedIter = kskLnksToBeRemoved.iterator();

          while (kskLnksToBeRemovedIter.hasNext()) {
            if (kskLnkIdsToBeRemoved == null) {
              kskLnkIdsToBeRemoved = new ArrayList<String>();
            }
            kskLnkIdsToBeRemoved.add(kskLnksToBeRemovedIter.next().getId());
          }
        }
        removeRelationship(req, resp, backendMessages, messages, as, kskLnkIdsToBeRemoved, errMsgs,
            domainId);

        // Create new relationship now
        createRelationship(req, resp, backendMessages, messages, as, kskLnks, domainId);
      } else {
        // Error case. Should never come here.
        xLogger.severe("ERROR!");
      }
    } catch (Exception e) {
      errMsg = e.getMessage();
      status = false;
      sendBasicError(resp, locale.toString(), errMsg, errMsgs, HttpServletResponse.SC_OK);
      xLogger.severe("{0} while managing relationship. Message: {1}", e.getClass(), e.getMessage());
    }
    if (status) {
      try {
        //BasicOutput basicOutput = new BasicOutput( true, null, null, locale.toString(), RESTUtil.VERSION_01 );
        //sendJsonResponse( resp, HttpServletResponse.SC_OK, basicOutput.toJSONString() );
        String
            basicOutput =
            GsonUtil.basicOutputToJson(true, null, null, locale.toString(), RESTUtil.VERSION_01);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, basicOutput);

      } catch (Exception e) {

      }
    }
    xLogger.fine("Exiting manageRelationship");
  }

  private List<IKioskLink> getKioskLinks(HttpServletResponse resp, String uid, RelationshipInput ri,
                                         Locale locale, EntitiesService as, Vector<String> errMsgs,
                                         Long domainId) throws IOException {
    xLogger.fine("Entering getKioskLinks");
    String kioskIdStr = ri.getKioskId();
    Long kioskId = Long.valueOf(kioskIdStr);
    String type = ri.getLinkType();
    @SuppressWarnings("rawtypes")
    Vector lks = ri.getLinkedKiosks();
    String errMsg = null;
    if (lks == null || lks.isEmpty()) {
      xLogger.severe("Error while managing relationship. No linked kiosks are specified.");
      errMsg = "Error while managing relationship. No linked kiosks are specified.";
      sendBasicError(resp, locale.toString(), errMsg, errMsgs, HttpServletResponse.SC_OK);
      return null;
    }
    List<IKioskLink> kskLnks = null; // List of KioskLink objects

    @SuppressWarnings({"rawtypes", "unchecked"})
    Enumeration<Hashtable> lksEnum = lks.elements();
    Date createdOn = new Date();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      while (lksEnum.hasMoreElements()) {
        // Create a new list only the first time
        if (kskLnks == null) {
          kskLnks = new ArrayList<IKioskLink>();
        }

        IKioskLink kl = JDOUtils.createInstance(IKioskLink.class);
        kl.setDomainId(domainId);
        @SuppressWarnings("rawtypes")
        Hashtable lkht = lksEnum.nextElement();
        String lkidStr = (String) lkht.get(JsonTagsZ.LINKED_KIOSK_ID);
        Long lkid = Long.valueOf(lkidStr);
        IKiosk lk = null;
        // Get the kiosk corresponding to lkid
        try {
          lk = JDOUtils.getObjectById(IKiosk.class, lkid, pm);
        } catch (JDOObjectNotFoundException e) {
          xLogger.warn(e.getClass().getName() + ": " + e.getMessage());
          errMsgs.add(
              "Error while processing linked kiosk with id " + lkid + " Message: Kiosk " + lkid
                  + " does not exist");
        }

        if (lk != null) {
          // Set the kiosklink id
          String kskLnkId = JDOUtils.createKioskLinkId(kioskId, type, lkid);
          kl.setId(kskLnkId);

          // Set the kioskId
          kl.setKioskId(kioskId);
          // Set the link type
          kl.setLinkType(type);
          // Set the linked kiosk id
          kl.setLinkedKioskId(lkid);
          // Set the description
          String desc = null;
          if (lkht.containsKey(JsonTagsZ.DESCRIPTION)) {
            desc = (String) lkht.get(JsonTagsZ.DESCRIPTION);
          }
          if (desc != null && desc.isEmpty()) {
            kl.setDescription(desc);
          }

          // Set the created on date
          kl.setCreatedOn(createdOn);

          // Set the created by
          kl.setCreatedBy(uid);

          // Set the credit limit
          String creditLimitStr = null;
          if (lkht.containsKey(JsonTagsZ.CREDIT_LIMIT)) {
            creditLimitStr = (String) lkht.get(JsonTagsZ.CREDIT_LIMIT);
          }
          if (creditLimitStr != null && !creditLimitStr.isEmpty()) {
            kl.setCreditLimit(new BigDecimal(creditLimitStr));
          }

          kl.setLinkedKioskName(lk.getName());

          kskLnks.add(kl); // Add the kiosk link object to the list of kiosk links

        }
      } // End while
    } finally {
      pm.close(); // close pm
    }
    xLogger.fine("Exiting getKioskLinks");
    return kskLnks;
  }

  private void createRelationship(HttpServletRequest req, HttpServletResponse resp,
                                  ResourceBundle backendMessages, ResourceBundle messages,
                                  EntitiesService as, List<IKioskLink> kskLnks, Long domainId)
      throws IOException, ServiceException {
    xLogger.fine("Entering createRelationship");
    if (kskLnks == null || kskLnks.isEmpty()) {
      throw new ServiceException("List of KioskLink objects to be created is null");
    }
    // Use the AccountsService to add kiosk links
    as.addKioskLinks(domainId, kskLnks);
    xLogger.fine("Exiting createRelationship");
  }

  private void removeRelationship(HttpServletRequest req, HttpServletResponse resp,
                                  ResourceBundle backendMessages, ResourceBundle messages,
                                  EntitiesService as, List<String> kskLnkIds,
                                  Vector<String> errMsgs, Long domainId)
      throws IOException, ServiceException {
    xLogger.fine("Entering removeRelationship");
    if (kskLnkIds == null || kskLnkIds.isEmpty()) {
      throw new ServiceException("KioskLink Ids is null");
    }

    try {
      // Call the removeKioskLinks method in the AccountsService
      as.deleteKioskLinks(domainId, kskLnkIds, null);
    } catch (ServiceException e) {
      errMsgs.add("Errors while processing linked kiosk " + e.getMessage());
      throw new ServiceException(e.getMessage());
    }
    xLogger.fine("Exiting removeRelationship");
  }

  // NOTE: The returned user-account object will NOT have associated kiosks; if you need it, then you will need to get it separately (say, via a as.getUserAccount( userId, true );
  private IUserAccount validateCaller(HttpServletRequest req, HttpServletResponse resp,
                                      ResourceBundle backendMessages, ResourceBundle messages,
                                      String action, Vector<String> errMsgs)
      throws IOException, ServiceException {
    xLogger.fine("Entering validateCaller");
    String errMsg = null; // In case of Success, errMsg = null
    // String role = null;
    IUserAccount u = null;
    boolean status = true;
    int statusCode = HttpServletResponse.SC_OK;
    String localeStr = Constants.LANG_DEFAULT;
    // user id and password of the caller
    // Authenticate the user
    String userId = req.getParameter(RestConstantsZ.USER_ID);
    String password = req.getParameter(RestConstantsZ.PASSWORD);
    AuthenticationService aus = Services.getService(AuthenticationServiceImpl.class);
    String token = req.getHeader(Constants.TOKEN);
    String sourceInitiatorStr = req.getHeader(Constants.ACCESS_INITIATOR);
    int actionInitiator = -1;
    if (sourceInitiatorStr != null) {
      try {
        actionInitiator = Integer.parseInt(sourceInitiatorStr);
      } catch (NumberFormatException e) {

      }
    }
    if (StringUtils.isNotEmpty(token)) {
      try {
        userId = aus.authenticateToken(token, actionInitiator);
        UsersService usersService = Services.getService(UsersServiceImpl.class);
        u = usersService.getUserAccount(userId);
      } catch (ServiceException e) {
        errMsg = e.getMessage();
        status = false;
      } catch (ObjectNotFoundException e) {
        errMsg =
            backendMessages != null ? backendMessages.getString("error.invalidusername") : null;
        status = false;
      } catch (UnauthorizedException e) {
        errMsg = e.getMessage();
        status = false;
        statusCode = HttpServletResponse.SC_UNAUTHORIZED;
      }
    } else {
      if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
        errMsg = "Invalid user name or password";
        status = false;
      } else {
        // Authenticate
        try {
          u = RESTUtil.authenticate(userId, password, null, req, resp);
        } catch (ServiceException e) {
          xLogger.severe("Authentication of caller failed: Exception: {0}, Msg: {1} ",
              e.getClass().getName(), e.getMessage());
          errMsg = e.getMessage();
          status = false;
        }
      }
    }
    if (!status) {
      sendBasicError(resp, localeStr, errMsg, errMsgs, statusCode);
      return null;
    }

    xLogger.fine("Exiting validateCaller");
    return u;
  }

  // Get related entities, along with pagination
  @SuppressWarnings("unchecked")
  private void getRelatedEntities(HttpServletRequest req, HttpServletResponse resp,
                                  ResourceBundle backendMessages, ResourceBundle messages) {
    xLogger.fine("Entered getRelatedEntities");
    // Get the request parameters
    String userId = req.getParameter(RestConstantsZ.USER_ID);
    String password = req.getParameter(RestConstantsZ.PASSWORD);
    String kioskIdStr = req.getParameter(RestConstantsZ.KIOSK_ID);
    String relationshipType = req.getParameter(RestConstantsZ.RELATIONSHIP);
    String sizeStr = req.getParameter(RestConstantsZ.SIZE);
    String localeStr = Constants.LANG_DEFAULT;
    Vector<Hashtable<String, String>> linkedKiosks = null;
    String errMsg = null;
    boolean status = true;
    int statusCode = HttpServletResponse.SC_OK;
    if (kioskIdStr == null || kioskIdStr.isEmpty() || relationshipType == null || relationshipType
        .isEmpty()) {
      errMsg = "Invalid parameters";
      status = false;
    } else {
      Long kioskId = Long.valueOf(kioskIdStr);
      int size = PageParams.DEFAULT_SIZE;
      if (sizeStr != null && !sizeStr.isEmpty()) {
        size = Integer.parseInt(sizeStr);
      }
      String offsetStr = req.getParameter(Constants.OFFSET);
      int offset = 0;
      if (StringUtils.isNotBlank(offsetStr)) {
        try {
          offset = Integer.parseInt(offsetStr);
        } catch (Exception e) {
          xLogger.warn("Invalid offset {0}: {1}", offsetStr, e.getMessage());
        }
      }
      PageParams pageParams = new PageParams(offset, size);
      try {
        // Authenticate user
        IUserAccount u = RESTUtil.authenticate(userId, password, kioskId, req, resp);
        if (userId == null) // in case of Basic authentication
        {
          userId = u.getUserId();
        }
        localeStr = u.getLocale().toString();
        // Get domain config
        DomainConfig dc = DomainConfig.getInstance(u.getDomainId());
        // Get the related kiosks
        Results
            results =
            RESTUtil.getLinkedKiosks(kioskId, relationshipType, userId, true, dc, pageParams);
        linkedKiosks = (Vector<Hashtable<String, String>>) results.getResults();
      } catch (ServiceException e) {
        errMsg = e.getMessage();
        status = false;
      } catch (UnauthorizedException e) {
        errMsg = e.getMessage();
        status = false;
        statusCode = HttpServletResponse.SC_UNAUTHORIZED;
      } catch (Exception e) {
        errMsg = "System error occurred. Please notify administrator. [" + e.getMessage() + "]";
        status = false;
      }
      // Form the JSON output
      try {
        //GetRelationshipsOutput gro = new GetRelationshipsOutput( status, relationshipType, linkedKiosks, null, errMsg, localeStr, RESTUtil.VERSION_01 );
        //sendJsonResponse( resp, statusCode, gro.toJSONString() );
        String
            Jsongro =
            GsonUtil.getRelationshipsOutputToJson(status, relationshipType, linkedKiosks, errMsg,
                localeStr, RESTUtil.VERSION_01);
        sendJsonResponse(resp, statusCode, Jsongro);
      } catch (Exception e) {
        xLogger.severe(
            "{0} when sending JSON response for getRelatedEntities for kiosk {1} and relationships {2}: {3}",
            e.getClass().getName(), kioskIdStr, relationshipType, e.getMessage());
        resp.setStatus(500);
      }
    }
    xLogger.fine("Exiting getRelatedEntities");
  }

  private void sendBasicError(HttpServletResponse resp, String localeStr, String errMsg,
                              Vector<String> errMsgs, int statusCode) throws IOException {
    xLogger.fine("Entering sendBasicError");
    // Create a BasicOutput object
    try {
      //BasicOutput basicOutput = new BasicOutput( false, errMsg, errMsgs, localeStr, RESTUtil.VERSION_01 );
      //sendJsonResponse( resp, statusCode, basicOutput.toJSONString() );
      String
          basicOutput =
          GsonUtil.basicOutputToJson(false, errMsg, errMsgs, localeStr, RESTUtil.VERSION_01);
      sendJsonResponse(resp, statusCode, basicOutput);
    } catch (Exception e) {
      xLogger
          .severe("SetupDataServlet Protocol Exception: Class: {0} : {1}", e.getClass().getName(),
              e.getMessage());
      resp.setStatus(500);
    }
    xLogger.fine("Exiting sendBasicError");
  }
}
