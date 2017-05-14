package com.logistimo.proto;

import java.util.List;

/**
 * Created by vani on 18/01/17.
 */
public class MobileTransactionsModel {
  /**
   * String version
   */
  public String v;
  /**
   * String status
   */
  public String st;
  /**
   * Kiosk id
   */
  public Long kid;
  /**
   * Transaction list
   */
  public List<MobileTransactionModel> trn;
}
