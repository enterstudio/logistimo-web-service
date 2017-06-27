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

package com.logistimo.utils;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.constants.CharacterConstants;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by charan on 21/05/15.
 */
public class CommonUtils {

  public static String getFormattedPrice(float price) {
    return String.format("%.2f", price);
  }

  public static String getFormattedPrice(BigDecimal price) {
    return String.format("%.2f", price);
  }

  // Get roles visible to a given role
  public static List<String> getRoles(String role) {
    if (role == null || role.isEmpty()) {
      return null;
    }
    List<String> roles = new ArrayList<String>();
    for (int i = 0; i < SecurityConstants.ROLES.length; i++) {
      if (SecurityUtil.compareRoles(SecurityConstants.ROLES[i], role)
          <= 0) {
        roles.add(SecurityConstants.ROLES[i]);
      }
    }
    return roles;
  }


  public static Date createTimestamp(Date t, String tz) {
    Calendar
        cal =
        tz != null ? GregorianCalendar.getInstance(TimeZone.getTimeZone(tz))
            : GregorianCalendar.getInstance();
    cal.setTime(t);
    cal.set(Calendar.DATE, 1); // reset date to the first of the month
    return LocalDateUtil.getGMTZeroTimeFromCal(cal);
  }

  // Check if any item in list a is present in list b
  public static boolean listContains(List<String> a, List<String> b) {
    if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
      return false;
    }
    for (String str : a) {
      if (b.contains(str)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generates Hex encoded MD5 hash for the given string
   *
   * @param payLoad - String/Text
   * @return hex coded md5 for the given payLoad.
   */
  public static String getMD5(String payLoad) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      byte[] digestedPayLoad = messageDigest.digest(payLoad.getBytes(StandardCharsets.UTF_8));
      return new String(Hex.encodeHex(digestedPayLoad));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }


  public static String getAddress(String city, String taluk, String district, String state) {
    StringBuilder address = new StringBuilder();
    if (StringUtils.isNotBlank(city)) {
      address.append(city).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);
    }
    if (StringUtils.isNotBlank(taluk)) {
      address.append(taluk).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);
    }
    if (StringUtils.isNotBlank(district)) {
      address.append(district).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);

    }
    if (StringUtils.isNotBlank(state)) {
      address.append(state).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);
    }
    address.setLength(address.length() - 2);
    return address.toString();
  }
}
