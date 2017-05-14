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

package com.logistimo.bulkuploads.headers;

import com.logistimo.services.Resources;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class InventoryHeader implements IHeader {

  @Override
  public String getUploadableCSVHeader(Locale locale, String type) {
    ResourceBundle bundle = Resources.get().getBundle("Messages", locale);
    String
        format =
        "Operation* (a = add / e = edit / d = delete; if empty it is defaulted to add; ensure that the Material Name and "
            + bundle.getString("kiosk")
            + " Name are EXACT for edit/delete; if inventory already exists then add will perform edit operation),"
            +
            bundle.getString("kiosk.name") + "* (1-50 characters)," +
            bundle.getString("material.name") + "* (1-75 characters)," +
            bundle.getString("material.reorderlevel") + "," +
            bundle.getString("max") + "," +
            bundle.getString("config.consumptionrates") + "," +
            bundle.getString("material.retailerprice") + "," +
            bundle.getString("tax") + "," +
            bundle.getString("inventory.model")
            + " (sq = system defined replenishment; us = user specified replenishment; leave empty to keep existing value),"
            +
            bundle.getString("inventory.servicelevel") + " (65 - 99)";
    return format;
  }
}
