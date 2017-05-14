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

package com.logistimo.services.taskqueue;

import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TaskOptions implements Serializable {

  public static final TaskBuilder Builder = new TaskBuilder();
  private String url;
  private int method = ITaskService.METHOD_GET;
  private Map<String, String> headerMap = new HashMap<String, String>();
  private Map<String, List<String>> params = new HashMap<String, List<String>>();
  private long etaMillis = -1;
  private Long taskId;
  private String payload;
  private String contentType;

  public TaskOptions(String url) {
    this.url = ConfigUtil.get("task.url", "http://localhost:8080") + url;
  }

  public int getMethod() {
    return method;
  }

  public void setMethod(int method) {
    this.method = method;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String taskUrl) {
    this.url = taskUrl;
  }

  public Map<String, List<String>> getParams() {
    return params;
  }

  public void setParams(Map<String, List<String>> params) {
    this.params = params;
  }

  public Map<String, String> getHeaders() {
    return headerMap;
  }

  public long getEtaMillis() {
    return etaMillis;
  }

  public void setEtaMillis(long etaMillis) {
    this.etaMillis = etaMillis;
  }

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long taskId) {
    this.taskId = taskId;
  }

  public void payload(byte[] payload, String contentType) {
    this.payload = Base64.encodeBase64String(payload);
    this.contentType = contentType;
  }

  public byte[] getPayload() {
    return Base64.decodeBase64(payload);
  }

  public void setPayload(byte[] payload) {
    this.payload = Base64.encodeBase64String(payload);
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public TaskOptions method(int m) {
    this.method = m;
    return this;
  }

  public TaskOptions header(String key, String string) {
    this.headerMap.put(key, string);
    return this;
  }

  public TaskOptions param(String key, String string) {
    List<String> current = params.get(key);
    if (current == null) {
      synchronized (key) {
        if (current == null) {
          current = new ArrayList<String>(1);
        }
      }
    }
    current.add(string);
    this.params.put(key, current);
    return this;
  }

  public TaskOptions etaMillis(long etaMillis) {
    this.etaMillis = etaMillis;
    return this;
  }

  public Map<String, String> getHeaderMap() {
    return headerMap;
  }

  public void setHeaderMap(Map<String, String> headerMap) {
    this.headerMap = headerMap;
  }

  @Override
  public String toString() {
    return "TaskOptions{" +
        "id=" + taskId +
        ", url='" + url + '\'' +
        ", method=" + method +
        ", headerMap=" + headerMap +
        ", params=" + params +
        ", etaMillis=" + etaMillis +
        '}';
  }
}
