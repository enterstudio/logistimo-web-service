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
package com.logistimo.domains.utils;

import com.logistimo.domains.processor.DeleteProcessor;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.QueryParams;
import com.logistimo.services.ServiceException;
import com.logistimo.logger.XLog;
import com.logistimo.services.utils.ConfigUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Removes related entities using paged execution
 *
 * @author Arun
 */
public class EntityRemover {

  // Constants
  private static final String RELATIONS_SUFFIX = ".relations";

  // Logger
  private static final XLog xLogger = XLog.getLog(EntityRemover.class);

  /**
   * Remove all entities related to a given entity. The entityName should be the full class name of that entity (e.g. org.samaanguru.entity.Kiosk).
   * The related entities are to be specified in samaanguru.properties.
   *
   * @param entityClassName The full class-name of the parent entity
   * @param entityKey       The key or id of the entity, whose related entities are to be removed
   * @throws ServiceException Thrown if there was an error in scheduling the removal
   */
  public static void removeRelatedEntities(Long domainId, String entityClassName, Object entityKey,
                                           boolean skipCounting) throws ServiceException {
    Object[] keys = new Object[1];
    keys[0] = entityKey;
    removeRelatedEntities(domainId, entityClassName, keys, skipCounting);
  }

  public static void removeRelatedEntities(Long domainId, String entityClassName,
                                           Object[] entityKeys, boolean skipCounting)
      throws ServiceException {
    xLogger.fine("Entered removeRelatedEntities  with entity: {0}", entityClassName);

    // Get the related entities
    String relatedEntitiesStr = ConfigUtil.get(entityClassName + RELATIONS_SUFFIX);
    if (relatedEntitiesStr == null || relatedEntitiesStr.isEmpty()) {
      xLogger.warn("No related entities configured for entity {0}. Not removing anything.",
          entityClassName);
      ///throw new ServiceException( "No related entities configured for entity " + entityClassName + ". Not removing anything." );
      return;
    }

    // Get all the related entities of the given entity
    // NOTE: a list has been used, so that duplicate related class names can be specified in the configuration (e.g. KioskLink appears twice in Kiosk configuration)
    List<HashMap<String, String[]>>
        relatedEntityList =
        getRelatedEntities(entityClassName, relatedEntitiesStr);

    // Remove the records of all the related entities
    Iterator<HashMap<String, String[]>> iterator = relatedEntityList.iterator();
    while (iterator.hasNext()) {
      HashMap<String, String[]> relatedEntityMap = iterator.next();
      String relatedEntityName = null;
      try {
        relatedEntityName = relatedEntityMap.keySet().iterator().next();
      } catch (NoSuchElementException e) {
        xLogger.warn("No such element exception: {0}", e.getMessage());
      }
      String[] relatedEntityForeignIdNames = relatedEntityMap.get(relatedEntityName);
      if (relatedEntityForeignIdNames == null
          || relatedEntityForeignIdNames.length != entityKeys.length) {
        // Error - the key values should be the same as the key ids names required (from configuration)
        throw new ServiceException(
            "None or insufficient entity keys " + entityKeys.length + " to delete entities of kind "
                + entityClassName + ". Related entity key names: " + relatedEntityForeignIdNames);
      }
      // Perform the delete task
      HashMap<String, Object> filters = new HashMap<String, Object>();
      for (int i = 0; i < relatedEntityForeignIdNames.length; i++) {
        filters.put(relatedEntityForeignIdNames[i], entityKeys[i]);
      }
      // Get the query params
      try {
        xLogger.info("Scheduling task for deleting related entities {0} for entity {1} with id {2}",
            relatedEntityName, entityClassName, entityKeys);
        QueryParams qp = getQueryParams(relatedEntityName, filters);
        PagedExec.exec(domainId, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
            DeleteProcessor.class.getName(), (skipCounting ? "true" : "false"), null);
      } catch (Exception e) {
        xLogger.severe(
            "{0} when scheduling task for deleting related entities {1} for entity {2} with id {3} in domain {4}: {5}",
            e.getClass().getName(), relatedEntityName, entityClassName, entityKeys, domainId,
            e.getMessage());
      }
    }

    xLogger.fine("Exiting removeRelatedEntities");
  }

  // Get the related entities - entity name and attribute (column) name of parent entity
  private static List<HashMap<String, String[]>> getRelatedEntities(String entityClassName,
                                                                    String relatedEntitiesStr) {
    xLogger.fine("Entering getRelatedEntities: {0}", relatedEntitiesStr);

    List<HashMap<String, String[]>> list = new ArrayList<HashMap<String, String[]>>();

    if (relatedEntitiesStr != null && !relatedEntitiesStr.isEmpty()) {
      String[] relEntitiesArr = relatedEntitiesStr.split(",");
      for (int i = 0; i < relEntitiesArr.length; i++) {
        String[] relEntity = relEntitiesArr[i].split(":");
        if (relEntity.length == 2) {
          String relEntityClassName = relEntity[0];
          // Get the key names (there could be multiple)
          String[] relEntityKeyNames = new String[1];
          if (relEntity[1].indexOf("&")
              == -1) // i.e. contains the "|" separator, and hence multiple key names
          {
            relEntityKeyNames[0] = relEntity[1];
          } else {
            relEntityKeyNames = relEntity[1].split("&");
          }
          xLogger.fine("relEntityKeyNames: {0}, relEntity[ 1 ] = {1}", relEntityKeyNames.toString(),
              relEntity[1]);
          // Create a map for this related entity
          HashMap<String, String[]> map = new HashMap<String, String[]>();
          map.put(relEntityClassName, relEntityKeyNames);
          // Update list
          list.add(map);
        } else {
          xLogger.warn("Invalid value for application property {0}{1}: {2}", entityClassName,
              RELATIONS_SUFFIX, relEntitiesArr[i]);
        }
      }
    }
    xLogger.fine("Exiting getRelatedEntities: {0}", list.toString());
    return list;
  }

  // Get a query given the class name and its filters
  private static QueryParams getQueryParams(String entityClassName, HashMap<String, Object> filters)
      throws ClassNotFoundException {
    xLogger.fine("Entering getQuery");

    String queryStr = "SELECT FROM " + entityClassName;
    // Form the query filters
    String qFilters = "";
    String qDeclarations = "";
    Map<String, Object> paramMap = new HashMap<String, Object>();
    if (filters != null && !filters.isEmpty()) {
      queryStr += " WHERE ";
      Set<String> attributes = filters.keySet();
      Iterator<String> iterator = attributes.iterator();
      while (iterator.hasNext()) {
        String attrName = iterator.next();
        String attrVarName = attrName + "Param";
        Object attrValue = filters.get(attrName);
        String attrType = attrValue.getClass().getSimpleName();
        // Update the filter
        if (!qFilters.isEmpty()) {
          qFilters += " && ";
        }
        qFilters += attrName + " == " + attrVarName;
        // Update the declaration
        if (!qDeclarations.isEmpty()) {
          qDeclarations += ", ";
        }
        qDeclarations += attrType + " " + attrVarName;
        // Update parameter map
        paramMap.put(attrVarName, attrValue);
      }
      // Update query
      queryStr += qFilters + " PARAMETERS " + qDeclarations;
    }

    xLogger.fine("Exiting getQuery");
    return new QueryParams(queryStr, paramMap);
  }
}
