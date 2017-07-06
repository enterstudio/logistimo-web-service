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

package com.logistimo.api.models.mobile;

/**
 * Created by kumargaurav on 22/06/17.
 */
public class DashQueryModel {

  public String country;
  public String state;
  public String district;
  public String incetags; // store tags inclusive
  public String exetags; // store tags inclusive
  public String mtags; // material tags
  public String mnm; //material name
  public String loc; //location
  public Integer p; //inventory dashboard period
  public String date; //date
  public Long domainId; //domain id
  public String groupby; //group by
  public String tp; //temp. dashboard period
  public String aty; //asset type
  public String locty; //location type
  public String timezone; // timezone

  public DashQueryModel() {
  }

  public DashQueryModel(String country, String state, String district, String incetags,
                        String exetags, String mtags, String mnm, String loc, Integer p,
                        String date, Long domainId, String groupby) {
    this.country = country;
    this.state = state;
    this.district = district;
    this.incetags = incetags;
    this.exetags = exetags;
    this.mtags = mtags;
    this.mnm = mnm;
    this.loc = loc;
    this.p = p;
    this.date = date;
    this.domainId = domainId;
    this.groupby = groupby;
  }

  public DashQueryModel(String country, String state, String district, String excludeETags, String loc, String tPeriod, Long domainId, String assetType, String level) {
    this.country = country;
    this.state = state;
    this.district = district;
    this.exetags = excludeETags;
    this.loc = loc;
    this.tp = tPeriod;
    this.domainId = domainId;
    this.aty = assetType;
    this.locty = level;
  }

}

