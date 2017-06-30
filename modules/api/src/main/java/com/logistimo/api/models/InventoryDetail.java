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

import com.logistimo.api.models.mobile.CurrentStock;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by yuvaraj on 25/04/17.
 */
public class InventoryDetail implements Serializable {

  protected static final long serialVersionUID = 1L;
  /**
   * Material id
   */
  public Long mId;
  /**
   * Material name
   */
  public String mnm;
  /**
   * Entity id
   */
  public Long eId;
  /**
   * Entity Name
   */
  public String enm; //Entity Name
  /**
   * Total stock count
   */
  public BigDecimal tc = BigDecimal.ZERO;
  /**
   * Allocated stock count
   */
  public BigDecimal a = BigDecimal.ZERO;
  /**
   * Available stock count
   */
  public BigDecimal av = BigDecimal.ZERO;
  /**
   * In transit stock count
   */
  public BigDecimal it = BigDecimal.ZERO;
  /**
   * Min stock
   */
  public BigDecimal min = BigDecimal.ZERO; // re-order levels or MIN
  /**
   * Max Stock
   */
  public BigDecimal max = BigDecimal.ZERO;
  /**
   * Last updated time (can be in format dd/MM/yyyy HH:mm)
   */
  public String lu;
  /**
   * Stock event
   */
  public int se;
  /**
   * Event duration
   */
  public long ed;
  /**
   * Material Tags
   */
  public List<String> mat_tgs;
  /**
   * Material description
   */
  public String description;
  /**
   * Material Image path
   */
  public String img_path;
  /**
   * Current Date
   */
  public long current_date;
  /**
   * Current Stock
   */
  public CurrentStock currentStock;
}
