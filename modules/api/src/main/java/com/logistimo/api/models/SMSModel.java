package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Mohan Raja
 */
public class SMSModel {
  public String token;
  public String type;
  public List<SMSInv> materials;
  public String partialId;
  public Date saveTS;
  public Date actualTS;
  public String actualTD;
  public String userId;
  public Long kioskId;
  public Long destKioskId;

  public Long domainId;

  public void addMaterial(long id, BigDecimal quantity, BigDecimal closingStock) {
    if (materials == null) {
      materials = new ArrayList<>();
    }
    SMSInv i = new SMSInv();
    i.id = id;
    i.quantity = quantity;
    i.cs = closingStock;
    materials.add(i);
  }

  public boolean isValid() {
    return token != null && type != null && materials != null && materials.size() > 0 &&
        saveTS != null && userId != null && kioskId != null;
  }

  public static class SMSInv {
    public long id; // Short Id for inventory
    public long matId; // Material Id from short Id of inventory
    public BigDecimal quantity;
    public BigDecimal cs; //Closing Stock
    public BigDecimal curStk; //Current Stock
  }
}
