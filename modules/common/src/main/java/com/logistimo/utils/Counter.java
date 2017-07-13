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

package com.logistimo.utils;

import com.logistimo.AppFactory;

import com.logistimo.models.ICounter;
import com.logistimo.constants.QueryConstants;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.jdo.Query;

/**
 * @author Mohan Raja
 */
public class Counter {

  private static final String TABLE_NAME = "TN";
  private static final String TAGS = "tgs";
  private static final String EXCLUDED_TAGS = "extgs";
  private static final String K_ID = "kId";
  private static final String TK_ID = "tkId";
  private static final String USER_ID = "userId";
  private static final String KIOSK_ID = "kioskId";
  private static final String LINK_TYPE = "linkType";
  private static final String ORDER_TYPE = "otype";
  private static final String K_TAGS = "ktgs";
  private static final String SD_ID = "sdId";
  private static final String LINKED_KIOSK_NAME = "lknm";
  private static final String ORD_TYPE = "oty";
  public static final String OBJECT_TYPE = "objType";
  private static final String O_TAGS = "otgs";
  private static final String TYPE_ENTITY = "en";
  private static final String TYPE_ORDER = "or";

  private static ICounter getInstance(Long domainId, Map<String, Object> keys) {
    return AppFactory.get().getCounter().init(domainId, keys);
  }

  public static ICounter getInstance(Long domainId, String name) {
    return AppFactory.get().getCounter().init(domainId, name);
  }

  public static ICounter getUserCounter(Long domainId) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "users");
    return getInstance(domainId, map);
  }

  public static ICounter getKioskCounter(Long domainId) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "kiosks");
    return getInstance(domainId, map);
  }

  public static ICounter getDomainKioskCounter(Long domainId) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(SD_ID, domainId);
    map.put(OBJECT_TYPE, "kiosks");
    return getInstance(domainId, map);
  }

  public static ICounter getKioskCounter(Long domainId, String tag, String exTag) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "kiosks");
    if(StringUtils.isNotEmpty(tag)){
      map.put(TAGS, tag);
    }else if(StringUtils.isNotEmpty(exTag)){
      map.put(EXCLUDED_TAGS, exTag);
    }
    return getInstance(domainId, map);
  }

  public static ICounter getMaterialCounter(Long domainId, String tag) {
    return getMaterialCounter(domainId, null, tag);
  }

  public static ICounter getMaterialCounter(Long domainId, Long kioskId, String tag) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "materials");
    map.put(K_ID, kioskId);
    map.put(TAGS, tag);
    return getInstance(domainId, map);
  }

  public static ICounter getHandlingUnitCounter(Long domainId) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "handlingunits");
    return getInstance(domainId, map);
  }

  public static ICounter getUserToKioskCounter(Long domainId, String userId) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "userkiosks");
    map.put(USER_ID, userId);
    return getInstance(domainId, map);
  }

  public static ICounter getKioskLinkCounter(Long domainId, Long kioskId, String linkType,
                                             String q) {
    throw new UnsupportedOperationException("This method is no longer supported");
  }

  public static ICounter getOrderCounter(Long domainId, Long kioskId, String otype,
                                         Integer ordType) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "orders");
    map.put(ORDER_TYPE, otype);
    map.put(K_ID, kioskId);
    map.put(ORD_TYPE, ordType);
    return getInstance(domainId, map);
  }

  public static ICounter getOrderCounter(Long domainId, String tag, Integer ordType,  String... tgType) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "orders");
    if (tgType != null && tgType.length > 0) {
      switch (tgType[0]) {
        case TYPE_ORDER:
          map.put(O_TAGS, tag);
          break;
        default:
          map.put(K_TAGS, tag);
          break;
      }
    } else {
      map.put(K_TAGS, tag);
    }
    map.put(ORD_TYPE, ordType);
    return getInstance(domainId, map);
  }

  public static int getCount(Query query, Object o1, Object o2) {
    return getCnt(query, o1, o2, null);
  }

  public static int getCountByMap(Query query, Map map) {
    return getCnt(query, null, null, map);
  }

  private static int getCnt(Query query, Object o1, Object o2, Map map) {
    if (query == null) {
      return -1;
    }
    query.setResult(QueryConstants.COUNT);
    query.setRange(null);
    query.setOrdering(null);
    Long cnt;
    if (map != null) {
      cnt = (Long) query.executeWithMap(map);
    } else {
      cnt = (Long) query.execute(o1, o2);
    }
    return Integer.parseInt(String.valueOf(cnt));

  }

  public static ICounter getTransferOrderCounter(Long domainId, Long entityId, String otype) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "orders");
    map.put(K_ID, entityId);
    map.put(ORD_TYPE, 0);
    map.put(ORDER_TYPE, otype);
    return getInstance(domainId, map);
  }
}
