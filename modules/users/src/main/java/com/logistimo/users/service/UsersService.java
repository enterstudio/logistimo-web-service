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

package com.logistimo.users.service;

import com.logistimo.api.models.UserDeviceModel;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.IUserDevice;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by charan on 04/03/17.
 */
public interface UsersService extends Service {

  /**
   * Check to see if a user id exists in the system or not.
   *
   * @param userId The id of the user to be checked
   * @return true, if the user exists (even if inactive); false, if this id does not exist
   */
  boolean userExists(String userId) throws ServiceException;

  /**
   * Add a new user account
   */
  String addAccount(Long domainId, IUserAccount account) throws ServiceException;

  /**
   * Get a user account, given the domain and user ID (also gets kiosks associated with this user); using 'deep=false' in alternate method will only retrieve the user object
   */
  IUserAccount getUserAccount(String userId) throws ObjectNotFoundException;

  /**
   * Get all users visible to a given role only
   */
  Results getUsers(Long domainId, IUserAccount user, boolean activeUsersOnly, String nameStartsWith,
                   PageParams pageParams) throws ServiceException;

  /**
   * Get all active/inactive users of a given role
   */
  Results getUsers(Long domainId, String role, boolean activeUsersOnly, String nameStartsWith,
                   PageParams pageParams) throws ServiceException;

  /**
   * Get all active/inactive users visible to a user (along with all super users(across domains) if the user is a super user)
   */
  Results getUsers(Long domainId, IUserAccount user, boolean activeUsersOnly,
                   boolean includeSuperusers, String nameStartsWith, PageParams pageParams,
                   boolean includeChildDomainUsers)
      throws ServiceException;

  Results getUsers(Long domainId, IUserAccount user, boolean activeUsersOnly, String nameStartsWith,
                   PageParams pageParams, boolean includeChildDomainUsers) throws ServiceException;

  /**
   * Get all the super users (across domains)
   */
  List<IUserAccount> getSuperusers() throws ServiceException;

  Results getUsersByFilter(Long domainId, IUserAccount user, Map<String, Object> filters,
                           PageParams pageParams) throws ServiceException;

  Set<String> getElementSetByUserFilter(Long domainId, IUserAccount user, String paramName,
                                        String paramValue, PageParams pageParams)
      throws ServiceException;

  /**
   * Update a given user account
   *
   * @param account   - user account with updated values
   * @param updatedBy - Id of user who is updating the user account
   */
  void updateAccount(IUserAccount account, String updatedBy) throws ServiceException;

  List<Long> moveAccessibleDomains(String userId, Long srcDomainId, Long destDomainId)
      throws ServiceException;

  List<Long> addAccessibleDomains(String userId, List<Long> accDomainIds) throws ServiceException;

  /**
   * Delete user accounts, given list of fully qualified user Ids (i.e. domainId.userId)
   */
  void deleteAccounts(Long domainId, List<String> userIds, String suser) throws ServiceException;

  /**
   * Authenticate a user in the context of a given domain and password
   */
  IUserAccount authenticateUser(String userId, String password, Integer lgSrc)
      throws ServiceException, ObjectNotFoundException;

  /**
   * Change the password of a given user
   */
  void changePassword(String userId, String oldPassword, String newPassword)
      throws ServiceException;

  /**
   * Enable a previously disabled user account (pass in fully qualified user Id - i.e. domainId.userId)
   */
  void enableAccount(String userId) throws ServiceException;

  /**
   * Disable a user account (pass in fully qualified user Id - i.e. domainId.userId)
   */
  void disableAccount(String userId) throws ServiceException;

  boolean hasAccessToDomain(String username, Long domainId)
      throws ServiceException, ObjectNotFoundException;

  void updateUserLoginHistory(String userId, Integer lgSrc, String usrAgnt, String ipAddr,
                              Date loginTime, String version);

  /**
   * Update the mobile login related fields for the specified user account.
   */
  void updateMobileLoginFields(IUserAccount account);

  /**
   * updateKiosk
   * Updates user last mobile access time
   */
  boolean updateLastMobileAccessTime(String userId, long aTime);

  List<String> getEnabledUserIds(List<String> userIds);

  List<String> getEnabledUserIdsWithTags(List<String> tagIds, Long domainId);

  /**
   * Check if a custom ID is available in a domain or it's child domains
   */
  boolean customIdExists(Long domainId, String customId, String userId) throws ServiceException;

  /**
   * Set the UI preference for a user
   */
  void setUiPreferenceForUser(String userId, boolean uiPref) throws ServiceException;

  boolean hasAccessToUser(String userId, String rUserId, Long domainId, String role);

  void addEditUserDevice(UserDeviceModel ud) throws ServiceException;

  IUserDevice getUserDevice(String userid, String appname) throws ServiceException;

  List<IUserAccount> getUsersByIds(List<String> userIds);

}
