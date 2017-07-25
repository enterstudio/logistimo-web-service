package com.logistimo.approvals.client.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charan on 22/06/17.
 */
public class ApprovalFilters {

  private int offset = 0;

  private int size = 50;

  private String type;

  @SerializedName("type_id")
  private String typeId;

  private String status;

  @SerializedName("expiring_in")
  private String expiringInMinutes;

  @SerializedName("requester_id")
  private String requesterId;

  @SerializedName("approver_id")
  private String approverId;

  @SerializedName("approver_status")
  private String approverStatus;

  private List<AttributeFilter> attributes = new ArrayList<>(1);

  @SerializedName("domain_id")
  private long domainId;

  @SerializedName("order_by")
  private String orderedBy;

  public int getOffset() {
    return offset;
  }

  public ApprovalFilters setOffset(int offset) {
    this.offset = offset;
    return this;
  }

  public int getSize() {
    return size;
  }

  public ApprovalFilters setSize(int size) {
    this.size = size;
    return this;
  }

  public String getType() {
    return type;
  }

  public ApprovalFilters setType(String type) {
    this.type = type;
    return this;
  }

  public String getTypeId() {
    return typeId;
  }

  public ApprovalFilters setTypeId(String typeId) {
    this.typeId = typeId;
    return this;
  }

  public String getStatus() {
    return status;
  }

  public ApprovalFilters setStatus(String status) {
    this.status = status;
    return this;
  }

  public String getExpiringInMinutes() {
    return expiringInMinutes;
  }

  public ApprovalFilters setExpiringInMinutes(String expiringInMinutes) {
    this.expiringInMinutes = expiringInMinutes;
    return this;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public ApprovalFilters setRequesterId(String requesterId) {
    this.requesterId = requesterId;
    return this;
  }

  public String getApproverId() {
    return approverId;
  }

  public ApprovalFilters setApproverId(String approverId) {
    this.approverId = approverId;
    return this;
  }

  public String getApproverStatus() {
    return approverStatus;
  }

  public ApprovalFilters setApproverStatus(String approverStatus) {
    this.approverStatus = approverStatus;
    return this;
  }

  public long getDomainId() {
    return domainId;
  }

  public ApprovalFilters setDomainId(long domainId) {
    this.domainId = domainId;
    return this;
  }

  public String getOrderedBy() {
    return orderedBy;
  }

  public ApprovalFilters setOrderedBy(String orderedBy) {
    this.orderedBy = orderedBy;
    return this;
  }

  public List<AttributeFilter> getAttributes() {
    return attributes;
  }

  public void setAttributes(
      List<AttributeFilter> attributes) {
    this.attributes = attributes;
  }

  public void addAttribute(AttributeFilter attributeFilter) {
    this.attributes.add(attributeFilter);
  }
}
