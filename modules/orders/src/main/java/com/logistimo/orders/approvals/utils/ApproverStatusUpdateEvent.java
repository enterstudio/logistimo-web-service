package com.logistimo.orders.approvals.utils;

import java.util.Date;
import java.util.List;

/**
 * Created by nitisha.khandelwal on 04/07/17.
 */

public class ApproverStatusUpdateEvent {

  private String approvalId;

  private String type;

  private String typeId;

  private String requesterId;

  private Date requestedAt;

  private String userId;

  private List<String> nextApproverIds;

  private String status;

  private Date expiryTime;

  private int expiryInHours;

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

  public Date getRequestedAt() {
    return requestedAt;
  }

  public String getUserId() {
    return userId;
  }

  public List<String> getNextApproverIds() {
    return nextApproverIds;
  }

  public String getStatus() {
    return status;
  }

  public int getExpiryInHours() {
    return expiryInHours;
  }

  public Date getExpiryTime() {
    return expiryTime;
  }
}
