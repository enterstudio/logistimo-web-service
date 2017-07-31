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

package com.logistimo.entities.service;


import com.logistimo.assets.entity.IAsset;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.entities.entity.IApprovers;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.entity.IPoolGroup;
import com.logistimo.entities.models.EntityLinkModel;
import com.logistimo.entities.models.LocationSuggestionModel;
import com.logistimo.entities.models.UserEntitiesModel;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.users.entity.IUserAccount;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;

public interface EntitiesService extends Service {



  /**
   * Get users associated with a given kiosk
   */
  Results getUsersForKiosk(Long kioskId, PageParams pageParams) throws ServiceException;

  Results getUsersForKiosk(Long kioskId, PageParams pageParams, PersistenceManager pm)
      throws ServiceException;

  //Kiosk methods

  /**
   * Add a new kiosk in a given domain
   */
  Long addKiosk(Long domainId, IKiosk kiosk) throws ServiceException;

  /**
   * Update a kiosk (domain Id is expected to be set within the kiosk
   */
  void updateKiosk(IKiosk kiosk, Long domainId) throws ServiceException;

  void updateKiosk(IKiosk kiosk, Long domainId, String username) throws ServiceException;

  /**
   * Get a kiosk, given a unique kiosk Id (this is unique across domains), along with associated users and poolgroups (if any); deep=false in alternate method will only get the kiosk
   */
  IKiosk getKiosk(Long kioskId) throws ServiceException;

  /**
   * Get kiosk objects of given list of kiosk ids.
   */
  List<IKiosk> getKiosksByIds(List<Long> kioskIds) throws ServiceException;

  IKiosk getKioskIfPresent(Long kioskId) throws ServiceException, ObjectNotFoundException;

  IKiosk getKiosk(Long kioskId, boolean deep) throws ServiceException;

  IKiosk getKioskIfPresent(Long kioskId, boolean deep)
      throws ServiceException, ObjectNotFoundException;

  IKiosk getKioskByName(Long domainId, String kioskName) throws ServiceException;

  /**
   * Is the user associated with this kiosk
   */
  boolean hasKiosk(String userId, Long kioskId);

  /**
   * Delete a set of kiosks, given their ids
   */
  void deleteKiosks(Long domainId, List<Long> kiosks, String userId) throws ServiceException;

  /**
   * Get kiosks accessible/visible to a given user
   */
  Results getKiosks(IUserAccount user, Long domainId, String tags, String excludedTags, PageParams pageParams)
      throws ServiceException;

  /**
   * Get kiosks for a given user, with pagination; If routeTag is given, then only kiosks with the given tag are returned, else pass routeTag as null
   */
  Results getKiosksForUser(IUserAccount user, String routeTag, PageParams pageParams)
      throws ServiceException;

  /**
   * Checks and returns the permission level the requested user with the kioskId, based on their roles and
   * their direct relation with the kiosk (for managers) and the domain (for admins).
   */
  boolean hasAccessToKiosk(String userId, Long kioskId, Long domainId, String role);

  /**
   * Checks and returns the permission this user has with the given kiosk by checking permissions of all kiosks
   * this user has access to which have relationship with the requested kiosk.
   */
  Integer hasAccessToKiosk(String userId, Long kioskId);

  /**
   * Get kiosk Ids for a given user Id
   */
  Results getKioskIdsForUser(String userId, String routeTag, PageParams pageParams)
      throws ServiceException;

  /**
   * Find all kiosks that match a certain criteria. paramName can be one of the following:
   * JsonTags.NAME
   * JsonTags.COUNTRY
   * JsonTags.STATE
   * JsonTags.DISTRICT
   * JsonTags.TALUK
   * JsonTags.CITY
   * JsonTags.OWNER_ID
   */
  List<IKiosk> findKiosks(Long domainId, String paramName, String paramValue)
      throws ServiceException;

  /**
   * Get all kioks with pagination in a given domain
   */
  Results getAllKiosks(Long domainId, String tag, String excludedTag, PageParams params);

  //PoolGroup methods

  /**
   * Add a pool group in a given domain
   */
  Long addPoolGroup(Long domainId, IPoolGroup group) throws ServiceException;

  /**
   * Update a given pool group (the domain ID is expected to be part of the group object)
   */
  void updatePoolGroup(IPoolGroup group) throws ServiceException;

  /**
   * Get a pool group given its ID (this ID is unique across domains, hence domain ID is not required)
   */
  IPoolGroup getPoolGroup(Long groupId) throws ServiceException;

  /**
   * Delete a set of pool groups, given their IDs
   */
  void deletePoolGroups(Long domainId, List<Long> groupIds) throws ServiceException;

  /**
   * Find all pool groups with pagination, in a given domain
   */
  List<IPoolGroup> findAllPoolGroups(Long domainId, int pageNumber, int numOfEntries)
      throws ServiceException;

  /**
   * Get poolgroup Ids that a given kiosk is in
   */
  List<Long> getPoolGroupIds(Long kioskId) throws ServiceException;

  // Supply-chain relationships amongst kiosks

  /**
   * Add new kiosk links
   */
  List<String> addKioskLinks(Long domainId, List<IKioskLink> links) throws ServiceException;

  /**
   * Update a given kiosk link
   */
  void updateKioskLink(IKioskLink kl) throws ServiceException;

  /**
   * Get the kiosk links for a given kiosk and specific type of link (e.g. VENDOR, CUSTOMER)
   *
   * @param kioskId  ID of kiosk of interest
   * @param linkType The type of link of interest (e.g. VENDOR, CUSTOMER)
   * @param text linked kiosk name starts with
   * @return A list of KioskLink objects
   */
  Results getKioskLinks(Long kioskId, String linkType, String routeTag, String text,
                        PageParams pageParams)
      throws ServiceException;

  /**
   * Get the kiosk links for a given kiosk, specific type of a link and for given linked kiosk (e.g., CUSTOMER, VENDOR)
   *
   * @param kioskId ID of kiosk of interest
   * @param linkType The type of link of interest (e.g. VENDOR, CUSTOMER)
   * @param routeTag linked kiosk name starts with
   * @param text  A list of KioskLink objects
   * @param linkedKioskId ID of linked kiosk of interest
   * @return A list of KioskLink objects
   */
  Results getKioskLinks(Long kioskId, String linkType, String routeTag, String text,
                        PageParams pageParams, Boolean isCountOnly, Long linkedKioskId,
                        String entityTag)
      throws ServiceException;

  /**
   * Get a given kiosk link
   */
  IKioskLink getKioskLink(String linkId) throws ObjectNotFoundException, ServiceException;

  /**
   * Check if the kiosk-link exists
   */
  boolean hasKioskLink(Long kioskId, String linkType, Long linkedKioskId) throws ServiceException;

  IKioskLink getKioskLink(Long kioskId, String linkType, Long linkedKioskId)
      throws ServiceException, ObjectNotFoundException;

  /**
   * Delete kiosk links given the link keys, or Ids
   *
   * @param linkIds The unique key of each link (say, as generated by the datastore)
   */
  void deleteKioskLinks(Long domainId, List<String> linkIds, String sUser) throws ServiceException;

  /**
   * Update ManagedEntityRoute
   */
  void updateManagedEntitiesOrdering(Long domainId, String userId, String routeQueryString)
      throws ServiceException;

  /**
   * Reset ManagedEntityRoute
   */
  void resetManagedEntitiesOrdering(String userId) throws ServiceException;

  /**
   * Update RelatedEntitiesRoute
   */
  void updateRelatedEntitiesOrdering(Long domainId, Long kioskId, String linkType,
                                     String routeQueryString) throws ServiceException;

  /**
   * Reset ResetRelatedEntitiesRoute
   */
  void resetRelatedEntitiesOrdering(Long kioskId, String linkType) throws ServiceException;

  /**
   * Set ManagedEntitiesRouteInfo
   */
  void setManagedEntityRouteInfo(Long domainId, String userId, Long kioskId, String routeTag,
                                 Integer routeIndex) throws ServiceException;

  /**
   * Set RelatedEntitiesRouteInfo
   */
  void setRelatedEntityRouteInfo(Long domainId, Long kioskId, String linkType, Long linkedKioskId,
                                 String routeTag, Integer routeIndex) throws ServiceException;


  /**
   * Get a domain, given its ID
   *
   * @deprecated Moved to DomainsService, but preserved here to enable backward compatibility in JSPs
   */
  @Deprecated
  IDomain getDomain(Long domainId) throws ServiceException, ObjectNotFoundException;

  /**
   * Get all domains
   *
   * @deprecated Moved to DomainsService, but preserved here to enable backward compatibility in JSPs
   */
  @Deprecated
  Results getAllDomains(PageParams pageParams) throws ServiceException;

  Results getAllDomainKiosks(Long domainId, String tags, String excludedTags, PageParams pageParams);

  List<Long> getAllDomainKioskIds(Long domainId);

  List<Long> getAllKioskIds(Long domainId);

  void updateEntityActivityTimestamps(Long entityId, Date timestamp, int actType);


  /**
   * Get the current domain entities and all its customers for a given domain
   */
  List<EntityLinkModel> getKioskLinksInDomain(Long domainId, String user, String role);

  /**
   * get the list of all districts for a given domain ID
   */
  List<String> getAllDistricts(Long domainId);

  /**
   * get list of all states for a given domain ID
   */
  List<String> getAllStates(Long domainId);

  /**
   * getting the list of all countries for a given domain ID
   */
  List<String> getAllCountries(Long domainId);

  /**
   * Get AssetTags to Register
   * @param kId
   * @return
   * @throws ServiceException
   */
  List<String> getAssetTagsToRegister(Long kId) throws ServiceException,
      ObjectNotFoundException;

  void registerOrUpdateDevices(List<IAsset> assetList)
      throws ServiceException, ObjectNotFoundException;

  UserEntitiesModel getUserWithKiosks(String userId)
      throws ObjectNotFoundException, ServiceException;

  UserEntitiesModel getUserWithKiosks(IUserAccount userAccount)
      throws ObjectNotFoundException, ServiceException;

  // Get the linked kiosks that for a given kiosk
  @SuppressWarnings("unchecked")
  Results getLinkedKiosks(Long kioskId, String linkType, String routeTag,
                          PageParams pageParams) throws ServiceException;

  List<LocationSuggestionModel> getCitySuggestions(Long domainId, String text);

  List<LocationSuggestionModel> getTalukSuggestions(Long domainId, String text, LocationSuggestionModel parentLocation);

  List<LocationSuggestionModel> getDistrictSuggestions(Long domainId, String text, LocationSuggestionModel parentLocation);

  /**
   * get list of all states for a given domain ID
   */
  List<LocationSuggestionModel> getStateSuggestions(Long domainId, String text);

  /**
   * Add primary and secondary approvers for a given kiosk id
   * @param kioskId
   * @param approvers
   * @return
   */
  void addApprovers(Long kioskId, List<IApprovers> approvers, String userName);

  /**
   * Get the list of approvers for a given kioskId and approver type
   * @param kioskId
   * @return
   */
  List<IApprovers> getApprovers(Long kioskId, int type);

  /**
   * Get the list of approvers for a given kioskId
   * @param kioskId
   * @return
   */
  List<IApprovers> getApprovers(Long kioskId);

  List<IApprovers> getApprovers(Long kioskId, PersistenceManager pm);

  /**
   * Get the list of approvers for a given kioskId and orderType
   * @param kioskId
   * @param orderType
   * @return
   */
  List<IApprovers> getApprovers(Long kioskId, String orderType);

  /**
   * Get the list of approvers for a given kioskId and orderType and approverType
   * @param kioskId
   * @param type
   * @param orderType
   * @return
   */
  List<IApprovers> getApprovers(Long kioskId, int type, String orderType);

  /**
   * Checks whether an user is an approver for purchase/sales/transfer order in that domain
   * @param userId
   * @param domainId
   * @return
   */
  boolean isAnApprover(String userId, Long domainId);

}
