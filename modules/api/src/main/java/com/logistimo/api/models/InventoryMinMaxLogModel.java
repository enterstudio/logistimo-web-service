package com.logistimo.api.models;

import java.math.BigDecimal;

/**
 * Created by naveensnair on 22/08/16.
 */
public class InventoryMinMaxLogModel {

  /**
   * invKey - inventory key
   */
  public Long invkey;

  /**
   * cr     - consumption rate
   */
  public BigDecimal cr;

  /**
   * kid    - kiosk id
   */
  public Long kid;
  /**
   * mid    - material id
   */
  public Long mid;

  /**
   * min    - min
   */
  public BigDecimal min;

  /**
   * max    - max
   */
  public BigDecimal max;

  /**
   * t      - time stamp
   */
  public String t;

  /**
   * user name
   */
  public String username;

  /**
   * user id
   */
  public String uid;

  /**
   * source of update. [0 - user, 1 - system]
   */
  public String source;

  /**
   * minDur   - minimum duration
   */
  public BigDecimal minDur;

  /**
   * maxDur   - maximum duration
   */
  public BigDecimal maxDur;

  /**
   * freq -Min max logs frequency
   */
  public String freq;
}
