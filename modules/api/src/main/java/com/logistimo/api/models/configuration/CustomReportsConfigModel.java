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

import com.logistimo.api.models.UserModel;

import java.util.List;

/**
 * Created by naveensnair on 24/12/14.
 */
public class CustomReportsConfigModel {
  public String tn; // template name
  public String fn; // file Name
  public String tk; // template key
  public String dsc; // description
  public String invsn; // inventory sheet name
  public String usn; // user sheet name
  public String ksn; // entity sheet name
  public String msn; // material sheet name
  public String osn; // order sheet name
  public String od; // number of days order data can be exported
  public String tsn; // transaction sheet name
  public String mtsn; // manualtransaction sheet name
  public String td; // number of days transaction data can be exported
  public String mtd; // number of days manual transaction data can be exported
  public String tcsn; // transaction count sheet name
  public String tct; // transaction count time
  public String tce; // transaction count event
  public String tcd; // number of days transaction count data can be exported
  public String itsn; // inventory trends sheet name
  public String itt; // inventory trends time
  public String ite; // inventory trends event
  public String itd; // number of days inventory trends data can be exported
  public String hsn; // historical sheet name
  public String hd; // number of days before report generation
  public String rgth; // report generation time in hours
  public String rgtw; // report generation time week day
  public String rgtm; // report generation time month day
  public List<UserModel> mn; // manager names
  public List<UserModel> an; // administrators names
  public List<UserModel> sn; // super users names
  public boolean ib; // include batch details
  public boolean ogf; // order generation frequency
  public boolean tgf; // transaction generation frequency
  public boolean mtgf; // manual transaction generation frequency
  public boolean tcrgf; // transaction count report generation frequency
  public boolean itrgf; // inventory trends report generation frequency
  public String rgt; // report generation time
  public String lut; // last updated time
  public String ct; // creation time
  public String origname; //original name for update
  public String createdBy; // user who last updated config
  public String lastUpdated; // last updated time
  public String fName; // full name
  public List<String> exusrs;
  public List<String> usrTgs;
}
