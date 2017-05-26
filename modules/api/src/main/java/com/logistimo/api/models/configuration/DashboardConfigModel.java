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

/**
 * Created by Mohan Raja on 02/04/15.
 */
public class DashboardConfigModel {
  public boolean edm; //Dashboard panel enabled to managers
  public boolean ape; //Activity panel enabled
  public boolean rpe; //Revenue panel enabled
  public boolean ope; //Order panel enabled
  public boolean ipe; //Inventory panel enabled
  public String[] dmtg; //Default Material tag
  public String dimtg; //Default Inventory Material tag
  public String detg; //Default Entity tag
  public String aper; //Default period for activity
  public String dtt; //Default Transaction type
  public boolean atdd; //Actual date of transaction disabled
  public String createdBy; // user who last updated config
  public String lastUpdated; // last updated time
  public String fn; // first name
  public String[] exet; //list of exclude entity tags
  /**
   * Hide activity dashboard and reports for list of user tags
   */
  public String[] dutg;
  /**
   * List of exclude temperature status
   */
  public String[] exts;
}
