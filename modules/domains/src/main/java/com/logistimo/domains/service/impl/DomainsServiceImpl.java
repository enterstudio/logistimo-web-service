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

package com.logistimo.domains.service.impl;

import com.google.gson.Gson;

import com.logistimo.AppFactory;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.CopyConfigModel;
import com.logistimo.domains.IMultiDomain;
import com.logistimo.domains.ObjectsToDomainModel;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.entity.IDomainPermission;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.utils.EntityRemover;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.utils.QueryUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@org.springframework.stereotype.Service
public class DomainsServiceImpl extends ServiceImpl implements DomainsService {

  private static final XLog xLogger = XLog.getLog(DomainsServiceImpl.class);
  // Domains task url
  private static final String DOMAINS_TASK_URL = "/task/domains";
  private static final String CONFIG_TASK_URL = "/task/createconfig";

  private ITaskService taskService = AppFactory.get().getTaskService();

  /**
   * Get a domain, given a domain ID
   */
  public IDomain getDomain(Long domainId) throws ServiceException, ObjectNotFoundException {
    xLogger.fine("Entering getDomain");

    if (domainId == null) {
      throw new ServiceException("Invalid domain ID");
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    IDomain domain = null;
    String errMsg = null;
    boolean notFound = false;
    try {
      domain = JDOUtils.getObjectById(IDomain.class, domainId);
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("Domain {0} not found", domainId);
      notFound = true;
      errMsg = e.getMessage();
    } catch (Exception e) {
      errMsg = e.getMessage();
    } finally {
      pm.close();
    }
    if (notFound) {
      throw new ObjectNotFoundException(errMsg);
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg);
    }

    xLogger.fine("Exiting getDomain");
    return domain;
  }

  @SuppressWarnings("unchecked")
  public Results getAllDomains(PageParams pageParams) throws ServiceException {
    xLogger.fine("Entering getAllDomains");

    PersistenceManager pm = PMF.get().getPersistenceManager();
    String errMsg = null;
    Query query = pm.newQuery(JDOUtils.getImplClass(IDomain.class));
    query.setOrdering("nNm asc");
    if (pageParams != null) {
      QueryUtil.setPageParams(query, pageParams);
    }
    List<IDomain> domains = null;
    String cursor = null;
    try {
      domains = (List<IDomain>) query.execute();
      domains = (List<IDomain>) pm.detachCopyAll(domains);
      if (domains != null) {
        domains.size(); // to retrieve the results before closing the PM
        cursor = QueryUtil.getCursor(domains);
      }
    } catch (Exception e) {
      errMsg = e.getMessage();
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg);
    }
    xLogger.fine("Exiting getAllDomains");

    return new Results(domains, cursor);
  }

  /**
   * Get the linked domains of the given domain (returns a list of DomainLink)
   * NOTE: depth of -1 gets the entire subtree
   */
  @SuppressWarnings("unchecked")
  public List<IDomainLink> getDomainLinks(Long domainId, int linkType, int depth)
      throws ServiceException {
    xLogger.fine("Entered getLinkedDomains");
    List<IDomainLink>
        links =
        getDomainLinks(domainId, linkType, null)
            .getResults(); // get the next level of links for this domain
    if (links == null || links.isEmpty()) {
      return null;
    }
    List<IDomainLink> allLinks = new ArrayList<IDomainLink>(links.size());
    allLinks.addAll(links);
    if (depth == 0) {
      return allLinks;
    }
    // Get the next level of links
    Iterator<IDomainLink> it = links.iterator();
    while (it.hasNext()) {
      IDomainLink dl = it.next();
      List<IDomainLink> nextLinks = getDomainLinks(dl.getLinkedDomainId(), linkType, depth--);
      if (nextLinks != null && !nextLinks.isEmpty()) {
        allLinks.addAll(nextLinks);
      }
    }
    xLogger.fine("Exiting getLinkedDomains");
    return allLinks;
  }

  public List<IDomainLink> getAllDomainLinks(Long domainId, int linkType) throws ServiceException {
    return getDomainLinks(domainId, linkType, -1);
  }

  /**
   * Get the domain links for a given
   */
  @SuppressWarnings("unchecked")
  public Results getDomainLinks(Long domainId, int linkType, PageParams pageParams)
      throws ServiceException {
    xLogger.fine("Entered getLinkedDomains");
    if (domainId == null) {
      throw new IllegalArgumentException("Invalid domain Id");
    }
    int type = linkType;
    String cursor = null;
    List<IDomainLink> links = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String
        queryStr =
        "SELECT FROM " + JDOUtils.getImplClass(IDomainLink.class).getName()
            + " WHERE dId == dIdParam && ty == tyParam PARAMETERS Long dIdParam, Integer tyParam ORDER BY nldnm ASC"; // sorted by linked domain name
    Query q = pm.newQuery(queryStr);
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      links = (List<IDomainLink>) q.execute(domainId, new Integer(type));
      links = (List<IDomainLink>) pm.detachCopyAll(links);
      if (links != null && !links.isEmpty()) {
        links.size(); // ensure retrieval

        cursor = QueryUtil.getCursor(links); // get cursor
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }

    xLogger.fine("Exiting getLinkedDomains");
    return new Results(links, cursor);
  }

  /**
   * Check if links of a certain type exist
   */
  @SuppressWarnings("unchecked")
  public boolean hasDomainLinks(Long domainId, int linkType) throws ServiceException {
    xLogger.fine("Entered hasLinkedDomains");
    if (domainId == null) {
      throw new IllegalArgumentException("Invalid domain ID");
    }
    boolean has = false;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String
        queryStr =
        "SELECT key FROM " + JDOUtils.getImplClass(IDomainLink.class).getName()
            + " WHERE dId == dIdParam && ty == tyParam PARAMETERS Long dIdParam, Integer tyParam ORDER BY nldnm ASC";
    Query q = pm.newQuery(queryStr);
    QueryUtil.setPageParams(q, new PageParams(null, 1));
    try {
      List<String> keys = (List<String>) q.execute(domainId, new Integer(linkType));
      xLogger.fine("Got keys: {0}", keys);
      has = keys != null && !keys.isEmpty();
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    xLogger.fine("Exiting hasLinkedDomains");
    return has;
  }

  /**
   * Add domain links
   */
  public void addDomainLinks(List<IDomainLink> domainLinks, IDomainPermission permission)
      throws ServiceException {
    xLogger.fine("Entered addDomainLinks");
    if (domainLinks == null || domainLinks.isEmpty()) {
      throw new IllegalArgumentException("Invalid domain links list");
    }
    List<IDomainLink> allLinks = new ArrayList<IDomainLink>();
    // Check if key and date are set, if not set those
    Date now = new Date();
    Iterator<IDomainLink> it = domainLinks.iterator();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      while (it.hasNext()) {
        IDomainLink link = it.next();
        if (link.getKey() == null) {
          link.createKey();
        }
        if (link.getCreatedOn() == null) {
          link.setCreatedOn(now);
        }
        allLinks.add(link);
        if (permission != null) {
          IDomain domain = JDOUtils.getObjectById(IDomain.class, link.getLinkedDomainId(), pm);
          domain.setHasParent(true);
          IDomainPermission domainPermission = null;
          try {
            domainPermission =
                JDOUtils.getObjectById(IDomainPermission.class, link.getLinkedDomainId(), pm);
          } catch (Exception e) {
            xLogger.warn("No permissions available in DB adding domain link. Creating new.", e);
          }
          boolean persist = false;
          if (domainPermission == null) {
            domainPermission = JDOUtils.createInstance(IDomainPermission.class);
            domainPermission.setdId(link.getLinkedDomainId());
            persist = true;
          }
          domainPermission.setUsersView(permission.isUsersView());
          domainPermission.setUsersAdd(permission.isUsersAdd());
          domainPermission.setUsersEdit(permission.isUsersEdit());
          domainPermission.setUsersRemove(permission.isUsersRemove());
          domainPermission.setEntityView(permission.isEntityView());
          domainPermission.setEntityAdd(permission.isEntityAdd());
          domainPermission.setEntityEdit(permission.isEntityEdit());
          domainPermission.setEntityRemove(permission.isEntityRemove());
          domainPermission.setEntityGroupView(permission.isEntityGroupView());
          domainPermission.setEntityGroupAdd(permission.isEntityGroupAdd());
          domainPermission.setEntityGroupEdit(permission.isEntityGroupEdit());
          domainPermission.setEntityGroupRemove(permission.isEntityGroupRemove());
          domainPermission.setEntityRelationshipView(permission.isEntityRelationshipView());
          domainPermission.setEntityRelationshipAdd(permission.isEntityRelationshipAdd());
          domainPermission.setEntityRelationshipEdit(permission.isEntityRelationshipEdit());
          domainPermission.setEntityRelationshipRemove(permission.isEntityRelationshipRemove());
          domainPermission.setInventoryView(permission.isInventoryView());
          domainPermission.setInventoryAdd(permission.isInventoryAdd());
          domainPermission.setInventoryEdit(permission.isInventoryEdit());
          domainPermission.setInventoryRemove(permission.isInventoryRemove());
          domainPermission.setMaterialView(permission.isMaterialView());
          domainPermission.setMaterialAdd(permission.isMaterialAdd());
          domainPermission.setMaterialEdit(permission.isMaterialEdit());
          domainPermission.setMaterialRemove(permission.isMaterialRemove());
          domainPermission.setCopyMaterials(permission.isCopyMaterials());
          domainPermission.setCopyConfiguration(permission.isCopyConfiguration());
          domainPermission.setConfigurationEdit(permission.isConfigurationEdit());
          domainPermission.setConfigurationView(permission.isConfigurationView());

          //Adding the configuration for asset
          domainPermission.setAssetAdd(permission.isAssetAdd());
          domainPermission.setAssetEdit(permission.isAssetEdit());
          domainPermission.setAssetRemove(permission.isAssetRemove());
          domainPermission.setAssetView(permission.isAssetView());
          if (persist) {
            pm.makePersistent(domainPermission);
          }
          IDomain pDomain = JDOUtils.getObjectById(IDomain.class, link.getDomainId(), pm);
          pDomain.setHasChild(true);
        }
        // Get the reverse link
        IDomainLink rLink = JDOUtils.createInstance(IDomainLink.class).loadReverseLink(link);
        allLinks.add(rLink);
      }
      // Persist forward and reverse links
      pm.makePersistentAll(allLinks);
      pm.detachCopyAll(allLinks);
    } finally {
      pm.close();
    }
    // Perform the post-commit tasks, if any, for copying domain data/config. to the linked domains
    try {
      domainLinksPostCommitTasks(domainLinks, permission);
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }

    xLogger.fine("Exiting addDomainLinks");
  }

  /**
   * Remove domain links
   */
  public void deleteDomainLinks(List<String> keys) throws ServiceException {
    xLogger.fine("Entered removeDomainLinks");
    if (keys == null || keys.isEmpty()) {
      throw new IllegalArgumentException("Invalid keys to delete");
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    Iterator<String> it = keys.iterator();
    List<IDomainLink> links = new ArrayList<IDomainLink>(keys.size());
    String key = null;
    try {
      while (it.hasNext()) {
        try {
          key = it.next();
          IDomainLink link = JDOUtils.getObjectById(IDomainLink.class, key, pm);
          links.add(link);
          // Get the reverse link
          IDomainLink rLink = JDOUtils.createInstance(IDomainLink.class).loadReverseLink(link);
          links.add(JDOUtils.getObjectById(IDomainLink.class, rLink.getKey(), pm));
          //Remove domains corresponding permissions
          IDomain domain = JDOUtils.getObjectById(IDomain.class, link.getLinkedDomainId(), pm);
          domain.setHasParent(false);
          domain.setHasChild(false);
        } catch (Exception e) {
          xLogger
              .warn("{0} when trying to get domainlink with key {1}: {2}", e.getClass().getName(),
                  key, e.getMessage());
        }
      }
      // Delete links (forward and reverse)
      pm.deletePersistentAll(links);
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting removeDoaminLinks");
  }

  /**
   * Delete domain link
   */
  public void deleteDomainLink(IDomainLink domainLink, boolean hasChild) throws ServiceException {
    xLogger.fine("Entered delete domain link");
    if (domainLink == null) {
      throw new IllegalArgumentException("Invalid link to delete");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      String reverseKey = domainLink.getLinkedDomainId() + ".0." + domainLink.getDomainId();
      IDomainLink link = JDOUtils.getObjectById(IDomainLink.class, domainLink.getKey(), pm);
      IDomainLink rLink = JDOUtils.getObjectById(IDomainLink.class, reverseKey, pm);
      List<IDomainLink> links = new ArrayList<IDomainLink>(2);
      links.add(rLink);
      links.add(link);
      pm.deletePersistentAll(links);
      IDomain domain = JDOUtils.getObjectById(IDomain.class, domainLink.getDomainId(), pm);
      IDomainPermission domainPermission = null;
      try {
        domainPermission =
            JDOUtils.getObjectById(IDomainPermission.class, domainLink.getDomainId(), pm);
      } catch (Exception e) {
        xLogger.warn("No permissions available in DB adding domain link. Creating new.", e);
      }
      boolean persist = false;
      if (domainPermission == null) {
        domainPermission = JDOUtils.createInstance(IDomainPermission.class);
        domainPermission.setdId(domainLink.getDomainId());
        persist = true;
      }
      domainPermission.setUsersView(true);
      domainPermission.setUsersAdd(true);
      domainPermission.setUsersEdit(true);
      domainPermission.setUsersRemove(true);
      domainPermission.setEntityView(true);
      domainPermission.setEntityAdd(true);
      domainPermission.setEntityEdit(true);
      domainPermission.setEntityRemove(true);
      domainPermission.setEntityGroupView(true);
      domainPermission.setEntityGroupAdd(true);
      domainPermission.setEntityGroupEdit(true);
      domainPermission.setEntityGroupRemove(true);
      domainPermission.setEntityRelationshipView(true);
      domainPermission.setEntityRelationshipAdd(true);
      domainPermission.setEntityRelationshipEdit(true);
      domainPermission.setEntityRelationshipRemove(true);
      domainPermission.setInventoryView(true);
      domainPermission.setInventoryAdd(true);
      domainPermission.setInventoryEdit(true);
      domainPermission.setInventoryRemove(true);
      domainPermission.setMaterialView(true);
      domainPermission.setMaterialAdd(true);
      domainPermission.setMaterialEdit(true);
      domainPermission.setMaterialRemove(true);
      domainPermission.setConfigurationView(true);
      domainPermission.setConfigurationEdit(true);
      domainPermission.setCopyConfiguration(true);
      domainPermission.setCopyMaterials(true);
      if (persist) {
        pm.makePersistent(domainPermission);
      }
      domain.setHasParent(false);
      domain.setHasChild(false);
      if (!hasChild) {
        IDomain
            parentDomain =
            JDOUtils.getObjectById(IDomain.class, domainLink.getLinkedDomainId(), pm);
        parentDomain.setHasChild(false);
      }
    } catch (Exception e) {
      xLogger.severe("Error in deleting domain link", e);
      throw new ServiceException("Error in deleting domain link");
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting removeDomainLinks");
  }

  /**
   * Add a new domain
   */
  public Long addDomain(IDomain domain) throws ServiceException {
    xLogger.fine("Entering addDomain");
    if (domain == null) {
      throw new ServiceException("Invalid domain input");
    }
    Date now = new Date();
    // Update domain object
    domain.setCreatedOn(now);
    domain.setLastUpdatedOn(now);

    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(domain);
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }

    xLogger.fine("Exiting addDomain");

    return domain.getId();
  }

  /**
   * Add domain permission
   */
  public void createDefaultDomainPermissions(Long domainId) throws ServiceException {

    xLogger.fine("Entering domainpermission");
    if (domainId == null) {
      throw new ServiceException("Invalid domain");
    }

    String errMsg = null;

    PersistenceManager pm = PMF.get().getPersistenceManager();
    IDomainPermission permission = JDOUtils.createInstance(IDomainPermission.class);
    if (permission != null) {
      permission.setdId(domainId);
    }
    try {
      pm.makePersistent(permission);
    } catch (Exception e) {
      xLogger.severe("Error updating domainpermission: {0}", domainId, e);
      errMsg = e.getMessage();
    } finally {
      pm.close();
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg);
    }

    xLogger.fine("Exiting disableDomain");

  }

  /**
   * Update/disable a given domain
   */
  public void updateDomain(IDomain domain) throws ServiceException {
    xLogger.fine("Entering disableDomain");

    if (domain == null) {
      throw new ServiceException("Invalid domain");
    }

    String errMsg = null;

    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IDomain d = JDOUtils.getObjectById(IDomain.class, domain.getId(), pm);
      d.setName(domain.getName());
      d.setDescription(domain.getDescription());
      d.setOwnerId(domain.getOwnerId());
      d.setIsActive(domain.isActive());
      d.setLastUpdatedBy(domain.getLastUpdatedBy());
      d.setLastUpdatedOn(new Date());
    } catch (Exception e) {
      xLogger.severe("Error updating domain: {0}", domain.getId(), e);
      errMsg = e.getMessage();
    } finally {
      pm.close();
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg);
    }

    xLogger.fine("Exiting disableDomain");
  }

  /**
   * Remove a set of domains, given their IDs.
   * NOTE: Given the current data model, to ensure not dangling records in the relationship tables that don't have
   * domainId (e.g. UserToKiosk), we have to ensure that we delete all the kiosks first, and then the domains themselves.
   */
  public void deleteDomains(List<Long> domainIds) throws ServiceException {
    xLogger.fine("Entered deleteDomains");
    if (domainIds == null || domainIds.size() == 0) {
      throw new ServiceException("Invalid domain ID list");
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Iterate over the domains and delete them
      Iterator<Long> it = domainIds.iterator();
      while (it.hasNext()) {
        Long dId = it.next();
        IDomain d = JDOUtils.getObjectById(IDomain.class, dId);
        // Delete all related entities of the domain
        EntityRemover
            .removeRelatedEntities(dId, JDOUtils.getImplClass(IDomain.class).getName(), dId, true);
        // Remove the domain
        pm.deletePersistent(d);
      }
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    } finally {
      // Close PM
      pm.close();
    }

    xLogger.fine("Exiting deleteDomains");
  }

  /**
   * Add objects to new domains (objectId is typically String or Long or Key)
   * NOTE: This the object(s) to be removed MUST implement the IDomain interface
   */
  public void addObjectsToDomains(List<Object> objectIds, Class<?> clazz, List<Long> domainIds)
      throws ObjectNotFoundException, ServiceException {
    xLogger.fine("Entered addObjectsToDomain");
    if (clazz == null || objectIds == null || objectIds.isEmpty() || domainIds == null || domainIds
        .isEmpty()) {
      throw new IllegalArgumentException("Invalid input parameters - one or more of them is null");
    }
    // Retrieve the object
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      for (Object objectId : objectIds) {
        try {
          IMultiDomain o = (IMultiDomain) pm.getObjectById(clazz, objectId);
          o.addDomainIds(domainIds);
        } catch (JDOObjectNotFoundException e) {
          throw new ObjectNotFoundException(e.getMessage());
        } catch (Exception e) {
          throw new ServiceException(e.getMessage());
        }
      }
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting addObjectsToDomain");
  }

  /**
   * Remove objects from a given domain
   * NOTE: This the object(s) to be removed MUST implement the IDomain interface
   */
  public void removeObjectsFromDomains(List<Object> objectIds, Class<?> clazz, List<Long> domainIds)
      throws ObjectNotFoundException, ServiceException {
    xLogger.fine("Entered removeObjectsFromDomain");
    if (clazz == null || objectIds == null || objectIds.isEmpty() || domainIds == null || domainIds
        .isEmpty()) {
      throw new IllegalArgumentException("Invalid input parameters - one or more of them is null");
    }
    // Retrieve the object
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      for (Object objectId : objectIds) {
        try {
          IMultiDomain o = (IMultiDomain) pm.getObjectById(clazz, objectId);
          for (Long domainId : domainIds) {
            o.removeDomainId(domainId);
          }
        } catch (JDOObjectNotFoundException e) {
          throw new ObjectNotFoundException(e.getMessage());
        } catch (Exception e) {
          throw new ServiceException(e.getMessage());
        }
      }
    } finally {
      pm.close();
    }

    xLogger.fine("Exiting removeObjectsFromDomain");
  }

  @Override
  public List<Long> getAllChildDomains() throws ServiceException {
    xLogger.fine("Entered getAllChildDomains");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String
        queryStr =
        "SELECT ldId FROM " + JDOUtils.getImplClass(IDomainLink.class).getName()
            + " WHERE ty == tyParam PARAMETERS Integer tyParam";
    Query q = pm.newQuery(queryStr);
    List<Long> dIds = new ArrayList<Long>();
    try {
      List<Long> results = (List<Long>) q.execute(new Integer(IDomainLink.TYPE_CHILD));
      dIds.addAll(results);
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    xLogger.fine("Exiting getAllChildDomains");
    return dIds;
  }

  @Override
  public void init(Services services) throws ServiceException {
  }

  @Override
  public void destroy() throws ServiceException {
  }

  @Override
  public Class<? extends Service> getInterface() {
    return DomainsServiceImpl.class;
  }

  // Post-commit tasks, after a domain link is added
  private void domainLinksPostCommitTasks(List<IDomainLink> links, IDomainPermission permission)
      throws ServiceException, TaskSchedulingException {
    xLogger.fine("Entered postCommitTasks");

    if (permission == null || links == null || links.isEmpty()) {
      return; // do nothing
    }
    // Get the list of linked domain Ids
    Long domainId = links.get(0).getDomainId();
    List<Long> linkedDomainIds = new ArrayList<Long>(links.size());
    Iterator<IDomainLink> it = links.iterator();
    while (it.hasNext()) {
      linkedDomainIds.add(it.next().getLinkedDomainId());
    }
    // Inherit materials, if needed
    if (permission.isCopyMaterials()) {
      try {
        scheduleAddObjectsToDomain(domainId, linkedDomainIds,
            "com.logistimo.materials.entity.Material", "materialId");
        scheduleAddObjectsToDomain(domainId, linkedDomainIds,
            "com.logistimo.materials.entity.HandlingUnit", "id");
      } catch (Exception e) {
        xLogger.warn("{0} when trying to inherit materials from domain {1} to domains {2}: {3}",
            e.getClass().getName(), domainId, linkedDomainIds, e.getMessage());
      }
    }
    // Copy configuration, if needed
    if (permission.isCopyConfiguration()) {
      it = links.iterator();
      while (it.hasNext()) {
        IDomainLink dl = it.next();
        try {
          copyConfiguration(domainId, dl.getLinkedDomainId());
        } catch (Exception e) {
          xLogger.warn("{0} when trying to inherit materials from domain {1} to domains {2}: {3}",
              e.getClass().getName(), domainId, dl.getLinkedDomainId(), e.getMessage());
        }
      }
    }

    xLogger.fine("Exiting postCommitTasks");
  }

  private void scheduleAddObjectsToDomain(Long domainId, List<Long> linkedDomainIds, String cls,
                                          String keyField) throws TaskSchedulingException {
    xLogger.fine("Entered inheritMaterials");
    ObjectsToDomainModel
        otdm =
        new ObjectsToDomainModel(ObjectsToDomainModel.ACTION_ADD, linkedDomainIds,
            cls, keyField, domainId);
    Map<String, String> taskParams = new HashMap<>();
    taskParams.put("action", "addobjectstodomain");
    taskParams.put("data", new Gson().toJson(otdm));
    xLogger.fine("Scheduling task to add linked domains: {0}, json post: {1}", linkedDomainIds,
        new Gson().toJson(otdm));
    // Schedule task
    taskService.schedule(ITaskService.QUEUE_DOMAINS, DOMAINS_TASK_URL, taskParams,
        ITaskService.METHOD_POST);
    xLogger.fine("Exiting inheritMaterials");
  }

  // Copy configuration from one domain to another

  /**
   *
   * @param domainId
   * @param linkedDomainId
   * @throws TaskSchedulingException
   */
  @Override
  public void copyConfiguration(Long domainId, Long linkedDomainId) throws TaskSchedulingException {
    xLogger.fine("Entered copyConfiguration");
    CopyConfigModel ccm = new CopyConfigModel(domainId, linkedDomainId);
    Map<String, String> params = new HashMap<String, String>();
    params.put("action", "copydomainconfig");
    params.put("data", new Gson().toJson(ccm));
    // Schedule task
    taskService
        .schedule(ITaskService.QUEUE_DOMAINS, CONFIG_TASK_URL, params, ITaskService.METHOD_POST);
    xLogger.fine("Exiting copyConfiguration");
  }

  /**
   * Get the domain link permission for the given domain id
   */
  public IDomain getDomainPermission(Long domainId) {
    xLogger.fine("Entered domainPermissionRetriever");
    IDomain domain = null;
    if (domainId != null) {
      domain = JDOUtils.getObjectById(IDomain.class, domainId);
      xLogger.fine("Exiting getDomainPermission");
    }
    return domain;
  }

  /**
   * Get the domain link permission for given domain id
   */
  public IDomainPermission getLinkedDomainPermission(Long domainId) {
    xLogger.fine("Entered linkeddomainpermission");
    if (domainId == null) {
      return null;
    }
    IDomainPermission permission = null;
    try {
      permission = JDOUtils.getObjectById(IDomainPermission.class, domainId);
    } catch (Exception e) {
      xLogger.warn("No permissions available in DB for domain {0}", domainId, e);
    }
    xLogger.fine("Exiting linkeddomainpermission");
    return permission;
  }

  /**
   * Update the domain link permission for the given domain id
   */
  public void updateDomainPermission(IDomainPermission permission, Long domainId, String userName) {
    xLogger.fine("Entered domain permission updater");
    if (permission == null) {
      return;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    IDomainPermission prm = null;
    try {
      prm = JDOUtils.getObjectById(IDomainPermission.class, domainId, pm);
    } catch (Exception e) {
      xLogger.warn(
          "No permissions available in DB while updating permission for domain {0}. Creating new.",
          domainId, e);
    }
    if (prm == null) {
      pm.makePersistent(permission);
    } else {
      prm.setUsersView(permission.isUsersView());
      prm.setUsersAdd(permission.isUsersAdd());
      prm.setUsersEdit(permission.isUsersEdit());
      prm.setUsersRemove(permission.isUsersRemove());
      prm.setEntityView(permission.isEntityView());
      prm.setEntityAdd(permission.isEntityAdd());
      prm.setEntityEdit(permission.isEntityEdit());
      prm.setEntityRemove(permission.isEntityRemove());
      prm.setEntityGroupView(permission.isEntityGroupView());
      prm.setEntityGroupAdd(permission.isEntityGroupAdd());
      prm.setEntityGroupEdit(permission.isEntityGroupEdit());
      prm.setEntityGroupRemove(permission.isEntityGroupRemove());
      prm.setInventoryView(permission.isInventoryView());
      prm.setInventoryAdd(permission.isInventoryAdd());
      prm.setInventoryEdit(permission.isInventoryEdit());
      prm.setInventoryRemove(permission.isInventoryRemove());
      prm.setMaterialView(permission.isMaterialView());
      prm.setMaterialAdd(permission.isMaterialAdd());
      prm.setMaterialEdit(permission.isMaterialEdit());
      prm.setMaterialRemove(permission.isMaterialRemove());
      prm.setConfigurationView(permission.isConfigurationView());
      prm.setConfigurationEdit(permission.isConfigurationEdit());
      prm.setEntityRelationshipView(permission.isEntityRelationshipView());
      prm.setEntityRelationshipAdd(permission.isEntityRelationshipAdd());
      prm.setEntityRelationshipEdit(permission.isEntityRelationshipEdit());
      prm.setEntityRelationshipRemove(permission.isEntityRelationshipRemove());
      prm.setAssetView(permission.isAssetView());
      prm.setAssetAdd(permission.isAssetAdd());
      prm.setAssetEdit(permission.isAssetEdit());
      prm.setAssetRemove(permission.isAssetRemove());
    }
    try {
      IDomain domain = JDOUtils.getObjectById(IDomain.class, domainId, pm);
      domain.setLastUpdatedBy(userName);
      domain.setLastUpdatedOn(new Date());
    } catch (Exception e) {
      xLogger.warn("Error in updating last updated status for domain {0}", domainId, e);
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting update domain permission");
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<IDomain> getDomains(String q, PageParams pageParams)
      throws ServiceException, ObjectNotFoundException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(JDOUtils.getImplClass(IDomain.class));
    Map<String, Object> params = new HashMap<>();
    if (!q.isEmpty()) {
      query.declareParameters("String txtParam");
      query.setFilter("nNm.startsWith(txtParam)");
      params.put("txtParam", q.toLowerCase());
    }
    List<IDomain> domains;
    QueryUtil.setPageParams(query, pageParams);
    try {
      domains = (List<IDomain>) query.executeWithMap(params);
      domains = (List<IDomain>) pm.detachCopyAll(domains);
      if (domains != null) {
        domains.size();
      }
    } catch (Exception e) {
      xLogger.severe("Error in fetching list of domains", e);
      throw new InvalidServiceException(backendMessages.getString("domains.fetch.error"));
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return domains;
  }

  @Override
  public List<IDomain> getAllRootDomains() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(JDOUtils.getImplClass(IDomain.class));
    query.setFilter("hasParent == false");
    query.setOrdering("nNm asc");
    List<IDomain> domains;
    try {
      domains = (List<IDomain>) query.execute();
      domains = (List<IDomain>) pm.detachCopyAll(domains);
    } catch (Exception e) {
      xLogger.severe("Error while fetching root domains", e);
      throw new InvalidServiceException(backendMessages.getString("domains.fetch.error"));
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return domains;
  }

  @Override
  public IDomain getDomainByName(String domainName) throws ServiceException {
    if (domainName == null || domainName.isEmpty()) {
      throw new ServiceException("Invalid parameters");
    }
    IDomain d = null;
    // Form query
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      // Form the query
      Query domainQuery = pm.newQuery(JDOUtils.getImplClass(IDomain.class));
      domainQuery.setFilter(" nNm == nameParam");
      domainQuery.declareParameters("String nameParam");
      // Execute the query
      try {
        List<IDomain> results = (List<IDomain>) domainQuery.execute(domainName.toLowerCase());
        if (results != null && !results.isEmpty()) {
          d = results.get(0);
          d = pm.detachCopy(d);
        }
      } finally {
        domainQuery.closeAll();
      }

    } catch (Exception e) {
      xLogger.severe("{0} when trying to get Domain for Domain Name {1}. Message: {2}",
          e.getClass().getName(),
          domainName, e);
    } finally {
      pm.close();
    }
    return d;
  }
}
