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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohansrinivas on 10/20/16.
 */
public class DomainStatisticsModel {
  public Long did;   //domainId
  public String dnm;  // Domain name
  public Long akc; // active kiosk count
  public Long lkc; // live kiosk count
  public Long kc; // kiosk count
  public Long uc; // user count
  public Long mlwa; // monitored live working asset
  public Long mwa;  // monitored working asset
  public Long mawa; // monitored active working asset
  public Long mac;  // monitored asset count
  public Long miac;  // monitoring  asset count
  public List<DomainStatisticsModel> child = new ArrayList<>();
  public boolean hc; // has child flag
  /**
   * Last run time
   */
  public String lrt;

}
