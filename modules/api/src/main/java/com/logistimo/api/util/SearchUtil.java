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

/**
 *
 */
package com.logistimo.api.util;

import com.logistimo.assets.entity.IAsset;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.QueryUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Search items on attributes/values
 *
 * @author Arun
 */
public class SearchUtil {

  private static final XLog xLogger = XLog.getLog(SearchUtil.class);

  // Find users based on first name in a given domain
  public static Results findUsers(Long domainId, String firstName, PageParams pageParams)
      throws ServiceException {
    String nName = firstName;
    if (firstName != null) {
      nName = firstName.toLowerCase();
    }
    return find(JDOUtils.getImplClass(IUserAccount.class), "nName", nName, domainId, null, null,
        pageParams, false, false);
  }

  // Find materials based on name in a given domain
  public static Results findMaterials(Long domainId, String name, PageParams pageParams)
      throws ServiceException {
    if (name == null || name.trim().isEmpty()) {
      throw new ServiceException("No name specified");
    }
    return find(JDOUtils.getImplClass(IMaterial.class), "uName", name.toLowerCase(), domainId, null,
        null, pageParams, true, false);
  }

  // Find handling units based on name in a given domain
  public static Results findHandlingUnits(Long domainId, String name, PageParams pageParams)
      throws ServiceException {
    if (name == null || name.trim().isEmpty()) {
      throw new ServiceException("No name specified");
    }
    return find(JDOUtils.getImplClass(IHandlingUnit.class), "nName", name.toLowerCase(), domainId,
        null, null, pageParams, true, false);
  }

  // Find materials based on name in a given domain
  public static Results findAssets(Long domainId, String name, PageParams pageParams)
      throws ServiceException {
    if (name == null || name.trim().isEmpty()) {
      throw new ServiceException("No name specified");
    }
    return find(JDOUtils.getImplClass(IAsset.class), "nsId", name.toLowerCase(), domainId, null,
        null, pageParams, true, false);
  }

  private static Results findKiosk(Long domainId, String name, PageParams pageParams,
                                   IUserAccount user, boolean isSDomain) throws ServiceException {
    String nName = name;
    if (name != null) {
      nName = name.toLowerCase();
    }
    Results
        results =
        find(JDOUtils.getImplClass(IKiosk.class), "nName", nName, domainId, null, null, pageParams,
            true, isSDomain);
    if (results != null && user != null
        && SecurityUtil.compareRoles(user.getRole(), SecurityConstants.ROLE_DOMAINOWNER) < 0) {
      List<IKiosk> kiosks = results.getResults();
      EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);
      Results entityResults = entitiesService.getKiosksForUser(user, null, null);
      if(entityResults.getNumFound() <= 0) {
        return null;
      }
      List<IKiosk> userKiosks = (List<IKiosk>) entityResults.getResults();
      if (userKiosks == null || userKiosks.isEmpty()) {
        return null;
      }
      // Filter kiosks
      if (kiosks != null) {
        Iterator<IKiosk> it = kiosks.iterator();
        while (it.hasNext()) {
          if (!userKiosks.contains(it.next())) {
            it.remove();
          }
        }
      }
    }
    return results;
  }

  // Find kiosks based on name in a given domain; if user is given, then filter kiosks visible to this user
  @SuppressWarnings("unchecked")
  public static Results findKiosks(Long domainId, String name, PageParams pageParams,
                                   IUserAccount user) throws ServiceException {
    return findKiosk(domainId, name, pageParams, user, false);
  }

  public static Results findDomainKiosks(Long domainId, String name, PageParams pageParams,
                                         IUserAccount user) throws ServiceException {
    return findKiosk(domainId, name, pageParams, user, true);
  }

  // Find domains based on name given in a domain
  public static Results findDomains(String name, PageParams pageParams) throws ServiceException {
    String nName = name;
    if (name != null) {
      nName = name.trim().toLowerCase();
    }
    return find(JDOUtils.getImplClass(IDomain.class), "nNm", nName, null, "nNm", "ASC", pageParams,
        false, false);
  }

  // Find linked kiosks of a given kiosk, starting with name
  @SuppressWarnings("unchecked")
  public static Results findLinkedKiosks(Long kioskId, String linkType, String linkedKioskName,
                                         PageParams pageParams) throws ServiceException {
    if (kioskId == null || linkedKioskName == null || linkedKioskName.isEmpty()) {
      return null;
    }
    String nName = linkedKioskName.toLowerCase();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String
        queryString =
        "SELECT FROM " + JDOUtils.getImplClass(IKioskLink.class).getName()
            + " WHERE kioskId == kioskIdParam && linkType == linkTypeParam && "
            + "lknm >= lknmParam1 && lknm < lknmParam2 PARAMETERS Long kioskIdParam, "
            + "String linkTypeParam, String lknmParam1, String lknmParam2 ORDER BY lknm ASC";
    Query q = pm.newQuery(queryString);
    // Set pagination params. if required
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    List<IKioskLink> links = null;
    String cursor = null;
    try {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("kioskIdParam", kioskId);
      params.put("linkTypeParam", linkType);
      params.put("lknmParam1", nName);
      params.put("lknmParam2", (nName + Constants.UNICODE_REPLACEANY));
      links = (List<IKioskLink>) q.executeWithMap(params);
      links.size();
      cursor = QueryUtil.getCursor(links);
      links = (List<IKioskLink>) pm.detachCopyAll(links);
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    return new Results(links, cursor);
  }

  /**
   * Find all objects starting with a given value on a given attribute
   * NOTE: If suitable indices are not found, an exception is thrown by GAE
   */
  @SuppressWarnings({"rawtypes"})
  private static Results find(Class queryClass, String field, String value, Long domainId,
                              String orderByField, String orderDirection, PageParams pageParams,
                              boolean isMDomain, boolean isSDomain) throws ServiceException {
    xLogger.fine("Entered find");
    Results results = null;
    if (value == null || value.trim().isEmpty()) {
      throw new ServiceException("No search value provided");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Clean up input
    String queryString = value.trim();
    Query q = pm.newQuery(queryClass);
    // Set the filter and parameters
    String filter = field + " >= " + field + "Param1" + " && " + field + " < " + field + "Param2";
    String declaration = "String " + field + "Param1, String " + field + "Param2";
    if (domainId != null) {
      if (isMDomain) {
        if (isSDomain) {
          filter += " && sdId == dIdParam";
        } else {
          filter += " && dId.contains(dIdParam)";
        }
      } else {
        filter += " && dId == dIdParam";
      }
      declaration += ", Long dIdParam";
    }
    q.setFilter(filter);
    q.declareParameters(declaration);
    // Set ordering, if needed
    if (orderByField != null && !orderByField.isEmpty()) {
      if (orderDirection == null || orderDirection.isEmpty()) {
        orderDirection = "asc";
      }
      q.setOrdering(orderByField + " " + orderDirection);
    }
    // Set pagination params. if required
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    // Execute query
    try {
      List list = null;
      if (domainId != null) {
        list =
            (List) q.execute(queryString, (queryString + Constants.UNICODE_REPLACEANY), domainId);
      } else {
        list = (List) q.execute(queryString, (queryString + Constants.UNICODE_REPLACEANY));
      }
      list.size();
      String cursor = QueryUtil.getCursor(list);
      list = (List) pm.detachCopyAll(list);
      // Return results
      results = new Results(list, cursor);
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    xLogger.fine("Exiting find");
    return results;
  }

  public static boolean isDistrictAvailable(String country, String state) {
    try {
      ConfigurationMgmtService
          cms =
          Services.getService(ConfigurationMgmtServiceImpl.class, null);
      IConfig c = cms.getConfiguration(IConfig.LOCATIONS);
      if (c != null && c.getConfig() != null) {
        String jsonString = c.getConfig();
        JSONObject jsonObject = new JSONObject(jsonString);
        int
            disCnt =
            ((JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) jsonObject
                .get("data")).get(country)).get("states")).get(state)).get("districts"))
                .length();
        return disCnt > 0;
      }
    } catch (Exception ignored) {
      // do nothing
    }
    return true;
  }

}
