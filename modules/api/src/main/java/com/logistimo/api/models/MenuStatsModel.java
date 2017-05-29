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

import com.logistimo.api.models.configuration.AssetConfigModel;
import com.logistimo.api.models.configuration.SupportConfigModel;

import java.util.Date;
import java.util.List;

/**
 * Created by Mohan Raja on 09/03/15.
 */
public class MenuStatsModel {
  public boolean iAccTbEn; // Is Accounts Tab Enabled
  public boolean iOrdTbEn; // Is Orders Tab Enabled
  public boolean iConfTbEn; // Is Configuration Tab Enabled
  public boolean iRepTbEn; // Is Reports Tab Enabled
  public boolean iAdm; // Is Administrator and SuperUser
  public boolean iSU; // Is SuperUser
  public boolean iMan; // Is Manager
  public boolean iTempOnly;
  public boolean iTempWLg;
  public String hImg; //Header Image
  public boolean iOCEnabled; // Is Old Console Enabled
  public boolean onlyNewUI; // Is domain a new domain
  public boolean iATD; // Is Actual Transaction Date enabled
  public boolean iPredEnabled; // Is Predictive Enabled
  public int mmt; // Min Max type
  public String mmd; // Min Max Duration
  public AssetConfigModel ac;

  /**
   * Is Demand Only.. Disable Orders.. Just capture demand.
   */
  public boolean iDmdOnly;
  /**
   * Default Domain currency.
   */
  public String cur;
  public List<SupportConfigModel> support;
  /**
   * Manager has more than max allowed entities
   */
  public boolean mxE = false;
  public boolean accd = false;
  public boolean iAU; // Is Asset User
  /**
   * Country
   */
  public String cnt;
  /**
   * State
   */
  public String st;
  /**
   * District
   */
  public String dst;

  public String dnm; //Domain name
  public String unm; //User name
  public String lng; //Language
  public Long eid; //Default entity Id.
  public String em; //email id.
  public String ufn; // user full name
  public Date createdOn;
  public Long dId;
  public boolean hasChild;
  public boolean tr; //Is transfer or release
  public boolean allocateInventory; // Allocate quantity to demand item
  /**
   * Hide by user tags
   */
  public boolean hbUTag;
  /**
   * view transfer order
   */
  public boolean vt = false;
  /**
   * create transfer order
   */
  public boolean ct = false;
  /**
   * view orders
   */
  public boolean vo = false;
  /**
   * order new stock
   */
  public boolean ns = false;
  /**
   * Is creating shipments allowed
   */
  public boolean acs = false;
  /**
   * revenue tab flag in user activity panel
   */
  public boolean rpe = false;
  /**
   * Manager has dashboard access or not
   */
  public boolean mdp = false;
}
