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

package com.logistimo.orders.approvals.utils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nitisha.khandelwal on 17/07/17.
 */

public class ApprovalCreatedEvent {

  private String approvalId;

  private String type;

  private String typeId;

  private String requesterId;

  private String status;

  private String conversationId;

  private Long sourceDomainId;

  private List<Long> domains;

  private Date expireAt;

  private Map<String, String> attributes;

  private String activeApproverType;

  private Date createdAt;

  private Date updatedAt;

  private String updatedBy;

  public String getApprovalId() {
    return approvalId;
  }

  public String getType() {
    return type;
  }

  public String getTypeId() {
    return typeId;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public String getStatus() {
    return status;
  }

  public String getConversationId() {
    return conversationId;
  }

  public Long getSourceDomainId() {
    return sourceDomainId;
  }

  public List<Long> getDomains() {
    return domains;
  }

  public Date getExpireAt() {
    return expireAt;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public String getActiveApproverType() {
    return activeApproverType;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setApprovalId(String approvalId) {
    this.approvalId = approvalId;
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

  public void setStatus(String status) {
    this.status = status;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public void setSourceDomainId(Long sourceDomainId) {
    this.sourceDomainId = sourceDomainId;
  }

  public void setDomains(List<Long> domains) {
    this.domains = domains;
  }

  public void setExpireAt(Date expireAt) {
    this.expireAt = expireAt;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public void setActiveApproverType(String activeApproverType) {
    this.activeApproverType = activeApproverType;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }
}
