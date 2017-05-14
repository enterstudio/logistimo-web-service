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
package com.logistimo.events.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.logistimo.accounting.entity.Account;
import com.logistimo.assets.entity.AssetStatus;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.inventory.entity.Invntry;
import com.logistimo.inventory.entity.Transaction;
import com.logistimo.materials.entity.Material;
import com.logistimo.orders.entity.Order;
import com.logistimo.users.entity.UserAccount;

import com.logistimo.domains.IMultiDomain;
import com.logistimo.services.Resources;
import com.logistimo.services.impl.PMF;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.utils.JsonUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * Represents an event of any type.
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Event implements IEvent {

  // Logger
  private static final XLog xLogger = XLog.getLog(Event.class);

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long ky;
  @Persistent(table = "EVENT_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain Id
  @Persistent
  private Integer id;
  @Persistent
  private String prms; // any event parameters (a JSON key value pair)
  @Persistent
  private String
      oty;
  // object type or entity class name (i.e. the object's class name associated with this event)
  @Persistent
  private String oId; // object id of object in question (encoded Key string)
  @Persistent
  @Column(length = 400)
  private String msg; // message or other custom info. (not indexed by default)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String uids; // CSV of user IDs to be notified, in case explicitly specified
  @Persistent
  private Date t; // time of event
  @Persistent
  private Boolean rt = false; // whether real-time notification is required
  @Persistent
  private Integer st = STATUS_CREATED; // status of event
  @Persistent
  private Long etaMillis = -1L;

  public Event() {
  }

  public static Map<String, Object> getParamsMap(String paramsStringJson) {
    if (paramsStringJson == null || paramsStringJson.isEmpty()) {
      return null;
    }
    try {
      return JsonUtil.toMap(new JSONObject(paramsStringJson));
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String getParamsString(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return null;
    }
    try {
      return JsonUtil.toJSON(params).toString();
    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String getEventName(int eventId, Locale locale) {
    switch (eventId) {
      case CREATED:
        return "created";
      case MODIFIED:
        return "modified";
      case DELETED:
        return "deleted";
      case NOT_CREATED:
        return "not created";
      case EXPIRED:
        return "expired";
      case NO_ACTIVITY:
        return "no activity";
      case FULFILLMENT_DUE:
        return "fulfillment due";
      case STATUS_CHANGE:
        return "status changed";
      case STOCKOUT:
        return "zero stock"; // "stockout";
      case STOCK_REPLENISHED:
        return "stock replenished";
      case UNDERSTOCK:
        return "< min.";
      case OVERSTOCK:
        return "> max.";
      case STOCKCOUNT_EXCEEDS_THRESHOLD:
        return "stockcount exceeds threshold";
      case STOCK_COUNTED:
        return "stock counted";
      case STOCK_ISSUED:
        return "issued";
      case STOCK_RECEIVED:
        return "received";
      case STOCK_WASTED:
        return "wasted";
      case STOCK_TRANSFERRED:
        return "transferred";
      case CREDIT_LIMIT_EXCEEDED:
        return "> credit-limit";
      case PAYMENT_DUE:
        return "payment due";
      case PAID:
        return "paid";
      case HIGH_EXCURSION:
        return "high excursion";
      case LOW_EXCURSION:
        return "low excursion";
      case INCURSION:
        return "incursion";
      case NO_DATA_FROM_DEVICE:
        return "no data from device";
      case HIGH_WARNING:
        return "high warning";
      case LOW_WARNING:
        return "low warning";
      case HIGH_ALARM:
        return "high alarm";
      case LOW_ALARM:
        return "low alarm";
      case SENSOR_DISCONNECTED:
        return "sensor disconnected";
      case SENSOR_CONNECTION_NORMAL:
        return "sensor connected";
      case BATTERY_ALARM:
        return "battery alarm";
      case BATTERY_LOW:
        return "battery low";
      case BATTERY_NORMAL:
        return "battery normal";
      case ASSET_INACTIVE:
        return "asset inactive";
      case ASSET_ACTIVE:
        return "asset active";
      case POWER_OUTAGE:
        return "power outage";
      case POWER_NORMAL:
        return "power available";
      case DEVICE_DISCONNECTED:
        return "device disconnected";
      case DEVICE_CONNECTION_NORMAL:
        return "device connected";
      default:
        return "Unknown";
    }
  }

  public static String getObjectType(String oty, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    if (Order.class.getName().equals(oty)) {
      return messages.getString("order");
    } else if (Invntry.class.getName().equals(oty)) {
      return messages.getString("inventory");
    } else if (Transaction.class.getName().equals(oty)) {
      return messages.getString("transaction");
    } else if (UserAccount.class.getName().equals(oty)) {
      return messages.getString("user");
    } else if (Material.class.getName().equals(oty)) {
      return messages.getString("material");
    } else if (Kiosk.class.getName().equals(oty)) {
      return messages.getString("kiosk");
    } else if (Account.class.getName().equals(oty)) {
      return messages.getString("accounting");
    } else if (AssetStatus.class.getName().equals(oty)) {
      return messages.getString("assets");
    } else {
      return "Unknown";
    }
  }

  public IEvent init(Long dId, int id, Map<String, Object> params, String objectType,
                     String objectId, boolean isRealTime, String message,
                     Map<Integer, List<String>> userIds, long etaMillis) {
    return init(dId, id, params, objectType, objectId, isRealTime, message, userIds, etaMillis,
        new Date());
  }

  public IEvent init(Long dId, int id, Map<String, Object> params, String objectType,
                     String objectId, boolean isRealTime, String message,
                     Map<Integer, List<String>> userIds, long etaMillis, Date eventTime) {
    setDomainId(dId);
    this.id = new Integer(id);
    this.prms = getParamsString(params);
    this.oty = objectType;
    this.oId = objectId;
    this.rt = isRealTime;
    this.t = eventTime;
    this.etaMillis = etaMillis;
    setMessage(message);
    setUserIds(userIds);
    // Set the superdomains for this object
    try {
      Object obj = getEventObject();
      if (obj != null && IMultiDomain.class.isAssignableFrom(obj.getClass())) {
        IMultiDomain mObj = (IMultiDomain) obj;
        this.dId = mObj.getDomainIds();
        this.sdId = dId;
      } else {
        DomainsUtil.addToDomain(this, dId, null);
      }
    } catch (Exception e) {
      xLogger.severe(
          "{0} when adding event with ID {1} on object {2}-{3} in source domain {4} to multiple domains: {5}",
          e.getClass().getName(), id, oty, oId, dId, e.getMessage());
    }
    return this;
  }

  @Override
  public Long getKey() {
    return ky;
  }

  @Override
  public int getId() {
    return NumberUtil.getIntegerValue(id);
  }

  @Override
  public void setId(int id) {
    this.id = new Integer(id);
  }

  @Override
  public Map<String, Object> getParams() {
    return getParamsMap(prms);
  }

  @Override
  public void setParams(Map<String, Object> params) {
    this.prms = getParamsString(params);
  }

  public Long getDomainId() {
    return sdId;
  }

  public void setDomainId(Long domainId) {
    sdId = domainId;
  }

  public List<Long> getDomainIds() {
    return dId;
  }

  public void setDomainIds(List<Long> domainIds) {
    this.dId.clear();
    this.dId.addAll(domainIds);
  }

  public void addDomainIds(List<Long> domainIds) {
    if (domainIds == null || domainIds.isEmpty()) {
      return;
    }
    if (this.dId == null) {
      this.dId = new ArrayList<>();
    }
    for (Long dId : domainIds) {
      if (!this.dId.contains(dId)) {
        this.dId.add(dId);
      }
    }
  }

  public void removeDomainId(Long domainId) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.remove(domainId);
    }
  }

  public void removeDomainIds(List<Long> domainIds) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.removeAll(domainIds);
    }
  }

  @Override
  public String getObjectType() {
    return oty;
  }

  @Override
  public void setObjectType(String objectType) {
    this.oty = objectType;
  }

  @Override
  public String getObjectId() {
    return oId;
  }

  @Override
  public void setObjectId(String oId) {
    this.oId = oId;
  }

  @Override
  public String getMessage() {
    return msg;
  }

  @Override
  public void setMessage(String message) {
    this.msg = message;
  }

  // Get user Ids by frequency of notification
  @Override
  @SuppressWarnings("unchecked")
  public Map<Integer, List<String>> getUserIds() {
    if (uids == null || uids.isEmpty()) {
      return null;
    }
    Map<Integer, List<String>> userIdsMap = new HashMap<Integer, List<String>>();
    try {
      JSONObject json = new JSONObject(uids);
      Iterator<String> keys = json.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        Integer freq = Integer.valueOf(key);
        List<String> userIds = JsonUtil.toList(json.getJSONArray(key));
        if (userIds != null && !userIds.isEmpty()) {
          userIdsMap.put(freq, userIds);
        }
      }
    } catch (JSONException e) {
      xLogger.severe("JSONException when getting userIds {0} from event {1}:{2} in domain {2}: {3}",
          uids, oty, id, getDomainId(), e.getMessage());
      return null;
    }
    return userIdsMap;
  }

  @Override
  public void setUserIds(Map<Integer, List<String>> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      uids = null;
      return;
    }
    try {
      JSONObject json = new JSONObject();
      Iterator<Entry<Integer, List<String>>> it = userIds.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, List<String>> entry = it.next();
        Integer freq = entry.getKey();
        List<String> uIds = entry.getValue();
        if (uIds != null && !uIds.isEmpty()) {
          json.put(String.valueOf(freq), JsonUtil.toJSON(uIds));
        }
      }
      uids = json.toString();
    } catch (JSONException e) {
      xLogger.severe("JSONException when getting userIds {0} from event {1}:{2} in domain {2}: {3}",
          uids, oty, id, getDomainId(), e.getMessage());
    }
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date t) {
    this.t = t;
  }

  @Override
  public int getStatus() {
    return NumberUtil.getIntegerValue(st);
  }

  @Override
  public void setStatus(int status) {
    this.st = new Integer(status);
  }

  @Override
  public boolean isRealTime() {
    return (rt != null && rt.booleanValue());
  }

  @Override
  public void setRealTime(boolean isRealTime) {
    rt = isRealTime;
  }

  @Override
  public Long getEtaMillis() {
    return etaMillis;
  }

  @Override
  public void setEtaMillis(Long etaMillis) {
    this.etaMillis = etaMillis;
  }

  @Override
  public boolean hasCustomOptions() {
    String msg = getMessage();
    return ((msg != null && !msg.isEmpty()) || (uids != null && !uids.isEmpty()));
  }



  // Get the object on which this event occurred - e.g. inventory, order, material, entity, useraccount, and so on
  @Override
  public Object getObject(PersistenceManager pm) {
    if (oty == null || oty.isEmpty() || oId == null || oId.isEmpty()) {
      return null;
    }
    boolean closePm = false;
    if (pm == null) {
      pm = PMF.get().getPersistenceManager();
      closePm = true;
    }
    try {
      return pm.getObjectById(Class.forName(oty), oId);
    } catch (Exception e) {
      xLogger
          .warn("{0} when getting object of type {1} of event with oId {2}", e.getClass().getName(),
              oty, oId);
      return null;
    } finally {
      if (closePm) {
        pm.close();
      }
    }
  }

  private Object getEventObject() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Get the object associated with the event
      if (oId != null) {
        Object o = pm.getObjectById(Class.forName(oty), oId);
        return pm.detachCopy(o);
      }
    } catch (Exception e) {
      xLogger.warn("{0} when getting object {3} in domain {4}: {5} for  event {1}:{2}",
          e.getClass().getName(), oty, id, oId, dId, e.getMessage());
    } finally {
      pm.close();
    }
    return null;
  }

}