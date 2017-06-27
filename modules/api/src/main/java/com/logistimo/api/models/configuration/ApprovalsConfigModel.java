package com.logistimo.api.models.configuration;

import com.logistimo.api.models.UserModel;

import java.util.List;

/**
 * Created by naveensnair on 12/05/17.
 */
public class ApprovalsConfigModel {
  public List<UserModel> pa; //primary approvers
  public List<UserModel> sa; //secondary approvers
  public List<PurchaseSalesOrderApproval> psoa;
  public String createdBy; //user who last updated config
  public String lastUpdated; //last updated time
  public String fn; //first name
  public int px; //purchase order approval expiry time
  public int sx; //sales order approval expiry time
  public int tx; //transfer order approval expiry time

  public static class PurchaseSalesOrderApproval {
    public List<String> eTgs; //entity tags
    public boolean poa; //purchase order approval
    public boolean soa; //sales order approval
  }
}
