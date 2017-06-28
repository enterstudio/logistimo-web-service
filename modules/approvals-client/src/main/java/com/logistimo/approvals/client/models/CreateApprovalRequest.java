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

package com.logistimo.approvals.client.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by nitisha.khandelwal on 31/05/17.
 */

public class CreateApprovalRequest {

  private String type;

  @SerializedName("type_id")
  private String typeId;

  @SerializedName("requester_id")
  private String requesterId;

  @SerializedName("source_domain_id")
  private Long sourceDomainId;

  private List<Long> domains;

  private String message;

  private Map<String, String> attributes;

  private List<Approver> approvers;

  public String getType() {
    return type;
  }

  public String getTypeId() {
    return typeId;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public Long getSourceDomainId() {
    return sourceDomainId;
  }

  public List<Long> getDomains() {
    return domains;
  }

  public String getMessage() {
    return message;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public List<Approver> getApprovers() {
    return approvers;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public void setSourceDomainId(Long sourceDomainId) {
    this.sourceDomainId = sourceDomainId;
  }

  public void setDomains(List<Long> domains) {
    this.domains = domains;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public void setApprovers(List<Approver> approvers) {
    this.approvers = approvers;
  }
}

