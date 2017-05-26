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


import com.logistimo.models.superdomains.DomainSuggestionModel;

import java.util.Date;
import java.util.List;

public class UserModel {
  public String id; //User ID
  public String pw; //Password
  public String ro; //Role
  public String cid; //Custom ID
  public List<String> tgs; // Tags

  public String fnm; //First Name
  public String lnm; //Last Name
  public String gen; //Gender
  public int age; //Age

  public String phm; //Mobile Phone
  public String phl; //Land Phone
  public String em; //Email

  public String cnt; //Country
  public String st; //State
  public String ds; //District
  public String tlk; //Taluk
  public String ct; //City
  public String stn; //Street Name
  public String pin; //PIN code

  public String lng; //Language
  public String tz; //TimeZone

  public Date ll; //Last Login
  public Date lr; //Last Reconnected

  public boolean en; //Enabled / Disabled
  public Date ms; //Member since
  public String ua; //User Agent
  public String pua; //Previous User Agent
  public String ip; //IP Address
  public String av; //App Version

  public String br; //Phone Brand
  public String mo; //Phone Model
  public String sp; //Phone Service Provider

  public String msn; //Registered On
  public String genn; //Gender Name
  public String ron; //Role Display name
  public String lln; //Last Login Name
  public String lrn; //Last Reconnected Name
  public String lngn; //Language Display Name
  public String tzn; //TimeZone Display Name
  public String pk; //Primary Kiosk
  public String sId; //Sim Id
  public String imei; // IMEI no
  public List<EntityModel> entities; // User Entities

  /**
   * Formatted last login date
   */
  public String llStr;

  /**
   * Formatted last reconnect date
   */
  public String lrStr;

  public String updaBy;  //updated by
  public String updaOn;  //updated on
  public String regBy; //Regiestered By
  public String updaByn;
  public String regByn;
  public boolean isAdm; // Flag indicating that the user in this model is an Administrator
  public List<DomainSuggestionModel>
      accDsm;
  // Details wrt display of Domains to which the user has access. Includes user's source domain by default.
  public Integer lgr = -1;
  public int atexp; // token expiry
  public String per; // User Permission
  public String pd; // Permission Display
  public Integer lgSrc; // login src
  public Long sdid; //source domain
  public String sdname; //source domain name

}
