package com.logistimo.api.models;

import java.util.List;

/**
 * Created by mohan raja on 22/01/15
 */
public class UploadDataViewModel {
  public String mnm; // Material Name
  public Long mid; // Material Id
  public String enm; // Entity Name
  public Long eid; // Entity Id
  public String cst; // Closing Stock
  public String ost; // Opening Stock
  public String rQty; // Receipt Quantity
  public String iQty; // Issue Quantity
  public String dQty; // Discards Quantity
  public String stodur; // Stock Out Duration
  //    public String noSto; // Number of stockouts
  public String mcr; // Manual Consumption Rate
  public String mcrc; // Manual Consumption Rate Color
  public String ccr; // Computed Consumption Rate
  public String moq; // Manual Order Quantity
  public String moqc; // Manual Order Quantity Color
  public String coq; // Computed Order Quantity
  public List<String> tag; // Tags
  public String ven; // Vendor
  public String repPer; // Reporting Period
  public String upBy; // Uploaded By
  public String upTm; // Upload Time
}
