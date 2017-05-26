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
