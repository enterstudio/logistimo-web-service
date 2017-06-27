package com.logistimo.orders.approvals.models;

import com.logistimo.approvals.client.models.ApprovalFilters;
import com.logistimo.constants.Constants;

import java.util.Objects;

/**
 * Created by charan on 22/06/17.
 */
public class OrderApprovalFilters extends ApprovalFilters {


  private Long entityId;
  private Long orderId;


  public OrderApprovalFilters setEntityId(Long entityId) {
    this.entityId = entityId;
    setAttributeKey("entity");
    setAttributeValue(Objects.toString(entityId, Constants.EMPTY));
    return this;
  }

  public OrderApprovalFilters setOrderId(Long orderId) {
    this.orderId = orderId;
    setType("ORDER");
    setTypeId(Objects.toString(orderId, Constants.EMPTY));
    return this;
  }

  public Long getEntityId() {
    return entityId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public OrderApprovalFilters setExpiringInMinutes(Integer expiringIn) {
    setExpiringInMinutes(Objects.toString(expiringIn, Constants.EMPTY));
    return this;
  }
}
