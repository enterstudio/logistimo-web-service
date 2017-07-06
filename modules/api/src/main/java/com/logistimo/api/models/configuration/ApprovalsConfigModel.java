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
 * Created by naveensnair on 12/05/17.
 */
public class ApprovalsConfigModel {
  public List<UserModel> pa; //primary approvers
  public List<UserModel> sa; //secondary approvers
  public List<PurchaseSalesOrderApproval> psoa;
  public String createdBy; //user who last updated config
  public String lastUpdated; //last updated time
  public String fn; //first name
  public int px; //purchase order approval expiry time
  public int sx; //sales order approval expiry time
  public int tx; //transfer order approval expiry time

  public static class PurchaseSalesOrderApproval {
    public List<String> eTgs; //entity tags
    public boolean poa; //purchase order approval
    public boolean soa; //sales order approval
  }
}
