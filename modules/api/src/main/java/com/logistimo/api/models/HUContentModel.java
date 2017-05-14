package com.logistimo.api.models;

import java.math.BigDecimal;

/**
 * Created by yuvaraj on 16/07/16.
 */
public class HUContentModel {
  public Long id; // id
  public Long dId; // domain Id
  public Long cntId; // id of material / handling unit
  public String cntName; // Name of material / handling unit
  public Integer ty; // 0-Material / 1-Handling unit
  public BigDecimal quantity;
}
