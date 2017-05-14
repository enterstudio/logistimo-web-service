package com.logistimo.api.builders;

import com.logistimo.api.constants.ConfigConstants;
import com.logistimo.api.constants.NotificationConstants;
import com.logistimo.api.models.configuration.NotificationsConfigModel;
import com.logistimo.api.models.configuration.NotificationsModel;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by naveensnair on 05/12/14.
 */
public class NotificationBuilder {

  public String buildModel(NotificationsConfigModel model, String eventConfigJson)
      throws ServiceException {
    String message = "";
    String notifMessage;
    if (model != null) {
      notifMessage = model.or;
      if (StringUtils.isNotEmpty(model.os) && StringUtils.isNotEmpty(model.pr)) {
        notifMessage += " (" + model.os + " = " + model.pr + ")";
      }
      if (StringUtils.isNotEmpty(model.ist) && StringUtils.isNotEmpty(model.mst)) {
        notifMessage += " [" + model.ist + " = " + model.mst + "]";
      }

      JSONObject jsonObject = new JSONObject();
      JSONObject jsonFinalObject = new JSONObject();
      JSONArray jsonFinal = new JSONArray();
      try {
        jsonObject.put("name", notifMessage);
        JSONObject json = buildParams(model);
        if (json != null) {
          jsonObject.put("params", json);
        }
        JSONObject jsonb = buildBBParam(model);
        if (jsonb != null) {
          jsonObject.put("bboptions", jsonb);
        }
        jsonObject.put("message", model.mt);
        jsonObject.put("notifyoptions", buildNotifyOptions(model));
        jsonFinal.put(jsonObject);
        jsonFinalObject.put(model.id, jsonFinal);
        if (StringUtils.isNotEmpty(eventConfigJson)) {
          message = buildJson(jsonFinalObject, eventConfigJson, true);
        } else {
          message = jsonFinalObject.toString();
        }
      } catch (JSONException e) {
        throw new ServiceException("Error while building JSON object for notifications", e);
      }
    }
    return message;
  }

  public JSONObject buildParams(NotificationsConfigModel model) {
    JSONObject jsonObject = null;
    try {
      if (StringUtils.isNotEmpty(model.nid) && StringUtils.isNotEmpty(model.pr)) {
        jsonObject = new JSONObject();
        jsonObject.put(model.nid, getStatus(model.pr));
      }
      if (StringUtils.isNotEmpty(model.ist) && StringUtils.isNotEmpty(model.mst)) {
        if (jsonObject == null) {
          jsonObject = new JSONObject();
        }
        jsonObject.put(model.ist, model.mst);
      }
      // Add material tags to exlcude and entity tags to exclude to the jsonObject.
      if (StringUtils.isNotEmpty(model.emt)) {
        if (jsonObject == null) {
          jsonObject = new JSONObject();
        }
        String[] emtArray = StringUtils.split(model.emt, ',');
        JSONArray emtJsonArray = new JSONArray();
        for (String value : emtArray) {
          emtJsonArray.put(value);
        }
        jsonObject.put("mtagstoexclude", emtJsonArray);
      }
      if (StringUtils.isNotEmpty(model.eet)) {
        if (jsonObject == null) {
          jsonObject = new JSONObject();
        }
        String[] eetArray = StringUtils.split(model.eet, ',');
        JSONArray eetJsonArray = new JSONArray();
        for (String value : eetArray) {
          eetJsonArray.put(value);
        }
        jsonObject.put("etagstoexclude", eetJsonArray);
      }
      if (StringUtils.isNotEmpty(model.eot)) {
        if (jsonObject == null) {
          jsonObject = new JSONObject();
        }
        String[] eotArray = StringUtils.split(model.eot, ',');
        JSONArray eotJsonArray = new JSONArray();
        for (String value : eotArray) {
          eotJsonArray.put(value);
        }
        jsonObject.put("otagstoexclude", eotJsonArray);
      }
    } catch (JSONException ignored) {
      // ignore
    }
    return jsonObject;
  }

  public JSONObject buildBBParam(NotificationsConfigModel model) {
    JSONObject jsonObject = null;
    try {
      if (model.bb) {
        jsonObject = new JSONObject();
        jsonObject.put("post", true);
        if (StringUtils.isNotEmpty(model.tags)) {
          jsonObject.put("tags", model.tags);
        }
      }
    } catch (JSONException ignored) {
      // ignore
    }
    return jsonObject;
  }

  public NotificationsModel buildNotifConfigModel(String config, String notifType, Long domainId,
                                                  Locale locale, String timezone)
      throws ServiceException, ObjectNotFoundException {
    NotificationsModel model = new NotificationsModel();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    List<String> val = dc.getDomainData(ConfigConstants.NOTIFICATIONS);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      try {
        UsersService as = Services.getService(UsersServiceImpl.class, locale);
        model.fn = as.getUserAccount(model.createdBy).getFullName();
      } catch (Exception e) {
        //ignore.. get for display only.
      }
    }
    model.config = buildNotificationsModel(config, notifType);
    return model;
  }

  public Map<String, List<NotificationsConfigModel>> buildNotificationsModel(String config,
                                                                             String notifType)
      throws ServiceException {

    List<String>
        orderKeyList =
        new ArrayList<String>(Arrays.asList(NotificationConstants.orderKeys));
    List<String>
            shipmentKeyList =
            new ArrayList<String>(Arrays.asList(NotificationConstants.shipmentKeys));
    List<String>
        inventoryKeysList =
        new ArrayList<String>(Arrays.asList(NotificationConstants.inventoryKeys));
    List<String>
        setupKeysList =
        new ArrayList<String>(Arrays.asList(NotificationConstants.setupKeys));
    List<String>
        temperatureKeysList =
        new ArrayList<String>(Arrays.asList(NotificationConstants.temperatureKeys));
    List<String>
        assetAlarmKeysList =
        new ArrayList<String>(Arrays.asList(NotificationConstants.assetAlarmKeys));
    List<String>
        accountKeysList =
        new ArrayList<String>(Arrays.asList(NotificationConstants.accountKeys));
    Map<String, List<NotificationsConfigModel>>
        notificationMap =
        new HashMap<String, List<NotificationsConfigModel>>();

    if (StringUtils.isNotEmpty(config)) {
      try {
        JSONObject jsonObject = new JSONObject(config);
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
          String key = keys.next();
          JSONArray jsonArray = (JSONArray) jsonObject.get(key);
          if (orderKeyList.contains(key) && notifType.equalsIgnoreCase("orders")) {
            notificationMap = buildModel("orders", key, jsonArray, notificationMap);
          } else if (shipmentKeyList.contains(key) && notifType.equalsIgnoreCase("shipments")) {
            notificationMap = buildModel("shipments", key, jsonArray, notificationMap);
          } else if (inventoryKeysList.contains(key) && notifType.equalsIgnoreCase("inventory")) {
            notificationMap = buildModel("inventory", key, jsonArray, notificationMap);
          } else if (setupKeysList.contains(key) && notifType.equalsIgnoreCase("setup")) {
            notificationMap = buildModel("setup", key, jsonArray, notificationMap);
          } else if (temperatureKeysList.contains(key) && notifType
              .equalsIgnoreCase("temperature")) {
            notificationMap = buildModel(notifType.toLowerCase(), key, jsonArray, notificationMap);
          } else if (accountKeysList.contains(key) && notifType.equalsIgnoreCase("accounts")) {
            notificationMap = buildModel("accounts", key, jsonArray, notificationMap);
          } else if (assetAlarmKeysList.contains(key) && notifType
              .equalsIgnoreCase("assetAlarms")) {
            notificationMap = buildModel(notifType.toLowerCase(), key, jsonArray, notificationMap);
          }
        }
      } catch (JSONException e) {
        throw new ServiceException("Error while building JSON object for notifications", e);
      }
    }
    return notificationMap;
  }

  public Map<String, List<NotificationsConfigModel>> buildModel(String notification, String Key,
                                                                JSONArray jsonArray,
                                                                Map<String, List<NotificationsConfigModel>> notificationMap)
      throws ServiceException {
    if (jsonArray != null) {
      String event = "";
      List<NotificationsConfigModel> notificationsConfigModelList = null;
      try {
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject jsonObject = jsonArray.getJSONObject(i);
          NotificationsConfigModel model = new NotificationsConfigModel();
          if (jsonObject != null) {
            String name = jsonObject.getString("name");
            if (name.contains("(") && name.contains("=")) {
              event = name.substring(0, name.indexOf('(') - 1); //model.or,model.inv,model.st
              model.os = name.substring(name.indexOf('(') + 1, name.indexOf('=')); //model.os
              JSONObject params = (JSONObject) jsonObject.get("params");
              if (params != null) {
                Iterator<String> it = params.keys();
                while (it.hasNext()) {
                  String key = it.next();
                  if (!(key.equalsIgnoreCase("etagstoexclude") || key
                      .equalsIgnoreCase("mtagstoexclude") || key
                      .equalsIgnoreCase("otagstoexclude"))) {
                    model.pr = retrieveStatus(params.get(key).toString()); //model.pr
                    model.nid = key; //model.nid
                  }
                  if (key.equalsIgnoreCase("mtagstoexclude")) {
                    JSONArray mtagsToExclude = params.getJSONArray(key);
                    for (int j = 0; j < mtagsToExclude.length(); j++) {
                      model.emt =
                          (model.emt == null ? mtagsToExclude.get(j).toString()
                              : model.emt + mtagsToExclude.get(j).toString());
                      if (j < mtagsToExclude.length() - 1) {
                        model.emt += ',';
                      }
                    }
                  }
                  if (key.equalsIgnoreCase("etagstoexclude")) {
                    JSONArray etagsToExclude = params.getJSONArray(key);
                    for (int j = 0; j < etagsToExclude.length(); j++) {
                      model.eet =
                          (model.eet == null ? etagsToExclude.get(j).toString()
                              : model.eet + etagsToExclude.get(j).toString());
                      if (j < etagsToExclude.length() - 1) {
                        model.eet += ',';
                      }
                    }
                  }
                  if (key.equalsIgnoreCase("otagstoexclude")) {
                    JSONArray otagsToExclude = params.getJSONArray(key);
                    for (int j = 0; j < otagsToExclude.length(); j++) {
                      model.eot =
                          (model.eot == null ? otagsToExclude.get(j).toString()
                              : model.eot + otagsToExclude.get(j).toString());
                      if (j < otagsToExclude.length() - 1) {
                        model.eot += ',';
                      }
                    }
                  }
                  if (key.equalsIgnoreCase("status") && !name.startsWith("Status changed")) {
                    model.ist = key;
                    model.mst = params.getString(key);
                  }
                }

              }
            } else {
              event = name; //model.or,model.inv,model.st
              JSONObject params = (JSONObject) jsonObject.opt("params");
              if (params != null) {
                Iterator<String> it = params.keys();
                while (it.hasNext()) {
                  String key = it.next();
                  if (key.equalsIgnoreCase("mtagstoexclude")) {
                    JSONArray mtagsToExclude = params.getJSONArray(key);
                    for (int j = 0; j < mtagsToExclude.length(); j++) {
                      model.emt =
                          (model.emt == null ? mtagsToExclude.get(j).toString()
                              : model.emt + mtagsToExclude.get(j).toString());
                      if (j < mtagsToExclude.length() - 1) {
                        model.emt += ',';
                      }
                    }
                  }
                  if (key.equalsIgnoreCase("etagstoexclude")) {
                    JSONArray etagsToExclude = params.getJSONArray(key);
                    for (int j = 0; j < etagsToExclude.length(); j++) {
                      model.eet =
                          (model.eet == null ? etagsToExclude.get(j).toString()
                              : model.eet + etagsToExclude.get(j).toString());
                      if (j < etagsToExclude.length() - 1) {
                        model.eet += ',';
                      }
                    }
                  }
                  if (key.equalsIgnoreCase("otagstoexclude")) {
                    JSONArray otagsToExclude = params.getJSONArray(key);
                    for (int j = 0; j < otagsToExclude.length(); j++) {
                      model.eot =
                          (model.eot == null ? otagsToExclude.get(j).toString()
                              : model.eot + otagsToExclude.get(j).toString());
                      if (j < otagsToExclude.length() - 1) {
                        model.eot += ',';
                      }
                    }
                  }
                  if (key.equalsIgnoreCase("status")) {
                    if (name.contains("[") && name.contains("=")) {
                      event = name.substring(0, name.indexOf('[') - 1);
                    }
                    model.ist = key;
                    model.mst = params.getString(key);
                  }
                }
              }
            }
            JSONObject notifyOptions = (JSONObject) jsonObject.opt("notifyoptions");
            JSONObject bbOptions = (JSONObject) jsonObject.opt("bboptions");
            retrieveNotifyOptions(notifyOptions, model);
            if (bbOptions != null) {
              retrieveBBOptions(bbOptions, model);
            }
          }
          if ("orders".equalsIgnoreCase(notification)) {
            model.or = event;
          }if ("shipments".equalsIgnoreCase(notification)) {
            model.ship = event;
          } else if ("inventory".equalsIgnoreCase(notification)) {
            model.inv = event;
          } else if ("setup".equalsIgnoreCase(notification)) {
            model.st = event;
          } else if ("temperature".equalsIgnoreCase(notification) || "assetAlarms"
              .equalsIgnoreCase(notification)) {
            model.temp = event;
          } else if ("accounts".equalsIgnoreCase(notification)) {
            model.acc = event;
          }
          model.mt = jsonObject != null ? jsonObject.get("message").toString() : null;
          if (notificationMap.containsKey(Key)) {
            notificationsConfigModelList = notificationMap.get(Key);
          } else {
            notificationsConfigModelList = new ArrayList<NotificationsConfigModel>();
          }
          notificationsConfigModelList.add(model);
          notificationMap.put(Key, notificationsConfigModelList);
        }
      } catch (JSONException e) {
        throw new ServiceException("Error while building JSON object for notifications", e);
      }
    }
    return notificationMap;
  }

  public void retrieveBBOptions(JSONObject bbOptions, NotificationsConfigModel model) {
    if (bbOptions != null) {
      try {
        model.bb = (Boolean) bbOptions.get("post");
        if (null != bbOptions.get("tags")) {
          model.tags = (String) bbOptions.get("tags");
        }
      } catch (JSONException ignored) {
        // ignore
      }
    }
  }

  public void retrieveNotifyOptions(JSONObject notifyOptions, NotificationsConfigModel model)
      throws ServiceException {
    if (notifyOptions != null) {
      try {
        Iterator<String> iterator = notifyOptions.keys();
        JSONObject jsonCustomers = null;
        JSONObject jsonVendors = null;
        JSONObject jsonAdmins = null;
        JSONObject jsonCreator = null;
        JSONObject jsonAssetUsers = null;
        JSONObject jsonUsers = null;
        JSONObject jsonUserTags = null;
        while (iterator.hasNext()) {
          String key = iterator.next();
          if ("customers".equalsIgnoreCase(key)) {
            jsonCustomers = (JSONObject) notifyOptions.get("customers");
          } else if ("vendors".equalsIgnoreCase(key)) {
            jsonVendors = (JSONObject) notifyOptions.get("vendors");
          } else if ("admins".equalsIgnoreCase(key)) {
            jsonAdmins = (JSONObject) notifyOptions.get("admins");
          } else if ("creator".equalsIgnoreCase(key)) {
            jsonCreator = (JSONObject) notifyOptions.get("creator");
          } else if ("users".equalsIgnoreCase(key)) {
            jsonUsers = (JSONObject) notifyOptions.get("users");
          } else if ("user_tags".equalsIgnoreCase(key)) {
            jsonUserTags = (JSONObject) notifyOptions.get("user_tags");
          } else if ("asset_users".equalsIgnoreCase(key)) {
            jsonAssetUsers = (JSONObject) notifyOptions.get("asset_users");
          }
        }
        if (jsonCustomers != null) {
          model.co = true;
          model.cot = jsonCustomers.get("frequency").toString();
        }
        if (jsonVendors != null) {
          model.vn = true;
          model.vnt = jsonVendors.get("frequency").toString();
        }
        if (jsonAdmins != null) {
          model.ad = true;
          model.adt = jsonAdmins.get("frequency").toString();
        }
        if (jsonCreator != null) {
          model.cr = true;
          model.crt = jsonCreator.get("frequency").toString();
        }
        if (jsonUsers != null) {
          model.usr = true;
          model.ust = jsonUsers.get("frequency").toString();
          model.uid = jsonUsers.get("ids").toString();
        }
        if (jsonUserTags != null) {
          model.usr = true;
          model.ust = jsonUserTags.get("frequency").toString();
          model.usrTgs = jsonUserTags.get("ids").toString();
        }
        if (jsonAssetUsers != null) {
          model.au = true;
          model.aut = jsonAssetUsers.get("frequency").toString();
        }
      } catch (JSONException e) {
        throw new ServiceException("Error while building JSON object for notifications", e);
      }
    }
  }

  public String deleteModel(NotificationsConfigModel model, String eventConfigJson)
      throws ServiceException {
    String message = "";
    String notifMessage = "";
    String notif = "";
    if (model != null) {
      if (StringUtils.isNotEmpty(model.or)) {
        notif = model.or;
      } else if (StringUtils.isNotEmpty(model.ship)) {
        notif = model.ship;
      } else if (StringUtils.isNotEmpty(model.inv)) {
        notif = model.inv;
      } else if (StringUtils.isNotEmpty(model.st)) {
        notif = model.st;
      } else if (StringUtils.isNotEmpty(model.acc)) {
        notif = model.acc;
      } else if (StringUtils.isNotEmpty(model.temp)) {
        notif = model.temp;
      }
      if (StringUtils.isNotEmpty(notif)) {
        notifMessage = notif;
        if (StringUtils.isNotEmpty(model.os) && StringUtils.isNotEmpty(model.pr)) {
          notifMessage += " (" + model.os + " = " + model.pr + ")";
        }
        if (StringUtils.isNotEmpty(model.ist) && StringUtils.isNotEmpty(model.mst)) {
          notifMessage += " [" + model.ist + " = " + model.mst + "]";
        }
      }
      JSONObject jsonObject = new JSONObject();
      JSONObject jsonFinalObject = new JSONObject();
      JSONArray jsonFinal = new JSONArray();
      try {
        jsonObject.put("name", notifMessage);
        JSONObject json = buildParams(model);
        if (json != null) {
          jsonObject.put("params", json);
        }
        jsonObject.put("message", model.mt);
        jsonObject.put("notifyoptions", buildNotifyOptions(model));
        jsonFinal.put(jsonObject);
        jsonFinalObject.put(model.id, jsonFinal);
        if (StringUtils.isNotEmpty(eventConfigJson)) {
          message = buildJson(jsonFinalObject, eventConfigJson, false);
        } else {
          message = jsonFinalObject.toString();
        }
      } catch (JSONException e) {
        throw new ServiceException("Error while building JSON object for notifications", e);
      }
    }
    return message;
  }

  private String buildJson(JSONObject jsonObject, String eventConfigJson, Boolean action)
      throws ServiceException {
    if (jsonObject == null) {
      return "";
    }
    try {
      JSONObject eventJSON = new JSONObject(eventConfigJson);
      Iterator<String> iteratorNew = jsonObject.keys();
      Iterator<String> iteratorConfig = eventJSON.keys();
      while (iteratorNew.hasNext()) {
        String key = iteratorNew.next();
        JSONArray obj = null;
        while (iteratorConfig.hasNext()) {
          if (key.equalsIgnoreCase(iteratorConfig.next())) {
            obj = eventJSON.getJSONArray(key);
            break;
          }
        }
        if (obj != null && obj.length() > 0) {
          obj = buildJSONArray(obj, jsonObject, key, action);
          eventJSON.remove(key);
          eventJSON.put(key, obj);
        } else {
          eventJSON.put(key, jsonObject.get(key));
        }
      }
      return eventJSON.toString();
    } catch (JSONException e) {
      throw new ServiceException("Error while building JSON object for notifications", e);
    }
  }

  private JSONArray buildJSONArray(JSONArray existingArray, JSONObject newObject, String key,
                                   Boolean add) throws JSONException, ServiceException {
    JSONArray jsonFinalArray = new JSONArray();
    JSONArray jsonArrayNew = (JSONArray) newObject.get(key);
    JSONObject jsonObjectNew = (JSONObject) jsonArrayNew.get(0);
    boolean match = false;
    for (int i = 0; i < existingArray.length(); i++) {
      try {
        JSONObject jsonObjectTemp = (JSONObject) existingArray.get(i);
        if (jsonObjectNew.has("name") && jsonObjectTemp.has("name")) {
          if (jsonObjectNew.get("name").toString().replace(" ", "")
              .equalsIgnoreCase(jsonObjectTemp.get("name").toString().replace(" ", ""))) {
            if (jsonObjectNew.has("params") && jsonObjectTemp.has("params")) {
              JSONObject paramsNew = (JSONObject) jsonObjectNew.get("params");
              JSONObject paramsTemp = (JSONObject) jsonObjectTemp.get("params");
              Iterator<String> keysNew = paramsNew.keys();
              Iterator<String> keysTemp = paramsTemp.keys();
              JSONObject paramsNewWithoutTags = new JSONObject();
              if (keysNew != null) {
                while (keysNew.hasNext()) {
                  String keyNew = keysNew.next();
                  if (!(keyNew.equalsIgnoreCase("mtagstoexclude") || keyNew
                      .equalsIgnoreCase("etagstoexclude") || keyNew
                      .equalsIgnoreCase("otagstoexclude"))) {
                    paramsNewWithoutTags.put(keyNew, paramsNew.get(keyNew));
                  }
                }

              }

              JSONObject paramsTempWithoutTags = new JSONObject();
              if (keysTemp != null) {
                while (keysTemp.hasNext()) {
                  String keyTemp = keysTemp.next();
                  if (!(keyTemp.equalsIgnoreCase("mtagstoexclude") || keyTemp
                      .equalsIgnoreCase("etagstoexclude") || keyTemp
                      .equalsIgnoreCase("otagstoexclude"))) {
                    paramsTempWithoutTags.put(keyTemp, paramsTemp.get(keyTemp));
                  }
                }

              }

              if (paramsNewWithoutTags.toString().replace(" ", "")
                  .equalsIgnoreCase(paramsTempWithoutTags.toString().replace(" ", "")) && add) {
                jsonFinalArray.put(jsonObjectNew);
              }
            } else {
              if (add) {
                jsonFinalArray.put(jsonObjectNew);
              }
            }
            match = true;
          } else {
            jsonFinalArray.put(jsonObjectTemp);
          }
        }
      } catch (JSONException e) {
        throw new ServiceException("Error while building JSON object for notifications", e);
      }
    }
    if (!match) {
      jsonFinalArray.put(jsonObjectNew);
    }
    return jsonFinalArray;
  }

  private String getStatus(String status) {
    String modifiedStatus = "";
    if ("confirmed".equalsIgnoreCase(status)) {
      modifiedStatus = "cf";
    } else if ("cancelled".equalsIgnoreCase(status)) {
      modifiedStatus = "cn";
    } else if ("fulfilled".equalsIgnoreCase(status)) {
      modifiedStatus = "fl";
    } else if ("shipped".equalsIgnoreCase(status)) {
      modifiedStatus = "cm";
    } else if ("pending".equalsIgnoreCase(status)) {
      modifiedStatus = "pn";
    } else if ("backordered".equalsIgnoreCase(status)) {
      modifiedStatus = "bo";
    } else {
      modifiedStatus = status;
    }
    return modifiedStatus;
  }

  private String retrieveStatus(String status) {
    String retrievedStatus = "";
    if ("cf".equalsIgnoreCase(status)) {
      retrievedStatus = "confirmed";
    } else if ("cn".equalsIgnoreCase(status)) {
      retrievedStatus = "cancelled";
    } else if ("fl".equalsIgnoreCase(status)) {
      retrievedStatus = "fulfilled";
    } else if ("cm".equalsIgnoreCase(status)) {
      retrievedStatus = "shipped";
    } else if ("sp".equalsIgnoreCase(status)) {
      retrievedStatus = "shipped";
    } else if ("pn".equalsIgnoreCase(status)) {
      retrievedStatus = "pending";
    } else if ("bo".equalsIgnoreCase(status)) {
      retrievedStatus = "backordered";
    } else {
      retrievedStatus = status;
    }
    return retrievedStatus;
  }

  private JSONObject buildNotifyOptions(NotificationsConfigModel model) throws ServiceException {
    JSONObject jsonNotify = new JSONObject();
    if (model.co) {
      putJsonObject(jsonNotify, "customers", model.cot, null);
    }
    if (model.vn) {
      putJsonObject(jsonNotify, "vendors", model.vnt, null);
    }
    if (model.ad) {
      putJsonObject(jsonNotify, "admins", model.adt, null);
    }
    if (model.cr) {
      putJsonObject(jsonNotify, "creator", model.crt, null);
    }
    if (model.usr) {
      if (StringUtils.isNotEmpty(model.uid)) {
        putJsonObject(jsonNotify, "users", model.ust, model.uid);
      }

      if (StringUtils.isNotEmpty(model.usrTgs)) {
        putJsonObject(jsonNotify, "user_tags", model.ust, model.usrTgs);
      }
    }
    if (model.au) {
      putJsonObject(jsonNotify, "asset_users", model.aut, null);
    }
    return jsonNotify;
  }

  private void putJsonObject(JSONObject jsonNotify, String name, String freq, String id)
      throws ServiceException {
    if (StringUtils.isNotBlank(freq)) {
      try {
        JSONObject js = new JSONObject();
        if (StringUtils.isNotBlank(id)) {
          js.put("ids", id);
        }
        js.put("frequency", freq);
        js.put("method", "0".equals(freq) ? "sms" : "email");
        jsonNotify.put(name, js);
      } catch (JSONException e) {
        throw new ServiceException("Error while building JSON object for notifications", e);
      }
    }
  }
}
