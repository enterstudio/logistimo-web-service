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

package com.logistimo.domains;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectsToDomainModel {

  // Commands
  public static final int ACTION_ADD = 0;
  public static final int ACTION_REMOVE = 1;

  @Expose
  private List<Long> domainIds = null; // list of domains to which each object is to be added
  @Expose
  private String className = null; // class of the object
  @Expose
  private List<Object> objectIds = null; // list of objects (Ids) which need to be added to domains
  @Expose
  private String keyField = null; // key field name, if to be used in a query
  @Expose
  private Long sourceDomainId = null; // source domain in which the query is to be executed
  @Expose
  private int action = ACTION_ADD;

  public ObjectsToDomainModel() {
  }

  public ObjectsToDomainModel(int action, List<Long> domainIds, String className,
                              List<Object> objectIds) {
    this.action = action;
    this.domainIds = domainIds;
    this.className = className;
    this.objectIds = objectIds;
  }

  public ObjectsToDomainModel(int action, List<Long> domainIds, String className, String keyField,
                              Long sourceDomainId) {
    this.action = action;
    this.domainIds = domainIds;
    this.keyField = keyField;
    this.className = className;
    this.sourceDomainId = sourceDomainId;

  }

  public int getAction() {
    return action;
  }

  public List<Long> getDomainIds() {
    return domainIds;
  }

  public String getClassName() {
    return className;
  }

  public List<Object> getObjectIds() {
    return objectIds;
  }

  public boolean hasObjectIds() {
    return objectIds != null && !objectIds.isEmpty();
  }

  public Long getSourceDomainId() {
    return sourceDomainId;
  }

  public String getQueryString() {
    if (keyField == null || className == null) {
      return null;
    }
    return "SELECT " + keyField + " FROM " + className
        + " WHERE dId.contains(dIdParam) PARAMETERS Long dIdParam";
  }

  public Map<String, Object> getQueryParams() {
    Map<String, Object> queryParams = new HashMap<String, Object>();
    queryParams.put("dIdParam", sourceDomainId);
    return queryParams;
  }
}