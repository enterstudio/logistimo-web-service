package com.logistimo.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Created by Mohan Raja on 19/03/15.
 */
public class MsgUtil {
  public static String cleanup(String msg) {
    return msg.replace("'", "\'");
  }

  public static String trim(String msg) {
    return msg == null ? "" : msg.trim();
  }

  public static String trimConcat(String msg1, String msg2) {
    return trimConcat(msg1, msg2, null);
  }

  public static String trimConcat(String msg1, String msg2, String separator) {
    if (separator == null) {
      return trim(msg1).concat(trim(msg2));
    } else {
      return trim(trim(msg1) + separator + trim(msg2));
    }
  }

  public static String bold(String msg) {
    return "<b>".concat(msg).concat("</b>");
  }

  public static String newLine() {
    return "<br/>";
  }

  public static String addErrorMsg(String msg) {
    return StringUtils.isNotBlank(msg) ? MsgUtil.newLine() + MsgUtil.newLine() + "- " + msg : "";
  }
}
