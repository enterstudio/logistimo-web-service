package com.logistimo.api.models;

/**
 * Created by mohan raja on 12/11/14
 */
public class InventoryBatchMaterialModel {
  public int slno;
  public Long mId; //materialId;
  public String mat; //material
  public Long eid; //ENtity Id
  public String ent; //entity
  public String bat; //batch
  public String exp; //expiry
  public String manr; //manufacturer
  public String mand; //manufactured
  public String cst; //current stock
  public String lup; //last updated

  public String t; //tag
  public String tt; //tag type
  public String o; //offset
  public String s; //size
  public Long sdid; //source domain id
  public String sdname; //source domain name
  public String add; //address
}
