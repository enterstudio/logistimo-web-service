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
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.pagination.QueryParams;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.utils.PropertyUtil;
import com.logistimo.constants.QueryConstants;
import com.logistimo.logger.XLog;

import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * The {@code DomainDeleter} class removes any leaf domain and all its associated objects.
 *
 * @author Mohan Raja
 * @see com.logistimo.domains.processor.DomainDeleteProcessor
 */
public class DomainDeleter {

  private static final String DEL_DOMAIN_PROP = "com.logistimo.domains.entity.Domain.relations";
  private static final XLog xLogger = XLog.getLog(DomainDeleter.class);
  private static final String DID_PARAM = "dIdParam";


  /**
   * Validate the given domain id {@code did} and returns error message if any, or return 'success'.
   *
   * @param did Domain id to be removed
   */
  public static String validateDeleteDomain(Long did) throws ServiceException {
    if (DomainsUtil.isLinkAvailable(did, IDomainLink.TYPE_CHILD)) {
      return "Domain cannot be removed if child is present. Please remove child first.";
    }
    return "success";
  }

  /**
   * Delete given domain {@code domainId} from data store.
   *
   * @param domainId id of domain to be removed.
   */
  public static void deleteDomain(Long domainId) throws ServiceException {
    String relatedObjectsStr = ConfigUtil.get(DEL_DOMAIN_PROP);
    Map<String, String[]> relatedClassesMap = PropertyUtil.parseProperty(relatedObjectsStr);
    for (String relatedClassName : relatedClassesMap.keySet()) {
      Map<String, String> params = new HashMap<>();
      params.put("name", "DeleteAllEntities");
      params.put("kind", relatedClassName.substring(relatedClassName.lastIndexOf(".") + 1));
      params.put("domainId", domainId.toString());
      try {
        AppFactory.get().getTaskService()
            .schedule(ITaskService.QUEUE_DEFAULT, "/task/mrstarter", params,
                ITaskService.METHOD_GET);
      } catch (Exception e) {
        xLogger.severe("{0} when scheduling task to remove domain objects {1}: {2}",
            e.getClass().getName(), domainId, e.getMessage(), e);
      }
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IDomain d = JDOUtils.getObjectById(IDomain.class, domainId, pm);
      pm.deletePersistent(d);
    } finally {
      pm.close();
    }
  }

  /**
   * Returns the {@link QueryParams} with query where
   * <i>domain ids</i> field contains {@code domainID}.
   *
   * @param className table name.
   * @param idField   domain ids field name.
   * @param domainId  domain id to check.
   * @return the generated query with filter conditions.
   */
  private static QueryParams constructQueryParam(String className, String[] idField,
                                                           Long domainId) {
    xLogger.fine("Entering constructQueryParam");
    StringBuilder queryStr = new StringBuilder();
    queryStr.append(QueryConstants.SELECT_FROM).append(className)
        .append(QueryConstants.WHERE)
        .append(idField[0]).append(CharacterConstants.DOT).append(QueryConstants.CONTAINS)
        .append(CharacterConstants.O_BRACKET).append(DID_PARAM).append(CharacterConstants.C_BRACKET)
        .append(QueryConstants.PARAMETERS)
        .append(QueryConstants.LONG).append(CharacterConstants.SPACE).append(DID_PARAM);
    Map<String, Object> paramMap = new HashMap<String, Object>(1);
    paramMap.put(DID_PARAM, domainId);
    xLogger.fine("Exiting getQuery query: {0} params: {1}", queryStr.toString(), paramMap);
    return new QueryParams(queryStr.toString(), paramMap);
  }
}
