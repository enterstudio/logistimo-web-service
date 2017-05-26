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
