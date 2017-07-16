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

package com.logistimo.users.service.impl;

import com.logistimo.AppFactory;
import com.logistimo.api.models.UserDeviceModel;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.QueryConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.entities.entity.IUserToKiosk;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.exceptions.EventGenerationException;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.SystemException;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.locations.LocationServiceUtil;
import com.logistimo.logger.XLog;
import com.logistimo.models.users.UserLoginHistoryModel;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.builders.UserDeviceBuilder;
import com.logistimo.users.dao.IUserDao;
import com.logistimo.users.dao.UserDao;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.IUserDevice;
import com.logistimo.users.entity.UserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.utils.Counter;
import com.logistimo.utils.GsonUtils;
import com.logistimo.utils.MessageUtil;
import com.logistimo.utils.PasswordEncoder;
import com.logistimo.utils.QueryUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * Created by charan on 04/03/17.
 */
@Service
public class UsersServiceImpl extends ServiceImpl implements UsersService {

  private static final XLog xLogger = XLog.getLog(UsersServiceImpl.class);
  private static final String LOGUSER_TASK_URL = "/s2/api/users/update/loginhistory";

  private ITagDao tagDao = new TagDao();
  private IUserDao userDao = new UserDao();

  /**
   * Check to see if a user id exists in the system or not.
   *
   * @param userId The id of the user to be checked
   * @return true, if the user exists (even if inactive); false, if this id does not exist
   */
  public boolean userExists(String userId) throws ServiceException {
    xLogger.fine("Entering userExists");

    if (userId == null || userId.isEmpty()) {
      throw new ServiceException("Invalid user Id: " + userId);
    }

    boolean userExists = false;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      JDOUtils.getObjectById(IUserAccount.class, userId, pm);
      userExists = true; // if we come here, then the user with this id must exist
    } catch (JDOObjectNotFoundException e) {
      // user with this id does NOT exist; do nothing
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting userExists");
    return userExists;
  }

  /**
   * Add a new user account
   */
  @SuppressWarnings("finally")
  public String addAccount(Long domainId, IUserAccount account) throws ServiceException {
    xLogger.fine("Entering addAccount");

    if (domainId == null || account == null) {
      throw new ServiceException("Invalid input parameters");
    }

    boolean userExists = false;
    String errMsg = null;
    Exception exception = null;
    //Assuming that all other fields including registeredBy is set by the calling function
    Date now = new Date();
    //account.setLastLogin(now);
    account.setMemberSince(now);
    account.setUpdatedOn(now);
    account.setEnabled(true);
    // Set the domain ID
    account.setDomainId(domainId);
    account.setFirstName(StringUtil.getTrimmedName(account.getFirstName()));
    account.setLastName(StringUtil.getTrimmedName(account.getLastName()));

    // Set the accDids field to domainid
    List<Long> accDids = new ArrayList<Long>();
    accDids.add(domainId);
    account.setAccessibleDomainIds(accDids);
    // Create the unique user key: <domainId>.<userId>
    String accountId = account.getUserId();
    xLogger.info("addAccount: userId is {0}", accountId);
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      try {
        if (!AppFactory.get().getAuthorizationService().authoriseUpdateKiosk(
            account.getRegisteredBy(), domainId)) {
          throw new UnauthorizedException(backendMessages.getString("permission.denied"));
        }
        //First check if the user already exists in the database
        @SuppressWarnings("unused")
        IUserAccount user = JDOUtils.getObjectById(IUserAccount.class, accountId, pm);
        //If we get here, it means the user exists
        xLogger.warn("addAccount: User {0} already exists", accountId);
        userExists = true;
      } catch (JDOObjectNotFoundException e) {
        xLogger.fine("addAccount: User {0} does not exist. Adding user to database", accountId);
        // Check if custom ID is specified for the user account. If yes, check if the specified custom ID already exists.
        boolean customIdExists = false;
        if (account.getCustomId() != null && !account.getCustomId().isEmpty()) {
          customIdExists = checkIfCustomIdExists(account);
        }
        if (customIdExists) {
          // Custom ID already exists in the database!
          xLogger.warn("addAccount: FAILED!! Cannot add account {0}. Custom ID {1} already exists",
              account.getUserId(), account.getCustomId());
          throw new ServiceException(
              backendMessages.getString("error.cannotadd") + " '" + account.getUserId() + "'. "
                  + messages.getString("customid") + " " + account.getCustomId() + " "
                  + backendMessages.getString("error.alreadyexists") + ".");
        }

        //Encode the password before setting it in the database
        String password = account.getEncodedPassword();
        account.setEncodedPassword(PasswordEncoder.MD5(password));
        if (account.getTags() != null) {
          account.setTgs(tagDao.getTagsByNames(account.getTags(), ITag.USER_TAG));
        }
        // Add the user to this domain and the parents of this domain (superdomains)
        account =
            (IUserAccount) DomainsUtil.addToDomain(account, domainId,
                pm); // Earlier pm.makePersistent(account);
        xLogger.info("addAccount: adding user {0}", account.getUserId(),
            account.getUserId());
        // Increment counter
        List<Long> domainIds = account.getDomainIds();
        //add user location ids
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("userId", account.getUserId());
        reqMap.put("userName", account.getRegisteredBy());
        Map<String, Object>
            lidMap =
            LocationServiceUtil.getInstance().getLocationIds(account, reqMap);
        if (lidMap.get("status") == "success") {
          updateUserLocationIds(account, lidMap, pm);
        }
        account = pm.detachCopy(account);
      }
      // Generate event, if configured
      try {
        EventPublisher.generate(domainId, IEvent.CREATED, null,
            JDOUtils.getImplClass(IUserAccount.class).getName(), account.getKeyString(), null);
      } catch (EventGenerationException e) {
        exception = e;
        errMsg = e.getMessage();
        xLogger.warn(
            "Exception when generating event for user creation for user {0} in domain {1}: {2}",
            account.getUserId(), domainId, e.getMessage());
      }
    } catch (Exception e) {
      errMsg = e.getMessage();
      exception = e;
    } finally {
      pm.close();
    }
    if (userExists) {
      errMsg =
          messages.getString("user") + " '" + account.getUserId() + "' " + backendMessages
              .getString("user.exists");
    }
    xLogger.fine("Exiting addAccount");

    if (errMsg != null) {
      throw new ServiceException(errMsg, exception);
    }

    return accountId;
  }

  @SuppressWarnings("unchecked")
  public IUserAccount getUserAccount(String userId)
      throws ObjectNotFoundException {
    xLogger.fine("Entering getUserAccount");
    if (userId == null) {
      throw new IllegalArgumentException("Invalid user ID");
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    IUserAccount user = null;
    Exception exception = null;
    boolean notFound = false;
    try {
      //Get the user object from the database
      user = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
      user = pm.detachCopy(user);
      return user;
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("getUserAccount: User {0} does not exist in the database", userId);
      notFound = true;
      exception = e;
    } catch (Exception e) {
      xLogger.warn("getUserAccount: Exception : {0}", e.getMessage(), e);
      exception = e;
    } finally {
      pm.close();
    }
    if (notFound) {
      throw new ObjectNotFoundException("USR001", userId);
    }

    throw new SystemException(exception);
  }

  public Results getUsers(Long domainId, IUserAccount user, boolean activeUsersOnly,
                          String nameStartsWith,
                          PageParams pageParams, boolean includeChildDomainUsers)
      throws ServiceException {
    return getUsers(domainId, user, activeUsersOnly, false, nameStartsWith, pageParams,
        includeChildDomainUsers);
  }

  // Get users visible to a given user
  @SuppressWarnings("unchecked")
  public Results getUsers(Long domainId, IUserAccount user, boolean activeUsersOnly,
                          String nameStartsWith, PageParams pageParams) throws ServiceException {
    return getUsers(domainId, user, activeUsersOnly, false, nameStartsWith, pageParams, false);
  }

  // Get all users of a given role
  @SuppressWarnings("unchecked")
  public Results getUsers(Long domainId, String role, boolean activeUsersOnly,
                          String nameStartsWith, PageParams pageParams) throws ServiceException {
    xLogger.fine("Entering getUsers");
    if (domainId == null || role == null) {
      throw new ServiceException("Invalid input parameters");
    }
    List<IUserAccount> filtered = null;
    List<IUserAccount> users = null;
    String cursor = null;
    // Get the users of the given role
    Results
        results =
        findAccountsByDomain(domainId, IUserAccount.ROLE, role, nameStartsWith,
            pageParams, false); // users of a given role
    if (results != null) {
      users = results.getResults();
      cursor = results.getCursor();
    }
    // Filter based on active-ness
    if (activeUsersOnly && users != null && !users.isEmpty()) {
      // Filter out inactive users, if needed
      filtered = new ArrayList<IUserAccount>();
      Iterator<IUserAccount> it = users.iterator();
      while (it.hasNext()) {
        IUserAccount u = it.next();
        if (u.isEnabled()) {
          filtered.add(u);
        }
      }
    }
    xLogger.fine("Exit getUsers");
    if (filtered != null) {
      return new Results(filtered, cursor);
    } else {
      return new Results(users, cursor);
    }
  }

  /**
   * Get users visible to a given user along with all super users(across domains)
   */
  public Results getUsers(Long domainId, IUserAccount user, boolean activeUsersOnly,
                          boolean includeSuperusers, String nameStartsWith, PageParams pageParams,
                          boolean includeChildDomainUsers)
      throws ServiceException {
    xLogger.fine("Entered getUsers, includeSuperusers: {0}", includeSuperusers);
    List<IUserAccount> filteredUsers = new ArrayList<IUserAccount>();
    String role = user.getRole();
    List<IUserAccount> users = null;
    Results results = null;
    boolean isDomainOwner = SecurityConstants.ROLE_DOMAINOWNER.equals(role);
    if (SecurityConstants.ROLE_SUPERUSER.equals(role) || isDomainOwner) {
      results =
          findAllAccountsByDomain(domainId, nameStartsWith, pageParams,
              includeChildDomainUsers, user); // all users in domain
      users = (List<IUserAccount>) results.getResults();
      if (includeSuperusers && SecurityConstants.ROLE_SUPERUSER.equals(role)) {
        List<IUserAccount> superusers = getSuperusers();
        if (superusers != null && !superusers.isEmpty()) {
          // Iterate through superusers. If any superuser is already present in users, do not add it.
          // Otherwise, add it. Check if the superuser's normalized name starts with the nameStartsWith in case it is specified.
          Iterator<IUserAccount> superusersIt = superusers.iterator();
          while (superusersIt.hasNext()) {
            IUserAccount superuser = superusersIt.next();
            String nsuperuserName = superuser.getFullName().toLowerCase();
            if (!users.contains(superuser) && (nameStartsWith == null || nameStartsWith.isEmpty()
                || nsuperuserName.startsWith(nameStartsWith))) {
              users.add(superuser);
            }
          }
        }
      }
      if (!activeUsersOnly && SecurityConstants.ROLE_SUPERUSER.equals(role)) {
        return new Results(users, results.getCursor());
      }
    } else {
      results =
          findAccountsByDomain(domainId, IUserAccount.REGISTERED_BY, user.getUserId(),
              nameStartsWith,
              pageParams,
              includeChildDomainUsers); // users that were registered/created by this user
      List<IUserAccount> regUsers = (List<IUserAccount>) results.getResults();
      if (!user.getUserId().equals(
          user.getRegisteredBy())) { // this user did not register himself, so is unlikely to be in the list; add him
        users = new ArrayList<IUserAccount>();
        users.add(user); // add this user
        users.addAll(regUsers); // add other users registered by him
        if (users.size() > 1) {
          sortUsers(users);
        }
      } else {
        users = regUsers;
      }
    }
    // Iterate and filter users as needed
    Iterator<IUserAccount> it = users.iterator();
    while (it.hasNext()) {
      IUserAccount u = it.next();
      if ((!activeUsersOnly || u.isEnabled()) &&
          (u.getUserId().equals(user.getUserId())
              || SecurityUtil.compareRoles(role, u.getRole()) >= 0 || SecurityConstants.ROLE_SUPERUSER
              .equals(role))) {
        filteredUsers.add(u);
      }
    }
    xLogger.fine("Exiting getUsers");
    return new Results(filteredUsers, QueryUtil.getCursor(results.getResults()));
  }

  public Results getUsersByFilter(Long domainId, IUserAccount user, Map<String, Object> filters,
                                  PageParams pageParams) throws ServiceException {
    xLogger.fine("Entered getUsersByFilter ");
    List<IUserAccount> users = null;
    String cursor = null;
    Results results;
    String role = user.getRole();
    boolean isDomainOwner = SecurityConstants.ROLE_DOMAINOWNER.equals(role);
    if (SecurityConstants.ROLE_SUPERUSER.equals(role)) {
      results = findAccountsByFilter(domainId, filters, pageParams, false);
      if (results == null) {
        return new Results(null, null);
      }
      users = results.getResults();
      cursor = results.getCursor();
    } else if (isDomainOwner) {
      results = findAccountsByFilter(domainId, filters, pageParams, true);
      if (results == null) {
        return new Results(null, null);
      }
      users = results.getResults();
      cursor = results.getCursor();

    } else {
      results =
          findAccountsByDomain(domainId, IUserAccount.REGISTERED_BY, user.getUserId(), "",
              pageParams, false); // users that were registered/created by this user
      List<IUserAccount> regUsers = (List<IUserAccount>) results.getResults();
      if (!user.getUserId().equals(
          user.getRegisteredBy())) { // this user did not register himself, so is unlikely to be in the list; add him
        users = new ArrayList<IUserAccount>();
        users.add(user); // add this user
        users.addAll(regUsers); // add other users registered by him
        if (users.size() > 1) {
          sortUsers(users);
        }
      } else {
        users = regUsers;
      }
    }
    xLogger.fine("Exit getUsersByFilter");
    return new Results(users, cursor, results.getNumFound(), 0);
  }

  /**
   * Get all the super users (across domains)
   */
  @SuppressWarnings("unchecked")
  public List<IUserAccount> getSuperusers() throws ServiceException {
    xLogger.fine("Entering getSuperusers");
    List<IUserAccount> superUsers = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(JDOUtils.getImplClass(IUserAccount.class));
    String roleParam = SecurityConstants.ROLE_SUPERUSER;
    String declaration = " String roleParam";
    q.setFilter("role == roleParam");
    q.declareParameters(declaration);
    try {
      superUsers = (List<IUserAccount>) q.execute(roleParam);
      if (superUsers != null) {
        superUsers.size(); // This is done so that pm can be closed without throwing an exception.
        superUsers = (List<IUserAccount>) pm.detachCopyAll(superUsers);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    xLogger.fine("Exiting getSuperusers");
    return superUsers;
  }

  /**
   * Update a given user account
   *
   * @param account   - user account with updated values
   * @param updatedBy - Id of user who is updating the user account
   */
  public void updateAccount(IUserAccount account, String updatedBy) throws ServiceException {
    xLogger.fine("Entering updateAccount");
    boolean userExists = true;
    String errMsg = null;
    Exception exception = null;
    Date now = new Date();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    //We use an atomic transaction here to check if the user exists and then update it
    ///Transaction tx = pm.currentTransaction();
    try {
      if (!AppFactory.get().getAuthorizationService()
          .authoriseUpdateKiosk(updatedBy, account.getDomainId())) {
        throw new UnauthorizedException(backendMessages.getString("permission.denied"));
      }
      //First check if the user already exists in the database
      IUserAccount user = JDOUtils.getObjectById(IUserAccount.class, account.getUserId(), pm);
      //If we get here, it means the user exists
      user.setRole(account.getRole());
      user.setFirstName(StringUtil.getTrimmedName(account.getFirstName()));
      user.setLastName(StringUtil.getTrimmedName(account.getLastName()));
      user.setMobilePhoneNumber(account.getMobilePhoneNumber());
      user.setLandPhoneNumber(account.getLandPhoneNumber());
      user.setCity(account.getCity());
      user.setStreet(account.getStreet());
      user.setTaluk(account.getTaluk());
      user.setDistrict(account.getDistrict());
      user.setState(account.getState());
      user.setCountry(account.getCountry());
      user.setPinCode(account.getPinCode());
      user.setGender(account.getGender());
      user.setAgeType(account.getAgeType());
      user.setAuthenticationTokenExpiry(account.getAuthenticationTokenExpiry());
      if (IUserAccount.AGETYPE_BIRTHDATE.equals(account.getAgeType())) {
        user.setBirthdate(account.getBirthdate());
      } else {
        user.setAge(account.getAge());
      }
      user.setLastLogin(account.getLastLogin());
      user.setEmail(account.getEmail());
      user.setLanguage(account.getLanguage());
      user.setPhoneBrand(account.getPhoneBrand());
      user.setPhoneModelNumber(account.getPhoneModelNumber());
      user.setImei(account.getImei());
      user.setPhoneServiceProvider(account.getPhoneServiceProvider());
      user.setTimezone(account.getTimezone());
      user.setSimId(account.getSimId());
      user.setPermission(account.getPermission());

      // Update user agent strings
      user.setUserAgent(account.getUserAgent());
      user.setPreviousUserAgent(account.getPreviousUserAgent());
      // Update IP Address
      user.setIPAddress(account.getIPAddress());
      user.setAppVersion(account.getAppVersion());
      // Last reconnected
      user.setLastMobileAccessed(account.getLastMobileAccessed());
      // Primary kiosk
      user.setPrimaryKiosk(account.getPrimaryKiosk());
      user.setUiPref(account.getUiPref());
      //updated time
      user.setUpdatedOn(now);
      user.setUpdatedBy(account.getUpdatedBy());
      //Accessible dids
      user.setAccessibleDomainIds(account.getAccessibleDomainIds());
      user.setLoginReconnect(account.getLoginReconnect());
      user.setStoreAppTheme(account.getStoreAppTheme());

      // Check if custom ID is specified for the user account. If yes, check if the specified custom ID already exists.
      boolean customIdExists = false;
      if (account.getCustomId() != null && !account.getCustomId().isEmpty() && !account
          .getCustomId().equals(user.getCustomId())) {
        customIdExists = checkIfCustomIdExists(account);
      }
      if (customIdExists) {
        // Custom ID already exists in the database!
        xLogger.warn(
            "updateUserAccount: FAILED!! Cannot update account {0}. Custom ID {1} already exists",
            account.getUserId(), account.getCustomId());
        throw new ServiceException(
            backendMessages.getString("error.cannotupdate") + " '" + account.getUserId() + "'. "
                + messages.getString("customid") + " " + account.getCustomId() + " "
                + backendMessages.getString("error.alreadyexists") + ".");
      }
      user.setCustomId(account.getCustomId());

      user.setTgs(tagDao.getTagsByNames(account.getTags(), ITag.USER_TAG));
      //add user location ids
      Map<String, Object> reqMap = new HashMap<>();
      reqMap.put("userId", account.getUserId());
      reqMap.put("userName", account.getRegisteredBy());
      Map<String, Object>
          lidMap =
          LocationServiceUtil.getInstance().getLocationIds(account, reqMap);
      if (lidMap.get("status") == "success") {
        updateUserLocationIds(account, lidMap, pm);
      }
      // Generate event, if configured
      try {
        EventPublisher.generate(account.getDomainId(), IEvent.MODIFIED, null,
            JDOUtils.getImplClass(IUserAccount.class).getName(), account.getKeyString(), null);
      } catch (EventGenerationException e) {
        xLogger.warn(
            "Exception when generating event for user-updation for user {0} in domain {1}: {2}",
            account.getUserId(), account.getDomainId(), e.getMessage());
        exception = e;
      }
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("updateAccount: FAILED!! User {0} does not exist in the database",
          account.getUserId());
      userExists = false;
      exception = e;
    } catch (Exception e) {
      errMsg = e.getMessage();
      exception = e;
    } finally {
      pm.close();
    }
    if (!userExists) {
      errMsg =
          messages.getString("user") + " '" + account.getUserId() + "' " + backendMessages
              .getString("error.notfound");
    }
    if (exception != null) {
      throw new ServiceException(exception);
    }
    xLogger.fine("Exiting updateAccount");
  }

  @Override
  public List<Long> moveAccessibleDomains(String userId, Long srcDomainId, Long destDomainId)
      throws ServiceException {
    return addAccessibleDomains(userId, Collections.singletonList(destDomainId),srcDomainId);
  }

  private List<Long> addAccessibleDomains(String userId, List<Long> accDomainIds, Long removeDomainId)
      throws ServiceException {
    PersistenceManager pm = null;
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      IUserAccount ua = getUserAccount(userId);
      List<Long> uAccDids = ua.getAccessibleDomainIds();
      if (uAccDids == null || uAccDids.isEmpty()) {
        xLogger.warn("Error while adding accessible domains for user {0}, uAccDids is null ", userId);
        throw new InvalidServiceException(
            backendMessages.getString("user.addaccessibledomain.error1") + " \'" + userId + "\'");
      }

      for (Long accDid : accDomainIds) {
        if (!uAccDids.contains(accDid)) {
          uAccDids.add(accDid);
        }
      }

      if(removeDomainId != null) {
        uAccDids.remove(removeDomainId);
      }

      Set<Long> allChildrenIds = new HashSet<>();
      for (Long uAccDid : uAccDids) {
        List<IDomainLink> chldLnks = ds.getDomainLinks(uAccDid, IDomainLink.TYPE_CHILD, -1);
        if (chldLnks != null && !chldLnks.isEmpty()) {
          for (IDomainLink chldLnk : chldLnks) {
            allChildrenIds.add(chldLnk.getLinkedDomainId());
          }
        }
      }
      uAccDids.removeAll(allChildrenIds);
      ua.setAccessibleDomainIds(uAccDids);
      pm = PMF.get().getPersistenceManager();
      pm.makePersistent(ua);
      return uAccDids;
    }catch (Exception e) {
      throw new ServiceException(e);
    } finally {
      if(pm != null) {
        pm.close();
      }
    }
  }

  public List<Long> addAccessibleDomains(String userId, List<Long> accDomainIds)
      throws ServiceException {
    return addAccessibleDomains(userId, accDomainIds, null);
  }

  /**
   * Delete user accounts, given list of fully qualified user Ids (i.e. domainId.userId)
   */
  @SuppressWarnings("unchecked")
  public void deleteAccounts(Long domainId, List<String> accountIds, String sUser)
      throws ServiceException {
    xLogger.fine("Entering deleteAccounts");
    IUserAccount account;
    boolean deleteError = false;
    String errMsg = null;
    Exception exception = null;
    String returnString = "";
    List<String> errorIds = new ArrayList<String>();
    String userName;
    sUser = (sUser != null ? sUser : " ");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      int numAccounts = 0;
      // Generate event, if configured
      if (!AppFactory.get().getAuthorizationService().authoriseUpdateKiosk(sUser, domainId)) {
        throw new UnauthorizedException(backendMessages.getString("permission.denied"));
      }
      for (String accountId : accountIds) {
        try {
          xLogger.info("deleteAccounts: Deleting user {0}", accountId);
          account = JDOUtils.getObjectById(IUserAccount.class, accountId, pm);
          userName = account.getFullName();//for audit log
          //If a user being deleted is a kiosk owner, we don't allow that
          Query query = pm.newQuery(JDOUtils.getImplClass(IUserToKiosk.class));
          query.setFilter("userId == userIdParam");
          query.declareParameters("String userIdParam");
          AuthenticationService aus = AppFactory.get().getAuthenticationService();
          try {
            List<IUserToKiosk> results = (List<IUserToKiosk>) query.execute(accountId);
            if (!results.isEmpty()) {
              //Log error
              returnString += accountId + " is associated with an entity;";
              errorIds.add(accountId);
              deleteError = true;
              xLogger.warn("deleteAccounts: Failed to delete user {0}. User is a kiosk owner.",
                  accountId);
            } else {
              // Generate delete event
              try {
                EventPublisher
                    .generate(domainId, IEvent.DELETED, null, UserAccount.class.getName(),
                        account.getKeyString(),
                        null, account);
              } catch (EventGenerationException e) {
                xLogger.warn(
                    "Exception when generating event for user-deletion for user {0} in domain {1}: {2}",
                    accountId, domainId, e.getMessage());
              }
              xLogger.fine("deleteAccounts: deleting user {0} from the database", accountId);
              pm.deletePersistent(account);
              aus.clearUserTokens(accountId);
              xLogger.info("AUDITLOG\t{0}\t{1}\tUSER\t " +
                  "DELETE\t{2}\t{3}", domainId, sUser, accountId, userName);
              --numAccounts;
            }
          } finally {
            query.closeAll();
          }
        } catch (JDOObjectNotFoundException e) {
          returnString += accountId + ";";
          errorIds.add(accountId);
          xLogger
              .warn("deleteAccounts: FAILED to delete user {0}!! User does not exist", accountId);
          exception = e;
        }
      } // end for
    } catch (Exception e) {
      errMsg = e.getMessage();
      exception = e;
      xLogger.warn(errMsg, exception);
    } finally {
      xLogger.fine("Exiting deleteAccounts");
      pm.close();
    }
    if (exception != null) {
      throw new ServiceException(exception);
    }
  }

  /**
   * Authenticate a user in the context of a given domain; Returns user object if authenticated, otherwise null
   */
  public IUserAccount authenticateUser(String userId, String password, Integer lgSrc)
      throws ServiceException, ObjectNotFoundException {
    xLogger.fine("Entering authenticateUser");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String errMsg = null;
    Exception exception = null;
    boolean isAuthenticated = false;
    boolean userNotFound = false;
    IUserAccount user = null;
    //We use an atomic transaction here to check if the user exists and then update it
    try {
      //First check if the user already exists in the database
      user = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
      //If we get here, it means the user exists
      //If the user account is disabled, then always fail authentication, otherwise continue
      if (user.isEnabled()) {
        String encodedPassword = PasswordEncoder.MD5(password);
        if (encodedPassword.equals(user.getEncodedPassword())) {
          // User is authenticated
          user.setLastLogin(new Date());
          if (lgSrc != null) {
            user.setLoginSource(lgSrc);
          }
          xLogger.info("User " + userId + " authenticated successfully");
          isAuthenticated = true;
        }
      } else {
        xLogger.warn("Authentication failed! User {0} is disabled", userId);
//				errMsg = backendMessages.getString( "error.invalidusername" ); // "User is disabled";
      }
      user = pm.detachCopy(user);
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("Authentication failed! User {0} does not exist", userId);
      errMsg =
          backendMessages
              .getString("error.invalidusername"); // "User " + userId + " does not exist";
      exception = e;
      userNotFound = true;
    } catch (Exception e) {
      errMsg = e.getMessage();
      xLogger.warn("Exception while authentication user", e);
      exception = e;
    } finally {
      pm.close();
    }
    if (userNotFound) {
      throw new ObjectNotFoundException("USR001", userId);
    } else if (exception != null) {
      throw new ServiceException(exception);
    }
    xLogger.fine("Exiting authenticateUser: {0}", isAuthenticated);

    if (isAuthenticated) {
      return user;
    } else {
      return null;
    }
  }

  /**
   * Change the password of a given user.
   *
   * @param userId      The user whose password is to be changed.
   * @param oldPassword The old password of this user
   * @param newPassword The new password to which the change is tareted
   * @throws ServiceException If there is an error in this process
   */
  public void changePassword(String userId, String oldPassword, String newPassword)
      throws ServiceException {
    xLogger.fine("Entering changePassword");
    if (newPassword == null || newPassword.isEmpty()) {
      throw new ServiceException("New password not specified");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String errMsg = null;
    Exception exception = null;
    //We use an atomic transaction here to check if the user exists and then update it
    try {
      //First check if the user already exists in the database
      IUserAccount user = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
      //If we get here, it means the user exists
      if (oldPassword != null) { // i.e. change password
        String encodedPassword = PasswordEncoder.MD5(oldPassword);
        if (encodedPassword.equals(user.getEncodedPassword())) {
          user.setEncodedPassword(PasswordEncoder.MD5(newPassword));
        } else {
          xLogger
              .warn("changePassword: WARNING!! Failed to authenticate user {0} with old password",
                  userId);
          errMsg = backendMessages.getString("error.invalidoldpassword");
        }
      } else { // simply reset the password (typically only a superuser will be given access to this)
        user.setEncodedPassword(PasswordEncoder.MD5(newPassword));
      }
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("changePassword: FAILED!! User {0} does not exist", userId);
      errMsg = messages.getString("user") + " " + backendMessages.getString("error.notfound");
      exception = e;
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      errMsg = e.getMessage();
      xLogger.warn("Exception in changePassword", e);
      exception = e;
    } finally {
      pm.close();
    }
    if (exception != null) {
      throw new ServiceException(exception);
    }

    xLogger.fine("Exiting changePassword");
  }

  /**
   * Disable the account of a given user - pass in a fully qualified user Id (i.e. domainId.userId)
   */
  public void disableAccount(String userId) throws ServiceException {
    xLogger.fine("Entering disableAccount");
    String errMsg = null;
    Exception exception = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    //We use an atomic transaction here to check if the user exists and then update it
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();
      try {
        //First check if the user already exists in the database
        IUserAccount user = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
        xLogger.info("disableAccount: Disabling user account {0}", userId);
        user.setEnabled(false);
      } catch (JDOObjectNotFoundException e) {
        xLogger.warn("disableAccount: FAILED!! user {0} does not exist", userId);
        exception = e;
      } catch (Exception e) {
        errMsg = e.getMessage();
        xLogger.warn("Exception in disableAccount()", e);
        exception = e;
      }
      tx.commit();
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      xLogger.fine("Exiting disableAccount");
      pm.close();
    }
    if (exception != null) {
      throw new ServiceException(exception);
    }
  }

  /**
   * Enable a user account (pass in fully qualified user Id - i.e. domainId.userId)
   */
  public void enableAccount(String userId) throws ServiceException {
    xLogger.fine("Entering enableAccount");
    String errMsg = null;
    Exception exception = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    //We use an atomic transaction here to check if the user exists and then update it
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();
      try {
        //First check if the user already exists in the database
        IUserAccount user = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
        xLogger.info("enableAccount: Enabling user account {0}", userId);
        user.setEnabled(true);
      } catch (JDOObjectNotFoundException e) {
        xLogger.warn("enableAccount: FAILED!! user {0} does not exist", userId);
        exception = e;
      } catch (Exception e) {
        errMsg = e.getMessage();
        xLogger.warn("Exception in enableAccount()", e);
        exception = e;
      }
      tx.commit();
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      xLogger.fine("Exiting enableAccount");
      pm.close();
    }
    if (exception != null) {
      throw new ServiceException(exception);
    }
  }

  /**
   * Find user accounts that meet the given criteria. paramName can be one of the following:
   * JsonTags.ID (either regular or fully qualified user Ids)
   * JsonTags.MOBILE_PH_NO
   * JsonTags.COUNTRY
   * JsonTags.STATE
   * JsonTags.DISTRICT
   * JsonTags.TALUK
   * JsonTags.CITY
   */
  @SuppressWarnings("unchecked")
  private Results findAccountsByDomain(Long domainId, String paramName, String paramValue,
                                       String nameStartsWith, PageParams pageParams,
                                       boolean includeChildDomainUsers)
      throws ServiceException {
    xLogger.fine("Entering findAccountsByDomain");
    if (domainId == null) {
      throw new ServiceException("Invalid domain ID");
    }

    Results results = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(JDOUtils.getImplClass(IUserAccount.class));
    String paramNameAlias = paramName + "Param";
    String filter = CharacterConstants.EMPTY;
    if (includeChildDomainUsers) {
      filter += "dId.contains(domainIdParam)";
    } else {
      filter += "sdId == domainIdParam";
    }
    filter += " && " + paramName + " == " + paramNameAlias;
    String declaration = "Long domainIdParam, String " + paramNameAlias;
    Map<String, Object> params = new HashMap<String, Object>(1);
    params.put("domainIdParam", domainId);
    params.put(paramNameAlias, paramValue);
    boolean hasNameStartsWith = nameStartsWith != null && !nameStartsWith.trim().isEmpty();
    String lNameStartsWith = null;
    params.put(paramNameAlias, paramValue);
    if (hasNameStartsWith) {
      filter += " && nName >= nNameParam1 && nName < nNameParam2";
      declaration += ", String nNameParam1, String nNameParam2";
      lNameStartsWith = nameStartsWith.trim().toLowerCase();
      params.put("nNameParam1", lNameStartsWith);
      params.put("nNameParam2", (lNameStartsWith + Constants.UNICODE_REPLACEANY));
    }
    q.setFilter(filter);
    q.declareParameters(declaration);
    q.setOrdering("nName asc");
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      List<IUserAccount> users = null;
      int numFound;
      users = (List<IUserAccount>) q.executeWithMap(params);
      numFound = Counter.getCountByMap(q, params);
      String cursor = null;
      if (users != null) {
        users.size();
        cursor = QueryUtil.getCursor(users);
        users = (List<IUserAccount>) pm.detachCopyAll(users);
      }
      // Get the results along with cursor, if present
      results = new Results(users, cursor, numFound, 0);
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();

    }
    xLogger.fine("Exiting findAccountsByDomain");
    return results;
  }

  /**
   * Find all user accounts with pagination, in a given domain
   * TODO Implement pagination
   */
  @SuppressWarnings("unchecked")
  private Results findAllAccountsByDomain(Long domainId, String nameStartsWith,
                                          PageParams pageParams, boolean includeChildDomainUsers, IUserAccount user)
      throws ServiceException {
    xLogger.fine("Entering findAllAccountsByDomain");

    if (domainId == null) {
      throw new ServiceException("Invalid domain Id");
    }

    boolean isSuperUsers = user.getRole().equals("ROLE_su");

    PersistenceManager pm = PMF.get().getPersistenceManager();
    Results results = null;
    String errMsg = null;
    Exception exception = null;
    // Formulate the query
    Query query = pm.newQuery(JDOUtils.getImplClass(IUserAccount.class));
    String declaration = "Long domainIdParam";
    String filter = CharacterConstants.EMPTY;
    if (includeChildDomainUsers) {
      filter += "dId.contains(domainIdParam)";
    } else {
      filter += "sdId == domainIdParam";
    }

    boolean hasNameStartsWith = nameStartsWith != null && !nameStartsWith.trim().isEmpty();
    String lNameStartsWith = "";
    if (hasNameStartsWith) {
      filter += "&& nName >= nNameParam1 && nName < nNameParam2";
      declaration += ", String nNameParam1, String nNameParam2";
      lNameStartsWith = nameStartsWith.trim().toLowerCase();
    }
    if(!isSuperUsers) {
      filter = filter.concat(QueryConstants.AND).concat("role != 'ROLE_su'");
    }
    query.setFilter(filter);
    query.declareParameters(declaration);
    query.setOrdering("nName asc");
    if (pageParams != null) {
      QueryUtil.setPageParams(query, pageParams);
    }
    try {
      List<IUserAccount> users = null;
      if (hasNameStartsWith) {
        users =
            (List<IUserAccount>) query.execute(domainId, lNameStartsWith,
                (lNameStartsWith + Constants.UNICODE_REPLACEANY));
      } else {
        users = (List<IUserAccount>) query.execute(domainId);
      }
      String cursor = null;
      if (users != null) {
        users
            .size(); // TODO - temp. fix for retrieving all obejcts and avoid "object manager closed" exception
        cursor = QueryUtil.getCursor(users);
        users = (List<IUserAccount>) pm.detachCopyAll(users); //detach data
      }
      // Form the results along with cursor of the last result entry (for future queries, if present)
      results = new Results(users, cursor);
    } catch (Exception e) {
      errMsg = e.getMessage();
      exception = e;
      xLogger.warn("exception in findAllAccounts()", e);
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    if (exception != null) {
      throw new ServiceException(exception);
    }

    xLogger.fine("Exiting findAllAccountsByDomain");
    return results;
  }

  public void updateMobileLoginFields(IUserAccount account) {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      IUserAccount u = JDOUtils.getObjectById(IUserAccount.class, account.getUserId(), pm);
      u.setLastMobileAccessed(account.getLastMobileAccessed());
      u.setIPAddress(account.getIPAddress());
      u.setUserAgent(account.getUserAgent());
      u.setPreviousUserAgent(account.getPreviousUserAgent());
      u.setAppVersion(account.getAppVersion());
      Map<String, Object> params = new HashMap<>(1);
      params.put("ipaddress", account.getIPAddress());
      EventPublisher.generate(u.getDomainId(),IEvent.IP_ADDRESS_MATCHED, params,
          UserAccount.class.getName(), u.getKeyString(),
          null);
    } catch (Exception e) {
      xLogger.warn("Exception while updating mobile login related fields for user {0}",
          account.getUserId(), e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  @Override
  public boolean hasAccessToDomain(String username, Long domainId)
      throws ServiceException, ObjectNotFoundException {
    IUserAccount userAccount = getUserAccount(username);
    List<Long> accDomains = userAccount.getAccessibleDomainIds();
    if (SecurityUtil
        .compareRoles(SecurityConstants.ROLE_SUPERUSER, userAccount.getRole()) == 0 ||
        userAccount.getDomainId().equals(domainId) || accDomains != null && accDomains
        .contains(domainId)) {
      return true;
    }
    DomainsService domainsService = Services.getService(DomainsServiceImpl.class);
    if (checkAccess(domainsService, domainId)) {
      return true;
    }
    if (accDomains != null) {
      for (Long accDomainId : accDomains) {
        if (checkAccess(domainsService, accDomainId)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean checkAccess(DomainsService domainsService, Long domainId)
      throws ServiceException {
    List<IDomainLink>
        domainLinks =
        domainsService.getAllDomainLinks(domainId, IDomainLink.TYPE_CHILD);
    if (domainLinks != null) {
      for (IDomainLink domainLink : domainLinks) {
        if (domainId.equals(domainLink.getLinkedDomainId())) {
          return true;
        }
      }
    }
    return false;
  }


  public void updateUserLoginHistory(String userId, Integer lgSrc, String usrAgnt, String ipAddr,
                                     Date loginTime, String version) {
    try {
      xLogger.fine("Updating user login history");
      if (StringUtils.isNotBlank(userId)) {
        UserLoginHistoryModel
            ulh =
            new UserLoginHistoryModel(userId, lgSrc, usrAgnt, ipAddr, loginTime, version);
        AppFactory.get().getTaskService()
            .schedule(ITaskService.QUEUE_DEFAULT, LOGUSER_TASK_URL,
                GsonUtils.toJson(ulh));
      }
    } catch (TaskSchedulingException e) {
      xLogger.warn(" {0} while updating the user login history, {1}", e.getMessage(), userId, e);
    } catch (Exception e) {
      xLogger.warn(" {0} while updating the user login history, {1}", e.getMessage(), userId, e);
    }
  }

  public Set<String> getElementSetByUserFilter(Long domainId, IUserAccount user, String paramName,
                                               String paramValue, PageParams pageParams)
      throws ServiceException {
    xLogger.fine("Entering getElementSetByUserFilter");
    if (domainId == null) {
      throw new ServiceException("Invalid domain ID");
    }

    boolean isSuperUsers = user.getRole().equals("ROLE_su");

    paramName = paramName.trim();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query
        q =
        pm.newQuery(" SELECT " + paramName + " FROM " + JDOUtils.getImplClass(IUserAccount.class)
            .getName());
    Set<String> elementSet = new HashSet<String>();
    StringBuilder filter = new StringBuilder();
    StringBuilder declaration = new StringBuilder();
    String order = "nName asc";
    filter.append("dId.contains(dIdParam) ");
    declaration.append("Long dIdParam");
    Map<String, Object> params;
    params = new HashMap<>();
    params.put("dIdParam", domainId);
    String paramNameAlias = paramName + "Param";
    paramValue = paramValue.trim();
    String lNameStartsWith = (paramName.equals("v") ? paramValue : paramValue.toLowerCase());
    filter.append(QueryConstants.AND)
        .append(paramName).append(QueryConstants.GR_EQUAL).append(paramNameAlias).append("1")
        .append(QueryConstants.AND).append(paramName).append(QueryConstants.LESS_THAN)
        .append(paramNameAlias).append("2");
    declaration.append(CharacterConstants.COMMA).append("String ").append(paramNameAlias)
        .append("1")
        .append(CharacterConstants.COMMA).append("String ").append(paramNameAlias).append("2");
    params.put(paramNameAlias + "1", lNameStartsWith);
    params.put(paramNameAlias + "2", lNameStartsWith + Constants.UNICODE_REPLACEANY);
    if (!paramName.equals("nName")) {
      order = paramName + " asc, " + order;
    }
    if(!isSuperUsers) {
      filter.append(QueryConstants.AND).append("role != 'ROLE_su'");
    }
    q.setFilter(filter.toString());
    q.declareParameters(declaration.toString());
    q.setOrdering(order);
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      List elements = (List) q.executeWithMap(params);
      if (elements != null) {
        //elements = (List)pm.detachCopyAll(elements);
        for (Object o : elements) {
          elementSet.add((String) o);
        }
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while trying to close query", ignored);
      }
      pm.close();
    }
    xLogger.fine("Exiting getElementSetByUserFilter");
    return elementSet;
  }

  /**
   * update the lastmobileAccess time
   *
   * @param userId user id
   * @param aTime  accessed time
   */
  @Override
  public boolean updateLastMobileAccessTime(String userId, long aTime) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    IUserAccount userAccount;
    Date now = new Date(aTime);
    try {
      userAccount = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
      if (userAccount.getLastMobileAccessed() == null
          || userAccount.getLastMobileAccessed().compareTo(now) < 0) {
        userAccount.setLastMobileAccessed(now);
      }
    } catch (Exception e) {
      xLogger
          .warn("{0} while updating last transacted time for the user, {1}", e.getMessage(), userId,
              e);
      return false;
    } finally {
      pm.close();
    }
    return true;
  }

  /**
   * Get the list of enabled userIds from the given userIds
   *
   * @Param uIds
   */
  public List<String> getEnabledUserIds(List<String> uIds) {
    if (uIds != null && uIds.size() > 0) {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      try {
        List<String> eUids = new ArrayList<>(1);
        for (String uid : uIds) {
          try {
            IUserAccount u = JDOUtils.getObjectById(IUserAccount.class, uid, pm);
            if (u.isEnabled()) {
              eUids.add(uid);
            }
          } catch (Exception e) {
            xLogger.warn("Error while getting enabled user {0}", uid, e);
          }
        }
        return eUids;
      } finally {
        pm.close();
      }
    }
    return null;
  }

  /**
   * Get the list of enabled userIds from the given tagNames
   */
  public List<String> getEnabledUserIdsWithTags(List<String> tagNames, Long domainId) {
    List<String> uIds = null;
    if (tagNames != null && tagNames.size() > 0) {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      String tagName = MessageUtil.getCSVWithEnclose(tagNames);
      String query = "SELECT UA.USERID FROM USERACCOUNT UA,USER_TAGS UT WHERE "
          + "UT.ID IN (SELECT ID FROM TAG WHERE NAME IN (" + tagName + ") AND TYPE=4)"
          + " AND UT.USERID = UA.USERID AND UA.ISENABLED = 1 AND UA.SDID = ?";
      Query q = pm.newQuery("javax.jdo.query.SQL", query);
      try {
        List l = (List) q.executeWithArray(domainId);
        uIds = new ArrayList<>(l.size());
        for (Object o : l) {
          uIds.add((String) o);
        }
      } catch (Exception e) {
        xLogger.warn("Error while getting enabled user by tags {0}", tagName, e);
      } finally {
        try {
          q.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
        pm.close();
      }
    }
    return uIds;
  }

  private Results findAccountsByFilter(Long domainId, Map<String, Object> filters,
                                       PageParams pageParams, boolean excluderSuUser)
      throws ServiceException {
    xLogger.fine("Entering findAccountsByFilter");
    if (domainId == null) {
      throw new ServiceException("Invalid domain ID");
    }
    Results results = null;
    QueryParams qp = null;
    if (excluderSuUser) {
      qp = userDao.getQueryParams(domainId, filters, true, true);
    } else {
      qp = userDao.getQueryParams(domainId, filters, true);
    }
    if (qp == null) {
      throw new ServiceException("Error while creating query parameters");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(qp.query);
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      List<IUserAccount> users;
      users = (List<IUserAccount>) q.executeWithMap(qp.params);
      String cursor = null;
      if (users != null) {
        users.size();
        cursor = QueryUtil.getCursor(users);
        users = (List<IUserAccount>) pm.detachCopyAll(users);
      }
      // Get the results along with cursor, if present
      results = new Results(users, cursor, Counter.getCountByMap(q, qp.params), 0);
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while trying to close query", ignored);
      }
      pm.close();
    }
    xLogger.fine("Exiting findAccountsByFilter");
    return results;
  }

  /**
   * Check if a custom ID is available in a domain or it's child domains
   */
  public boolean customIdExists(Long domainId, String customId, String userId)
      throws ServiceException {
    if (domainId == null || customId == null || customId.isEmpty() || userId == null || userId
        .isEmpty()) {
      throw new ServiceException("Invalid or null Domain ID: {0}, custom ID : {1}, user ID: {2}",
          domainId, customId, userId);
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(JDOUtils.getImplClass(IUserAccount.class));
    query.setFilter("dId.contains(domainIdParam) && cId == cidParam");
    query.declareParameters("Long domainIdParam, String cidParam");
    try {
      List<IUserAccount> results = (List<IUserAccount>) query.execute(domainId, customId);
      if (results != null && results.size() == 1) {
        if (userId != null) {
          IUserAccount res = results.get(0);
          if (userId.equals(res.getUserId())) {
            return false;
          }
        }
        return true;
      }
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return false;
  }

  /**
   * This method will update applicable location ids for an user
   */
  public void updateUserLocationIds(IUserAccount user, Map<String, Object> lidMap,
                                    PersistenceManager pm) {
    IUserAccount k = JDOUtils.getObjectById(IUserAccount.class, user.getUserId(), pm);
    k.setCountryId((String) lidMap.get("countryId"));
    k.setStateId((String) lidMap.get("stateId"));
    k.setDistrictId((String) lidMap.get("districtId"));
    k.setTalukId((String) lidMap.get("talukId"));
    k.setCityId((String) lidMap.get("placeId"));
    pm.makePersistent(k);
  }

  /**
   * Set the UI preference for a user. If true, then it means his preference is New UI. Otherwise, preference is Old UI.
   */
  public void setUiPreferenceForUser(String userId, boolean uiPref) throws ServiceException {
    // Get the user account for userId
    // Set the uiPref
    xLogger.fine("Entering setUiPreferenceForUser");
    if (userId == null) {
      throw new ServiceException("Invalid user Id");
    }

    boolean userExists = true;
    String errMsg = null;
    Exception exception = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      try {
        //First check if the user exists in the database.
        IUserAccount user = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
        // Only if the user exists, set the Ui preference.
        user.setUiPref(uiPref);
      } catch (JDOObjectNotFoundException e) {
        xLogger.warn("setUiPreferenceForUser: FAILED!! User {0} does not exist in the database",
            userId);
        userExists = false;
        exception = e;
      }
    } catch (Exception e) {
      exception = e;
      errMsg = e.getMessage();
    } finally {
      pm.close();
    }
    if (!userExists) {
      errMsg =
          messages.getString("user") + " '" + userId + "' " + backendMessages
              .getString("error.notfound");
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg, exception);
    }
  }

  @Override
  public void addEditUserDevice(UserDeviceModel ud) throws ServiceException {
    UserDeviceBuilder builder = new UserDeviceBuilder();
    IUserDevice userDevice = getUserDevice(ud.userid, ud.appname);
    userDevice = builder.buildUserDevice(userDevice, ud);
    xLogger.fine("Entering createUserDevice");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(userDevice);
    } catch (Exception e) {
      xLogger.warn("Issue with add edit user device {}", e.getMessage());
      throw new ServiceException(e.getMessage(), e);
    } finally {
      pm.close();
    }
  }

  @Override
  public IUserDevice getUserDevice(String userid, String appname) throws ServiceException {

    IUserDevice userDevice = null;
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      Query query = pm.newQuery(JDOUtils.getImplClass(IUserDevice.class));
      query.setFilter("userId == userIdParam && appname == appnameParam");
      query.declareParameters("String userIdParam, String appnameParam");
      //Query query = pm.newQuery("javax.jdo.query.SQL",q);
      query.setUnique(true);
      userDevice = (IUserDevice) query.execute(userid, appname);
      userDevice = pm.detachCopy(userDevice);
      return userDevice;
    } catch (Exception e) {
      xLogger.severe("{0} while getting user device {1}", e.getMessage(), userid, e);
      throw new ServiceException("Issue with getting user device for user :" + userid);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  private boolean checkIfCustomIdExists(IUserAccount userAccount) {
    xLogger.fine("Entering checkIfCustomIdExists");
    boolean customIdExists = false;
    xLogger.fine("object is an instance of UserAccount");
    Long domainId = userAccount.getDomainId();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Check if another user by the same custom ID exists in the database
    Query query = pm.newQuery(JDOUtils.getImplClass(IUserAccount.class));
    query.setFilter("dId.contains(domainIdParam) && cId == cidParam");
    query.declareParameters("Long domainIdParam, String cidParam");
    try {
      @SuppressWarnings("unchecked")
      List<IUserAccount>
          results =
          (List<IUserAccount>) query.execute(domainId, userAccount.getCustomId());
      if (results != null && results.size() == 1) {
        // UserAccount with this custom id already exists in the database!
        xLogger.warn(
            "Error while adding or updating user Account {0}: Custom ID {1} already exists.",
            userAccount.getUserId(), userAccount.getCustomId());
        customIdExists = true;
      }
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return customIdExists;
  }

  // Sort user data
  private static void sortUsers(List<IUserAccount> users) {
    Collections.sort(users, new Comparator<IUserAccount>() {
      @Override
      public int compare(IUserAccount o1, IUserAccount o2) {
        return o1.getFirstName().compareTo(o2.getFirstName());
      }
    });
  }

  @Override
  public boolean hasAccessToUser(String userId, String rUserId, Long domainId, String role) {
    Map<String, Object> params = new HashMap<>(2);
    params.put("domainIdParam", domainId);
    params.put("userIdParam", userId);
    String queryStr;
    if (userId != null && userId.equals(rUserId)) {
      return true;
    } else if (SecurityUtil.compareRoles(role, SecurityConstants.ROLE_DOMAINOWNER) >= 0) {
      //Todo: Need to check user domainId and all its children domains
      queryStr =
          "SELECT userId FROM " + JDOUtils.getImplClass(IUserAccount.class).getName()
              + " WHERE userId == userIdParam && dId.contains(domainIdParam)" +
              " PARAMETERS String userIdParam, Long domainIdParam";
    } else {
      queryStr =
          "SELECT userId FROM " + JDOUtils.getImplClass(IUserAccount.class).getName()
              + " WHERE userId == userIdParam && registeredBy == registeredByParam && sdId == domainIdParam"
              +
              " PARAMETERS String userIdParam, String registeredByParam, Long domainIdParam";
      params.put("registeredByParam", rUserId);
    }
    return getAccess(params, queryStr);
  }

  private boolean getAccess(Map<String, Object> params, String queryStr) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    boolean hasAccess = false;
    List<Long> results;
    Query q = null;
    try {
      q = pm.newQuery(queryStr);
      results = (List<Long>) q.executeWithMap(params);
      if (results != null && results.size() == 1) {
        hasAccess = true;
      }
    } finally {
      if (q != null) {
        try {
          q.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return hasAccess;
  }

  /**
   * Method to fetch the user account details for the given userIds
   *
   * @param userIds User Id list
   * @return List<IUserAccount>
   */
  public List<IUserAccount> getUsersByIds(List<String> userIds) {

    if (userIds == null || userIds.isEmpty()) {
      return null;
    }
    List<IUserAccount> results = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    try {

      StringBuilder queryBuilder = new StringBuilder("SELECT * FROM `USERACCOUNT` ");
      queryBuilder.append("WHERE USERID IN (");
      for (String userId : userIds) {
        queryBuilder.append("'").append(userId).append("'").append(CharacterConstants.COMMA);
      }
      queryBuilder.setLength(queryBuilder.length() - 1);
      queryBuilder.append(" )");
      query = pm.newQuery("javax.jdo.query.SQL", queryBuilder.toString());
      query.setClass(JDOUtils.getImplClass(IUserAccount.class));
      results = (List<IUserAccount>) query.execute();
      results = (List<IUserAccount>) pm.detachCopyAll(results);
    } catch (Exception e) {
      xLogger.warn("Exception while fetching approval status", e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return results;
  }
}
