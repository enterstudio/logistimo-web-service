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

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.pagination.QueryParams;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.utils.MsgUtil;

import org.apache.commons.lang.StringUtils;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.entities.pagination.processor.MoveProcessor;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.utils.PropertyUtil;
import com.logistimo.constants.QueryConstants;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * The {@code EntityMover} class moves one or more Entity and all its associated objects
 * from one domain to another.
 * Entity can't be moved to new Domain if its users are associated to other entities,
 * which are not selected for moving to new domain.
 *
 * @author Mohan Raja
 * @see EntityMoveHelper
 * @see MoveProcessor
 */
public class EntityMover {

  private static final XLog xLogger = XLog.getLog(EntityMover.class);
  private static final int MAX_CONTAINS_VALUE = 30;
  private static final String MOVE_ENTITY_PROP = "entity.move.relations";

  /**
   * Validate the {@code kiosks} and returns error message if any, or return 'success'.
   *
   * @param kiosks List of kiosk id to validate
   */
  public static String validateMoveEntitiesToDomain(List<Long> kiosks) throws ServiceException {
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, null);
    List<IKiosk> kioskList = as.getKiosksByIds(kiosks);
    Set<String> users = EntityMoveHelper.extractUserIds(kioskList);
    List<String> errors = EntityMoveHelper.validateUsers(users, kiosks);
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", Locale.ENGLISH);
    if (errors.size() > 0) {
      String errSt = errors.toString().substring(1);
      errSt = errSt.substring(0, errSt.length() - 1);
      xLogger.warn("User is associated to other entities: {0}", errSt);
      return "Some users are associated to other " + backendMessages.getString("kiosks.lower")
          + ", which are not included in this move." + MsgUtil.newLine() +
          "Please fix the following errors." + errSt;
    }
    String relatedObjectsStr = ConfigUtil.get(MOVE_ENTITY_PROP);
    Map<String, String[]> relatedClassesMap = PropertyUtil.parseProperty(relatedObjectsStr);
    for (String relatedClassName : relatedClassesMap.keySet()) {
      String[] fieldName = relatedClassesMap.get(relatedClassName);
      if (fieldName.length != 2) {
        return
            "Invalid no field names. Required only two field names. 1. key id field name 2. Source domain field name"
                +
                MsgUtil.newLine() + "Field names:" + Arrays.toString(fieldName);
      }
    }
    return "success";
  }

  /**
   * Moves list of given entity {@code kiosks} from its source domain to {@code destDomainId}.
   *
   * @param kiosks       List of kiosk ids to be moved to other domain.
   * @param destDomainId Destination domain id of all kiosks to be moved.
   * @throws ServiceException when there is error in fetching object.
   *
   * @see EntityMoveHelper
   */
  public static void moveEntitiesToDomain(List<Long> kiosks, Long destDomainId, String sDid)
      throws ServiceException {
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, null);
    List<IKiosk> kioskList = as.getKiosksByIds(kiosks);
    Set<String> users = EntityMoveHelper.extractUserIds(kioskList);
    Long sourceDomainId = kioskList.get(0).getDomainId();
    if (StringUtils.isNotEmpty(sDid)) {
      sourceDomainId = Long.parseLong(sDid);
    }
    String relatedObjectsStr = ConfigUtil.get(MOVE_ENTITY_PROP);
    Map<String, String[]> relatedClassesMap = PropertyUtil.parseProperty(relatedObjectsStr);
    for (String relatedClassName : relatedClassesMap.keySet()) {
      String[] fieldName = relatedClassesMap.get(relatedClassName);
      try {
        List ids;
        if (relatedClassName.endsWith("UserAccount")) {
          ids = new ArrayList<String>(users);
        } else {
          ids = kiosks;
        }
        xLogger
            .info("Scheduling task for moving Object: {0}[{1}] from domainID {2} to domainId {3}",
                relatedClassName, ids, sourceDomainId, destDomainId);
        execute(relatedClassName, fieldName, ids, sourceDomainId, destDomainId);
        xLogger.info("AUDITLOG\tENTITY\t " +
            "MOVE\t{0}\tfrom domainId:{1}\tto domainId:{2}", ids, sourceDomainId, destDomainId);
      } catch (Exception e) {
        xLogger.severe(
            "{0} when scheduling task for moving Object: {1} from domainID {2} to domainId {3}: {4}",
            e.getClass().getName(), relatedClassName, sourceDomainId, destDomainId, e.getMessage(),
            e);
      }
    }
  }

  /**
   * GQL supports only limited number of values in contains.
   * Here multiple queries are generated with no of ids limited to maximum of {@code MAX_CONTAINS_VALUE} per query.
   *
   * @param className      Table name.
   * @param idField        Array of column names with size always 2. First is column name of <i>id</i> field and
   *                       second is column name of <i>source domain id</i>.
   * @param ids            List of ids to filter rows in given table.
   * @param sourceDomainId Source domain id of all kiosks to be moved.
   * @param destDomainId   Destination domain id of all kiosks to be moved.
   * @throws Exception captured from {@code PagedExec.exec} method includes {@code TaskSchedulingException}
   *                   and {@code ProcessingException}.
   */
  @SuppressWarnings("unchecked")
  private static void execute(String className, String[] idField, List ids, Long sourceDomainId,
                              Long destDomainId) throws Exception {
    final int idsSize = ids.size();
    int idsIndex = 0;
    boolean last = false;
    while (!last) {
      List list;
      if (idsIndex + MAX_CONTAINS_VALUE < idsSize) {
        list = new ArrayList(ids.subList(idsIndex, idsIndex + MAX_CONTAINS_VALUE));
      } else {
        list = new ArrayList(ids.subList(idsIndex, idsSize));
        last = true;
      }
      QueryParams qp = constructQueryParam(className, idField, list, sourceDomainId);
      PagedExec.exec(destDomainId, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
          PagedExec.loadProcessor(MoveProcessor.class.getName()), String.valueOf(sourceDomainId),
          null, 0, true, false);
      idsIndex += MAX_CONTAINS_VALUE;
    }
  }

  /**
   * Returns the {@link QueryParams} with query where
   * <i>source domain id</i> field equals to {@code domainID} and {@code ids} contains values in <i>id</i> field.
   *
   * @param className      table name.
   * @param idField        id and source domain field name.
   * @param ids            list of unique id to filter.
   * @param sourceDomainId domain id to filter.
   * @return the generated query with filter conditions.
   */
  private static QueryParams constructQueryParam(String className, String[] idField,
                                                           Collection ids, Long sourceDomainId) {
    xLogger.fine("Entering constructQueryParam");
    StringBuilder queryStr = new StringBuilder();
    StringBuilder qDeclarations = new StringBuilder();
    Map<String, Object> paramMap = new HashMap<String, Object>(2);

    queryStr.append(QueryConstants.SELECT_FROM).append(className);
    queryStr.append(QueryConstants.WHERE);

    queryStr.append(idField[1]).append(QueryConstants.D_EQUAL).append(QueryConstants.S_DID_PARAM);
    qDeclarations.append(QueryConstants.LONG).append(CharacterConstants.SPACE)
        .append(QueryConstants.S_DID_PARAM);
    paramMap.put(QueryConstants.S_DID_PARAM, sourceDomainId);

    queryStr.append(" && idList.contains(").append(idField[0]).append(CharacterConstants.C_BRACKET);
    qDeclarations.append(",java.util.Collection idList");
    paramMap.put("idList", ids);

    queryStr.append(QueryConstants.PARAMETERS).append(qDeclarations);

    xLogger.fine("Exiting getQuery query: {0} params: {1}", queryStr.toString(), paramMap);
    return new QueryParams(queryStr.toString(), paramMap);
  }
}
