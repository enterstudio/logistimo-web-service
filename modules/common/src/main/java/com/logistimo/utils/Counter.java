package com.logistimo.utils;

import com.logistimo.AppFactory;
import com.logistimo.dao.JDOUtils;

import com.logistimo.entities.entity.IUserToKiosk;

import com.logistimo.models.ICounter;
import com.logistimo.constants.QueryConstants;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.jdo.Query;

/**
 * @author Mohan Raja
 */
public class Counter {

  private static final String TABLE_NAME = "TN";
  private static final String TAGS = "tgs";
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

  public static ICounter getKioskCounter(Long domainId, String tag) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "kiosks");
    map.put(TAGS, tag);
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

  public static ICounter getOrderCounter(Long domainId, String tag, Integer ordType) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(OBJECT_TYPE, "orders");
    map.put(K_TAGS, tag);
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
