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

package com.logistimo.entities.utils;

import com.logistimo.assets.entity.IAsset;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;

import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.entities.pagination.processor.EntityDomainUpdateProcessor;
import com.logistimo.services.ServiceException;
import com.logistimo.constants.CharacterConstants;

import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.PropertyUtil;
import com.logistimo.constants.QueryConstants;
import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

/**
 * The {@code DomainLinkUpdater} class adds/removes domain ids based on linked domain from all entities and its related objects.
 *
 * @author Mohan Raja
 * @see EntityDomainUpdateProcessor
 */
public class DomainLinkUpdater {

  private static final String ENTITY_DOMAIN_UPDATE_PROP = "entity.domain.update.relations";
  private static final XLog xLogger = XLog.getLog(DomainLinkUpdater.class);

  /**
   * Update Entity, Asset domain ids and all its related objects domain ids of {@code childDomainIdsStr} with
   * {@code sourceDomainId} and all its ancestors
   *
   * @throws ServiceException when there is error in fetching object
   *                          using {@link com.logistimo.entities.service.EntitiesService}.
   */
  public static void updateDomainLinks(Long sourceDomainId, String childDomainIdsStr, boolean isAdd)
      throws ServiceException {
    String relatedObjectsStr = ConfigUtil.get(ENTITY_DOMAIN_UPDATE_PROP);
    Map<String, String[]> relatedClassesMap = PropertyUtil.parseProperty(relatedObjectsStr);
    EntitiesService as = new EntitiesServiceImpl();
    Set<Long> kioskIds = new HashSet<>();
    List<String> childDomainIds = Arrays.asList(childDomainIdsStr.split(CharacterConstants.COMMA));
    for (String child : childDomainIds) {
      kioskIds.addAll(as.getAllKioskIds(Long.parseLong(child)));
    }
    for (Long kioskId : kioskIds) {
      for (String relatedClassName : relatedClassesMap.keySet()) {
        String[] fieldName = relatedClassesMap.get(relatedClassName);
        try {
          xLogger.info("Scheduling task for updating domain ids of Object: {0}[{1}]",
              relatedClassName, kioskId);
          QueryParams qp = constructQueryParam(relatedClassName, fieldName, kioskId);
          PagedExec.exec(isAdd ? 1L : 0L, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
              PagedExec.loadProcessor(EntityDomainUpdateProcessor.class.getName()),
              "0" + CharacterConstants.COLON + sourceDomainId, null, 0, true);
        } catch (Exception e) {
          xLogger.severe("{0} when scheduling task for updating domain ids of Object: {1}[2]",
              e.getClass().getName(), relatedClassName, kioskId, e.getMessage(), e);
        }
      }
    }

    /**
     * Propagating domain IDs to Assets and domain tags in AssetManagementService
     */
    for (String childDomainId : childDomainIds) {
      try {
        xLogger.info("Scheduling task for updating domain ids of Assets for domain: {0}",
            childDomainId);
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(QueryConstants.SELECT_FROM)
            .append(JDOUtils.getImplClass(IAsset.class).getName());
        queryStr.append(QueryConstants.WHERE).append("kId == null &&");
        queryStr.append("sdId").append(QueryConstants.D_EQUAL).append("dIdParam");
        queryStr.append(QueryConstants.PARAMETERS).append(QueryConstants.LONG)
            .append(CharacterConstants.SPACE).append("dIdParam");
        Map<String, Object> paramMap = new HashMap<>(1);
        paramMap.put("dIdParam", Long.parseLong(childDomainId));
        QueryParams qp = new QueryParams(queryStr.toString(), paramMap);

        PagedExec.exec(isAdd ? 1L : 0L, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
            PagedExec.loadProcessor(EntityDomainUpdateProcessor.class.getName()),
            "0" + CharacterConstants.COLON + sourceDomainId, null, 0, true);
      } catch (Exception e) {
        xLogger.severe("{0} when scheduling task for updating domain ids of Asset  for domain: {1}",
            e.getMessage(), childDomainId, e);
      }
    }

    // Update domain links for users
    updateDomainLinksForUsers(sourceDomainId, childDomainIds, isAdd);

  }

  /**
   * Returns the {@link QueryParams} with query where
   * <i>id</i> field equal to {@code id}.
   *
   * @param className table name.
   * @param idField   id and source domain field name.
   * @param id        entity id to filter.
   * @return the generated query with filter conditions.
   */
  private static QueryParams constructQueryParam(String className, String[] idField,
                                                           Long id) {
    xLogger.fine("Entering constructQueryParam");
    StringBuilder queryStr = new StringBuilder();
    queryStr.append(QueryConstants.SELECT_FROM).append(className);
    queryStr.append(QueryConstants.WHERE);
    queryStr.append(idField[0]).append(QueryConstants.D_EQUAL).append("kIdParam");
    queryStr.append(QueryConstants.PARAMETERS).append(QueryConstants.LONG)
        .append(CharacterConstants.SPACE).append("kIdParam");
    Map<String, Object> paramMap = new HashMap<String, Object>(2);
    paramMap.put("kIdParam", id);
    xLogger.fine("Exiting getQuery query: {0} params: {1}", queryStr.toString(), paramMap);
    return new QueryParams(queryStr.toString(), paramMap);
  }

  private static void updateDomainLinksForUsers(Long sourceDomainId, List<String> childDomainIds, boolean isAdd) {
    for (String childDomainId : childDomainIds) {
      try {
        xLogger.info("Scheduling task for updating domain ids of UserAccount for domain: {0}",
            childDomainId);
        StringBuilder queryStr = new StringBuilder();
        queryStr.append(QueryConstants.SELECT_FROM)
            .append(JDOUtils.getImplClass(IUserAccount.class).getName());
        queryStr.append(QueryConstants.WHERE);
        queryStr.append(" sdId").append(QueryConstants.D_EQUAL).append("dIdParam");
        queryStr.append(QueryConstants.PARAMETERS).append(QueryConstants.LONG)
            .append(CharacterConstants.SPACE).append("dIdParam");
        Map<String, Object> paramMap = new HashMap<>(2);
        paramMap.put("dIdParam", Long.parseLong(childDomainId));
        QueryParams qp = new QueryParams(queryStr.toString(), paramMap);

        PagedExec.exec(isAdd ? 1L : 0L, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
            PagedExec.loadProcessor(EntityDomainUpdateProcessor.class.getName()),
            "0" + CharacterConstants.COLON + sourceDomainId, null, 0, true);
      } catch (Exception e) {
        xLogger.severe("{0} when scheduling task for updating domain ids of users for domain: {1}",
            e.getMessage(), childDomainId, e);
      }
    }
  }
}
