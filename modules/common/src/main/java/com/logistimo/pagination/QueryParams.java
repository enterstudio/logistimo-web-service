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

package com.logistimo.pagination;

import com.logistimo.logger.XLog;

import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by charan on 03/04/17.
 */
public class QueryParams {

  private static final XLog xLogger = XLog.getLog(QueryParams.class);

  public String query;
  public QTYPE qType = QTYPE.JQL;
  public Class qClazz;
  public Map<String, Object> params;
  public List<String> listParams;

  public QueryParams(String query, List<String> listParams, QTYPE qType, Class qClazz) {
    this.query = query;
    this.listParams = listParams;
    this.qType = qType;
    this.qClazz = qClazz;
  }

  public QueryParams(String query, Map<String, Object> params) {
    this.query = query; // a JDO query, with filters, declarations and imports
    this.params = params; // optional, if there are filters in the query
  }

  public QueryParams(String query, Map<String, Object> params, QTYPE qType, Class qClazz) {
    this.query = query; // a JDO query, with filters, declarations and imports
    this.params = params; // optional, if there are filters in the query
    this.qType = qType;
    this.qClazz = qClazz;
  }

  @SuppressWarnings("unchecked")
  public QueryParams(String query, String paramsString) {
    this.query = query;
    if (paramsString != null && !paramsString.isEmpty()) {
      this.params = (Map<String, Object>) PagedExec.deserialize(paramsString);
    }
  }

  @SuppressWarnings("unchecked")
  public QueryParams(String query, String paramsString, QTYPE qType, Class qClazz) {
    this.query = query;
    if (paramsString != null && !paramsString.isEmpty()) {
      this.params = (Map<String, Object>) PagedExec.deserialize(paramsString);
    }
    this.qType = qType;
    this.qClazz = qClazz;
  }

  // Get the parameters serialized string, given a param. map (a serialized base-64 string is returned)
  public String toParamsString() {
    if (params == null || params.isEmpty()) {
      return null;
    }
    return PagedExec.serialize(params);
  }

  public String getQueryId(String cursor, String taskName) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      // Add query
      md.update(query.getBytes());
      // Add parameters, if any
      if (params != null && !params.isEmpty()) {
        Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<String, Object> param = it.next();
          // Add the key to the digest
          md.update(param.getKey().getBytes());
          if (param.getValue() != null) { // add the value to the digest
            try {
              ByteArrayOutputStream b = new ByteArrayOutputStream();
              ObjectOutputStream o = new ObjectOutputStream(b);
              o.writeObject(param.getValue());
              md.update(b.toByteArray());
              o.close();
            } catch (IOException e) {
              xLogger.warn(
                  "IOException when trying to get the byte value {0} for key {1} for query {2}",
                  param.getValue(), param.getKey(), query);
            }
          }
        }
      }
      // Add cursor, if any
      if (cursor != null && !cursor.isEmpty()) {
        md.update(cursor.getBytes());
      }
      if (taskName != null && !taskName.isEmpty()) {
        md.update(taskName.getBytes());
      }
      return new String(Hex.encodeHex(md.digest()));
    } catch (NoSuchAlgorithmException e) {
      xLogger.severe("No such algorithm MD5: {0}", e.getMessage());
    }
    return null;
  }

  public enum QTYPE {JQL, SQL, CQL}
}
