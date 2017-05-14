package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by naveensnair on 24/08/15.
 */
public class InvntryBatchModel {
  public List<Long> dId; // domain id
  public Long sdId; // source domain Id
  public Long kId; // kiosk id
  public Long mId; // material id
  public BigDecimal q = BigDecimal.ZERO; // quantity
  public String bid; // batch id/number
  public Date bexp; // batch expiry date
  public String bmfnm; // batch manufacturer name
  public Date bmfdt; // batch manufactured date
  public Date t; // created/last updated
  public String knm; // normalized kiosk name (for sorting in queries)
  public List<String> mtgs; // material tags
  public List<String> ktgs; // kiosk tags
  public Boolean vld = false; // true, if q > 0 and not expired
  public Boolean isExp; //expired batches
  public Integer perm; // Permission
  /**
   * Available to promise stock
   */
  public BigDecimal atpstk;
  /**
   * Allocated stock
   */
  public BigDecimal astk;
  /**
   * Order allocated quantity
   */
  public BigDecimal oastk;
}
