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

import com.logistimo.logger.XLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author Arun
 */
public class EventsConfig implements Serializable {

  // Template types
  public static final String TYPE_ORDERS = "orders";
  public static final String TYPE_INVENTORY = "inventory";
  public static final String TYPE_SETUP = "setup";
  // Variables
  public static final String VAR_ABNORMALSTOCKDURATION = "%abnormalStockDuration%";
  public static final String VAR_ABNORMALSTOCKEVENT = "%abnormalStockEvent%";
  public static final String VAR_BATCHNUMBER = "%batch%";
  public static final String VAR_BATCHEXPIRY = "%expiry%";
  public static final String VAR_CREDITLIMIT = "%creditLimit%";
  public static final String VAR_CREATIONTIME = "%creationTime%";
  public static final String VAR_CUSTOMER = "%customer%";
  public static final String VAR_CUSTOMERCITY = "%customerCity%";
  public static final String VAR_DEVICEID = "%deviceId%";
  public static final String VAR_DEVICEVENDOR = "%deviceVendor%";
  public static final String VAR_ENTITY = "%entity%";
  public static final String VAR_ENTITYCITY = "%entityCity%";
  public static final String VAR_ENTITYGROUP = "%entityGroup%";
  public static final String VAR_REQUIREDBYDATE = "%requiredByDate%";
  public static final String VAR_ESTIMATEDDATEOFARRIVAL = "%estimatedDateOfArrival%";
  public static final String VAR_IPADDRESS = "%ipAddress%";
  public static final String VAR_LASTLOGINTIME = "%lastLoginTime%";
  public static final String VAR_MATERIAL = "%material%";
  public static final String VAR_MATERIALSWITHALLOCATED = "%materialsWithAllocated%";
  public static final String VAR_MATERIALSWITHQUANTITIES = "%materialsWithQuantity%";
  public static final String VAR_MATERIALSWITHFULFILED = "%materialsWithFulfiled%";
  public static final String VAR_PACKAGESIZE = "%packageSize%";
  public static final String VAR_REASONCANCEL = "%reasonCancel%";
  public static final String VAR_REASONSHIPMENT = "%reasonShipment%";
  public static final String VAR_SHIPMENTID = "%shipmentID%";
  public static final String VAR_TRACKINGID = "%trackingID%";
  public static final String VAR_UPDATEDBY = "%updatedBy%";
  public static final String VAR_MATERIALS = "%materials%";
  public static final String VAR_MATERIALSWITHMETADATA = "%materialsWithMetadata%";
  public static final String VAR_MAXSTOCK = "%maxStock%";
  public static final String VAR_MINSTOCK = "%minStock%";
  public static final String VAR_MOBILEPHONE = "%mobilePhone%";
  public static final String VAR_NUMBEROFITEMS = "%numberOfItems%";
  public static final String VAR_ORDERID = "%orderId%";
  public static final String VAR_ORDERSTATUS = "%orderStatus%";
  public static final String VAR_SHIPMENTSTATUS = "%shipmentStatus%";
  public static final String VAR_PAYABLE = "%payable%";
  public static final String VAR_PAYMENT = "%payment%";
  public static final String VAR_QUANTITY = "%quantity%";
  public static final String VAR_REASON = "%reason%";
  public static final String VAR_ROLE = "%role%";
  public static final String VAR_SAFETYSTOCK = "%safetyStock%";
  public static final String VAR_SERIALNUMBER = "%serialnum%";
  public static final String VAR_STATUSCHANGETIME = "%statusChangeTime%";
  public static final String VAR_TEMPERATURE = "%temperature%";
  public static final String VAR_TEMPERATURERANGE = "%temperatureRange%";
  public static final String VAR_TEMPERATURESTATUS = "%temperatureStatus%";
  public static final String VAR_TEMPERATUREMIN = "%minTemperature%";
  public static final String VAR_TEMPERATUREMAX = "%maxTemperature%";
  public static final String VAR_ASSETTYPE = "%assetType%";
  public static final String VAR_TEMPERATUREEVENTCREATIONTIME = "%creationTime%";
  public static final String VAR_TRANSACTIONTYPE = "%transType%";
  public static final String VAR_TRANSPORTER = "%transporter%";
  public static final String VAR_UPDATIONTIME = "%updationTime%";
  public static final String VAR_USER = "%user%";
  public static final String VAR_USERID = "%userId%";
  public static final String VAR_VENDOR = "%vendor%";
  public static final String VAR_VENDORCITY = "%vendorCity%";
  public static final String VAR_TEMPERATURESTATUSUPDATEDTIME = "%temperatureStatusUpdatedTime%";
  public static final String VAR_DEVICESTATUSUPDATEDTIME = "%deviceStatusUpdatedTime%";
  public static final String VAR_STATUSUPDATEDTIME = "%statusUpdatedTime%";
  public static final String VAR_ASSET_MP = "%monitoringPoint%";
  public static final String VAR_MANUFACTURER = "%manufacturer%";
  public static final String VAR_MODEL = "%model%";
  public static final String VAR_MATERIAL_STATUS = "%status%";
  public static final String VAR_ASSET_SENSOR_ID = "%sensorId%";
  public static final String VAR_COMMENT = "%comment%";
  public static final String VAR_COMMETED_ON = "%commentedOn%";
  public static final String VAR_COMMENTED_BY = "%commentedBy%";
  public static final String VAR_RELATED_ASSET_SERIAL_NUM = "%relatedAssetSerialNumber%";
  public static final String VAR_RELATED_ASSET_TYPE = "%relatedAssetType%";
  public static final String VAR_RELATED_ASSET_MODEL = "%relatedAssetModel%";
  public static final String VAR_RELATED_ASSET_MANUFACTURER = "%relatedAssetManufacturer%";
  public static final String VAR_REGISTEREDBY = "%registeredBy%";
  public static final String VAR_PARENT_ASSET = "%parentAssetId%";
  public static final String VAR_PARENT_ASSET_TYPE = "%parentAssetType%";
  public static final String VAR_MANUFACTURE_YEAR = "%manufactureYear%";
  public static final String VAR_ASSET_STATUS = "%assetStatus%";

  private static final long serialVersionUID = 1L;
  public static final int VAR_MESSAGE_LIMIT = 50;
  // Logger
  private static final XLog xLogger = XLog.getLog(EventsConfig.class);


  private Map<String, EventSpec>
      eventSpecs =
      new HashMap<String, EventSpec>();
  // Event key (objectType:eventId) --> EventSpec

  public EventsConfig() {
  }

  public EventsConfig(String jsonString) throws JSONException {
    this(new JSONObject(jsonString));
  }

  @SuppressWarnings("unchecked")
  public EventsConfig(JSONObject json) throws JSONException {
    Iterator<String> keys = json.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      eventSpecs.put(key, new EventSpec(json.getJSONArray(key)));
    }
  }

  public static String createKey(int eventId, String objectType) {
    return objectType + ":" + eventId;
  }

  // Given a message template, replace template variables and return a completed message
  public static String replaceTemplateVariables(String msg, Map<String, String> values,
                                                boolean htmlize) {
    if (msg == null || msg.isEmpty() || values == null || values.isEmpty()) {
      return msg;
    }
    Iterator<String> it = values.keySet().iterator();
    while (it.hasNext()) {
      String var = it.next();
      String val = values.get(var);
      if (val == null) {
        val = "";
      }
                        /*if ( htmlize && !val.isEmpty() ) {
                                String fontColor = getFontColor( var );
				if ( fontColor != null )
					val = "<font style=\"color:" + fontColor + "\">" + val + "</font>";
			}*/
      msg = msg.replaceAll(var, val);
    }
    return msg;
  }

  public JSONObject toJSONObject() throws JSONException {
    if (eventSpecs.isEmpty()) {
      return null;
    }
    JSONObject json = new JSONObject();
    Iterator<Entry<String, EventSpec>> entries = eventSpecs.entrySet().iterator();
    while (entries.hasNext()) {
      Entry<String, EventSpec> e = entries.next();
      json.put(e.getKey(), e.getValue().toJSONObject());
    }
    return json;
  }

  public String toJSONString() throws JSONException {
    JSONObject json = toJSONObject();
    if (json != null) {
      return json.toString();
    }
    return null;
  }

  public EventSpec getEventSpec(int eventId, String objectType) {
    return eventSpecs.get(createKey(eventId, objectType));
  }

  public Map<String, EventSpec> getFullEventSpec() {
    return eventSpecs;
  }

  public void setEventSpecs(Map<String, EventSpec> eventSpecs) {
    this.eventSpecs = eventSpecs;
  }

  public void putEventSpec(int eventId, String objectType, EventSpec eventSpec) {
    eventSpecs.put(createKey(eventId, objectType), eventSpec);
  }

  public void removeEventSpec(int eventId, String objectType) {
    eventSpecs.remove(createKey(eventId, objectType));
  }

  public EventSpec.ParamSpec getEventParamSpec(int eventId, String objectType, Map<String, Object> params) {
    EventSpec eventSpec = getEventSpec(eventId, objectType);
    if (eventSpec != null) {
      return eventSpec.getParamSpec(params);
    }
    return null;
  }

  // Match an event (i.e. is configured) and its parameters; use a ParamComparator in case the parameter matching criteria is not equal-to.
  // Returns the matched event param. spec.
  public EventSpec.ParamSpec matchEvent(String objectType, int eventId, Map<String, Object> params,
                              EventSpec.ParamComparator comparator) {
    EventSpec es = eventSpecs.get(createKey(eventId, objectType));
    if (es == null) {
      return null;
    } else {
      return es.matchEvent(params, comparator);
    }
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

  // Get varying font colors for htmlization of message
        /*private static String getFontColor( String variable ) {
                if ( variable.indexOf( "Time" ) != -1 )
			return "green";
		else if ( variable.indexOf( "customer" ) != -1 || variable.indexOf( "entity" ) != -1 )
			return "red";
		else if ( variable.indexOf( "vendor" ) != -1 )
			return "#800080"; // purple (earlier maroon)
		else if ( variable.indexOf( "quantity" ) != -1 )
			return "maroon";
		return "blue";
	}*/
}
