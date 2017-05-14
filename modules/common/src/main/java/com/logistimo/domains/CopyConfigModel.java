package com.logistimo.domains;

import com.google.gson.annotations.Expose;

public class CopyConfigModel {

  @Expose
  private Long srcDomainId = null;
  @Expose
  private Long destDomainId = null;

  public CopyConfigModel() {
  }

  public CopyConfigModel(Long srcDomainId, Long destDomainId) {
    this.srcDomainId = srcDomainId;
    this.destDomainId = destDomainId;
  }

  public Long getSourceDomainId() {
    return srcDomainId;
  }

  public Long getDestinationDomainId() {
    return destDomainId;
  }
}
