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

import com.logistimo.constants.CharacterConstants;
import com.logistimo.services.Resources;
import com.logistimo.utils.FieldLimits;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class KiosksHeader implements IHeader {

  public String getUploadableCSVHeader(Locale locale, String type) {
    ResourceBundle bundle = Resources.get().getBundle("BackendMessages", locale);
    ResourceBundle messageBundle = Resources.get().getBundle("Messages", locale);
    String format;
    if ("assets".equalsIgnoreCase(type)) {
      format = bundle.getString("kiosk") + CharacterConstants.COMMA +
          bundle.getString("custom.entity") + CharacterConstants.COMMA +
          bundle.getString("city") + CharacterConstants.COMMA +
          bundle.getString("district") + CharacterConstants.COMMA +
          bundle.getString("state") + CharacterConstants.COMMA +
          bundle.getString("asset.type") + CharacterConstants.COMMA +
          bundle.getString("bulk.asset.id") + CharacterConstants.COMMA +
          bundle.getString("manufacturer") + CharacterConstants.COMMA +
          bundle.getString("model") + CharacterConstants.COMMA +
          bundle.getString("sensor.device.id") + CharacterConstants.COMMA +
          bundle.getString("sim1") + CharacterConstants.COMMA +
          bundle.getString("sim1.id") + CharacterConstants.COMMA +
          bundle.getString("sim1.ntw.provider") + CharacterConstants.COMMA +
          bundle.getString("sim2") + CharacterConstants.COMMA +
          bundle.getString("sim2.id") + CharacterConstants.COMMA +
          bundle.getString("sim2.ntw.provider") + CharacterConstants.COMMA +
          bundle.getString("imei");
    } else {
      format =
          "Operation* (a = add / e = edit / d = delete; if empty it is defaulted to add; ensure "
              + messageBundle.getString("kiosk.lower") + " name is EXACT for edit/delete)," +
              messageBundle.getString("kiosk.name") + "* (1-" + FieldLimits.TEXT_FIELD_MAX_LENGTH
              + ") characters)," +
              messageBundle.getString("users")
              + "* (semi-colon separated list of user IDs - e.g. user1;user2;user3)," +
              messageBundle.getString("country")
              + "* (ISO-3166 2-letter codes as at http://userpage.chemie.fu-berlin.de/diverse/doc/ISO_3166.html),"
              +
              messageBundle.getString("state")
              + "* (should be the same as in the corresponding LogiWeb drop-downs)," + messageBundle
              .getString("village") + "* (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH
              + " characters)" + "," +
              messageBundle.getString("latitude")
              + " (should be a number rounded to eight decimal places maximum between "
              + FieldLimits.LATITUDE_MIN + " and " + FieldLimits.LATITUDE_MAX + ")" + ","
              + messageBundle
              .getString("longitude")
              + " (should be a number rounded to eight decimal places maximum between"
              + FieldLimits.LONGITUDE_MIN + " and " + FieldLimits.LONGITUDE_MAX + ")" + "," +
              messageBundle.getString("district")
              + " (should be the same as in the corresponding LogiWeb drop-downs)," + messageBundle
              .getString("taluk")
              + " (should be the same as in the corresponding LogiWeb drop-downs)," +
              messageBundle.getString("streetaddress") + " (not more than "
              + FieldLimits.STREET_ADDRESS_MAX_LENGTH + " characters)" + "," +
              messageBundle.getString("zipcode") + " (not more than "
              + +FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
              messageBundle.getString("currency")
              + " (ISO-4217 3-letter codes as at http://en.wikipedia.org/wiki/ISO_4217)," +
              messageBundle.getString("tax") + " (in % between " + FieldLimits.TAX_MIN_VALUE
              + " and " + FieldLimits.TAX_MAX_VALUE + "; rounded to two decimal places maximum)," +
              messageBundle.getString("tax.id") + " (not more than "
              + +FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
              messageBundle.getString("inventory.policy")
              + " (sq = System-determined replenishment - s/Q; leave empty for default; default is user-determined replenishment),"
              + messageBundle.getString("inventory.servicelevel") + " (in %)," +
              messageBundle.getString("kiosk.name") + "[" + messageBundle.getString("new")
              + "] (new name if " + messageBundle.getString("kiosk.lower")
              + " name is to be modified; used ONLY if operation is edit)," +
              messageBundle.getString("add") + " " + messageBundle.getString("all") + " "
              + messageBundle.getString("materials")
              + "? (yes/no; 'yes' implies all materials will be added to this " + messageBundle
              .getString("kiosk.lower") + "; default is 'no'; used only with 'add' operation)," +
              messageBundle.getString("materials")
              + " (semi-colon separated Material Names if ALL materials are not to be added; e.g. material-name1;material-name2;material-name3; use only with 'add' operation and ENSURE that these materials are present),"
              +
              messageBundle.getString("stock") + " (initial stock level for all materials in this "
              + messageBundle.getString("kiosk.lower") + "; default is 0)," +
              messageBundle.getString("customers") + " (semi-colon separated " + messageBundle
              .getString("customer.lower") + " names; " + messageBundle.getString("kiosks.example")
              + " use only with 'add' operation and ENSURE that these " + messageBundle
              .getString("kiosks.lowercase") + " are already present)," +
              messageBundle.getString("vendors") + " (semi-colon separated " + messageBundle
              .getString("vendor.lower") + " names; " + messageBundle.getString("kiosks.example")
              + " use only with 'add' operation and ENSURE that these " + messageBundle
              .getString("kiosks.lowercase") + " are already present)," +
              messageBundle.getString("tags") + " (semi-colon separate tags; e.g. tag1;tag2;tag3),"
              +
              messageBundle.getString("customid.entity") + " (not more than "
              + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)," +
              messageBundle.getString("disable.batch") + " (true/false ; defaults to false)";
    }

    return format;
  }
}
