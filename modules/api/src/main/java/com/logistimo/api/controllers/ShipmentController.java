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

package com.logistimo.api.controllers;

import com.logistimo.api.builders.ShipmentBuilder;
import com.logistimo.api.models.OrderStatusModel;
import com.logistimo.api.models.ShipmentResponseModel;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.entities.auth.EntityAuthoriser;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.LogiException;
import com.logistimo.inventory.exceptions.InventoryAllocationException;
import com.logistimo.logger.XLog;
import com.logistimo.models.ResponseModel;
import com.logistimo.models.shipments.ShipmentMaterialsModel;
import com.logistimo.models.shipments.ShipmentModel;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.LockUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Mohan Raja on 04/10/16
 */
@Controller
@RequestMapping("/shipment")
public class ShipmentController {
  private static final XLog xLogger = XLog.getLog(ShipmentController.class);
  ShipmentBuilder builder = new ShipmentBuilder();

  @RequestMapping(value = "/add/", method = RequestMethod.POST)
  public
  @ResponseBody
  ShipmentMaterialsModel addShipment(@RequestBody ShipmentModel model, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    ResourceBundle
        backendMessages =
        Resources.get().getBundle("BackendMessages", sUser.getLocale());
    try {

      model.userID = userId;
      model.sdid = domainId;
      IShipmentService ss = Services.getService(ShipmentService.class);
      if (model.changeStatus != null) {
        model.status = ShipmentStatus.getStatus(model.changeStatus);
      }
      String shipId = ss.createShipment(model);
      ShipmentMaterialsModel m = new ShipmentMaterialsModel();
      if (StringUtils.isNotBlank(shipId)) {
        m.msg = "Shipment created successfully.";
        m.sId = shipId;
        return m;
      } else {
        throw new InvalidServiceException("Error while creating shipment");
      }
    } catch (InventoryAllocationException ie) {
      xLogger.severe("Error while creating shipment", ie);
      if (ie.getCode() != null) {
        throw new InvalidDataException(ie.getMessage());
      } else {
        throw new InvalidServiceException(backendMessages.getString("shipment.create.error"));
      }
    } catch (ServiceException se) {
      xLogger.severe("Error while creating shipment", se);
      if (se.getCode() != null) {
        throw new InvalidServiceException(se);
      } else {
        throw new InvalidServiceException("Error while creating shipments");
      }
    } catch (Exception e) {
      xLogger.severe("Error while creating shipments", e);
      throw new InvalidServiceException("Error while creating shipments");
    }
  }

  @RequestMapping(value = "/detail/{sID}", method = RequestMethod.GET)
  public
  @ResponseBody
  ShipmentModel getShipment(@PathVariable String sID, HttpServletRequest request) {
    try {
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      IShipmentService ss = Services.getService(ShipmentService.class);
      IShipment shipment = ss.getShipment(sID);
      return builder.buildShipmentModel(shipment, sUser, true);
    } catch (Exception e) {
      xLogger.severe("Error while getting shipment", e);
      throw new InvalidServiceException("Error while getting shipments");
    }
  }


  @RequestMapping(value = "/", method = RequestMethod.GET)
  public
  @ResponseBody
  @SuppressWarnings("unchecked")
  Results getShipments(@RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
                       @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
                       @RequestParam(required = false) Long custId,
                       @RequestParam(required = false) Long vendId,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       @RequestParam(required = false) String eftFrom,
                       @RequestParam(required = false) String eftTo,
                       @RequestParam(required = false) String trans,
                       @RequestParam(required = false) String trackId,
                       HttpServletRequest request) {
    try {
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      IShipmentService ss = Services.getService(ShipmentService.class);
      SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
      if (to != null && !to.isEmpty()) {
        to = sdf.format(LocalDateUtil.getOffsetDate(sdf.parse(to), 1));
      }
      if (eftTo != null && !eftTo.isEmpty()) {
        eftTo = sdf.format(LocalDateUtil.getOffsetDate(sdf.parse(eftTo), 1));
      }
      Results shipments = ss.getShipments(userId, domainId, custId, vendId,
          from != null ? sdf.parse(from) : null, to != null ? sdf.parse(to) : null,
          eftFrom != null ? sdf.parse(eftFrom) : null, eftTo != null ? sdf.parse(eftTo) : null,
          trans, trackId, ShipmentStatus.getStatus(status), size, offset);
      return new Results(builder.buildShipmentModels(shipments.getResults(), sUser), null,
          shipments.getNumFound(), shipments.getOffset());
    } catch (Exception e) {
      xLogger.severe("Error while creating shipments", e);
      throw new InvalidServiceException("Error while creating shipments");
    }
  }

  @RequestMapping(value = "/update/sitems", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateShipmentItems(@RequestBody ShipmentMaterialsModel model,
                             HttpServletRequest request, HttpServletResponse response) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long
        orderId =
        Long.parseLong(model.sId.substring(0, model.sId.indexOf(CharacterConstants.HYPHEN)));
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", orderId));
    }
    try {
      IShipmentService ss = Services.getService(ShipmentService.class, user.getLocale());
      model.userId = user.getUsername();
      boolean isSuccess;
      ResponseModel responseModel = new ResponseModel();
      if (model.isFulfil) {
        responseModel = ss.fulfillShipment(model, model.userId);
        isSuccess = responseModel.status;
      } else {
        isSuccess = ss.updateShipment(model);
      }
      if (isSuccess) {
        String returnMessage = backendMessages.getString("shipments.updated.successfully")
          + CharacterConstants.DOT;
          if (StringUtils.isNotEmpty(responseModel.message)) {
            returnMessage += CharacterConstants.SPACE + responseModel.message;
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
          }
        return returnMessage;
      } else {
        throw new Exception("Error while updating shipment");
      }
    } catch (InventoryAllocationException ie) {
      throw new InvalidDataException(ie.getMessage());
    } catch (LogiException ie) {
      xLogger.severe("Error while creating shipment", ie);
      if (ie.getCode() != null) {
        throw new InvalidDataException(ie.getMessage());
      } else {
        throw new InvalidServiceException(backendMessages.getString("shipment.create.error"));
      }
    } catch (InvalidServiceException e1) {
      xLogger.warn("Error while updating shipment status", e1);
      throw new InvalidServiceException(e1.getMessage());
    } catch (Exception e) {
      xLogger.severe("Error in updating shipment items", e);
      throw new InvalidServiceException("Error in updating shipment items");
    } finally {
      if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil.release(Constants.TX_O + orderId)) {
        xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + orderId);
      }
    }
  }

  @RequestMapping(value = "/update/{shipId}/status", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateShipmentStatus(@PathVariable String shipId, @RequestBody OrderStatusModel status,
                              HttpServletRequest request, HttpServletResponse response) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long orderId = Long.parseLong(shipId.substring(0, shipId.indexOf(CharacterConstants.HYPHEN)));
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", orderId));
    }
    ShipmentStatus shipmentStatus;
    if (ShipmentStatus.SHIPPED.toString().equals(status.st)) {
      shipmentStatus = ShipmentStatus.SHIPPED;
    } else if (ShipmentStatus.CANCELLED.toString().equals(status.st)) {
      shipmentStatus = ShipmentStatus.CANCELLED;
    } else {
      throw new InvalidServiceException("Invalid status to update");
    }
    try {
      IShipmentService ss = Services.getService(ShipmentService.class, user.getLocale());
      ResponseModel
          responseModel =
          ss.updateShipmentStatus(shipId, shipmentStatus, status.msg, user.getUsername(),
              status.cdrsn);
      boolean isSuccess = responseModel.status;
      if (!isSuccess) {
        throw new Exception("Error while updating status for shipment " + shipId);
      }
      String returnMessage = backendMessages.getString("shipment.status.updated.successfully")
          + CharacterConstants.DOT;
      if (StringUtils.isNotEmpty(responseModel.message)) {
        returnMessage += CharacterConstants.SPACE + responseModel.message;
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
      }
      return returnMessage;
    } catch (LogiException e) {
      if (e.getCode() != null) {
        xLogger.warn("Error in updating shipment status", e);
        throw new InvalidDataException(e.getMessage());
      } else {
        xLogger.severe("Error in updating shipment status", e);
        throw new InvalidServiceException("Error in updating shipment status");
      }
    } catch (Exception e) {
      xLogger.severe("Error in updating shipment status", e);
      throw new InvalidServiceException("Error in updating shipment status");
    } finally {
      if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil.release(Constants.TX_O + orderId)) {
        xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + orderId);
      }
    }
  }

  @RequestMapping("/{orderId}")
  public
  @ResponseBody
  List<ShipmentModel> getShipmentsByOrderId(@PathVariable Long orderId,
                                            HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      IShipmentService ss = Services.getService(ShipmentService.class, user.getLocale());
      List<IShipment> shipments = ss.getShipmentsByOrderId(orderId);
      OrderManagementService os = Services.getService(OrderManagementServiceImpl.class);
      IOrder order = os.getOrder(orderId);
      if (order.getServicingKiosk() != null) {
        Integer
            vPermission =
            EntityAuthoriser.authoriseEntityPerm(order.getServicingKiosk(), user.getRole(),
                user.getLocale(), user.getUsername(), user.getDomainId());

        if (vPermission < 1) {
          for (int i = 0; i < shipments.size(); i++) {
            ShipmentStatus status = shipments.get(i).getStatus();
            if (status != ShipmentStatus.SHIPPED && status != ShipmentStatus.FULFILLED) {
              shipments.remove(i--);
            }
          }
        }
      }
      return builder.buildShipmentModels(shipments, user);
    } catch (Exception e) {
      xLogger.warn("Error while getting shipments for order {0}", orderId, e);
      throw new InvalidServiceException(backendMessages.getString("shipments.fetch.error"));
    }
  }

  @RequestMapping(value = "/update/{sId}/transporter", method = RequestMethod.POST)
  public
  @ResponseBody
  ShipmentResponseModel updateShipmentInfo(@PathVariable String sId, @RequestBody String updValue,
                            @RequestParam(required = false, value = "orderUpdatedAt") String orderUpdatedAt,
                            HttpServletRequest request) {
    return updateShipmentData("tpName",updValue, orderUpdatedAt, sId, request, "ship.transporter.update.success","ship.transporter.update.error");
  }

  @RequestMapping(value = "/update/{sId}/trackingID", method = RequestMethod.POST)
  public
  @ResponseBody
  ShipmentResponseModel updateShipmentTrackingId(@PathVariable String sId, @RequestBody String updValue,
                                  @RequestParam(required = false, value = "orderUpdatedAt") String orderUpdatedAt,
                                  HttpServletRequest request) {
    return updateShipmentData("tId", updValue, orderUpdatedAt, sId, request, "ship.tracking.id.update.success", "ship.tracking.id.update.error");
  }

  @RequestMapping(value = "/update/{sId}/rfs", method = RequestMethod.POST)
  public
  @ResponseBody
  ShipmentResponseModel updateShipmentReason(@PathVariable String sId, @RequestBody String updValue,
                              @RequestParam(required = false, value = "orderUpdatedAt") String orderUpdatedAt,
                              HttpServletRequest request) {
    return updateShipmentData("rsn", updValue, orderUpdatedAt, sId, request, "ship.reason.update.success", "ship.reason.update.error");
  }

  @RequestMapping(value = "/update/{sId}/date", method = RequestMethod.POST)
  public
  @ResponseBody
  ShipmentResponseModel updateShipmentDate(@PathVariable String sId, @RequestBody String updValue,
                            @RequestParam(required = false, value = "orderUpdatedAt") String orderUpdatedAt,
                            HttpServletRequest request) {
      if(StringUtils.isNotBlank(updValue)) {
          return updateShipmentData("date",updValue, orderUpdatedAt, sId, request," ", "ship.expected.date.parse.error");
      } else {
        return null;
      }
  }


  private ShipmentResponseModel updateShipmentData(String updType, String updValue, String orderUpdatedAt,
                                     String sId, HttpServletRequest request, String succesKey, String errorKey) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    IShipment shipment;
    ShipmentResponseModel model;
    String successMessage = Constants.EMPTY;
    try {
      if("date".equals(updType)) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        successMessage = LocalDateUtil
            .format(sdf.parse(updValue), user.getLocale(), user.getTimezone(), true);

      }
      IShipmentService ss = Services.getService(ShipmentService.class, user.getLocale());
      String userId = user.getUsername();
      shipment = ss.updateShipmentData(updType, updValue, orderUpdatedAt, sId, userId);
      if(StringUtils.isEmpty(successMessage)) {
        successMessage = backendMessages.getString(succesKey);
      }

      model = new ShipmentResponseModel(successMessage, LocalDateUtil.formatCustom(shipment.getUpdatedOn(), Constants.DATETIME_FORMAT, null));
    } catch (Exception e) {
      xLogger.warn("Error while updating shipment", e);
      throw new InvalidServiceException(backendMessages.getString(errorKey));
    }
    return model;
  }


  @RequestMapping(value = "/transfilter", method = RequestMethod.GET)
  public
  @ResponseBody
  List<String> getTransSuggestions(@RequestParam String text, HttpServletRequest request) {
    List<String> transporters;
    try {
      SecureUserDetails user = SecurityUtils.getUserDetails(request);
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
      IShipmentService ss = Services.getService(ShipmentService.class, request.getLocale());
      transporters = ss.getTransporterSuggestions(domainId, text);
      return transporters;
    } catch (Exception e) {
      xLogger.warn("Error in getting id for suggestions", e);
    }
    return null;
  }

  @RequestMapping(value = "/update/{sId}/ps", method = RequestMethod.POST)
  public
  @ResponseBody
  ShipmentResponseModel updateShipmentPackageSize(@PathVariable String sId, @RequestBody String updValue,
                                   HttpServletRequest request) {
    return updateShipmentData("ps", updValue, null, sId, request, "ship.package.size.update.success","ship.package.size.update.error");
  }
}
