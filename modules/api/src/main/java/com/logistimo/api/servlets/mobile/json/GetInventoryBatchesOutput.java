package com.logistimo.api.servlets.mobile.json;

import com.google.gson.annotations.Expose;


import com.logistimo.inventory.entity.IInvntryBatch;

import java.util.List;

public class GetInventoryBatchesOutput extends JsonOutput {

  @Expose
  private String mid = null; // material Id
  @Expose
  private List<IInvntryBatch> btchs = null; // batches

  public GetInventoryBatchesOutput(String version, boolean status, String message,
                                   String materialId, List<IInvntryBatch> batches) {
    super(version, status, message);
    this.mid = materialId;
    this.btchs = batches;
  }

  public String getMaterialId() {
    return mid;
  }

  public List<IInvntryBatch> getBatches() {
    return btchs;
  }
}
