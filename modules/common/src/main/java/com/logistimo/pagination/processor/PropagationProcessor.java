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
package com.logistimo.pagination.processor;

import com.logistimo.services.taskqueue.ITaskService;

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.Results;
import com.logistimo.logger.XLog;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;

/**
 * @author Arun
 */
public class PropagationProcessor implements Processor {

  private static final XLog xLogger = XLog.getLog(PropagationProcessor.class);

  @SuppressWarnings("rawtypes")
  @Override
  public String process(Long domainId, Results results, String fieldDataJsonStr,
                        PersistenceManager pm) throws ProcessingException {
    xLogger.fine("Entered process");
    if (results == null) {
      return fieldDataJsonStr;
    }
    List list = results.getResults();
    if (list == null || list.isEmpty()) {
      return fieldDataJsonStr;
    }
    xLogger.info("Propagating to {0} in domain {1} with field data {2}",
        list.get(0).getClass().getSimpleName(), domainId, fieldDataJsonStr);
    // Get field data
    FieldData fd = null;
    try {
      fd = new FieldData(fieldDataJsonStr);
    } catch (Exception e) {
      xLogger.severe("{0} when getting field-data for {1}: {2}", e.getClass().getName(),
          fieldDataJsonStr, e.getMessage());
      return fieldDataJsonStr;
    }
    if (fd.methodName == null) {
      xLogger.severe("No method name given for field data: {0}", fieldDataJsonStr);
      return fieldDataJsonStr;
    }
    // Propagate the value via Java reflection
    Iterator it = list.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      try {
        Method m = o.getClass().getDeclaredMethod(fd.methodName, fd.paramTypes);
        m.setAccessible(true);
        m.invoke(o, fd.paramValues);
        pm.makePersistent(o);
      } catch (Exception e) {
        xLogger.warn("{0} when trying to get/set field {1} with value {2} via reflection: {3}",
            e.getClass().getName(), fd.methodName, fd.paramValues, e.getMessage());
      }
    }
    xLogger.fine("Exiting process");

    return fieldDataJsonStr;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_DEFAULT;
  }

  public static class FieldData {

    private static String TAG_METHODNAME = "name";
    private static String TAG_PARAMTYPES = "paramtypes";
    private static String TAG_PARAMVALUES = "paramvalues";

    public String methodName;
    @SuppressWarnings("rawtypes")
    public Class[] paramTypes = null;
    public Object[] paramValues = null;

    @SuppressWarnings("rawtypes")
    public FieldData(String methodName, Class[] paramTypes, Object[] paramValues) {
      this.methodName = methodName;
      this.paramTypes = paramTypes;
      this.paramValues = paramValues;
    }

    public FieldData(String fieldDataJsonStr) throws JSONException {
      JSONObject json = new JSONObject(fieldDataJsonStr);
      methodName = json.getString(TAG_METHODNAME);
      String val = json.optString(TAG_PARAMTYPES);
      if (val != null && !val.isEmpty()) {
        paramTypes = (Class[]) PagedExec.deserialize(val);
      }
      val = json.optString(TAG_PARAMVALUES);
      if (val != null && !val.isEmpty()) {
        paramValues = (Object[]) PagedExec.deserialize(val);
      }
    }

    public String toJSONString() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(TAG_METHODNAME, methodName);
      if (paramTypes != null) {
        json.put(TAG_PARAMTYPES, PagedExec.serialize(paramTypes));
      }
      if (paramValues != null) {
        json.put(TAG_PARAMVALUES, PagedExec.serialize(paramValues));
      }
      return json.toString();
    }
  }
}
