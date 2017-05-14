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

package com.logistimo.config.utils;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;

import com.logistimo.services.Resources;
import com.logistimo.constants.CharacterConstants;

import java.util.ResourceBundle;

/**
 * Created by charan on 08/03/17.
 */
public class DomainConfigUtil {

  /**
   * Constructs min/max duration of stock
   * Header message to be used in export sheets.
   *
   * @param dc DomainConfig Object
   * @return String e.g.,(Days of stock)
   */
  public static String getMinMaxDuration(DomainConfig dc) {
    String mmd = CharacterConstants.EMPTY;
    if (!dc.getInventoryConfig().isMinMaxAbsolute()) {
      ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", dc.getLocale());
      mmd = CharacterConstants.O_BRACKET;
      if (InventoryConfig.FREQ_WEEKLY.equals(dc.getInventoryConfig().getMinMaxDur())) {
        mmd += jsMessages.getString("weeksofstock");
      } else if (InventoryConfig.FREQ_MONTHLY.equals(dc.getInventoryConfig().getMinMaxDur())) {
        mmd += jsMessages.getString("monthsofstock");
      } else {
        mmd += jsMessages.getString("daysofstock");
      }
      mmd += CharacterConstants.C_BRACKET;
    }
    return mmd;
  }
}
