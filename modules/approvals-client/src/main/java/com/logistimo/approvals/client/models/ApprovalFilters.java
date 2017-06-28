package com.logistimo.approvals.client.models;

/**
 * Created by charan on 22/06/17.
 */
public class ApprovalFilters {

  private int offset = 0;

  private int size = 50;

  private String type;

  private String typeId;

  private String status;

  private String expiringInMinutes;

  private String requesterId;

  private String approverId;

  private String approverStatus;

  private String attributeKey;

  private String attributeValue;

  private long domainId;

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

  public String getAttributeKey() {
    return attributeKey;
  }

  public ApprovalFilters setAttributeKey(String attributeKey) {
    this.attributeKey = attributeKey;
    return this;
  }

  public String getAttributeValue() {
    return attributeValue;
  }

  public ApprovalFilters setAttributeValue(String attributeValue) {
    this.attributeValue = attributeValue;
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
}
