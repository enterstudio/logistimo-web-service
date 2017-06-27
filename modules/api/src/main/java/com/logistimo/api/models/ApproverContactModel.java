package com.logistimo.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by charan on 22/06/17.
 */
public class ApproverContactModel extends UserContactModel {

  @SerializedName("approver_type")
  private String approverType;

  public String getApproverType() {
    return approverType;
  }

  public void setApproverType(String approverType) {
    this.approverType = approverType;
  }
}
