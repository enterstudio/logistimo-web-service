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

package com.logistimo.services.utils;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.QueryConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.IMultiDomain;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IUserToKiosk;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.entity.Invntry;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.models.ICounter;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.impl.PMF;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.Counter;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Mohan Raja
 */
public class LogiCounter implements ICounter {

  public static final String SD_ID = "sdId";
  private static final XLog xLogger = XLog.getLog(LogiCounter.class);
  private static final String TABLE_NAME = "TN";
  private static final String TAGS = "tgs";
  private static final String EXCLUDED_TAGS = "extgs";
  private static final String K_ID = "kId";
  private static final String TK_ID = "tkId";
  private static final String K_TAGS = "ktgs";
  private static final String ORDER_TYPE = "otype";
  private static final String PARAM = "Param";
  private static final String DID_FILTER = "dId.contains(domainIdParam)";
  private static final String DID_EQ_FILTER = "dId == domainIdParam";
  private static final String SDID_EQ_FILTER = "sdId == domainIdParam";
  private static final String DOMAIN_ID_PARAM = "domainIdParam";
  private static final String MATERIAL = "Material";
  private static final String ORDER = "Order";
  private static final String SK_ID = "skId";
  private static final String LONG = "Long ";
  private static final String STRING = "String ";
  private static final String INTEGER = "Integer ";
  private static final String TAG_OBJECT = "Tag ";
  private static final String NAME = "name";
  private static final String O_TAGS = "otgs";
  private Long domainId = null;
  private Map<String, Object> keys = null;

  @Override
  public ICounter init(Long domainId, Map<String, Object> keys) {
    this.domainId = domainId;
    this.keys = keys;
    switch((String)keys.get(Counter.OBJECT_TYPE)){
      case "users":
        keys.put(LogiCounter.TABLE_NAME, JDOUtils.getImplClass(IUserAccount.class).getName());
        break;
      case "kiosks":
        keys.put(LogiCounter.TABLE_NAME, JDOUtils.getImplClass(IKiosk.class).getName());
        break;
      case "materials":
        keys.put(LogiCounter.TABLE_NAME, JDOUtils.getImplClass(IMaterial.class).getName());
        break;
      case "orders":
        keys.put(LogiCounter.TABLE_NAME, JDOUtils.getImplClass(IOrder.class).getName());
        break;
      case "trans":
        keys.put(LogiCounter.TABLE_NAME, JDOUtils.getImplClass(ITransaction.class).getName());
        break;
      case "handlingunits":
        keys.put(LogiCounter.TABLE_NAME, JDOUtils.getImplClass(IHandlingUnit.class).getName());
        break;
      case "userkiosks":
        keys.put(LogiCounter.TABLE_NAME, JDOUtils.getImplClass(IUserToKiosk.class).getName());
    }
    keys.remove(Counter.OBJECT_TYPE);
    return this;
  }

  @Override
  public ICounter init(Long domainId, String name) {
    return this;
  }

  @Override
  public int getCount() {
    if (keys == null) {
      return 0;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = null;
    try {
      q = pm.newQuery();
      Map<String, Object> params = new HashMap<>(keys.size());
      StringBuilder filter = new StringBuilder();
      StringBuilder declaration = new StringBuilder();
      StringBuilder variables = new StringBuilder(0);
      StringBuilder imports = new StringBuilder();

      Class clazz = Class.forName(String.valueOf(keys.get(LogiCounter.TABLE_NAME)));
      if (keys.get(SD_ID) != null) {
        filter.append(SDID_EQ_FILTER);
      } else if (IMultiDomain.class.isAssignableFrom(clazz)) {
        filter.append(DID_FILTER);
      } else {
        filter.append(DID_EQ_FILTER);
      }
      params.put(DOMAIN_ID_PARAM, domainId);
      declaration.append(LONG).append(DOMAIN_ID_PARAM);

      if (keys.containsKey(TAGS) || keys.containsKey(EXCLUDED_TAGS)) {
        boolean isExcluded = StringUtils.isNotEmpty((String) keys.get(EXCLUDED_TAGS));
        String value;
        if (isExcluded) {
          value = (String) keys.get(EXCLUDED_TAGS);
        } else {
          value = (String) keys.get(TAGS);
        }
        if (isValueValid(value)) {
          if (value.contains(CharacterConstants.COMMA)) {
            List<String> tags = StringUtil.getList(value, true);
            List<ITag> tagIdList = new TagDao().getTagsByNames(tags, ITag.KIOSK_TAG);
            filter.append(QueryConstants.AND).append(CharacterConstants.O_BRACKET);
            int i = 0;
            for (ITag iTag : tagIdList) {
              String tagParam = TAGS + PARAM + (++i);
              if (i != 1) {
                filter.append(isExcluded ? QueryConstants.AND : QueryConstants.OR).append(CharacterConstants.SPACE);
              }
              filter.append(CharacterConstants.O_BRACKET)
                  .append(isExcluded ? QueryConstants.NEGATION
                      : CharacterConstants.EMPTY).append(TAGS).append(QueryConstants.DOT_CONTAINS)
                  .append(tagParam)
                  .append(CharacterConstants.C_BRACKET).append(CharacterConstants.SPACE)
                  .append(QueryConstants.AND).append(tagParam).append(CharacterConstants.DOT)
                  .append(NAME).append(QueryConstants.D_EQUAL)
                  .append(CharacterConstants.S_QUOTE).append(iTag.getName())
                  .append(CharacterConstants.S_QUOTE).append(CharacterConstants.C_BRACKET);
              if(variables.length()>0){
                variables.append(CharacterConstants.SEMICOLON);
              }
              variables.append(TAG_OBJECT).append(tagParam);
            }
            filter.append(CharacterConstants.C_BRACKET);
          } else {
            filter.append(QueryConstants.AND)
                .append(isExcluded ? QueryConstants.NEGATION : CharacterConstants.EMPTY)
                .append(TAGS).append(QueryConstants.DOT_CONTAINS).append(TAGS).append(PARAM)
                .append(CharacterConstants.C_BRACKET);
            filter.append(QueryConstants.AND).append(TAGS + PARAM).append(CharacterConstants.DOT)
                .append(NAME).append(QueryConstants.D_EQUAL)
                .append(CharacterConstants.S_QUOTE).append(value)
                .append(CharacterConstants.S_QUOTE);
            variables.append(TAG_OBJECT).append(TAGS + PARAM);
          }
          imports.append("import com.logistimo.tags.entity.Tag;");
        }
      } else if (keys.containsKey(K_TAGS)) {
        if (isValueValid(keys.get(K_TAGS))) {
          filter.append(QueryConstants.AND).append(
              K_TAGS + QueryConstants.DOT_CONTAINS + K_TAGS + PARAM + CharacterConstants.C_BRACKET);
          filter.append(QueryConstants.AND).append(K_TAGS + PARAM).append(CharacterConstants.DOT)
              .append(NAME).append(QueryConstants.D_EQUAL)
              .append(CharacterConstants.S_QUOTE).append(keys.get(K_TAGS))
              .append(CharacterConstants.S_QUOTE);
          variables.append(TAG_OBJECT).append(K_TAGS + PARAM);
          imports.append("import com.logistimo.tags.entity.Tag;");
        }
      } else if (keys.containsKey(O_TAGS)) {
        if (isValueValid(keys.get(O_TAGS))) {
          filter.append(QueryConstants.AND).append(
              O_TAGS + QueryConstants.DOT_CONTAINS + O_TAGS + PARAM + CharacterConstants.C_BRACKET);
          filter.append(QueryConstants.AND).append(O_TAGS + PARAM).append(CharacterConstants.DOT)
              .append(NAME).append(QueryConstants.D_EQUAL)
              .append(CharacterConstants.S_QUOTE).append(keys.get(O_TAGS))
              .append(CharacterConstants.S_QUOTE);
          variables.append(TAG_OBJECT).append(O_TAGS + PARAM);
          imports.append("import com.logistimo.tags.entity.Tag;");
        }
      }

      String tableName = String.valueOf(keys.get(TABLE_NAME));
      if (tableName.endsWith(MATERIAL) && keys.containsKey(K_ID) && isValueValid(keys.get(K_ID))) {
        q.setClass(Invntry.class);
      } else {
        q.setClass(clazz);
      }

      for (String s : keys.keySet()) {
        Object value = keys.get(s);
        if (!isValueValid(value) || TABLE_NAME.equals(s) || TAGS.equals(s) || EXCLUDED_TAGS.equals(s) || K_TAGS.equals(s)
            || ORDER_TYPE.equals(s) || O_TAGS.equals(s)) {
          continue;
        }
        if (tableName.endsWith(ORDER) && s.equals(K_ID) && IOrder.TYPE_SALE
            .equals(String.valueOf(keys.get(ORDER_TYPE)))) {
          filter.append(QueryConstants.AND).append(SK_ID).append(QueryConstants.D_EQUAL).append(s)
              .append(PARAM);
        } else {
          filter.append(QueryConstants.AND).append(s).append(QueryConstants.D_EQUAL).append(s)
              .append(PARAM);
        }
        params.put(s + PARAM, value);
        if (value instanceof Long) {
          declaration.append(CharacterConstants.COMMA).append(LONG).append(s).append(PARAM);
        } else if (value instanceof String) {
          declaration.append(CharacterConstants.COMMA).append(STRING).append(s).append(PARAM);
        } else if (value instanceof Integer) {
          declaration.append(CharacterConstants.COMMA).append(INTEGER).append(s).append(PARAM);
        } else {
          xLogger.severe("Counter error: Unhandled type :{0} [{1}] for table {2}", value.getClass(),
              value, keys.get(TABLE_NAME));
          return 0;
        }
      }
      q.setResult(QueryConstants.ROW_COUNT);
      q.setFilter(filter.toString());
      q.declareParameters(declaration.toString());
      if (variables.length() > 0) {
        q.declareVariables(variables.toString());
      }
      q.declareImports(imports.toString());
      return ((Long) q.executeWithMap(params)).intValue();
    } catch (Exception e) {
      xLogger.severe("Error in fetching counter for table {0}:{1}", keys.get(TABLE_NAME),
          e.getMessage(), e);
    } finally {
      if (q != null) {
        try {
          q.closeAll();
        } catch (Exception ignored) {

        }
      }
      pm.close();
    }

    return 0;
  }

  private boolean isValueValid(Object o) {
    return o != null && StringUtils.isNotEmpty(String.valueOf(o));
  }

  @Override
  public void increment(int amount) {
  }

  @Override
  public void increment(int amount, PersistenceManager pm) {
  }

  @Override
  public boolean delete(PersistenceManager pm) {
    return true;
  }

  @Override
  public boolean delete() {
    return true;
  }
}
