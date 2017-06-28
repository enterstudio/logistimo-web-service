package com.logistimo.orders.approvals.models;

import com.google.gson.annotations.SerializedName;

import com.logistimo.models.StatusModel;
import com.logistimo.orders.models.OrderModel;
import com.logistimo.users.models.UserContactModel;

import java.util.Date;
import java.util.List;

/**
 * Created by naveensnair on 19/06/17.
 */
public class ApprovalModel {

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
}
