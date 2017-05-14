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

/**
 *
 */
package com.logistimo.reports.entity.slices;

import java.io.Serializable;

/**
 * @author Arun
 */
@SuppressWarnings("serial")
public class Counts implements Serializable {

  public float i = 0; // issues
  public float r = 0; // receipts
  public float s = 0; // stock-counts/levels
  public float w = 0; // wastage
  public float tr = 0; // transfer
  public float sd = 0; // stock-count difference (due to physical count)
  public float o = 0; // orders

  public Counts() {
  }

  public float getTotal() {
    return (i + r + s + o + w + tr);
  }

  public String toString() {
    return "{i=" + i + ",r=" + r + ",s=" + s + ", sd = " + sd + ",o=" + o + ", w = " + w + ", tr = "
        + tr + "}";
  }
}
