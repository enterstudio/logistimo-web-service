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

package com.logistimo.domains.utils;

import com.logistimo.AppFactory;
import com.logistimo.constants.MethodNameConstants;
import com.logistimo.domains.*;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import javax.jdo.PersistenceManager;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Utility methods commonly used to fetching or updating domain IDs across objects
 *
 * @author arun, Mohan Raja
 */
public class DomainsUtil {

  private static final XLog xLogger = XLog.getLog(DomainsUtil.class);

  public static Long getDomainId(IMultiDomain object) {
    List<Long> domainIds = object.getDomainIds();
    if (domainIds == null || domainIds.isEmpty()) {
      return null;
    }
    return domainIds.get(0);
  }

  public static List<Long> addDomainIds(List<Long> domainIds, IMultiDomain object) {
    if (domainIds == null || domainIds.isEmpty()) {
      return null;
    }
    List<Long> curDomainIds = object.getDomainIds();
    if (curDomainIds == null) {
      curDomainIds = new ArrayList<Long>();
    }
    Iterator<Long> it = domainIds.iterator();
    while (it.hasNext()) {
      Long dId = it.next();
      if (!curDomainIds.contains(dId)) {
        curDomainIds.add(dId);
      }
    }
    return curDomainIds;
  }

  public static List<Long> removeDomainId(Long domainId, IMultiDomain object) {
    List<Long> domainIds = object.getDomainIds();
    if (domainIds != null && !domainIds.isEmpty()) {
      domainIds.remove(domainId);
    }
    return domainIds;
  }

  public static List<Long> removeDomainIds(List<Long> domainIds, IMultiDomain object) {
    List<Long> oDomainIds = object.getDomainIds();
    if (oDomainIds != null && !oDomainIds.isEmpty()) {
      oDomainIds.removeAll(domainIds);
    }
    return oDomainIds;
  }

  /**
   * Add the given object to all parent domains of customer and vendor
   */
  public static Object addToDomain(IMultiDomain object, Long domainId, PersistenceManager pm)
      throws ServiceException {
    return addToDomain(object, domainId, pm, true);
  }

  /**
   * Add the given object to all parent domains of customer and vendor
   */
  public static Object addToDomain(IMultiDomain object, Long domainId, PersistenceManager pm,
                                   boolean setSourceDomain)
      throws ServiceException {
    xLogger.fine("Entered addToDomain: domainId: {0}", domainId);
    if (setSourceDomain) {
      // Set the given domain as the source domain
      object.setDomainId(domainId);
    }
    // Add this object to all domains it should be included in, as required
    object.addDomainIds(getMultiDomainIds(object));
    // Persist
    if (pm != null) {
      object = pm.makePersistent(object);
    }
    xLogger.fine("Exiting addToDomain");
    return object;
  }

  /**
   * Remove an object from a given domain; if persistence manager is given, then it is removed permanently
   * Returns the list of domains from which this object was removed
   */
  public static List<Long> removeFromDomain(IMultiDomain object, Long domainId,
                                            PersistenceManager pm) throws Exception {
    xLogger.fine("Entered removeFromDomain");
    List<Long> domainIds = getMultiDomainIds(object);
    // Remove this object from the given domain
    if (domainIds == null || domainIds.isEmpty()) {
      return null;
    }
    Iterator<Long> it = domainIds.iterator();
    while (it.hasNext()) {
      object.removeDomainId(it.next());
    }
    // Delete the object, if not part of any domain
    if ((object.getDomainIds() == null || object.getDomainIds().isEmpty())
        && pm != null) // i.e. object is part of no domain
    {
      pm.deletePersistent(object);
    }
    xLogger.fine("Exiting removeFromDomain");
    return domainIds;
  }

  // Add an object to all linked domains
  private static List<Long> getLinkedDomains(Long domainId, int linkType) throws ServiceException {
    xLogger.fine("Entered addOrRemoveFromLinkedDomains: domainId: {0}", domainId);
    List<Long> domainIds = new ArrayList<Long>();
    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    List<IDomainLink>
        domainLinks =
        ds.getAllDomainLinks(domainId, linkType); // entire sub-tree (upwards or downwards)
    // Add/remove the object to/from all parent domains
    if (domainLinks != null && !domainLinks.isEmpty()) {
      Iterator<IDomainLink> it = domainLinks.iterator();
      while (it.hasNext()) {
        domainIds.add(it.next().getLinkedDomainId());
      }
    }
    xLogger.fine("Exiting addOrRemoveFromLinkedDomains");
    return domainIds;
  }

  public static List<Long> getVisibleDomains(Long domainId, int linkType) throws ServiceException {
    List<Long> domainIds =  getLinkedDomains(domainId, linkType);
    if(!domainIds.isEmpty() && !domainIds.contains(domainId)) {
      domainIds.add(domainId);
    }
    return domainIds;
  }

  // Add or remove domains Ids belonging to the entity
        /*
        private static void addOrRemoveEntityDomains( boolean add, MultiDomainTransaction object ) throws ServiceException {
		xLogger.fine( "Entered addOrRemoveEntitysDomains" );
		Long kioskId = object.getKioskId();
		AccountsService as = Services.getService( AccountsServiceImpl.class );
		List<Long> domainIds = as.getKiosk( kioskId, false ).getDomainIds();
		if ( domainIds != null && !domainIds.isEmpty() ) {
			Iterator<Long> it = domainIds.iterator();
			while ( it.hasNext() ) {
				Long dId = it.next();
				if ( add )
					object.setDomainId( dId );
				else
					object.removeDomainId( dId );
			}
		}
		xLogger.fine( "Exiting addOrRemoveEntitysDomains" );
	}
	*/

  // Get domains to add to or remove from for a given type of multidomain object
  private static List<Long> getMultiDomainIds(IMultiDomain object) throws ServiceException {
    Set<Long> domainIds = new LinkedHashSet<Long>();
    Long sourceDomainId = object.getDomainId();
    domainIds.add(sourceDomainId);
    if (object instanceof ICrossDomain) { // it is a cross-domain object (like KioskLink); add it both the domains
      ICrossDomain crossDomain = (ICrossDomain) object;
      if (crossDomain.getLinkedDomainId() != null) {
        domainIds.add(crossDomain.getLinkedDomainId()); // add the linked domain ID as well
        domainIds.addAll(getLinkedDomains(crossDomain.getLinkedDomainId(),
            IDomainLink.TYPE_PARENT)); //add all parent domain ids to display count
      }
      domainIds.add(crossDomain
          .getKioskDomainId()); // add the kiosk domain ID as well, possible that relation is created from some other parent domain
      domainIds.addAll(getLinkedDomains(crossDomain.getKioskDomainId(), IDomainLink.TYPE_PARENT));
    } else if (object instanceof IOverlappedDomain) {
      Long kioskId = ((IOverlappedDomain) object).getKioskId();
      if (kioskId != null) {
        IMultiDomain kioskDomains = AppFactory.get().getDaoUtil().getKioskDomains(kioskId);
        object.setDomainId(kioskDomains.getDomainId());
        domainIds.addAll(kioskDomains.getDomainIds());
      } else {
        domainIds.addAll(getLinkedDomains(sourceDomainId, IDomainLink.TYPE_PARENT));
      }
    } else { // its a super/sub-domain, so add this to the parent/child domains, as appropriate
      // NOTE: objects can implement both Super and SubDomains (e.g. Material)
      // Superdomain, so add this object to all parent domains
      if (object instanceof ISuperDomain) {
        domainIds.addAll(getLinkedDomains(sourceDomainId, IDomainLink.TYPE_PARENT));
      }
      // Subdomain, so add this object to all child domains
      if (object instanceof ISubDomain) {
        domainIds.addAll(getLinkedDomains(sourceDomainId, IDomainLink.TYPE_CHILD));
      }
    }
    return new ArrayList<>(domainIds);
  }

  /**
   * Fetches and returns all parents/ancestors of {@code domainId} with given {@code domainId}.
   *
   * @param domainId      domain id.
   * @param includeSource whether {@code domainId} need to be added with parents or not.
   * @return all parents/ancestors of {@code domainId}.
   */
  public static Set<Long> getDomainLinks(Long domainId, int type, boolean includeSource)
      throws ServiceException {
    DomainsService as = Services.getService(DomainsServiceImpl.class);
    List<IDomainLink> domainLinks = as.getAllDomainLinks(domainId, type);
    Set<Long> parents;
    if (domainLinks == null) {
      parents = new HashSet<>(0);
    } else {
      parents = new HashSet<Long>(domainLinks.size());
    }
    if (includeSource) {
      parents.add(domainId);
    }
    if (domainLinks != null) {
      for (IDomainLink domainLink : domainLinks) {
        parents.add(domainLink.getLinkedDomainId());
      }
    }
    return parents;
  }

  /**
   * Fetches and returns all parents/ancestors of {@code domainId} with given {@code domainId}.
   *
   * @param domainId      domain id.
   * @param includeSource whether {@code domainId} need to be added with parents or not.
   * @return all parents/ancestors of {@code domainId}.
   */
  public static Set<Long> getDomainParents(Long domainId, boolean includeSource)
      throws ServiceException {
    return getDomainLinks(domainId, IDomainLink.TYPE_PARENT, includeSource);
  }

  /**
   * Fetches and returns all parents/ancestors of {@code domainId} with given {@code domainId}.
   *
   * @param domainId      domain id.
   * @param includeSource whether {@code domainId} need to be added with parents or not.
   * @return all parents/ancestors of {@code domainId}.
   */
  public static Set<Long> getDomainChildren(Long domainId, boolean includeSource)
      throws ServiceException {
    return getDomainLinks(domainId, IDomainLink.TYPE_CHILD, includeSource);
  }

  /**
   * Check whether parent/children of {@code domainId} is available.
   *
   * @param domainId domain id.
   * @param linkType check availability of parent or children.
   * @return available status of domain link.
   */
  public static boolean isLinkAvailable(Long domainId, int linkType) throws ServiceException {
    DomainsService as = Services.getService(DomainsServiceImpl.class);
    List<IDomainLink> domainLinks = as.getDomainLinks(domainId, linkType, 1);
    return domainLinks != null;
  }

  /**
   * Extract leaf domain ids within given list of domain id {@code domainIds}
   *
   * @param domainIds      List of domain ids to find the leaf nodes within list
   * @param ignoreDomainId DomainId and all its ancestors are ignored for finding leaf nodes
   * @return returns leaf domain ids within list
   */
  public static Set<Long> extractLeafDomains(List<Long> domainIds, Long ignoreDomainId)
      throws ServiceException {
    Set<Long> nonParentDomains = new HashSet<Long>(domainIds);
    if (ignoreDomainId != null) {
      nonParentDomains.removeAll(getDomainParents(ignoreDomainId, true));
    }
    Set<Long> leafDomains = new HashSet<Long>(nonParentDomains);
    for (Long nonParentDomain : nonParentDomains) {
      if (leafDomains.contains(nonParentDomain)) {
        leafDomains.removeAll(getDomainParents(nonParentDomain, false));
      }
    }
    return leafDomains;
  }

  /**
   * Logic to get non-intersecting parent list
   * 1. ListA: All domainIds of Object {@code o}.
   * 2. ListB: {@code domainId} and all its ancestors.
   * 3. ListC: ListA - ListB
   * 4. ListD: ListC and all ancestors of ListC
   * 5. Final List: ListB - ListD
   *
   * @param domainId domain id to be removed
   * @param o        multi domain object
   * @return non-intersecting domain ids (step 5)
   */
  @SuppressWarnings("unchecked")
  public static List<Long> extractNonIntersectingDomainIds(Long domainId, Object o)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
      ServiceException {
    List<Long>
        domainIds =
        (List<Long>) o.getClass().getMethod(MethodNameConstants.GET_DOMAIN_IDS).invoke(o);
    Set<Long> niDomains = getDomainParents(domainId, true);
    domainIds.removeAll(niDomains);
    Set<Long> allDomains = new HashSet<Long>();
    for (Long id : domainIds) {
      if (!allDomains.contains(id)) {
        allDomains.addAll(getDomainParents(id, true));
      }
    }
    niDomains.removeAll(allDomains);
    return new ArrayList<Long>(niDomains);
  }
}