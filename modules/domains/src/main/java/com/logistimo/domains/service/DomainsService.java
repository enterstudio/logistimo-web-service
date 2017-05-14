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

package com.logistimo.domains.service;

import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.entity.IDomainPermission;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.exception.TaskSchedulingException;

import java.util.List;

public interface DomainsService extends Service {

  /*** Domain APIs ***/

  /**
   * Get a domain, given its ID
   */
  IDomain getDomain(Long domainId) throws ServiceException, ObjectNotFoundException;

  /**
   * Get all domains in the system
   */
  Results getAllDomains(PageParams pageParams) throws ServiceException;

  /**
   * Create a new domain
   */
  Long addDomain(IDomain domain) throws ServiceException;

  /**
   * Remove a list of domains, given their domain IDs
   */
  void deleteDomains(List<Long> domainIds) throws ServiceException;

  /**
   * Update/disable a give domain
   */
  void updateDomain(IDomain domain) throws ServiceException;

  /**
   * Get the linked domains of the given domain (returns a list of DomainLink)
   */
  Results getDomainLinks(Long domainId, int linkType, PageParams pageParams)
      throws ServiceException;

  /**
   * Get the linked domains of the given domain (returns a list of DomainLink)
   * NOTE: depth of -1 gets the entire subtree
   */
  List<IDomainLink> getDomainLinks(Long domainId, int linkType, int depth) throws ServiceException;

  /**
   * Get the linked domains of the given domain (returns a list of DomainLink)
   */
  List<IDomainLink> getAllDomainLinks(Long domainId, int linkType) throws ServiceException;

  /**
   * Check if links of a certain type exist
   */
  boolean hasDomainLinks(Long domainId, int linkType) throws ServiceException;

  /**
   * Add domain links
   */
  void addDomainLinks(List<IDomainLink> domainLinks, IDomainPermission permission)
      throws ServiceException;

  /**
   * Remove domain links
   */
  void deleteDomainLinks(List<String> keys) throws ServiceException;

  /**
   * Remove domain link
   */
  void deleteDomainLink(IDomainLink domainLink, boolean hasChild) throws ServiceException;

  /**
   * Add objects to new domains (objectId is typically String or Long or Key)
   * NOTE: The object(s) to be removed MUST implement the IDomain interface
   */
  void addObjectsToDomains(List<Object> objectIds, Class<?> clazz, List<Long> domainIds)
      throws ObjectNotFoundException, ServiceException;

  /**
   * Remove objects from the given domains
   * NOTE: This the object(s) to be removed MUST implement the IDomain interface
   */
  void removeObjectsFromDomains(List<Object> objectIds, Class<?> clazz, List<Long> domainId)
      throws ObjectNotFoundException, ServiceException;

  /**
   * Get all domains with type 0 i.e domains which are already added as children.
   */
  List<Long> getAllChildDomains() throws ServiceException;

  /**
   * Get the domain permissions for the given domainId
   */
  IDomain getDomainPermission(Long domainId);

  /**
   * Get the linked domain permissions for the given domainId
   */
  IDomainPermission getLinkedDomainPermission(Long domainId);

  /**
   * Update domain permission for the given domainId
   */
  void updateDomainPermission(IDomainPermission permission, Long domainId, String userName);

  /**
   * Copy the configuration of current domain to child domains
   */
  void copyConfiguration(Long domainId, Long linkedDomainId) throws TaskSchedulingException;

  /**
   * Set the default domain permissions
   */
  void createDefaultDomainPermissions(Long domainId) throws ServiceException;

  List<IDomain> getDomains(String query, PageParams pageParams)
      throws ServiceException, ObjectNotFoundException;

  /**
   * Get all root domains , i.e., domains without parents
   */
  List<IDomain> getAllRootDomains();

  /**
   * Returns domain object for the given domain name, this is case insensitive and returns first domain
   * object that matches with this name
   *
   * @param domainName - Domain name ( Case insenstivie )
   * @return Domain object
   */
  IDomain getDomainByName(String domainName) throws ServiceException;
}
