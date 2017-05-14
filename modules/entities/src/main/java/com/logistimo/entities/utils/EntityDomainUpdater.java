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

import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;

import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.entities.pagination.processor.EntityDomainUpdateProcessor;
import com.logistimo.services.ServiceException;
import com.logistimo.constants.CharacterConstants;

import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.utils.PropertyUtil;
import com.logistimo.constants.QueryConstants;
import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code EntityDomainUpdater} class adds/removes one or more domain ids from an Entity and
 * all its associated objects.
 *
 * @author Mohan Raja
 * @see EntityDomainUpdateProcessor
 */
public class EntityDomainUpdater {

  private static final String ENTITY_DOMAIN_UPDATE_PROP = "entity.domain.update.relations";
  private static final XLog xLogger = XLog.getLog(EntityDomainUpdater.class);

  /**
   * Update Entity domain ids and all its related objects domain ids list with {@code domainId} and
   * all its ancestors.
   *
   * @param kioskId   Entity id.
   * @param domainIds domain id to be added to entity and its related objects.
   * @throws ServiceException when there is error in fetching object.
   *
   */
  public static void updateEntityDomain(Long kioskId, String domainIds, boolean isAdd)
      throws ServiceException {
    String relatedObjectsStr = ConfigUtil.get(ENTITY_DOMAIN_UPDATE_PROP);
    Map<String, String[]> relatedClassesMap = PropertyUtil.parseProperty(relatedObjectsStr);
    EntitiesService as = new EntitiesServiceImpl();
    Long sourceDomainId = as.getKiosk(kioskId).getDomainId();
    for (String relatedClassName : relatedClassesMap.keySet()) {
      String[] fieldName = relatedClassesMap.get(relatedClassName);
      try {
        xLogger.info("Scheduling task for updating domain ids of Object: {0}[{1}]",
            relatedClassName, kioskId);
        QueryParams qp = constructQueryParam(relatedClassName, fieldName, kioskId);
        PagedExec.exec(isAdd ? 1L : 0L, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
            PagedExec.loadProcessor(EntityDomainUpdateProcessor.class.getName()),
            sourceDomainId + CharacterConstants.COLON + domainIds, null, 0, true);
      } catch (Exception e) {
        xLogger.severe("{0} when scheduling task for updating domain ids of Object: {1}[2]",
            e.getClass().getName(), relatedClassName, kioskId, e.getMessage(), e);
      }
    }
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
}
