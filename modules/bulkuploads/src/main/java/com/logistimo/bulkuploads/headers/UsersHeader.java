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

import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.services.Resources;
import com.logistimo.utils.FieldLimits;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class UsersHeader implements IHeader {

  @Override
  public String getUploadableCSVHeader(Locale locale, String type) {
    ResourceBundle bundle = Resources.get().getBundle("Messages", locale);
    String
        format =
        "Operation* (a = add / e = edit / d = delete; if empty it is defaulted to add; all operations MUST include User ID),"
            +
            bundle.getString("user.id")
            + "* (a unique username without any spaces; " + FieldLimits.USERID_MIN_LENGTH + CharacterConstants.HYPHEN + FieldLimits.USERID_MAX_LENGTH + " characters; Tip: prefix it with [say] your organization code or suffix it with [say] a number for uniqueness),"
            + bundle.getString("login.password") + "* (" + FieldLimits.PASSWORD_MIN_LENGTH + CharacterConstants.HYPHEN + FieldLimits.PASSWORD_MAX_LENGTH + " characters)" + "," + bundle
            .getString("user.confirmpassword") + "* (same as password)," +
            bundle.getString("user.role") + "* (" + bundle.getString("role.domainowner")
            + " = ROLE_do / " + bundle.getString("role.kioskowner") + " = ROLE_ko / " + bundle
            .getString("role.servicemanager") + " = ROLE_sm)," +
            bundle.getString("user.firstname") + "* (" + FieldLimits.FIRSTNAME_MIN_LENGTH + CharacterConstants.HYPHEN + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" +"," + bundle
            .getString("user.lastname") + " (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
            bundle.getString("user.mobile")
            + "* " + "(not more than " + FieldLimits.MOBILE_PHONE_MAX_LENGTH + " characters; format: [country-code][space][number-without-spacesORdashes])," +
            bundle.getString("user.email") + " (* not more than " + FieldLimits.EMAIL_MAX_LENGTH + " characters; mandatory if user role is " + bundle
            .getString("kiosk") + " Manager or higher)," +
            bundle.getString("country")
            + "* (ISO-3166 2-letter codes as at http://userpage.chemie.fu-berlin.de/diverse/doc/ISO_3166.html),"
            + bundle.getString("language")
            + "* (ISO-639-1 2-letter codes as listed at http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes),"
            +
            bundle.getString("preferredtimezone")
            + "* (Timezones can be downloaded from the bulk upload page of LogiWeb)," +
            bundle.getString("user.gender") + " (Male = m / Female = f)," + bundle
            .getString("user.age") + " (in years between " + FieldLimits.AGE_MIN + " and " + FieldLimits.AGE_MAX + " without decimals)," +
            bundle.getString("user.landline") + "* " + "(not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters; format: [country-code][space][number-without-spacesORdashes])," +
            bundle.getString("state")
            + "* (should be the same as in the corresponding LogiWeb drop-downs)," + bundle
            .getString("district")
            + " (should be the same as in the corresponding LogiWeb drop-downs)," + bundle
            .getString("taluk")
            + " (should be the same as in the corresponding LogiWeb drop-downs)," +
            bundle.getString("village") + " (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + ","
            + bundle.getString("streetaddress") + " (not more than " + FieldLimits.STREET_ADDRESS_MAX_LENGTH + " characters)" + "," + bundle
            .getString("zipcode") + " (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
            bundle.getString("user.oldpassword")
            + " (specify ONLY IF you wish to change the password AND operation is edit)" + "," +
            bundle.getString("customid.user") + " (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
            bundle.getString("user.mobilebrand") + " (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
            bundle.getString("user.mobilemodel") + " (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
            bundle.getString("user.imei") + " (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
            bundle.getString("user.mobileoperator") + " (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
            bundle.getString("user.simId") + " (not more than " + FieldLimits.TEXT_FIELD_MAX_LENGTH + " characters)" + "," +
            bundle.getString("tags") + " (semi-colon separate tags; e.g. tag1;tag2;tag3)";
    return format;
  }
}
