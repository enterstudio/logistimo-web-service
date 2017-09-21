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

package com.logistimo.proto;

import com.google.gson.annotations.Expose;

/**
 * Created by vani on 21/09/17.
 */
public class MobileAccountingConfigModel {
  public static final String ENFORCE_CREDIT_LIMIT_ON_CONFIRM_ORDER = "cf";
  public static final String ENFORCE_CREDIT_LIMIT_ON_SHIP_ORDER = "sp";

  /**
   * Whether accounting is enabled or not
   */
  @Expose
  public Boolean enb;
  /**
   * When to enforce credit limit, "cf" if it is to be enforced when order is confirmed and "sp" if it is to be enforced when order is shipped
   */
  @Expose
  public String enfcrl;

}
