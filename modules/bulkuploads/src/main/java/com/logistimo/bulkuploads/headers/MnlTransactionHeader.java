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
public class MnlTransactionHeader implements IHeader {

  @Override
  public String getUploadableCSVHeader(Locale locale, String type) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String
        header =
        messages.getString("kiosk") + "* (" + messages.getString("kiosk") + " name)" + "," +
            messages.getString("material") + "* (Material name)" + "," +
            messages.getString("openingstock") + "* (Opening stock)" + "," +
            messages.getString("reportingperiod") + " (yyyy-MM-dd)," +
            messages.getString("receipts") + "," +
            messages.getString("issues") + "," +
            messages.getString("transactions.wastage.upper") + "," +
            messages.getString("stockout") + " " + messages.getString("duration") + "," +
//						messages.getString( "numberofstockouts" ) +"," +
            messages.getString("manual") + " " + messages.getString("consumption") + "," +
            messages.getString("computed") + " " + messages.getString("consumption") + "," +
            messages.getString("ordered") + " " + messages.getString("quantity") + "," +
            messages.getString("fulfilled") + " " + messages.getString("quantity") + "," +
            messages.getString("order") + " " + messages.getString("tags") + " "
            + "(semi-colon separated list of tags)" + "," +
            messages.getString("vendor") + " (" + messages.getString("vendor") + " name)";
    return header;
  }
}
