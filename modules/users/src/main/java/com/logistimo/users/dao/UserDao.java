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

package com.logistimo.users.dao;

import com.logistimo.dao.JDOUtils;
import com.logistimo.pagination.QueryParams;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.UserAccount;

import com.logistimo.services.impl.PMF;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.QueryConstants;
import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 03/03/15.
 */
public class UserDao implements IUserDao {
  private static final String DID_EQ_FILTER = "sdId == domainIdParam";
  private static final String DOMAIN_ID_PARAM = "domainIdParam";
  private static final String PARAM = "Param";
  private static final String DID_CONTAINS_FILTER = "dId.contains(domainIdParam)";
  private static final String ROLE_SU_FILTER = "role != roleParam";
  private static final String ROLE_SU_PARAM = "roleParam";
  private XLog xLogger = XLog.getLog(UserDao.class);
  private ITagDao tagDao = new TagDao();

  /**
   * Get the query params object based on the domain Id and the filters selected. This is used in the service during display and during export.
   *
   * @param domainId                Domain Id
   * @param filters                 Map of the filters (param name - param value)
   * @param includeChildDomainUsers Whether child domain users should be included or not
   */
  public QueryParams getQueryParams(Long domainId, Map<String, Object> filters,
                                              boolean includeChildDomainUsers) {
    return getQueryParams(domainId, filters, includeChildDomainUsers, false);
  }

  /**
   * Get the query params object based on the domain Id and the filters selected. This is used in the service during display and during export.
   *
   * @param domainId                Domain Id
   * @param filters                 Map of the filters (param name - param value)
   * @param includeChildDomainUsers Whether child domain users should be included or not
   * @param excludeSuperUsers        Whether superusers should be excluded
   */
  public QueryParams getQueryParams(Long domainId, Map<String, Object> filters,
                                              boolean includeChildDomainUsers,
                                              boolean excludeSuperUsers) {
    if (domainId == null) {
      return null;
    }
    // Form query params.
    StringBuilder filter = new StringBuilder();
    StringBuilder declaration = new StringBuilder();
    Map<String, Object> params = new HashMap<String, Object>();
    if (includeChildDomainUsers) {
      filter.append(DID_CONTAINS_FILTER);
    } else {
      filter.append(DID_EQ_FILTER);
    }
    declaration.append(QueryConstants.LONG).append(CharacterConstants.SPACE)
        .append(DOMAIN_ID_PARAM);
    if (excludeSuperUsers && (filters == null || !filters.containsKey("role"))) {
      filter.append(QueryConstants.AND);
      filter.append(ROLE_SU_FILTER);
      declaration.append(CharacterConstants.COMMA).append(QueryConstants.STRING)
          .append(CharacterConstants.SPACE)
          .append(ROLE_SU_PARAM);
      params.put(ROLE_SU_PARAM, "ROLE_su");
    }
    String
        queryStr =
        "SELECT FROM " + JDOUtils.getImplClass(IUserAccount.class).getName() + " WHERE ";
    String order = "nName" + QueryConstants.ASCENDING;

    boolean imports = false;

    params.put(DOMAIN_ID_PARAM, domainId);

    if (filters != null && !filters.isEmpty()) {
      // Get filters keys
      Set<String> keys = filters.keySet();
      // Iterate through keys.
      Iterator<String> keysIter = keys.iterator();
      while (keysIter.hasNext()) {
        String paramName = keysIter.next();
        Object paramValue = filters.get(paramName);
        // Fields are Strings
        boolean isParamNotEmpty = !(paramValue == null);
        String columnName = getColumnName(paramName);
        String variation = getVariation(paramName);
        String paramNameAlias = columnName + variation + PARAM;
        boolean sw = false; // Flag indicating that the paramName uses starts-with logic.
        if (!isParamNotEmpty) {
          continue;
        }
        switch (paramName) {
          case "role":
            filter.append(QueryConstants.AND).append(columnName).append(QueryConstants.D_EQUAL)
                .append(paramNameAlias);
            declaration.append(CharacterConstants.COMMA).append(QueryConstants.STRING)
                .append(CharacterConstants.SPACE).append(paramNameAlias);
            break;
          case "neverLogged":
            filter.append(QueryConstants.AND).append(columnName).append(QueryConstants.D_EQUAL)
                .append(paramNameAlias);
            declaration.append(CharacterConstants.COMMA).append(QueryConstants.DATE)
                .append(CharacterConstants.SPACE).append(paramNameAlias);
            paramValue = null;// neverLogged means lastLogin field is null
            imports = true;
            break;
          case "isEnabled":
            filter.append(QueryConstants.AND).append(columnName).append(QueryConstants.D_EQUAL)
                .append(paramNameAlias);
            declaration.append(CharacterConstants.COMMA).append(QueryConstants.BOOLEAN)
                .append(CharacterConstants.SPACE).append(paramNameAlias);
            break;
          case "utag":
            filter.append(QueryConstants.AND).append(columnName).append(QueryConstants.DOT_CONTAINS)
                .append(paramNameAlias).append(CharacterConstants.C_BRACKET);
            declaration.append(CharacterConstants.COMMA).append(QueryConstants.LONG)
                .append(CharacterConstants.SPACE).append(paramNameAlias);
            paramValue = tagDao.getTagFilter((String) paramValue, ITag.USER_TAG);
            break;
          case "from":
            filter.append(QueryConstants.AND).append(columnName).append(QueryConstants.GR_EQUAL)
                .append(paramNameAlias);
            declaration.append(CharacterConstants.COMMA).append(QueryConstants.DATE)
                .append(CharacterConstants.SPACE).append(paramNameAlias);
            if (!order.contains("lastLogin")) {
              order = columnName + QueryConstants.DESCENDING + CharacterConstants.COMMA + order;
            }
            imports = true;
            break;
          case "to":
            filter.append(QueryConstants.AND).append(columnName).append(QueryConstants.LESS_THAN)
                .append(paramNameAlias);
            declaration.append(CharacterConstants.COMMA).append(QueryConstants.DATE)
                .append(CharacterConstants.SPACE).append(paramNameAlias);
            if (!order.contains("lastLogin")) {
              order = columnName + QueryConstants.DESCENDING + CharacterConstants.COMMA + order;
            }
            imports = true;
            break;
          default:
            // Name - search key, mobile Number, app version - Search based on starts with logic. Trim the string.
            String paramValueStr = (String) paramValue;
            if (!paramValueStr.isEmpty()) {
              paramValueStr = paramValueStr.trim();
              isParamNotEmpty = !paramValueStr.isEmpty();
            }
            if (isParamNotEmpty) {
              String
                  startsWith =
                  (paramName.equals("v") ? paramValueStr : paramValueStr.toLowerCase());
              filter.append(QueryConstants.AND)
                  .append(paramName).append(QueryConstants.GR_EQUAL).append(paramNameAlias)
                  .append("1")
                  .append(QueryConstants.AND).append(paramName).append(QueryConstants.LESS_THAN)
                  .append(paramNameAlias).append("2");
              declaration.append(CharacterConstants.COMMA).append(QueryConstants.STRING)
                  .append(CharacterConstants.SPACE).append(paramNameAlias).append("1")
                  .append(CharacterConstants.COMMA).append(QueryConstants.STRING)
                  .append(CharacterConstants.SPACE).append(paramNameAlias).append("2");
              params.put(paramNameAlias + "1", startsWith);
              params.put(paramNameAlias + "2", startsWith + Constants.UNICODE_REPLACEANY);
              sw = true;
            }
            break;
        }
        if (!sw) {
          params.put(paramNameAlias, paramValue);
        }
      }
    }
    queryStr +=
        filter.toString() + CharacterConstants.SPACE + QueryConstants.PARAMETERS
            + CharacterConstants.SPACE + declaration.toString();

    if (imports) {
      queryStr += CharacterConstants.SPACE + QueryConstants.DATE_IMPORT;
    }
    queryStr += QueryConstants.ORDERBY + CharacterConstants.SPACE + order;
    return new QueryParams(queryStr, params);
  }

  private String getColumnName(String paramName) {
    String columnName = paramName;
    if (paramName.equals("from") || paramName.equals("to") || paramName.equals("neverLogged")) {
      columnName = "lastLogin";
    } else if (paramName.equals("utag")) {
      columnName = "tgs";
    }
    return columnName;
  }

  private String getVariation(String paramName) {
    String variation = "";
    if (paramName.equals("from")) {
      variation = "From";
    } else if (paramName.equals("to")) {
      variation = "To";
    }
    return variation;
  }

  /**
   * Get users given the offset and size
   *
   * @param offset Offset
   * @param size   Number of users
   */
  public List<IUserAccount> getUsers(int offset, int size) {
    PersistenceManager pm = null;
    Query query = null;
    List<IUserAccount> users = null;
    try {
      pm = PMF.get().getPersistenceManager();
      String
          queryStr =
          "SELECT * FROM USERACCOUNT ORDER BY USERID ASC limit " + offset + CharacterConstants.COMMA
              + size;
      query = pm.newQuery("javax.jdo.query.SQL", queryStr);
      query.setClass(UserAccount.class);
      users = (List<IUserAccount>) query.execute();
      users = (List<IUserAccount>) pm.detachCopyAll(users);
    } catch (Exception e) {
      xLogger.warn("Error encountered during user id migrations", e);
    } finally {
      if (query != null) {
        query.closeAll();
      }
      if (pm != null) {
        pm.close();
      }
    }
    return users;
  }
}
