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

package com.logistimo.orders.approvals.models;

import com.google.gson.annotations.SerializedName;
import com.logistimo.models.StatusModel;
import com.logistimo.orders.approvals.ApprovalType;
import com.logistimo.orders.models.OrderModel;
import com.logistimo.users.models.UserContactModel;

import java.util.Date;
import java.util.List;

/**
 * Created by naveensnair on 19/06/17.
 */
public class ApprovalModel {

  @SerializedName("approval_id")
  private String id;

  @SerializedName("order_id")
  private Long orderId;

  @SerializedName("created_at")
  private Date createdAt;

  @SerializedName("expires_at")
  private Date expiresAt;

  @SerializedName("requester")
  private UserContactModel requester;

  private StatusModel status;

  private List<ApproverModel> approvers;

  @SerializedName("conversation_id")
  private String conversationId;

  private OrderModel order;

  @SerializedName("approval_type")
  private ApprovalType approvalType;

  @SerializedName("status_updated_by")
  private UserContactModel statusUpdatedBy;

  @SerializedName("active_approver_type")
  private String activeApproverType;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Date expiresAt) {
    this.expiresAt = expiresAt;
  }

  public UserContactModel getRequester() {
    return requester;
  }

  public void setRequester(UserContactModel requester) {
    this.requester = requester;
  }

  public StatusModel getStatus() {
    return status;
  }

  public void setStatus(StatusModel status) {
    this.status = status;
  }

  public List<ApproverModel> getApprovers() {
    return approvers;
  }

  public void setApprovers(
      List<ApproverModel> approvers) {
    this.approvers = approvers;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public OrderModel getOrder() {
    return order;
  }

  public void setOrder(OrderModel order) {
    this.order = order;
  }

  public ApprovalType getApprovalType() {
    return approvalType;
  }

  public void setApprovalType(ApprovalType approvalType) {
    this.approvalType = approvalType;
  }

  public UserContactModel getStatusUpdatedBy() {
    return statusUpdatedBy;
  }

  public void setStatusUpdatedBy(UserContactModel statusUpdatedBy) {
    this.statusUpdatedBy = statusUpdatedBy;
  }

  public String getActiveApproverType() {
    return activeApproverType;
  }

  public void setActiveApproverType(String activeApproverType) {
    this.activeApproverType = activeApproverType;
  }
}
