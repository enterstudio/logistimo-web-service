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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

/**
 * @author Arun
 */
public class RestConstantsZ {
  // Parameter names
  public static final String ACTION = "a";
  public static final String CURSOR = "cs";
  public static final String EMAIL = "email";
  public static final String ENDDATE = "ed";
  public static final String ENDUSER_ID = "euid";
  public static final String ENDUSER_IDS = "euids";
  public static final String EXCLUDE_SHIPMENT_ITEMS = "xshpit";
  public static final String FILTER = "fl";
  public static final String JSON_STRING = "j";
  public static final String KIOSK_ID = "kid";
  public static final String KIOSK_IDS = "kids";
  public static final String LOADALL = "ld";
  public static final String LOCALE = "lcl";
  public static final String MATERIAL_ID = "mid";
  public static final String MATERIAL_IDS = "mids";
  public static final String MIN_RESPONSE = "mrsp";
  public static final String NOTIFICATION = "n";
  public static final String
      NUM_RESULTS =
      "r";
  // DEPRECATED: used only when fetching orders; user SIZE instead
  public static final String OLD_PASSWORD = "opwd";
  public static final String ONLY_AUTHENTICATE = "oa";
  public static final String ORDER_ID = "oid";
  public static final String ORDER_TYPE = "oty"; // order type - sale, purchase
  public static final String PASSWORD = "p";
  public static final String PERIOD = "pr";
  public static final String SHIPMENT_ID = "sid"; // shipment ID
  public static final String SIZE = "s";
  public static final String STARTDATE = "sd";
  public static final String STATUS = "st";
  public static final String TIME = "t";
  public static final String TRANS_TYPE = "ty";
  public static final String TRANSACTION_TYPE = "tty";
  public static final String
      TYPE =
      "ty";
  // NOTE: same as TRANS_TYPE, but just a generic type representation
  public static final String USER_ID = "uid";
  public static final String UPDATED_PASSWORD = "upwd";
  public static final String VERSION = "v";
  public static final String USER_AGENT = "User-Agent";
  public static final String IP_ADDRESS = "X-REAL-IP";
  public static final String PREVIOUS_UPDATED_TIME="put";


  // Action values
  public static final String ACTION_LOGIN = "li";
  public static final String ACTION_LOGOUT = "lo";
  public static final String ACTION_GETINVENTORY = "gi";
  public static final String ACTION_EXPORT = "exp";
  public static final String ACTION_UPDINVENTORY = "ui";
  public static final String ACTION_GETORDERS = "gos";
  public static final String ACTION_GETORDER = "go";
  public static final String ACTION_CANCELORDER = "co";
  public static final String ACTION_UPDATEORDER = "uo";
  public static final String ACTION_UPDATEORDERSTATUS = "uos";
  public static final String ACTION_CREATEUSERKIOSK = "cuk";
  public static final String ACTION_UPDATEUSERKIOSK = "uuk";
  public static final String ACTION_UPDATEPASSWORD = "up";
  public static final String ACTION_CREATERELATIONSHIP = "cr";
  public static final String ACTION_REMOVERELATIONSHIP = "rr";
  public static final String ACTION_UPDATERELATIONSHIP = "ur";
  public static final String ACTION_GETRELATEDENTITIES = "gre";
  public static final String ACTION_GETTRANSACTIONAGGREGATE = "gta";
  public static final String ACTION_RESETPASSWORD = "rp";
  public static final String ACTION_REMOVE = "rm";
  public static final String ACTION_GETTRANSACTIONS_OLD = "gtrn";
  public static final String ACTION_GETSHPIPMENTS = "gs";
  public static final String ACTION_GETTRANSACTIONS = "gt";

  // Filter types
  public static final String FILTER_ONLYSTOCK = "onlystck";

  // Transaction Types
  public static final String TYPE_ISSUE = "i";
  public static final String TYPE_RECEIPT = "r";
  public static final String TYPE_STOCKCOUNT = "p";
  public static final String TYPE_WASTAGE = "w";
  public static final String TYPE_RETURN = "rt";
  public static final String TYPE_ORDER = "o";
  public static final String TYPE_REORDER = "oo";
  public static final String TYPE_TRANSFER = "t";
  public static final String TYPE_USER = "u";
  public static final String TYPE_KIOSK = "k";
  public static final String TYPE_USERKIOSK = "uk";

  // Status codes
  public static final String STATUS_CANCELLED = "cn";
  public static final String STATUS_CHANGED = "ch";
  public static final String STATUS_COMPLETED = "cm";
  public static final String STATUS_CONFIRMED = "cf";
  public static final String STATUS_FULFILLED = "fl";
  public static final String STATUS_PENDING = "pn";

  // Roles
  public static final String ROLE_KIOSKOWNER = "ROLE_ko";
  public static final String ROLE_SERVICEMANAGER = "ROLE_sm";
  public static final String ROLE_DOMAINOWNER = "ROLE_do";
  public static final String ROLE_SUPERUSER = "ROLE_su";

  // Order types
  public static final String OTYPE_SALE = "sle";
  public static final String OTYPE_PURCHASE = "prc";

  // Relationship types
  public static final String RELATIONSHIP = "r";
  public static final String TYPE_CUSTOMER = "c";
  public static final String TYPE_VENDOR = "v";

  // Notification types
  public static final String TYPE_EMAIL = "e";
  public static final String TYPE_SMS = "s";
  public static final String TYPE_EMAILANDSMS = "es";

  // Status display
  public static String getStatusDisplay(String statusCode) {
    if (STATUS_CANCELLED.equals(statusCode)) {
      return "cancelled";
    } else if (STATUS_CHANGED.equals(statusCode)) {
      return "changed";
    } else if (STATUS_COMPLETED.equals(statusCode)) {
      return "shipped";
    } else if (STATUS_CONFIRMED.equals(statusCode)) {
      return "confirmed";
    } else if (STATUS_FULFILLED.equals(statusCode)) {
      return "fulfilled";
    } else if (STATUS_PENDING.equals(statusCode)) {
      return "pending";
    } else {
      return "unkown";
    }
  }
}
