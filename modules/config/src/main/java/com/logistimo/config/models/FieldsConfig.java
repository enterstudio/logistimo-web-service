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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents the configuration of fields
 *
 * @author Arun
 */
public class FieldsConfig implements Serializable {

  // JSON Tags
  public static final String MANDATORY = "mndt";
  public static final String MAXSIZE = "mxsz";
  public static final String NAME = "nme";
  public static final String STATUSTRIGGER = "sttrg";
  public static final String TYPE = "typ";
  public static final String USEINTEMPLATES = "usntmpl";
  public static final String VALUE = "val";
  private static final long serialVersionUID = 8130451310577128106L;
  // Max fields
  public static int MAX_ORDERFIELDS = 3;
  private Map<String, Field> fields = null;
  private List<String> ordering = null; // field keys in order

  public FieldsConfig() {
    fields = new HashMap<String, Field>();
    ordering = new ArrayList<String>();
  }

  public FieldsConfig(JSONArray jsonFields) throws JSONException {
    this();
    if (jsonFields == null) {
      throw new JSONException("Illegal argument");
    }
    for (int i = 0; i < jsonFields.length(); i++) {
      Field f = new Field(jsonFields.getJSONObject(i));
      fields.put(f.getId(), f);
      ordering.add(f.getId());
    }
  }

  public Iterator<String> keys() {
    if (isEmpty()) {
      return null;
    }
    return ordering.iterator();
  }

  public Field getField(String key) {
    return fields.get(key);
  }

  public void putField(Field f) {
    String key = f.getId();
    fields.put(key, f);
    ordering.add(key);
  }

  public boolean isEmpty() {
    return (fields == null || fields.isEmpty());
  }

  public List<Field> getByStatus(String status) {
    if (status == null) {
      return null;
    }
    List<Field> list = new ArrayList<Field>();
    Iterator<String> it = ordering.iterator();
    while (it.hasNext()) {
      String key = it.next();
      Field f = fields.get(key);
      if (f != null && (status.equals(f.statusTrigger) || (f.statusTrigger == null
          || f.statusTrigger.isEmpty()))) {
        list.add(f);
      }
    }
    return list;
  }

  public JSONArray toJSONArray() throws JSONException {
    if (isEmpty()) {
      return null;
    }
    JSONArray array = new JSONArray();
    Iterator<String> it = ordering.iterator();
    while (it.hasNext()) {
      String key = it.next();
      Field f = fields.get(key);
      if (f != null) {
        array.put(f.toJSONObject());
      }
    }
    return array;
  }

  public static class Field implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name = null; // field name
    public String type = null; // type of field - text, checkbox, radio
    public String value = null; // single or comma-separate values
    public int maxSize = 0; // maximum size of this field
    public boolean mandatory = false;
    public boolean useInTemplates = false;
    public String statusTrigger = null; // order status that triggers this field

    public Field() {
    }

    public Field(JSONObject json) throws JSONException {
      name = json.getString(NAME);
      type = json.getString(TYPE);
      try {
        value = json.getString(VALUE);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        maxSize = json.getInt(MAXSIZE);
      } catch (Exception e) {
        // do nothing
      }
      try {
        mandatory = json.getBoolean(MANDATORY);
      } catch (Exception e) {
        // do nothing
      }
      try {
        useInTemplates = json.getBoolean(USEINTEMPLATES);
      } catch (Exception e) {
        // do nothing
      }
      try {
        statusTrigger = json.getString(STATUSTRIGGER);
      } catch (Exception e) {
        // do nothing
      }
    }

    public String getId() {
      if (name != null) {
        return name.replaceAll(" ", "");
      }
      return null;
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      if (name != null && !name.isEmpty()) {
        json.put(NAME, name);
      }
      if (type != null && !type.isEmpty()) {
        json.put(TYPE, type);
      }
      if (value != null) {
        json.put(VALUE, value);
      }
      if (statusTrigger != null && !statusTrigger.isEmpty()) {
        json.put(STATUSTRIGGER, statusTrigger);
      }
      json.put(MAXSIZE, maxSize);
      json.put(MANDATORY, mandatory);
      json.put(USEINTEMPLATES, useInTemplates);
      return json;
    }
  }
}
