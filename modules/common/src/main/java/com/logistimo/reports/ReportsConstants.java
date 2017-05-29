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

package com.logistimo.reports;

/**
 * Created by charan on 08/03/17.
 */
public interface ReportsConstants {

  // Report types
  String TYPE_TRANSACTION0 = "trns0";
  // old; used for reports0.jsp (which is till a good cross-validator against reports.jsp)
  String TYPE_TRANSACTION_RAW = "trnsraw"; // raw transactions
  String TYPE_TRANSACTION = "transactioncounts"; // transaction trends (daily)
  String TYPE_STOCK = "stck";
  String TYPE_STOCKLEVEL = "stlv";
  String TYPE_STOCKEVENT = "stev"; // stock-out / under/over stock durations
  String TYPE_STOCKEVENTRESPONSETIME = "replenishmentresponsetimes";
  // stock event response times
  String TYPE_DEMANDBOARD = "dmbr";
  String TYPE_ORDERS = "ords";
  String TYPE_ORDERRESPONSETIMES = "orderresponsetimes";
  String TYPE_CONSUMPTION = "consumptiontrends";
  String TYPE_USERACTIVITY = "useractivity";
  String TYPE_DOMAINACTIVITY = "domainactivity";
  String TYPE_POWERDATA = "powerdata";
  // Filters
  String FILTER_DOMAIN = "dmn";
  String FILTER_CUSTOMER = "cst";
  String FILTER_KIOSK = "ksk";
  String FILTER_MATERIAL = "mtrl";
  String FILTER_COUNTRY = "cntr";
  String FILTER_STATE = "stt";
  String FILTER_DISTRICT = "dstr";
  String FILTER_TALUK = "tlk";
  String FILTER_CITY = "cty";
  String FILTER_PINCODE = "pnc";
  String FILTER_STATUS = "stus";
  String FILTER_LOCATION = "lctn";
  String FILTER_POOLGROUP = "plgr";
  String FILTER_OTYPE = "otype";
  String FILTER_USER = "usr";
  String FILTER_VENDOR = "vnd";
  String FILTER_EVENT = "evt"; // event type filter
  String FILTER_KIOSKTAG = "ktag";
  String FILTER_MATERIALTAG = "mtag";
  String FILTER_ABNORMALDURATION =
      "abndur"; //abnormality duration filter in stock views
  String FILTER_ABNORMALSTOCKVIEW =
      "abnstockview"; //filter for fetching abnormal inventory model in stock views instead of IInvntryEventLogModel
  String FILTER_ORDERTAG = "otag";
  // Non-field filters
  String FILTER_LATEST = "ltst";
  public final static String USER = "uid";

  //Cassandra data constants
  public final static String DOMAIN = "did";
  public final static String MATERIAL = "mid";
  public final static String KIOSK = "kid";
  public final static String MATERIAL_TAG = "mtag";
  public final static String KIOSK_TAG = "ktag";
  public final static String USER_TAG = "utag";
  public final static String ASSET_TYPE = "atype";
  public final static String STATE = "state";
  public final static String TABLE_SLICE_STATE = "st";
  public final static String COUNTRY = "country";
  public final static String TABLE_SLICE_COUNTRY = "cn";
  public final static String DISTRICT = "district";
  public final static String TABLE_SLICE_DISTRICT = "dis";
  public final static String MONTH = "month";
  public final static String DAY = "day";
  public final static String QUERY_LITERAL_AND = " and ";
  public final static String SELECT_ALL_FROM = "SELECT * FROM";
  public final static String MONTHLY_STATS_QUERY = "SELECT * FROM did_kid_month";
  public final static String DOMAIN_STATS_ASSET_TYPE = " did_atype_day ";
  public final static String DOMAIN_DAY_TABLE = "did_day";
  public final static String
      DOMAIN_STATS_QUERY =
      " SELECT did,akc,lkc,kc,mc,uc,mlwa,mwa,mawa,mac,miac FROM ";
  public final static String MASTER_DATA_APP_NAME = "master.data.app";
  public final static String LOGISTIMO_AGGREGATION_NAME = "logistimo.agg.data.app";
  public final static String INVENTORY_DAY_TABLE = "did_mid_kid_day";
  public final static String TIME_FIELD = "t";
  // Literals
  String MANDATORY = "mnd";

  // Filters by object - i.e. relevent set of filters per object
  /*
         List<String> DOMAIN_FILTERS = Arrays.asList( FILTER_DOMAIN, FILTER_STATE, FILTER_DISTRICT, FILTER_TALUK, FILTER_PINCODE, FILTER_KIOSK );
  List<String> USER_FILTERS = Arrays.asList( FILTER_DOMAIN, FILTER_STATE, FILTER_DISTRICT, FILTER_TALUK, FILTER_PINCODE, FILTER_KIOSK );
  List<String> MATERIAL_FILTERS = Arrays.asList( FILTER_DOMAIN, FILTER_STATE, FILTER_DISTRICT, FILTER_TALUK, FILTER_PINCODE, FILTER_KIOSK );
  List<String> KIOSK_FILTERS = Arrays.asList( FILTER_CUSTOMER, FILTER_VENDOR );
  List<String> INVENTORY_FILTERS = Arrays.asList( FILTER_CUSTOMER, FILTER_VENDOR );
  */
  String OPTIONAL = "opt";
  // Frequency of data
  String FREQ_DAILY = "daily";
  String FREQ_MONTHLY = "monthly";
  /**
   * Sorting direction to be used in reporting.
   */
  String SORT_ASC = "sort_asc";
  
}
