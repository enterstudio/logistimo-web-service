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

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nitisha.khandelwal on 31/05/17.
 */
public class CreateApprovalResponse {

  @SerializedName("approval_id")
  private String approvalId;

  @SerializedName("type")
  private String type;

  @SerializedName("type_id")
  private String typeId;

  @SerializedName("requester_id")
  private String requesterId;

  @SerializedName("status")
  private String status;

  @SerializedName("conversation_id")
  private String conversationId;

  @SerializedName("source_domain_id")
  private Long sourceDomainId;

  @SerializedName("domains")
  private List<Long> domains;

  @SerializedName("expire_at")
  private Date expireAt;

  @SerializedName("attributes")
  private Map<String, String> attributes;

  @SerializedName("active_approver_type")
  private String activeApproverType;

  @SerializedName("approvers")
  private List<ApproverResponse> approvers;

  @SerializedName("created_at")
  private Date createdAt;

  @SerializedName("updated_at")
  private Date updatedAt;

  @SerializedName("updated_by")
  private String updatedBy;

  public String getApprovalId() {
    return approvalId;
  }

  public void setApprovalId(String approvalId) {
    this.approvalId = approvalId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTypeId() {
    return typeId;
  }

  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public Long getSourceDomainId() {
    return sourceDomainId;
  }

  public void setSourceDomainId(Long sourceDomainId) {
    this.sourceDomainId = sourceDomainId;
  }

  public List<Long> getDomains() {
    return domains;
  }

  public void setDomains(List<Long> domains) {
    this.domains = domains;
  }

  public Date getExpireAt() {
    return expireAt;
  }

  public void setExpireAt(Date expireAt) {
    this.expireAt = expireAt;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public List<ApproverResponse> getApprovers() {
    return approvers;
  }

  public void setApprovers(List<ApproverResponse> approvers) {
    this.approvers = approvers;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getActiveApproverType() {
    return activeApproverType;
  }

  public void setActiveApproverType(String activeApproverType) {
    this.activeApproverType = activeApproverType;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }
}
