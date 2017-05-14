package com.logistimo.utils;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;

import org.apache.commons.codec.binary.Hex;

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


}
