/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

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
