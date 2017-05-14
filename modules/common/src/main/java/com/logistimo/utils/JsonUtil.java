/**
 *
 */
package com.logistimo.utils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Arun
 */
public class JsonUtil {

  // Get map from a JSON object; each Object value in the Map can be either String, List<String> or (recursively) Map<String,Object>
  @SuppressWarnings("unchecked")
  public static Map<String, Object> toMap(JSONObject json) throws JSONException {
    if (json == null) {
      return null;
    }
    Map<String, Object> map = new HashMap<String, Object>();
    Iterator<String> keys = json.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      Object o = json.get(key);
      if (o instanceof JSONArray) {
        map.put(key, toList((JSONArray) o));
      } else if (o instanceof JSONObject) {
        map.put(key, toMap((JSONObject) o));
      } else {
        map.put(key, o);
      }
    }
    return map;
  }

  // Get a List from a JSON Array
  public static List<String> toList(JSONArray array) throws JSONException {
    if (array == null || array.length() == 0) {
      return null;
    }
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < array.length(); i++) {
      list.add(array.getString(i));
    }
    return list;
  }

  // Convert a map to a JSON object; each value in Map can be a String, List<String> or Map<String,Object>
  @SuppressWarnings("unchecked")
  public static JSONObject toJSON(Map<String, Object> map) throws JSONException {
    if (map == null || map.isEmpty()) {
      return null;
    }
    JSONObject json = new JSONObject();
    Iterator<String> it = map.keySet().iterator();
    while (it.hasNext()) {
      String key = it.next();
      Object o = map.get(key);
      if (o instanceof List) {
        json.put(key, toJSON((List<String>) o));
      } else if (o instanceof Map) {
        json.put(key, toJSON((Map<String, Object>) o));
      } else {
        json.put(key, o);
      }
    }
    return json;
  }

  // Convert a List to a JSONAray
  public static JSONArray toJSON(List<String> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    JSONArray array = new JSONArray();
    Iterator<String> it = list.iterator();
    while (it.hasNext()) {
      array.put(it.next());
    }
    return array;
  }

}