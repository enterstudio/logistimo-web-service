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
package com.logistimo.config.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.services.Resources;
import com.logistimo.utils.JsonUtil;
import com.logistimo.utils.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * Represents the configuration of a given event
 *
 * @author Arun
 */
public class EventSpec implements Serializable {


  private static final long serialVersionUID = 1L;

  private Map<String, ParamSpec> paramSpecs = new HashMap<String, ParamSpec>();

  public EventSpec(JSONArray json) throws JSONException {
    if (json == null) {
      throw new JSONException("Invalid JSON");
    }
    for (int i = 0; i < json.length(); i++) {
      ParamSpec paramSpec = new ParamSpec(json.getJSONObject(i));
      paramSpecs.put(ParamSpec.createKey(paramSpec.getParams()), paramSpec);
    }
  }

  public JSONArray toJSONObject() throws JSONException {
    JSONArray json = new JSONArray();
    Iterator<String> it = paramSpecs.keySet().iterator();
    while (it.hasNext()) {
      String key = it.next();
      json.put(paramSpecs.get(key).toJSONObject());
    }
    return json;
  }


  public Map<String, ParamSpec> getParamSpecs() {
    return paramSpecs;
  }

  public ParamSpec getFirstParamSpec() {
    if (paramSpecs == null || paramSpecs.isEmpty()) {
      return null;
    }
    return paramSpecs.values().iterator().next();
  }

  public ParamSpec getParamSpec(Map<String, Object> params) {
    ParamSpec paramSpec = paramSpecs.get(ParamSpec.createKey(params));
    if (paramSpec
        == null) // check if a spec. with no params. exists; if so, then it matches anything
    {
      paramSpec = paramSpecs.get(ParamSpec.createKey(null));
    }
    return paramSpec;
  }

  public String getParamValue(String paramName) { // get the first specs. param. value
    ParamSpec paramSpec = getFirstParamSpec();
    if (paramSpec == null) {
      return null;
    }
    Map<String, Object> params = paramSpec.getParams();
    if (params == null || params.isEmpty()) {
      return null;
    }
    return (String) params.get(paramName);
  }

  // Match an event's parameters against what is configured; the matched paramSpec is returned or null, if no matches
  public ParamSpec matchEvent(Map<String, Object> params, ParamComparator comparator) {
    ParamSpec paramSpec = null;
    if (comparator == null) {
      paramSpec = getParamSpec(params); // do a param-value equals comparison (via a keyed hash)
    } else if (paramSpecs != null && !paramSpecs.isEmpty()) {
      Iterator<ParamSpec> it = paramSpecs.values().iterator();
      while (it.hasNext()) {
        ParamSpec pSpec = it.next();
        Map<String, Object> params2 = pSpec.getParams();
        if (params2 == null || params2.isEmpty()) { // matches anything
          paramSpec = pSpec;
          break;
        }
        if (params2 != null && !params2.isEmpty()) {
          if (comparator.compare(params, params2)) {
            paramSpec = pSpec;
            break;
          }
        }
      }
    }
    return paramSpec;
  }

  public static interface ParamComparator {
    // params1 are the parameters of the event that occurred
    // params2 should be the parameters of the event as specified/configured
    public boolean compare(Map<String, Object> params1, Map<String, Object> params2);
  }

  public static class ParamSpec implements Serializable {

    private static final long serialVersionUID = 1L;
    // JSON Tags
    private static final String NAME = "name";
    private static final String PARAMS = "params";
    private static final String MSG_TEMPLATE = "message";
    private static final String NOTIFY_OPTIONS = "notifyoptions";
    private static final String BB_OPTIONS = "bboptions"; // bulletin board options

    private String name = null; // fully qualified, unique (displayable) name of this param. spec.
    private Map<String, Object> params = null; // Object can be String, List or a Map
    private String msgTemplate = null;
    private Map<String, NotifyOptions>
        notifyOptions =
        new HashMap<String, NotifyOptions>();
    // map of subscriber-type --> NotifyOptions for that type of user
    private BBOptions bbOptions = null; // bulletin-board options

    @SuppressWarnings("unchecked")
    public ParamSpec(JSONObject json) throws JSONException {
      name = json.getString(NAME);
      Map<String, Object> paramsMap = JsonUtil.toMap(json.optJSONObject(PARAMS));
      if (paramsMap != null && !paramsMap.isEmpty()) {
        params = new TreeMap<String, Object>(paramsMap);
      }
      msgTemplate = json.getString(MSG_TEMPLATE);
      JSONObject notifyOptionsJson = json.optJSONObject(NOTIFY_OPTIONS);
      if (notifyOptionsJson != null) {
        Iterator<String> keys = notifyOptionsJson.keys();
        while (keys.hasNext()) {
          String subscriberType = keys.next();
          notifyOptions.put(subscriberType,
              new NotifyOptions(subscriberType, notifyOptionsJson.getJSONObject(subscriberType)));
        }
      }
      JSONObject bbjson = json.optJSONObject(BB_OPTIONS);
      if (bbjson != null) {
        bbOptions = new BBOptions(bbjson);
      }
    }

    public static String createKey(Map<String, Object> params) {
      String key = "*";
      if (params != null && !params.isEmpty()) {
        Iterator<Entry<String, Object>> it = params.entrySet().iterator();
        while (it.hasNext()) {
          Entry<String, Object> entry = it.next();
          key += "." + entry.getKey() + "=" + entry.getValue().toString();
        }
      }
      return key;
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(NAME, name);
      if (params != null && !params.isEmpty()) {
        json.put(PARAMS, JsonUtil.toJSON(params));
      }
      json.put(MSG_TEMPLATE, msgTemplate);
      if (notifyOptions != null && !notifyOptions.isEmpty()) {
        JSONObject notifyOptionsJson = new JSONObject();
        Iterator<String> subscriberTypes = notifyOptions.keySet().iterator();
        while (subscriberTypes.hasNext()) {
          String subscriberType = subscriberTypes.next();
          notifyOptionsJson.put(subscriberType, notifyOptions.get(subscriberType).toJSONObject());
        }
        json.put(NOTIFY_OPTIONS, notifyOptionsJson);
      }
      if (bbOptions != null) {
        json.put(BB_OPTIONS, bbOptions.toJSONObject());
      }
      return json;
    }

    public String getName() {
      return name;
    }

    public Map<String, Object> getParams() {
      return params;
    }

    public String getMessageTemplate() {
      return msgTemplate;
    }

    public boolean isRealTime() {
      // Check notify options first
      if (notifyOptions != null && !notifyOptions.isEmpty()) {
        Iterator<NotifyOptions> it = notifyOptions.values().iterator();
        while (it.hasNext()) {
          if (it.next().isRealTime()) {
            return true;
          }
        }
      }
      // Is real-time, even if BB post is enabled
      if (bbOptions != null) {
        return bbOptions.post;
      }
      return false;
    }

    public long getEtaMillis() {
      long etaMillis = -1L;
      if (params != null) {
        String eta = (String) params.get("remindminsafter");
        if (eta != null && StringUtil.isStringLong(eta)) {
          //ETA(millisecond) - Time when event need to triggered. Input data will be simply minutes, setting event execution time to minutes from now.
          long etaMins = Long.parseLong(eta);
          if (etaMins > 0) {
            etaMillis = System.currentTimeMillis() + etaMins * 60 * 1000;
          }
        }
      }

      return etaMillis;
    }

    // Get all subscribers for a given frequency of notification
    public List<Subscriber> getSubscribers(int frequency) {
      if (notifyOptions.isEmpty()) {
        return null;
      }
      List<Subscriber> subscribers = new ArrayList<Subscriber>();
      Iterator<NotifyOptions> it = notifyOptions.values().iterator();
      while (it.hasNext()) {
        NotifyOptions options = it.next();
        if (options.frequency == frequency) {
          subscribers.add(options.subscriber);
        }
      }
      return subscribers;
    }

    // Get a map of fequency to subscribers
    public Map<Integer, List<Subscriber>> getSubcribers() {
      if (notifyOptions.isEmpty()) {
        return null;
      }
      Map<Integer, List<Subscriber>> subscriberMap = new HashMap<Integer, List<Subscriber>>();
      Iterator<NotifyOptions> it = notifyOptions.values().iterator();
      while (it.hasNext()) {
        NotifyOptions options = it.next();
        List<Subscriber> subscribers = subscriberMap.get(options.frequency);
        if (subscribers == null) {
          subscribers = new ArrayList<Subscriber>();
          subscriberMap.put(options.frequency, subscribers);
        }
        if (!subscribers.contains(options.subscriber)) {
          subscribers.add(options.subscriber);
        }
      }
      return subscriberMap;
    }

    public NotifyOptions getNotifyOptions(String subscriberType) {
      return notifyOptions.get(subscriberType);
    }

    public void putNotifyOptions(String subscriberType, NotifyOptions options) {
      notifyOptions.put(subscriberType, options);
    }

    public void removeNotifyOptions(String subscriberType) {
      notifyOptions.remove(subscriberType);
    }

    public BBOptions getBBOptions() {
      return bbOptions;
    }

    public void setBBOptions(BBOptions bbOptions) {
      this.bbOptions = bbOptions;
    }

    @Override
    public boolean equals(Object paramSpec) {
      if (paramSpec == null) {
        return false;
      }
      Map<String, Object> params2 = ((ParamSpec) paramSpec).getParams();
      if (params == null && params2 == null) {
        return true;
      }
      if (params != null) {
        return params.equals(params2);
      }
      return false;
    }
  }

  public static class NotifyOptions implements Serializable {

    private static final long serialVersionUID = 1L;
    // JSON tags
    private static final String FREQUENCY = "frequency";
    private static final String METHOD = "method";
    private static final String SMS_NOTIFY = "smsnotify";
    private static final String REPEAT_OPTIONS = "repeatoptions";
    public static int IMMEDIATE = 0;
    public static int DAILY = 1;
    public static int WEEKLY = 2;
    public static int MONTHLY = 3;
    private Subscriber subscriber = null;
    private int frequency = DAILY;
    private String method = null; // MessageService.SMS or .EMAIL
    private boolean
        smsNotify =
        false;
    // additionally notify via SMS, if the method was MessageService.EMAIL
    private RepeatOptions repeatOptions = null;

    public NotifyOptions(String subscriberType, JSONObject json) throws JSONException {
      String idsStr = json.optString(Subscriber.IDS);
      subscriber = new Subscriber(subscriberType, StringUtil.getList(idsStr));
      frequency = json.getInt(FREQUENCY);
      method = json.getString(METHOD);
      smsNotify = json.optBoolean(SMS_NOTIFY);
      JSONObject repeatOptionsJson = json.optJSONObject(REPEAT_OPTIONS);
      if (repeatOptionsJson != null) {
        repeatOptions = new RepeatOptions(repeatOptionsJson);
      }
    }

    public static String getFrequencyDisplay(int frequency, Locale locale) {
      ResourceBundle messages = Resources.get().getBundle("Messages", locale);
      String key = null;
      if (frequency == IMMEDIATE) {
        key = "immediately";
      } else if (frequency == DAILY) {
        key = "daily";
      } else if (frequency == WEEKLY) {
        key = "weekly";
      } else if (frequency == MONTHLY) {
        key = "monthly";
      }
      if (key == null) {
        return "";
      } else {
        try {
          return messages.getString(key);
        } catch (Exception e) {
          e.printStackTrace();
          return "";
        }
      }
    }

    public Subscriber getSubscriber() {
      return subscriber;
    }

    public int getFrequency() {
      return frequency;
    }

    public String getMethod() {
      return method;
    }

    public boolean isSmsNotify() {
      return smsNotify;
    }

    public RepeatOptions getRepeatOptions() {
      return repeatOptions;
    }

    public boolean isRealTime() {
      return (frequency == IMMEDIATE);
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      if (subscriber.ids != null && !subscriber.ids.isEmpty()) {
        json.put(Subscriber.IDS, StringUtil.getCSV(subscriber.ids));
      }
      json.put(FREQUENCY, frequency);
      json.put(METHOD, method);
      if (smsNotify) {
        json.put(SMS_NOTIFY, smsNotify);
      }
      if (repeatOptions != null) {
        json.put(REPEAT_OPTIONS, repeatOptions.toJSONObject());
      }
      return json;
    }
  }

  public static class Subscriber implements Serializable {
    // Subscriber types
    public static final String CUSTOMERS = "customers";
    public static final String VENDORS = "vendors";
    public static final String ADMINISTRATORS = "admins";
    public static final String CREATOR = "creator";
    public static final String POOLGROUP = "poolgroup"; // a poolgroup (of entity owners)
    public static final String GROUP = "group"; // a generic group of users
    public static final String USERS = "users"; // a user ID as defined in the system
    public static final String EMAILS = "emails"; // an email
    public static final String PHONES = "phones"; // a phone number
    public static final String ASSET_USERS = "asset_users";
    public static final String USER_TAGS = "user_tags";
    private static final long serialVersionUID = 1L;
    // JSON tags
    private static final String IDS = "ids";

    public String type = null;
    public List<String>
        ids =
        null;
    // optional list of user Ids (for TYPE_USER), or email addresses (for TYPE_EMAIL) or phone numbers (for TYPE_PHONE)

    public Subscriber(String type, List<String> ids) {
      this.type = type;
      this.ids = ids;
    }

    public Subscriber(String subscriberKey) {
      int index = subscriberKey.indexOf(":");
      if (index == -1) {
        type = subscriberKey;
      } else {
        type = subscriberKey.substring(0, index);
        if ((index + 1) <= subscriberKey.length()) {
          ids = new ArrayList<String>();
          ids.add(subscriberKey.substring(index + 1));
        }
      }
    }

    public List<String> getKeys() {
      List<String> keys = new ArrayList<String>();
      if (ids == null || ids.isEmpty()) {
        keys.add(type);
      } else {
        Iterator<String> it = keys.iterator();
        keys.add(type + ":" + it.next());
      }
      return keys;
    }

    @Override
    public boolean equals(Object o) {
      if (type.equals(((Subscriber) o).type)) {
        if (ids != null) {
          return (ids.equals(((Subscriber) o).ids));
        } else {
          return true;
        }
      }
      return false;
    }
  }

  public static class RepeatOptions implements Serializable {

    private static final long serialVersionUID = 1L;
    // JSON tags
    private static final String REPEAT_FREQUENCY = "frequency";
    private static final String REPEAT_LIMIT = "limit";
    private static final String REPEAT_BEFORE = "before";

    private int frequency = 0; // days
    private int limit = 0; // no. of times to repeat sending an alert
    private boolean before = false; // repeat above frequency before or after the event

    public RepeatOptions(int repeatFrequency, int repeatLimit, boolean repeatBefore) {
      this.frequency = repeatFrequency;
      this.limit = repeatLimit;
      this.before = repeatBefore;
    }

    public RepeatOptions(JSONObject json) throws JSONException {
      frequency = json.getInt(REPEAT_FREQUENCY);
      limit = json.getInt(REPEAT_LIMIT);
      before = json.getBoolean(REPEAT_BEFORE);
    }

    public int getFrequency() {
      return frequency;
    }

    public int getLimit() {
      return limit;
    }

    public boolean getBefore() {
      return before;
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(REPEAT_FREQUENCY, frequency);
      json.put(REPEAT_LIMIT, limit);
      json.put(REPEAT_BEFORE, before);
      return json;
    }
  }

  // Bulletin-board options
  public static class BBOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String POST = "post";
    private static final String TAGS = "tags";

    public boolean post = false;
    public List<String> tags = null;

    public BBOptions(boolean post, List<String> tags) {
      this.post = post;
      this.tags = tags;
    }

    public BBOptions(JSONObject json) throws JSONException {
      post = json.getBoolean(POST);
      tags = StringUtil.getList(json.optString(TAGS));
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(POST, post);
      if (tags != null && !tags.isEmpty()) {
        json.put(TAGS, StringUtil.getCSV(tags));
      }
      return json;
    }
  }
}
