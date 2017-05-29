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

package com.logistimo.api.models.configuration;

import java.util.List;
import java.util.Map;

/**
 * Created by mohan raja.
 */
public class CapabilitiesConfigModel {
  public String[] cap; //capabilities
  public String[] tm; //transaction menu
  public String[] et; //entity types
  public boolean er; //edit routing time
  public boolean lr; //login reconnect
  public List<String> hi; //hide inventory
  public List<String> ho; //hide orders
  public boolean sv; //send vendors
  public boolean sc; //send customers
  public String gcs; //geo coding strategy
  public String ro; //role
  public String createdBy; //userId last saved configuration
  public String lastUpdated; //last updated time
  public String fn; //first name
  public boolean eshp; // Enable shipping orders on mobile
  public int atexp; // Authentication token expiry
  public boolean llr; // Local login required

  public Map<String, CapabilitiesConfigModel> roleConfig;

  public List<String> hii; //hide inventory material tags on issue
  public List<String> hir; //hide inventory material tags on receipt
  public List<String> hip; // hide inventory material tags on stock count
  public List<String> hiw; // hide inventory material tags on discards
  public List<String> hit; // hide inventory material tags on transfers

  public int mdri = 0; // Master data refresh interval
  public int iri = 0; // Inventory refresh interval
  public int aplui = 0; // Application log upload interval
  public int stwd = 0; // SMS transmission wait duration
}
