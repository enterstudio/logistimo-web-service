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

import java.util.List;

/**
 * Created by mohan raja on 22/01/15
 */
public class UploadDataViewModel {
  public String mnm; // Material Name
  public Long mid; // Material Id
  public String enm; // Entity Name
  public Long eid; // Entity Id
  public String cst; // Closing Stock
  public String ost; // Opening Stock
  public String rQty; // Receipt Quantity
  public String iQty; // Issue Quantity
  public String dQty; // Discards Quantity
  public String stodur; // Stock Out Duration
  //    public String noSto; // Number of stockouts
  public String mcr; // Manual Consumption Rate
  public String mcrc; // Manual Consumption Rate Color
  public String ccr; // Computed Consumption Rate
  public String moq; // Manual Order Quantity
  public String moqc; // Manual Order Quantity Color
  public String coq; // Computed Order Quantity
  public List<String> tag; // Tags
  public String ven; // Vendor
  public String repPer; // Reporting Period
  public String upBy; // Uploaded By
  public String upTm; // Upload Time
}
