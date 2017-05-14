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

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.utils.StringUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Arun
 */
public class MessageConfig implements Serializable {

  public static final String ORDERSTATUS_TEMPLATE = "ost";
  private static final String
      ORDERSTATUS_TEMPLATE_DEFAULT =
      "Order %orderId% was '%orderStatus%' on %time%";

  private static final long serialVersionUID = 1L;

  private String
      orderStatusMsgTemplate =
      ORDERSTATUS_TEMPLATE_DEFAULT;
  // DEPRECATED (as of 30/4/2012)
  private Map<String, NotifyTemplate>
      notifyMap =
      new HashMap<String, NotifyTemplate>();
  // transType --> NotifyTemplate

  public MessageConfig() {
  }

  @SuppressWarnings("unchecked")
  public MessageConfig(JSONObject json) throws JSONException {
    if (json == null) {
      return;
    }
    // Form the notification map
    Iterator<String> en = json.keys();
    while (en.hasNext()) {
      String transType = en.next();
      try {
        notifyMap.put(transType, new NotifyTemplate(json.getJSONObject(transType)));
      } catch (JSONException e) {
        // ignore
      }
    }
    // FOR BACKWARD COMPATIBILITY of order status message template
    try {
      String orderMsgTemplate = json.getString(ORDERSTATUS_TEMPLATE);
      if (orderMsgTemplate != null && !orderMsgTemplate.isEmpty()) {
        NotifyTemplate orderTemplate = notifyMap.get( "o");
        if (orderTemplate == null) {
          orderTemplate = new NotifyTemplate();
          notifyMap.put( "o", orderTemplate);
        }
        orderTemplate.messageTemplate = orderMsgTemplate;
      }
    } catch (JSONException e) {
      // do nothing
    }
    // END BACKWARD COMPATIBILITY
  }

  // DEPRECATED (as of 30/4/2012)
  public String getOrderStatusMsgTemplate() {
    NotifyTemplate orderTemplate = notifyMap.get( "o");
    String orderMsgTemplate = null;
    if (orderTemplate != null) {
      orderMsgTemplate = orderTemplate.messageTemplate;
    }
    if (orderMsgTemplate == null || orderMsgTemplate.isEmpty()) {
      return orderStatusMsgTemplate;
    }
    return orderMsgTemplate;
  }

  public NotifyTemplate getNotificationTemplate(String transType) {
    return notifyMap.get(transType);
  }

  public void setNotificationTemplate(Map<String, NotifyTemplate> notifyMap) {
    this.notifyMap = notifyMap;
    // FOR BACKWARD COMPATIBILITY: reset earlier order msg. template
    orderStatusMsgTemplate = null;
  }
  // END DEPRECATED

  public void putNotificationTemplate(String transType, NotifyTemplate nt) {
    notifyMap.put(transType, nt);
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    JSONObject json = new JSONObject();
    Iterator<String> it = notifyMap.keySet().iterator();
    while (it.hasNext()) {
      String transType = it.next();
      try {
        NotifyTemplate nt = notifyMap.get(transType);
        if (nt != null && nt.isValid()) {
          json.put(transType, nt.toJSONObject());
        }
      } catch (JSONException e) {
        // ignore
      }
    }
    return json;
  }

  public static class TemplateVariable {
    public String var; // variable id
    public String name;
    public int length;

    public TemplateVariable(String var, String name, int length) {
      this.var = var;
      this.name = name;
      this.length = length;
    }
  }

  public static class NotifyTemplate implements Serializable {
    public static final String REASONS = "rsns";
    public static final String SENDTO = "sndto";
    public static final String MESSAGETEMPLATE = "msgtmpl";
    private static final long serialVersionUID = 1L;
    public List<String> reasons = null;
    public List<String> sendTo = null;
    public String messageTemplate = null;

    public NotifyTemplate() {
    }

    public NotifyTemplate(JSONObject json) {
      reasons = StringUtil.getList(json.optString(REASONS));
      sendTo = StringUtil.getList(json.optString(SENDTO));
      messageTemplate = json.optString(MESSAGETEMPLATE);
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      if (reasons != null && !reasons.isEmpty()) {
        json.put(REASONS, StringUtil.getCSV(reasons));
      }
      if (sendTo != null && !sendTo.isEmpty()) {
        json.put(SENDTO, StringUtil.getCSV(sendTo));
      }
      if (messageTemplate != null && !messageTemplate.isEmpty()) {
        json.put(MESSAGETEMPLATE, messageTemplate);
      }
      return json;
    }

    public boolean hasReason(String reason) {
      return (reasons != null && reasons.contains(reason));
    }

    public boolean hasSendTo(String userType) {
      return (sendTo != null && sendTo.contains(userType));
    }

    public boolean isValid() {
      return ((messageTemplate != null && !messageTemplate.isEmpty() && sendTo != null && !sendTo
          .isEmpty()));
    }
  }
}
