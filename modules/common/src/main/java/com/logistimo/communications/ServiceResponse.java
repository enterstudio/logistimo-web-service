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
package com.logistimo.communications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract representation of a response from a service provider.
 *
 * @author Arun
 */
@SuppressWarnings("serial")
public class ServiceResponse implements Serializable {

  public static final int METHOD_ID = 0; // sends a request/job id as response
  public static final int METHOD_STATUS = 1; // sends status string as response
  public static final int METHOD_MESSAGE = 3; // Some error or other message

  private String pid = null; // provider ID
  private int method = METHOD_ID;
  private List<String> responses = null; // response string
  private Map<String, List<String>>
      respMap =
      null;
  // map of response (e.g. jobId) to addresss (CSV)
  private boolean jidUnique = false; // job-Id is unique per number

  public ServiceResponse(String pid) {
    this(pid, false);
  }

  public ServiceResponse(String pid, boolean jobIdUnique) {
    this.pid = pid;
    this.responses = new ArrayList<String>();
    this.respMap = new HashMap<String, List<String>>();
    this.jidUnique = jobIdUnique;
  }

  public ServiceResponse(String pid, boolean jobIdUnique, Map<String, List<String>> respMap) {
    this.pid = pid;
    this.responses = new ArrayList<>();
    this.respMap = respMap;
    this.jidUnique = jobIdUnique;
  }

  public String getProviderId() {
    return pid;
  }

  public int getMethod() {
    return method;
  }

  public void setMethod(int method) {
    this.method = method;
  }

  public List<String> getResponses() {
    return responses;
  }

  public void addResponse(String resp, List<String> addresses) {
    responses.add(resp);
    respMap.put(resp, addresses);
  }

  public List<String> getAddresses(String resp) {
    return respMap.get(resp);
  }

  public boolean isJobIdUnique() {
    return jidUnique;
  }

  public void setJobIdUnique(boolean unique) {
    jidUnique = unique;
  }

  public Map<String, List<String>> getRespMap() {
    return respMap;
  }
}
