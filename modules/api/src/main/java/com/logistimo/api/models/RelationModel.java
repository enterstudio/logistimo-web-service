package com.logistimo.api.models;

import java.math.BigDecimal;

/**
 * Created by mohan raja on 12/12/14
 */
public class RelationModel {
  public long entityId;
  public String[] linkIds;
  public BigDecimal cl; // Credit Limit
  public String desc; // Description
  public String linkType; // Customer / Vendor
}
