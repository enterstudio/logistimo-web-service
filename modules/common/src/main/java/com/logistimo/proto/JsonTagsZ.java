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
 * Short-forms of JsonTags for newer versions of the protocol
 *
 * @author Arun
 */
public class JsonTagsZ {
  // Json Keys
  public static final String ACTUAL_TRANSACTION_DATE = "atd";
  public static final String ADD_ALL_MATERIALS = "aam";
  public static final String AGE = "ag";
  public static final String ALL = "all";
  public static final String ALLOCATED_QUANTITY = "alq";
  public static final String ALLOW_EMPTY_ORDERS = "emor";
  public static final String ALLOW_ROUTETAG_EDITING = "arte";
  public static final String ALTITUDE = "galt";
  public static final String APP_UPGRADE = "aupg";
  public static final String ASSET_ONLY = "asnly";
  public static final String AUTOMATICALLY_POST_ISSUES_ON_SHIPPING_ORDER = "api";
  public static final String AUTOMATICALLY_POST_RECEIPTS_ON_FULFILLING_ORDER = "apr";
  public static final String AVAILABLE_QUANTITY = "avq";
  public static final String BATCH_ENABLED = "ben";
  public static final String BATCH_EXPIRY = "bexp";
  public static final String BATCH_ID = "bid";
  public static final String BATCH_MANUFACTUER_NAME = "bmfnm";
  public static final String BATCH_MANUFACTURED_DATE = "bmfdt";
  public static final String BATCHES = "bt";
  public static final String CAPTURE_ACTUAL_TRANSACTION_DATE = "catd";
  public static final String CITY = "cty";
  public static final String CLOSING_STOCK = "cstk";
  public static final String
      CLOSING_STOCK_IN_BATCH =
      "cstkb";
  // Changed from csb to cstkb for naming consistency
  public static final String CONFIGURATION = "cf";
  public static final String CREDIT_LIMIT = "crl";
  public static final String CR_DAILY = "crD"; // daily consumption rate
  public static final String CR_WEEKLY = "crW"; // weekly consumption rate
  public static final String CR_MONTHLY = "crM"; // monthly consumption rate
  public static final String
      CREATABLE_ENTITY_TYPES =
      "crents";
  // type of entities that can be created (CSV list - either ents,csts,vnds)
  public static final String CONFIRMED_FULFILLMENT_TIMERANGE = "cft";
  public static final String COUNTRY = "cn";
  public static final String DEFAULT = "df";
  public static final String CURRENCY = "cu";
  public static final String CURSOR = "cs";
  public static final String CUSTOMERS = "csts";
  public static final String CUSTOMERS_MANDATORY = "cstm";
  public static final String CUSTOM_KIOSKID = "ckid";
  public static final String CUSTOM_MATERIALID = "cmid";
  public static final String CUSTOM_USERID = "cuid";
  public static final String CUSTOM_VENDORID = "cvid";
  public static final String DATA = "dat";
  public static final String DATA_TYPE = "dty";
  public static final String DATE_FORMAT = "df";
  public static final String DATE_OF_ACTUAL_RECEIPT = "dar";
  public static final String DEMAND_COUNT = "dc";
  public static final String DEMAND_FORECAST = "dfr"; // demand forecast
  public static final String DEMAND_QUANTITY = "dq";
  public static final String DESCRIPTION = "dsc";
  public static final String DEST_USER_ID = "duid";
  public static final String DISCARDS = "w";
  public static final String DIMENSION_TYPE = "dt";
  public static final String DIMENSION_VALUE = "dv";
  public static final String DISABLE_BATCH_MGMT = "dbm";
  public static final String DISABLE_ORDER_PRICING = "dop";
  public static final String DISCOUNT = "dsnt";
  public static final String DISTRICT = "dst";
  public static final String ENABLE_SHIPPING_MOBILE = "eshp";
  public static final String EOQ = "eoq"; // economic order quantity
  public static final String EMAIL = "eml";
  public static final String ENFORCE_HANDLING_UNIT_CONSTRAINT = "ehuc";
  public static final String ERRORS = "er";
  public static final String ERROR_CODE = "cd";
  public static final String ESTIMATED_FULFILLMENT_TIMERANGES = "efts";
  public static final String EXPECTED_TIME_OF_ARRIVAL = "eta";
  public static final String REQUIRED_BY_DATE = "rbd";
  public static final String EXTENSION = "xt";
  public static final String FIRST_NAME = "fn";
  public static final String FULFILLED_QUANTITY = "flq";
  public static final String GATEWAY_PHONE_NUMBER = "gwph";
  public static final String GATEWAY_ROUTING_KEYWORD = "gwky";
  public static final String GENDER = "gn";
  public static final String GEO_ACCURACY = "gacc";
  public static final String GEO_ERROR_CODE = "gerr";
  public static final String GEOCODING_STRATEGY = "gcds";
  public static final String HANDLING_UNIT = "hu";
  public static final String HANDLING_UNIT_ID = "hid";
  public static final String HANDLING_UNIT_NAME = "hnm";
  public static final String IGNORE_ORDER_RECOMMENDATION_REASONS = "igorrsns";
  public static final String IS_TEMPERATURE_SENSITIVE = "istmp"; // SENSITIVE
  public static final String INFO = "inf";
  public static final String INVENTORY_ID = "iid";
  public static final String INVENTORY_ITEMS_ABNORMAL = "invitmsabn";
  public static final String INVENTORY_POLICY = "ip";
  public static final String INTERVALS = "intrvls";
  public static final String INTERVAL_REFRESHING_CONFIG_HOURS = "cfgrf";
  public static final String INTERVAL_REFRESHING_MASTER_DATA_HOURS = "mdrf";
  public static final String INTERVAL_REFRESHING_INVENTORY_HOURS = "invrf";
  public static final String INTERVAL_SENDING_STATS_HOURS = "stats";
  public static final String INTERVAL_SENDING_SUPPORT_LOG_HOURS = "splgs";
  public static final String INTERVAL_SENDING_APP_LOG_HOURS = "applgs";
  public static final String INTERVAL_WAIT_BEFORE_SENDING_SMS_HOURS = "smsw";
  public static final String INTRANSIT_QUANTITY = "itq";
  public static final String ISSUES = "i";
  public static final String ISSUE_COUNT = "ic";
  public static final String ISSUE_QUANTITY = "iq";
  public static final String HOST_DOMAIN_NAME = "hst";
  public static final String KIOSK = "k";
  public static final String KIOSK_ID = "kid";
  public static final String KIOSKS = "ki";
  public static final String LANDLINE = "ll";
  public static final String LANGUAGE = "lg";
  public static final String LAST_NAME = "ln";
  public static final String LATITUDE = "lat";
  public static final String LOGIN_AS_RECONNECT = "lasr";
  public static final String LONGITUDE = "lng";
  public static final String LINKED_KIOSK_ID = "lkid";
  public static final String LINKED_KIOSK_NAME = "lknm";
  public static final String LINKED_KIOSKS = "lks";
  public static final String LINKED_KIOSK_IDS_TOBEREMOVED = "lkidsrm";
  public static final String LINK_TYPE = "lkty";
  public static final String MANDATORY = "mnd";
  public static final String MANUFACTURER_PRICE = "mp";
  public static final String MATERIAL_ID = "mid";
  public static final String MATERIAL_NAME = "mnm";
  public static final String MATERIALS = "mt";
  public static final String MAX = "max";
  public static final String MESSAGE = "ms";
  public static final String MIN = "min";
  public static final String MINDUR = "dmin";
  public static final String MAXDUR = "dmax";
  public static final String MINMAX_FREQUENCY = "frmx";
  public static final String MMDUR = "dmm";
  public static final String MOBILE = "mob";
  public static final String MANAGE_MASTER_DATA = "mgmd";
  public static final String MATERIAL_STATUS = "mst";
  public static final String MATERIAL_STATUS_OPERATION = "mtst";
  public static final String NAME = "n";
  public static final String NEW_NAME = "nn";
  public static final String NO_LOCAL_LOGIN_WITH_VALID_TOKEN = "nll";
  public static final String NUM_RESULTS = "r";
  public static final String OLD_PASSWORD = "op";
  public static final String OPENING_STOCK = "ostk";
  public static final String
      OPENING_STOCK_IN_BATCH =
      "ostkb";
  // Changed from osb to ostkb for naming consistency
  public static final String ORDER_CANCELLATION_REASONS = "rsnsco";
  public static final String ORDER_CONFIG = "ords";
  public static final String ORDER_GENERATION = "ogn";
  public static final String ORDER_MARKASFULFILLED = "omaf";
  public static final String ORDER_PERIODICITY = "opd";
  public static final String ORDER_STATUS = "ost";
  public static final String ORDERS = "os";
  public static final String ORDER_TAGS = "otg";
  public static final String ORDER_TYPE = "oty";
  public static final String ORIGINAL_ORDERED_QUANTITY = "oq";
  public static final String PACKAGE_SIZE = "pksz";
  public static final String PART_ID = "pid";
  public static final String PASSWORD = "p";
  public static final String PAYABLE = "pyb";
  public static final String PAYMENT = "pymt";
  public static final String PAYMENT_OPTION = "popt";
  public static final String PERMISSIONS = "prms";
  public static final String PHYSICAL_STOCK = "p";
  public static final String PINCODE = "pc";
  public static final String QUANTITY = "q";
  public static final String RECEIPTS = "r";
  public static final String REASONS_BY_TAG = "rsnsbytg";
  public static final String REASON_FOR_CANCELLING_ORDER = "rsnco";
  public static final String REASONS_FOR_CANCELLING_ORDER = "rsnsco";
  public static final String REASON_FOR_EDITING_ORDER_QUANTITY = "rsneoq";
  public static final String REASONS_FOR_EDITING_ORDER_QUANTITY = "rsnseoq";
  public static final String REASON_FOR_IGNORING_RECOMMENDED_QUANTITY = "rsnirq";
  public static final String REASON_FOR_PARTIAL_FULFILLMENT = "rsnpf";
  public static final String REASONS_FOR_PARTIAL_FULFILLMENT = "rsnspf";
  public static final String REASON_FOR_PARTIAL_SHIPMENT = "rsnps";
  public static final String REASONS_FOR_PARTIAL_SHIPMENT = "rsnsps";
  public static final String RECOMMENDED_ORDER_QUANTITY = "roq";
  public static final String RETAILER_PRICE = "rp";
  public static final String ROLE = "rle";
  public static final String SENDER_ID = "sndid";
  public static final String SERIAL_NUMBER = "srno";
  public static final String SERVICE_LEVEL = "sl";
  public static final String SHIPMENT_ID = "sid";
  public static final String SHIPMENTS = "shps";
  public static final String SHORT_MATERIAL_ID = "smid";
  public static final String SMS = "sms";
  public static final String STATE = "ste";
  public static final String STATUS = "st";
  public static final String STREET_ADDRESS = "sa";
  public static final String TALUK = "tlk";
  public static final String TAX = "tx";
  public static final String TAX_ID = "txid";
  public static final String TAGS = "tg";
  public static final String TAGS_INVENTORY = "tgi";
  public static final String TAGS_ORDERS = "tgo";
  public static final String TAGS_INVENTORY_OPERATION = "tgiov";
  public static final String TEMP_SENSITVE_MATERIALS = "tsm";
  public static final String TIME_START = "ts";
  public static final String TIME_END = "te";
  public static final String TIMESTAMP = "t";
  public static final String TIMESTAMP_OF_SAVE_MILLIS = "svtm";
  public static final String TIMEZONE = "tz";
  public static final String TOTAL_PRICE = "tp";
  public static final String TRACKING_ID = "tid";
  public static final String TRANSACTIONS = "trn";
  public static final String TRANSACTION_LOG = "trl";
  public static final String TRANSACTION_NAMING = "tnm";
  public static final String TRANSACTION_TYPE = "ty";
  public static final String TRANSFER = "t";
  public static final String TRANSFER_RELEASE = "tr";
  public static final String TRANSPORTER = "trsp"; //transporter for orders
  public static final String
      TRANSPORTER_MANDATORY =
      "trspm";
  // transporter details specification mandatory before shipping order
  public static final String TYPE = TRANSACTION_TYPE; // repeat but different naming convention
  public static final String UPDATED_TIME = "ut";
  public static final String UPDATESTOCK_ON_SHIPPED = "usos";
  public static final String UPDATESTOCK_ON_FULFILLED = "usof";
  public static final String USER = "u";
  public static final String USERS = "us";
  public static final String USER_ID = "uid";
  public static final String VIEW_ONLY = "vnly";
  public static final String VIEW_INVENTORY_ONLY = "vinv";
  public static final String VENDORS = "vnds";
  public static final String VENDORS_MANDATORY = "vndm";
  public static final String VENDOR = "vndr";
  public static final String VENDORID = "vid";
  public static final String VENDOR_CITY = "vcty";
  public static final String VERSION = "v";
  public static final String REASON = "rsn";
  public static final String REASONS = "rsns";
  public static final String REASONS_WASTAGE = "wrsns";
  public static final String REASONS_RETURN = "rtrsns";
  public static final String REASONS_ISSUE = "irsns";
  public static final String REASONS_RECEIPT = "rrsns";
  public static final String REASONS_STOCKCOUNT = "prsns";
  public static final String REASONS_TRANSFER = "trsns";
  public static final String RECEIPT_COUNT = "rc";
  public static final String RECEIPT_QUANTITY = "rq";
  public static final String RETURN_COUNT = "rtc";
  public static final String RETURN_QUANTITY = "rtq";
  public static final String
      ROUTE_ENABLED_USER =
      "rteu";
  // route enabled for a user on a managed entity (set on a user)
  public static final String
      ROUTE_ENABLED_CUSTOMERS =
      "rtecsts";
  // route enabled for customers (set on a kiosk)
  public static final String
      ROUTE_ENABLED_VENDORS =
      "rtevnds";
  // route enabled for vendors (set on a kiosk)
  public static final String ROUTE_INDEX = "rti";
  public static final String ROUTE_TAG = "rtt";
  public static final String SIZE = "sz";
  public static final String STOCKCOUNT_DIFFERENCE = "sdf";
  public static final String STOCK_COUNT = "sc";
  public static final String STOCK_QUANTITY = "sq";
  public static final String STOCK_DURATION = "dsq";
  public static final String SUPPORT_CONTACT_NAME = "scnm";
  public static final String SUPPORT_EMAIL = "seml";
  public static final String SUPPORT_PHONE = "sphn";
  public static final String TEMPERATURE = "tmp";
  ;
  public static final String TRANSFER_COUNT = "trc";
  public static final String TRANSFER_QUANTITY = "trq";
  public static final String WASTAGE_COUNT = "wc";
  public static final String WASTAGE_QUANTITY = "wq";
  public static final String EXPIRED_NONZERO_BATCHES = "xbt";

  public static final String VIEW_PROFILE = "vprf";
  public static final String EDIT_PROFIE = "eprf";
  public static final String PRINT_INVENTORY = "prni";
  public static final String EXPORT_INVENTORY = "expi";

  // Status constants
  public static final String STATUS_TRUE = "0";
  public static final String STATUS_FALSE = "1";
  public static final String STATUS_PARTIAL = "2";
  // Date type values
  public static final String TYPE_BINARY = "bn";
  public static final String INVENTORY_VISIBLE = "invv";
}
