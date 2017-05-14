package com.logistimo.api.models;

import com.logistimo.models.shipments.ShipmentItemBatchModel;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author charan
 */
public class DemandItemBatchModel {

  public BigDecimal q;
  public BigDecimal fq;
  public String e;
  public String m;
  public String mdt;
  public String id;
  public String mst;
  /**
   * Used for displaying tooltip breakdown on Order detail page
   */
  public List<ShipmentItemBatchModel> bd;
}
