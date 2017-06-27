package com.logistimo.orders.approvals.utils;

import java.util.Date;

/**
 * Created by nitisha.khandelwal on 02/06/17.
 */

public class ApprovalStatusUpdateEvent {

  private String approvalId;

  private String type;

  private String typeId;

  private String status;

  private String updatedBy;

  private Date updatedAt;

  public String getApprovalId() {
    return approvalId;
  }

  public String getType() {
    return type;
  }

  public String getTypeId() {
    return typeId;
  }

  public String getStatus() {
    return status;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }
}
