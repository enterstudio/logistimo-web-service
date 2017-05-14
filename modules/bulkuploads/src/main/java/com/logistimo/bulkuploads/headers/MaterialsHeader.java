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
public class MaterialsHeader implements IHeader {

  // Get the uploadable CSV header
  public String getUploadableCSVHeader(Locale locale, String type) {
    ResourceBundle bundle = Resources.get().getBundle("Messages", locale);
    String
        format =
        "Operation* (a = add / e = edit / d = delete; if empty it is defaulted to add; ensure that the Material Name is EXACT for edit/delete),"
            +
            bundle.getString("material.name") + "* (1-75 characters)," + bundle
            .getString("shortname") + " (6 characters max.; for use in SMS mode only)," +
            bundle.getString("description") + " (max. 80 characters)," +
            bundle.getString("material.addinfo") + " (max. 400 characters)," +
            bundle.getString("material.addinfocheck") + " (yes/no; defaults to 'yes')," +
            bundle.getString("tags") + " (semi-colon separated tag names - e.g. tag1;tag2;tag3),"
            + bundle.getString("isbinaryvalued") + " (yes/no; default is 'no')," +
            bundle.getString("material.seasonal") + " (yes/no; default is 'no')," +
            bundle.getString("material.msrp") + " (valid number)," + bundle
            .getString("material.retailerprice") + " (valid number)," +
            bundle.getString("currency")
            + " (ISO-4217 3-letter codes as at http://en.wikipedia.org/wiki/ISO_4217)," +
            bundle.getString("material.name") + "[" + bundle.getString("new")
            + "] (new name if material name is to be modified; used ONLY if operation is edit)," +
            bundle.getString("customid.material") + " (Not more than 300 characters)," +
            bundle.getString("batch.enable") + " (yes/no; default is 'no')," +
            bundle.getString("temperature.sensitive") + " (yes/no; default is 'no')," +
            bundle.getString("temperature") + " " + bundle.getString("min") + "(" + bundle
            .getString("temperature.indegreescelsius") + ")," +
            bundle.getString("temperature") + " " + bundle.getString("max") + "(" + bundle
            .getString("temperature.indegreescelsius") + ")";

    return format;
  }
}
