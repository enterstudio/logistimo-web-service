package com.logistimo.api.models.configuration;

import java.math.BigDecimal;

/**
 * Created by naveensnair on 12/11/14.
 */
public class AccountingConfigModel {
  public boolean ea; // enable accounting
  public BigDecimal cl = BigDecimal.ZERO; // credit limit
  public String en; // enforce
  public String createdBy;// user updated last config
  public String lastUpdated;// last updated time
  public String fn; //last name
}
