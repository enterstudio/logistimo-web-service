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

/**
 *
 */
package com.logistimo.config.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.logistimo.AppFactory;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Represents the configuration of a given domain
 *
 * @author Arun
 */
public class DomainConfig implements Serializable {

  // JSON Tags
  public static final String ALLOW_EMPTY_ORDERS = "empords";
  public static final String ALLOW_MARK_ORDER_AS_FULFILLED = "mrkordfl";
  public static final String BULLETIN_BOARD = "bboard";
  public static final String CAPABILITIES = "cpb";
  public static final String CAPABILITIES_BY_ROLE = "cpbrole";
  public static final String TRANSMENU = "trn";
  public static final String TRANSNAMING = "tnm";
  public static final String TRANSPORTER_MANDATORY = "tspm";
  public static final String TRANSPORTER_IN_SMS = "tspsms";
  public static final String ORDER_AUTOGI = "ogi"; // auto goods issued (GI) on order shipped
  public static final String ORDER_AUTOGR = "ogr"; // auto goods receipt (GR) on order fulfilment
  public static final String
      ORDER_FIELDS =
      "ordflds";
  // additiona form fields in order processing form
  public static final String
      ORDER_STATUS_SMS_ROLES =
      "ostrls";
  // DEPRECATED (as of 30/4/2012) CSV roles to which order status notification can be sent
  public static final String ORDERGEN = "ogn";
  public static final String PAYMENT_OPTIONS = "popts";
  public static final String PACKAGE_SIZES = "pkszs";
  public static final String COUNTRY = "cnt";
  public static final String STATE = "state";
  public static final String DISTRICT = "district";
  public static final String LANGUAGE = "lng";
  public static final String TIMEZONE = "tmz";
  public static final String CURRENCY = "crr";
  public static final String TAX = "tax";
  public static final String DEMANDBOARD = "dbd";
  public static final String TAGS_MATERIAL = "tgs";
  public static final String TAGS_KIOSK = "ktgs";
  public static final String TAGS_ORDER = "otgs";
  public static final String ROUTE_TAGS = "rttgs";
  public static final String TAGS_USER = "utgs";
  public static final String ROUTE_BY = "rtby";
  public static final String FORCETAGS_MATERIAL = "ftgs";
  public static final String FORCETAGS_KIOSK = "kftgs";
  public static final String FORCETAGS_ORDER = "oftgs";
  public static final String FORCETAGS_USER = "uftgs";
  public static final String SEND_CUSTOMERS = "sndcsts";
  public static final String SEND_VENDORS = "sndvnds";
  public static final String TAGS_INVENTORY = "tgi";
  public static final String TAGS_ORDERS = "tgo";
  public static final String HEADER = "hdr";
  public static final String OPTIMIZER = "optmz";
  public static final String MESSAGES = "msgs";
  public static final String ACCOUNTING = "accntng";
  public static final String INVENTORY = "invntry";
  public static final String APPROVALS = "approvals";
  public static final String VENDOR_ID = "vid";
  public static final String EVENTS = "evnts";
  public static final String ORDERS = "orders";
  public static final String CUSTOMREPORTS = "customreports";
  public static final String PAYMENTS = "payments";
  public static final String TEMPERATURE = "temperature";
  public static final String DASHBOARD = "dashboard";
  public static final String UIPREFERENCE = "uipref";
  public static final String ONLYNEWUI = "onlynewui";
  public static final String SUPPORT_BY_ROLE = "supbyrole";
  public static final String ADMIN_CONTACT = "admincontact";
  public static final String PRIMARY = "primary";
  public static final String SECONDARY = "secondary";
  public static final String ENABLE_SWITCH_TO_NEW_HOST = "enswtonewhost";
  public static final String NEW_HOST_NAME = "newhostname";
  public static final String ENTITY_TAGS_ORDER = "eto";
  // Defaults
  public static final String TRANSNAMING_ISSUESRECEIPTS = "0";
  public static final String TRANSNAMING_SALESPURCHASES = "1";
  public static final String TRANSNAMING_DEFAULT = TRANSNAMING_ISSUESRECEIPTS;
  public static final String ORDERGEN_TRUE = "0";
  public static final String ORDERGEN_FALSE = "1";
  public static final String ORDERGEN_DEFAULT = ORDERGEN_TRUE;
  // Values
  public static final String CAPABILITY_INVENTORY = "inventory";
  public static final String CAPABILITY_ORDERS = "orders";
  // Routes
  public static final String ROUTE_BY_ORDERS = "orders";
  public static final String ROUTE_BY_INVENTORY = "inventory";
  public static final String UPDATE_DATA = "updateData";
  public static final String DISABLE_TAGS_INVENTRY_OPERATION = "tgiov";
  public static final String AUTHENTICATION_TOKEN_EXPIRY = "atexp";
  // Synchroization by mobile
  public static final String SYNCHRONIZATION_BY_MOBILE = "syncbymob";
  // Local login required
  public static final String LOCAL_LOGIN_REQUIRED = "llr";
  public static final String DISABLE_ORDERS_PRICING = "dop";
  //loc ids
  public static final String COUNTRY_ID = "cntid";
  public static final String STATE_ID = "stateid";
  public static final String DISTRICT_ID = "districtid";
  private static final long serialVersionUID = 4047681117629775550L;
  //Mobile GUI Theme
  public static final String STORE_APP_THEME = "storeAppTheme";
  // Logger
  private static final XLog xLogger = XLog.getLog(DomainConfig.class);
  // Properties
  private List<String> capabilities = null;
  private String transNaming = TRANSNAMING_DEFAULT;
  private String orderGen = ORDERGEN_DEFAULT;
  private String country = null;
  private String state = null;
  private String district = null;
  private String countryId = null;
  private String stateId = null;
  private String districtId = null;
  private String language = Constants.LANG_DEFAULT;
  private String timezone = null;
  private String currency = null;
  private float tax = 0;
  private DemandBoardConfig dbc = null;
  private String tags = null; // material tags (CSV)
  private String ktags = null; // kiosk tags (CSV)
  private String otags = null; // order tags (CSV)
  private String routeTags = null; // entity route tags (CSV)
  private String utags = null; // user tags (CSV)
  private String
      routeBy =
      ROUTE_BY_ORDERS;
  // whether actual route is to be determined using orders (vs inventory transactions)
  private boolean forceTags = true; // force tags for materials
  private boolean forceKTags = true; // force tags for kiosks
  private boolean forceOTags = true; // force tags for orders
  private boolean forceUTags = true; // force tags for users
  private String pageHeader = null; // custom header for web service, encapsulated as a div tag
  private boolean autoGI = true;
  @Deprecated
  private boolean autoGR = true;
  private OptimizerConfig optimizerConfig = null;
  private MessageConfig
      msgConfig =
      null;
  // DEPRECATED as of gae 1.2.3 (last usage in 1.2.2x), Nov. 2012; instead use eventSpec
  private List<String>
      orderStatusSMSRoles =
      null;
  // DEPRECATED (as of 30/4/2012) a list of roles to which order status is to be sent
  // Capabilities - generic (also for backward compatility with serialized configs)
  private List<String> transMenu = null; // menu items to be disabled
  private String tagsInventory = null; // tags to be hidden from Inventory in client app.
  private String tagsOrders = null; // tags to be hidden from Orders in client app.
  private boolean
      sendVendors =
      true;
  // send vendor info. to mobile (sent only if Orders are enabled)
  private boolean sendCustomers = true; // send customer info. to mobile
  private boolean loginAsReconnect = false; // make login button on mobile act as reconnect
  private boolean allowEmptyOrders = false; // allow placing empty orders with no items
  private boolean
      allowMarkOrderAsFulfilled =
      false;
  // allow marking an order as fulfilled when placing/updating it
  private String paymentOptions = null; // CSV of order payment options, if any
  private String packageSizes = null; // CSV of order package sizes, if any
  private Long vendorId = null; // default vendor ID, if any
  // Role specific capability configs.
  private Map<String, CapabilityConfig> capabilityMap = null;
  // Additional message fields for orders
  private FieldsConfig orderFields = null;
  // Transporter flag
  private boolean isTransporterMandatory = false;
  private boolean isTransporterInSms = false;
  // Accounting configuration
  private AccountingConfig accountingConfig = null;
  // Issue reasons - capability
  private String wastageReasons = null; // DEPRECATED as of 28/4/2012
  // Inventory config
  private InventoryConfig inventoryConfig = null;
  // Approval config
  private ApprovalsConfig approvalsConfig = null;
  // Event specification for this domain
  private EventsConfig eventsConfig = null;
  // Bulletin board config.
  private BBoardConfig bbConfig = null;
  // Geo-coding strategy
  private String geoCodingStrategy = CapabilityConfig.GEOCODING_STRATEGY_OPTIMISTIC;
  // Creatable entities
  private List<String> creatableEntityTypes; // the types of entities that can be created
  // Allow editing route tags
  private boolean allowRouteTagEditing = false;
  // Orders config (may store only partial config, the rest being properties in this object itself - TODO: later move it to OrdersConfig)
  private OrdersConfig ordersConfig = null;
  // Custom Reports Config
  private CustomReportsConfig customReportsConfig = null;
  // Payments Config
  private PaymentsConfig paymentsConfig = null;
  // Temperature Config
  private AssetConfig assetConfig = null;
  // DashboardConfig
  private DashboardConfig dashboardConfig = null;
  // UI Preference for the domain - true indicates domain's ui preference is new UI. false indicates preference is old UI
  private boolean uiPref = false;
  // Flag that indicates domains created with no option to switch back to old UI
  private boolean onlyNewUI = false;
  // Role specific Support configuration
  private Map<String, SupportConfig> supportConfigMap = null;
  //Admin contact configuration
  private Map<String, AdminContactConfig> adminContactConfigMap = null;
  // Disable shipping on mobile
  private boolean enableShippingOnMobile = false;
  // Enable switching to new host
  private boolean enableSwitchToNewHost = false;
  // New host name field
  private String newHostName = null;
  private Map<String, List<String>> domainData = new HashMap<>();
  private Map<String, String> tagsInvByOperation = new HashMap<>();

  private Map<String, Integer> entityTagOrder = new HashMap<>();

  private SyncConfig syncConfig = null; // Synchronization intervals on mobile

  private int authenticationTokenExpiry = 30;

  private boolean disableOrdersPricing = false;

  private boolean localLoginRequired = true;

  private int storeAppTheme = Constants.GUI_THEME_BLACK;

  public DomainConfig() {
    optimizerConfig = new OptimizerConfig();
    inventoryConfig = new InventoryConfig();
    approvalsConfig = new ApprovalsConfig();
    ordersConfig = new OrdersConfig();
    capabilityMap = new HashMap<String, CapabilityConfig>();
    accountingConfig = new AccountingConfig();
    eventsConfig = new EventsConfig();
    bbConfig = new BBoardConfig();
    routeBy = (isCapabilityDisabled(CAPABILITY_ORDERS) ? ROUTE_BY_INVENTORY : ROUTE_BY_ORDERS);
    customReportsConfig = new CustomReportsConfig();
    paymentsConfig = new PaymentsConfig();
    assetConfig = new AssetConfig();
    dashboardConfig = new DashboardConfig();
    supportConfigMap = new HashMap<String, SupportConfig>();
    adminContactConfigMap = new HashMap<>();
    syncConfig = new SyncConfig();
  }

  public DomainConfig(String configString) throws ConfigurationException {
    xLogger.fine("Entering DomainConfig constructor. {0}", configString);
    if (configString == null) {
      return;
    }
    try {
      JSONObject json = new JSONObject(configString);
      // Get capabilities for disablement
      try {
        String commaSepCapabilities = (String) json.get(CAPABILITIES);
        this.capabilities = StringUtil.getList(commaSepCapabilities);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the transaction menus (for disablement)
      try {
        String commaSepMenuItems = (String) json.get(TRANSMENU);
        this.transMenu = StringUtil.getList(commaSepMenuItems);
      } catch (JSONException e) {
        // do nothing
      }

      try {
        String updatedData = (String) json.get(UPDATE_DATA);
        Map<String, List<String>> map = new Gson().fromJson(updatedData, Map.class);
        this.domainData = map;
      } catch (JSONException e) {
        //do nothing
      }
      // Get transaction naming
      try {
        this.transNaming = (String) json.get(TRANSNAMING);
      } catch (JSONException e) {
        // do nothing
      }
      // Get order generation configuration
      try {
        this.orderGen = (String) json.get(ORDERGEN);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the country
      try {
        this.country = (String) json.get(COUNTRY);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the state
      try {
        this.state = (String) json.get(STATE);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the district
      try {
        this.district = (String) json.get(DISTRICT);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the country id
      try {
        this.countryId = (String) json.get(COUNTRY_ID);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the state id
      try {
        this.stateId = (String) json.get(STATE_ID);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the district id
      try {
        this.districtId = (String) json.get(DISTRICT_ID);
      } catch (JSONException e) {
        // do nothing
      }

      // Get the language
      try {
        this.language = (String) json.get(LANGUAGE);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the timezone
      try {
        this.timezone = (String) json.get(TIMEZONE);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the currency
      try {
        this.currency = (String) json.get(CURRENCY);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the tax
      try {
        this.tax = Float.parseFloat((String) json.get(TAX));
      } catch (JSONException e) {
        // do nothing
      } catch (NumberFormatException e) {
        // ignore
      }
      // Get the demand board config
      try {
        this.dbc = new DemandBoardConfig((String) json.get(DEMANDBOARD));
      } catch (JSONException e) {
        // do nothing
      } catch (ConfigurationException e) {
        // do nothing
      }
      // Get the material tags
      try {
        this.tags = (String) json.get(TAGS_MATERIAL);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the kiosk tags
      try {
        this.ktags = (String) json.get(TAGS_KIOSK);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the order tags
      try {
        this.otags = (String) json.getString(TAGS_ORDER);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the route tags
      try {
        routeTags = (String) json.get(ROUTE_TAGS);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the user tags
      try {
        this.utags = (String) json.get(TAGS_USER);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        String entityTagOrder = (String) json.get(ENTITY_TAGS_ORDER);
        Type mapType = new TypeToken<Map<String, Integer>>() {
        }.getType();
        Map<String, Integer> map = new Gson().fromJson(entityTagOrder, mapType);
        this.entityTagOrder = map;
      } catch (JSONException e) {
        //do nothing
      }
      // Get the route-by value
      try {
        routeBy = json.getString(ROUTE_BY);
      } catch (Exception e) {
        routeBy = (isCapabilityDisabled(CAPABILITY_ORDERS) ? ROUTE_BY_INVENTORY : ROUTE_BY_ORDERS);
      }
      // Get user force-tags flag
      try {
        this.forceUTags = "true".equals((String) json.get(FORCETAGS_USER));
      } catch (JSONException e) {
        // do nothing
      }
      // Get material force-tags flag
      try {
        this.forceTags = "true".equals((String) json.get(FORCETAGS_MATERIAL));
      } catch (JSONException e) {
        // do nothing
      }
      // Get kiosk force-tags flag
      try {
        this.forceKTags = "true".equals((String) json.get(FORCETAGS_KIOSK));
      } catch (JSONException e) {
        // do nothing
      }
      // Get kiosk force-tags flag
      try {
        this.forceOTags = "true".equals((String) json.get(FORCETAGS_ORDER));
      } catch (JSONException e) {
        // do nothing
      }
      // Get the tags to be hidden from Inventory
      try {
        this.tagsInventory = (String) json.get(TAGS_INVENTORY);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the tags to be hidden from Orders
      try {
        this.tagsOrders = (String) json.get(TAGS_ORDERS);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the custom header for the service manager pages
      try {
        this.pageHeader = (String) json.get(HEADER);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.autoGI = json.getBoolean(ORDER_AUTOGI);
      } catch (JSONException e) {
        // do nothing
      }

      try {
        this.sendVendors = json.getBoolean(SEND_VENDORS);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.sendCustomers = json.getBoolean(SEND_CUSTOMERS);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.allowEmptyOrders = json.getBoolean(ALLOW_EMPTY_ORDERS);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.allowMarkOrderAsFulfilled = json.getBoolean(ALLOW_MARK_ORDER_AS_FULFILLED);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.paymentOptions = json.getString(PAYMENT_OPTIONS);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.packageSizes = json.getString(PACKAGE_SIZES);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.vendorId = Long.valueOf(json.getString(VENDOR_ID));
      } catch (Exception e) {
        // do nothing
      }
      try {
        this.optimizerConfig = new OptimizerConfig(json.getJSONObject(OPTIMIZER));
      } catch (JSONException e) {
        this.optimizerConfig = new OptimizerConfig();
      }
      try {
        this.capabilityMap =
            CapabilityConfig.getCapabilitiesMap(json.getJSONObject(CAPABILITIES_BY_ROLE));
      } catch (JSONException e) {
        this.capabilityMap = new HashMap<String, CapabilityConfig>();
      }
      try {
        this.orderFields = new FieldsConfig(json.getJSONArray(ORDER_FIELDS));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.isTransporterMandatory = json.getBoolean(TRANSPORTER_MANDATORY);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.isTransporterInSms = json.getBoolean(TRANSPORTER_IN_SMS);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.accountingConfig = new AccountingConfig(json.getJSONObject(ACCOUNTING));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        inventoryConfig = new InventoryConfig(json.getJSONObject(INVENTORY));
      } catch (JSONException e) {
        inventoryConfig = new InventoryConfig();
      }
      try{
        approvalsConfig = new ApprovalsConfig(json.getJSONObject(APPROVALS));
      } catch (JSONException e) {
        approvalsConfig = new ApprovalsConfig();
      }
      // FOR BACKWARD COMPATIBILITY
      try {
        String reasonsCsv = json.getString(CapabilityConfig.WASTAGE_REASONS);
        if (reasonsCsv != null && !reasonsCsv.isEmpty()) {
          inventoryConfig.putTransReason(Constants.TYPE_WASTAGE, reasonsCsv);
        }
      } catch (JSONException e) {
        // do nothing
      }
      // END BACKWARD COMPATIBILITY
      // Events
      try {
        this.eventsConfig = new EventsConfig(json.getJSONObject(EVENTS));
      } catch (Exception e) {
        this.eventsConfig = new EventsConfig();
      }
      // Bulletin board configuration
      try {
        this.bbConfig = new BBoardConfig(json.getJSONObject(BULLETIN_BOARD));
      } catch (Exception e) {
        this.bbConfig = new BBoardConfig();
      }
      // Geo-coding strategy
      try {
        this.geoCodingStrategy = json.getString(CapabilityConfig.GEOCODING_STRATEGY);
      } catch (Exception e) {
        // ignore
      }
      // Creatable entities
      try {
        this.creatableEntityTypes =
            StringUtil.getList(json.getString(CapabilityConfig.CREATABLE_ENTITY_TYPES));
      } catch (Exception e) {
        // ignore
      }
      // Allow route tag editing
      try {
        this.allowRouteTagEditing = json.getBoolean(CapabilityConfig.ALLOW_ROUTETAG_EDITING);
      } catch (Exception e) {
        // ignore
      }
      // Orders config.
      try {
        this.ordersConfig = new OrdersConfig(json.getJSONObject(ORDERS));
      } catch (Exception e) {
        // ignore
        this.ordersConfig = new OrdersConfig();
      }

      // Custom Reports Config.
      try {
        this.customReportsConfig =
            new CustomReportsConfig(json.getJSONObject(CUSTOMREPORTS), getLocale(), timezone);
      } catch (Exception e) {
        // ignore
        this.customReportsConfig = new CustomReportsConfig();
      }

      // Payments Config
      try {
        this.paymentsConfig =
            new PaymentsConfig(json.getJSONObject(PAYMENTS), getLocale(), timezone);
      } catch (Exception e) {
        // ignore
        this.paymentsConfig = new PaymentsConfig();
      }

      // Temperature Monitoring Config
      try {
        this.assetConfig = new AssetConfig(json.getJSONObject(TEMPERATURE), getLocale(), timezone);
      } catch (Exception e) {
        // ignore
        this.assetConfig = new AssetConfig();
      }

      // Dashboard Config
      try {
        this.dashboardConfig =
            new DashboardConfig(json.getJSONObject(DASHBOARD), getLocale(), timezone);
      } catch (Exception e) {
        // ignore
        this.dashboardConfig = new DashboardConfig();
      }

      // Domain's UI preference
      try {
        this.uiPref = json.getBoolean(UIPREFERENCE);
      } catch (JSONException e) {
        // do nothing
      }

      // Only New UI
      try {
        this.onlyNewUI = json.getBoolean(ONLYNEWUI);
      } catch (JSONException e) {
        // do nothing
      }

      try {
        this.supportConfigMap = SupportConfig.getSupportMap(json.getJSONObject(SUPPORT_BY_ROLE));
      } catch (JSONException e) {
        this.supportConfigMap = new HashMap<String, SupportConfig>();
      }
      //administrative contacts
      try {
        this.adminContactConfigMap =
            AdminContactConfig.getAdminContactMap(json.getJSONObject(ADMIN_CONTACT));
      } catch (JSONException e) {
        this.adminContactConfigMap = new HashMap<String, AdminContactConfig>();
      }

      try {
        this.enableShippingOnMobile = json.getBoolean(CapabilityConfig.ENABLE_SHIPPING_ON_MOBILE);
      } catch (JSONException e) {
        // do nothing
      }

      try {
        this.authenticationTokenExpiry = json.getInt(AUTHENTICATION_TOKEN_EXPIRY);
      } catch (JSONException e) {
        // do nothing
      }

      try {
        this.localLoginRequired = json.getBoolean(LOCAL_LOGIN_REQUIRED);
      } catch (JSONException e) {
        // do nothing
      }

      loginAsReconnect = json.optBoolean(CapabilityConfig.LOGIN_AS_RECONNECT, false);

      try {
        this.enableSwitchToNewHost = json.getBoolean(ENABLE_SWITCH_TO_NEW_HOST);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.newHostName = json.getString(NEW_HOST_NAME);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        JSONObject invTags = json.getJSONObject(DISABLE_TAGS_INVENTRY_OPERATION);
        Iterator<String> en = invTags.keys();
        while (en.hasNext()) {
          String transType = en.next();
          tagsInvByOperation.put(transType, invTags.getString(transType));
        }
      } catch (JSONException e) {
        //do nothhing
      }
      try {
        this.syncConfig = new SyncConfig(json.getJSONObject(SYNCHRONIZATION_BY_MOBILE));
      } catch (JSONException e) {
        // Do nothing
        this.syncConfig = new SyncConfig();
      }
      try {
        this.disableOrdersPricing = json.getBoolean(DISABLE_ORDERS_PRICING);
      } catch (JSONException e) {
        //do nothing
      }
      try {
        this.storeAppTheme = json.getInt(STORE_APP_THEME);
      } catch (JSONException e) {
        //do nothing
      }
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public static DomainConfig getInstance(Long domainId) {
    DomainConfig dc = null;
    if (domainId == null) {
      return null;
    }
    // Check the memcache first
    MemcacheService cache = AppFactory.get().getMemcacheService();
    String cacheKey = getCacheKey(domainId);
    if (cache != null) {
      dc = (DomainConfig) cache.get(cacheKey);
      if (dc != null) {
        return dc;
      }
    }
    // Get from datastore
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      String
          configStr =
          cms.getConfiguration(IConfig.CONFIG_PREFIX + domainId.toString()).getConfig();
      xLogger.fine("configStr: " + configStr);
      dc = new DomainConfig(configStr);
    } catch (ObjectNotFoundException e) {
      dc = new DomainConfig(); // default
    } catch (ServiceException e) {
      xLogger.severe("Service exception: {0}", e.getMessage());
      return null;
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0}", e.getMessage());
      return null;
    }
    // Update memcache
    if (cache != null) {
      cache.put(cacheKey, dc);
    }

    return dc;
  }

  // Get the memcache key
  public static String getCacheKey(Long domainId) {
    return IConfig.CONFIG_PREFIX + domainId;
  }

  public String toJSONSring() throws ConfigurationException {
    String jsonStr = null;
    try {
      JSONObject json = new JSONObject();
      // Get the capabilities to be disabled
      String capabilitiesStr = StringUtil.getCSV(this.capabilities);
      if (capabilitiesStr != null) {
        json.put(CAPABILITIES, capabilitiesStr);
      }
      // Add transaction menu (to be disabled)
      // Get a comma-separated string of trans. menu
      String transMenuStr = StringUtil.getCSV(this.transMenu);
      if (transMenuStr != null) {
        json.put(TRANSMENU, transMenuStr);
      }
      // Add transaction naming
      if (transNaming != null) {
        json.put(TRANSNAMING, transNaming);
      }
      // Add order generation config.
      if (orderGen != null) {
        json.put(ORDERGEN, orderGen);
      }
      // Add entity property defaults
      if (country != null) {
        json.put(COUNTRY, country);
      }
      if (state != null) {
        json.put(STATE, state);
      }
      if (district != null) {
        json.put(DISTRICT, district);
      }
      //loc ids
      if (countryId != null) {
        json.put(COUNTRY_ID, countryId);
      }
      if (stateId != null) {
        json.put(STATE_ID, stateId);
      }
      if (districtId != null) {
        json.put(DISTRICT_ID, districtId);
      }
      if (language != null) {
        json.put(LANGUAGE, language);
      }
      if (timezone != null) {
        json.put(TIMEZONE, timezone);
      }
      if (currency != null) {
        json.put(CURRENCY, currency);
      }
      if (tax > 0) {
        json.put(TAX, String.valueOf(tax));
      }
      if (dbc != null) {
        json.put(DEMANDBOARD, dbc.toJSONString());
      }
      if (tags != null) {
        json.put(TAGS_MATERIAL, tags);
        json.put(FORCETAGS_MATERIAL, String.valueOf(forceTags));
      }
      if (ktags != null) {
        json.put(TAGS_KIOSK, ktags);
        json.put(FORCETAGS_KIOSK, String.valueOf(forceKTags));
      }
      if (otags != null) {
        json.put(TAGS_ORDER, otags);
        json.put(FORCETAGS_ORDER, String.valueOf(forceOTags));
      }
      if (routeTags != null) {
        json.put(ROUTE_TAGS, routeTags);
      }
      if (routeBy != null) {
        json.put(ROUTE_BY, routeBy);
      }
      if (utags != null) {
        json.put(TAGS_USER, utags);
        json.put(FORCETAGS_USER, String.valueOf(forceUTags));
      }
      if (tagsInventory != null) {
        json.put(TAGS_INVENTORY, tagsInventory);
      }
      if (tagsOrders != null) {
        json.put(TAGS_ORDERS, tagsOrders);
      }
      if (pageHeader != null && !pageHeader.isEmpty()) {
        json.put(HEADER, pageHeader);
      }
      json.put(ORDER_AUTOGI, autoGI);
      // Add parameters to determine whether to send vendors/customers
      json.put(SEND_VENDORS, sendVendors);
      json.put(SEND_CUSTOMERS, sendCustomers);
      // Allow empty orders?
      json.put(ALLOW_EMPTY_ORDERS, allowEmptyOrders);
      // Mark order as fulfulled?
      json.put(ALLOW_MARK_ORDER_AS_FULFILLED, allowMarkOrderAsFulfilled);
      if (paymentOptions != null && !paymentOptions.isEmpty()) {
        json.put(PAYMENT_OPTIONS, paymentOptions);
      }
      if (packageSizes != null && !packageSizes.isEmpty()) {
        json.put(PACKAGE_SIZES, packageSizes);
      }
      if (vendorId != null) {
        json.put(VENDOR_ID, vendorId.toString());
      }
      // Add optimization config.
      if (optimizerConfig != null) {
        json.put(OPTIMIZER, optimizerConfig.toJSONObject());
      }
      // Add the message config.
      if (msgConfig != null) {
        json.put(MESSAGES, msgConfig.toJSONObject());
      }
      if (capabilityMap != null && !capabilityMap.isEmpty()) {
        json.put(CAPABILITIES_BY_ROLE, CapabilityConfig.getCapabilitiesJSON(capabilityMap));
      }
      if (orderFields != null) {
        json.put(ORDER_FIELDS, orderFields.toJSONArray());
      }
      json.put(TRANSPORTER_MANDATORY, isTransporterMandatory);
      json.put(TRANSPORTER_IN_SMS, isTransporterInSms);
      // Accounting
      if (accountingConfig != null) {
        json.put(ACCOUNTING, accountingConfig.toJSONObject());
      }
      // Inventory config.
      if (inventoryConfig != null) {
        json.put(INVENTORY, inventoryConfig.toJSONObject());
      }
      if (approvalsConfig != null) {
        json.put(APPROVALS, approvalsConfig.toJSONObject());
      }
      // Events config.
      if (eventsConfig != null) {
        json.put(EVENTS, eventsConfig.toJSONObject());
      }
      // Bulletin board config.
      if (bbConfig != null) {
        json.put(BULLETIN_BOARD, bbConfig.toJSONObject());
      }
      // Geo-coding strategy
      if (geoCodingStrategy != null) {
        json.put(CapabilityConfig.GEOCODING_STRATEGY, geoCodingStrategy);
      }
      // Creatable entities
      if (creatableEntityTypes != null && !creatableEntityTypes.isEmpty()) {
        json.put(CapabilityConfig.CREATABLE_ENTITY_TYPES, StringUtil.getCSV(creatableEntityTypes));
      }
      // Allow route tag editing
      json.put(CapabilityConfig.ALLOW_ROUTETAG_EDITING, allowRouteTagEditing);
      // Orders config.
      if (ordersConfig != null) {
        json.put(ORDERS, ordersConfig.toJSONObject());
      }
      // Custom Reports config.
      if (customReportsConfig != null) {
        json.put(CUSTOMREPORTS, customReportsConfig.toJSONObject());
      }
      // Payments Config
      if (paymentsConfig != null) {
        json.put(PAYMENTS, paymentsConfig.toJSONObject());
      }
      // Temperature Monitoring Config
      if (assetConfig != null) {
        json.put(TEMPERATURE, assetConfig.toJSONObject());
      }
      // Dashboard Config
      if (dashboardConfig != null) {
        json.put(DASHBOARD, dashboardConfig.toJSONObject());
      }
      // UI Preference
      json.put(UIPREFERENCE, uiPref);
      // Only New UI
      json.put(ONLYNEWUI, onlyNewUI);
      json.put(AUTHENTICATION_TOKEN_EXPIRY, authenticationTokenExpiry);
      json.put(LOCAL_LOGIN_REQUIRED, localLoginRequired);

      if (supportConfigMap != null && !supportConfigMap.isEmpty()) {
        json.put(SUPPORT_BY_ROLE, SupportConfig.getSupportJSON(supportConfigMap));
      }
      if (adminContactConfigMap != null && !adminContactConfigMap.isEmpty()) {
        json.put(ADMIN_CONTACT, AdminContactConfig.getAdminContactJSON(adminContactConfigMap));
      }

      json.put(CapabilityConfig.ENABLE_SHIPPING_ON_MOBILE, enableShippingOnMobile);

      if (loginAsReconnect) {
        json.put(CapabilityConfig.LOGIN_AS_RECONNECT,
            loginAsReconnect);                        // Get JSON String
      }

      if (domainData != null) {
        String jsonDomainData = new Gson().toJson(domainData);
        json.put(UPDATE_DATA, jsonDomainData);
      }

      if (entityTagOrder != null) {
        String jsonEntityTagOrder = new Gson().toJson(entityTagOrder);
        json.put(ENTITY_TAGS_ORDER, jsonEntityTagOrder);
      }
      if (enableSwitchToNewHost) {
        json.put(ENABLE_SWITCH_TO_NEW_HOST, enableSwitchToNewHost);
      }

      if (newHostName != null && !newHostName.isEmpty()) {
        json.put(NEW_HOST_NAME, newHostName);
      }
      json.put(DISABLE_ORDERS_PRICING, disableOrdersPricing);
      if (!tagsInvByOperation.isEmpty()) {
        JSONObject invTags = new JSONObject();
        Iterator<String> it = tagsInvByOperation.keySet().iterator();
        while (it.hasNext()) {
          String transType = it.next();
          String tagsCsv = (String) tagsInvByOperation.get(transType);
          if (tagsCsv != null && !tagsCsv.isEmpty()) {
            invTags.put(transType, tagsCsv);
          }
        }
        json.put(DISABLE_TAGS_INVENTRY_OPERATION, invTags);
      }
      if (syncConfig != null) {
        json.put(SYNCHRONIZATION_BY_MOBILE, syncConfig.toJSONObject());
      }
      jsonStr = json.toString();
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }

    return jsonStr;
  }

  public List<String> getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(String[] capabilitiesArray) {
    if (capabilitiesArray == null || capabilitiesArray.length == 0) {
      this.capabilities = null;
    } else {
      this.capabilities = Arrays.asList(capabilitiesArray);
    }
  }

  public void setCapabilities(List<String> capabilities) {
    this.capabilities = capabilities;
  }

  public boolean isCapabilityDisabled(String capability) {
    return (capabilities != null && capabilities.contains(capability));
  }

  public Map<String, CapabilityConfig> getCapabilityMapByRole() {
    return capabilityMap;
  }

  public CapabilityConfig getCapabilityByRole(String role) {
    if (capabilityMap == null) {
      return null;
    }
    return capabilityMap.get(role);
  }

  public void setCapabilityByRole(String role, CapabilityConfig cc) {
    if (capabilityMap == null) {
      capabilityMap = new HashMap<String, CapabilityConfig>();
    }
    capabilityMap.put(role, cc);
  }

  public List<String> getTransactionMenus() {
    return transMenu;
  }

  public void setTransactionMenus(String[] transMenuArray) {
    if (transMenuArray == null || transMenuArray.length == 0) {
      this.transMenu = null;
    } else {
      this.transMenu = Arrays.asList(transMenuArray);
    }
  }

  public void setTransactionMenus(List<String> transMenu) {
    this.transMenu = transMenu;
  }

  public String getTransactionMenusString() {
    return StringUtil.getCSV(this.transMenu);
  }

  public String getTransactionNaming() {
    return transNaming;
  }

  public void setTransactionNaming(String transNaming) {
    this.transNaming = transNaming;
  }

  public String getOrderGeneration() {
    return orderGen;
  }

  public void setOrderGeneration(String orderGen) {
    this.orderGen = orderGen;
  }

  public boolean autoOrderGeneration() {
    return "0".equals(orderGen);
  }

  public String getGeoCodingStrategy() {
    if (geoCodingStrategy == null) {
      geoCodingStrategy = CapabilityConfig.GEOCODING_STRATEGY_OPTIMISTIC;
    }
    return geoCodingStrategy;
  }

  public void setGeoCodingStrategy(String geoCodingStrategy) {
    this.geoCodingStrategy = geoCodingStrategy;
  }

  public List<String> getCreatableEntityTypes() {
    return creatableEntityTypes;
  }

  public void setCreatableEntityTypes(List<String> creatableEntityTypes) {
    this.creatableEntityTypes = creatableEntityTypes;
  }

  public boolean allowRouteTagEditing() {
    return allowRouteTagEditing;
  }

  public void setAllowRouteTagEditing(boolean allow) {
    this.allowRouteTagEditing = allow;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public List<String> getDomainData(String key) {
    return domainData != null ? domainData.get(key) : null;
  }

  public void addDomainData(String key, List<String> val) {
    if (domainData == null) {
      domainData = new HashMap<>();
    }
    domainData.put(key, val);
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public float getTax() {
    return tax;
  }

  public void setTax(float tax) {
    this.tax = tax;
  }

  public DemandBoardConfig getDemandBoardConfig() {
    return dbc;
  }

  public void setDemandBoardConfig(DemandBoardConfig dbc) {
    this.dbc = dbc;
  }

  // Get/set material tags
  public String getMaterialTags() {
    return tags;
  }

  public void setMaterialTags(String tagsCSV) {
    this.tags = tagsCSV;
  }

  // Get/set kiosk tags
  public String getKioskTags() {
    return ktags;
  }

  public void setKioskTags(String tagsCSV) {
    this.ktags = tagsCSV;
  }

  // Get/set order tags -- These are order tags that are configured under tags configuration
  public String getOrderTags() {
    return otags;
  }

  public void setOrderTags(String tagsCSV) {
    this.otags = tagsCSV;
  }

  public String getRouteTags() {
    return routeTags;
  }

  public void setRouteTags(String routeTagsCSV) {
    routeTags = routeTagsCSV;
  }

  // Get/set user tags
  public String getUserTags() {
    return utags;
  }

  public void setUserTags(String tagsCSV) {
    this.utags = tagsCSV;
  }

  public String getRouteBy() {
    return routeBy;
  }

  public void setRouteBy(String routeBy) {
    this.routeBy = routeBy;
  }

  public boolean forceTagsMaterial() {
    return forceTags;
  }

  public void setForceTagsMaterial(boolean forceTags) {
    this.forceTags = forceTags;
  }

  public boolean forceTagsKiosk() {
    return forceKTags;
  }

  public void setForceTagsKiosk(boolean forceTags) {
    this.forceKTags = forceTags;
  }

  public boolean forceTagsOrder() {
    return forceOTags;
  }

  public void setForceTagsOrder(boolean forceTags) {
    this.forceOTags = forceTags;
  }

  public String getTagsInventory() {
    return tagsInventory;
  }

  public void setTagsInventory(String tagsInventory) {
    this.tagsInventory = tagsInventory;
  }

  public String getTagsOrders() {
    return tagsOrders;
  }

  public void setTagsOrders(String tagsOrders) {
    this.tagsOrders = tagsOrders;
  }

  public boolean forceTagsUser() {
    return forceUTags;
  }

  public void setForceTagsUser(boolean forceTags) {
    this.forceUTags = forceTags;
  }

  public String getPageHeader() {
    return pageHeader;
  }

  public void setPageHeader(String pageHeader) {
    this.pageHeader = pageHeader;
  }

  public boolean autoGI() {
    return autoGI;
  }

  public void setAutoGI(boolean autoGI) {
    this.autoGI = autoGI;
  }

  @Deprecated
  /**
   * Use autoGI instead, this returns the same as autoGI.
   */
  public boolean autoGR() {
    return autoGI;
  }

  /**
   * No longer supported.
   *
   * @param autoGR - auto generate receipts
   */
  @Deprecated
  public void setAutoGR(boolean autoGR) {
    this.autoGR = autoGR;
  }

  public boolean sendVendors() {
    return sendVendors;
  }

  public void setSendVendors(boolean sendVendors) {
    this.sendVendors = sendVendors;
  }

  public boolean sendCustomers() {
    return sendCustomers;
  }

  public void setSendCustomers(boolean sendCustomers) {
    this.sendCustomers = sendCustomers;
  }

  public boolean isLoginAsReconnect() {
    return loginAsReconnect;
  }

  public void setLoginAsReconnect(boolean loginAsReconnect) {
    this.loginAsReconnect = loginAsReconnect;
  }

  public boolean allowEmptyOrders() {
    return allowEmptyOrders;
  }

  public void setAllowEmptyOrders(boolean allowEmptyOrders) {
    this.allowEmptyOrders = allowEmptyOrders;
  }

  @Deprecated
  public boolean allowMarkOrderAsFulfilled() {
    return false;
  }

  @Deprecated
  public void setAllowMarkOrderAsFulfilled(boolean allowMarkOrderAsFulfilled) {
    this.allowMarkOrderAsFulfilled = allowMarkOrderAsFulfilled;
  }

  public String getPaymentOptions() {
    return paymentOptions;
  }

  public void setPaymentOptions(String paymentOptions) {
    this.paymentOptions = paymentOptions;
  }

  public String getPackageSizes() {
    return packageSizes;
  }

  public void setPackageSizes(String packageSizes) {
    this.packageSizes = packageSizes;
  }

  public Long getVendorId() {
    return vendorId;
  }

  public void setVendorid(Long vendorId) {
    this.vendorId = vendorId;
  }

  public boolean isTransporterMandatory() {
    return isTransporterMandatory;
  }

  public void setTransporterMandatory(boolean mandatory) {
    isTransporterMandatory = mandatory;
  }

  public boolean isTransporterInStatusSms() {
    return isTransporterInSms;
  }

  public void setTransporterInStatusSms(boolean inSms) {
    this.isTransporterInSms = inSms;
  }

  public Map<String, Integer> getEntityTagOrder() {
    return entityTagOrder;
  }

  public void setEntityTagOrder(Map<String, Integer> entityTagOrder) {
    this.entityTagOrder = entityTagOrder;
  }
  // END DEPRECATED

  // DEPRECATED: Moved to InventoryConfig
  public String getWastageReasons() {
    String reasons = inventoryConfig.getTransReason(Constants.TYPE_WASTAGE);
    // NOTE: To ensure backward compatibility, also check for reasons at this level
    if (reasons == null || reasons.isEmpty()) {
      reasons = wastageReasons;
    }
    return reasons;
  }

  public void setWastageReasons(String wastageReasons) {
    this.wastageReasons = wastageReasons;
  }

  public EventsConfig getEventsConfig() {
    if (eventsConfig == null) {
      eventsConfig = new EventsConfig();
    }
    return eventsConfig;
  }

  public void setEventsConfig(EventsConfig eventsConfig) {
    this.eventsConfig = eventsConfig;
  }

  public BBoardConfig getBBoardConfig() {
    if (bbConfig == null) {
      return new BBoardConfig();
    }
    return bbConfig;
  }

  public void setBBoardConfig(BBoardConfig bbConfig) {
    this.bbConfig = bbConfig;
  }

  public boolean isBBoardEnabled() {
    return (bbConfig != null && bbConfig.isEnabled());
  }

  public boolean isDemandBoardPublic() {
    return (dbc != null && dbc.isPublic());
  }

  public Locale getLocale() {
    if (country == null) {
      return new Locale(language, "");
    } else {
      return new Locale(language, country);
    }
  }

  public OptimizerConfig getOptimizerConfig() {
    return optimizerConfig;
  }

  public void setOptimizerConfig(OptimizerConfig optimizerConfig) {
    this.optimizerConfig = optimizerConfig;
  }

  public OrdersConfig getOrdersConfig() {
    return ordersConfig;
  }

  public void setOrdersConfig(OrdersConfig ordersConfig) {
    this.ordersConfig = ordersConfig;
  }
  // END DEPRECATED

  // DEPRECATED (as of 30/4/2012)
  public List<String> getOrderStatusSmsRoles() {
    MessageConfig.NotifyTemplate nt = msgConfig.getNotificationTemplate(Constants.TYPE_ORDER);
    if (nt != null) {
      if (nt.sendTo == null || nt.sendTo.isEmpty()) {
        return orderStatusSMSRoles;
      } else {
        return nt.sendTo;
      }
    }
    return orderStatusSMSRoles;
  }

  public void setOrderStatusSmsRoles(List<String> orderStatusSMSRoles) {
    this.orderStatusSMSRoles = orderStatusSMSRoles;
  }

  public FieldsConfig getOrderFields() {
    return orderFields;
  }

  public void setOrderFields(FieldsConfig orderFields) {
    this.orderFields = orderFields;
  }

  public AccountingConfig getAccountingConfig() {
    return accountingConfig;
  }

  public void setAccountingConfig(AccountingConfig ac) {
    this.accountingConfig = ac;
  }

  public boolean isAccountingEnabled() {
    return (accountingConfig != null && accountingConfig.isAccountingEnabled());
  }

  public InventoryConfig getInventoryConfig() {
    return inventoryConfig;
  }

  public void setInventoryConfig(InventoryConfig inventoryConfig) {
    this.inventoryConfig = inventoryConfig;
  }

  public CustomReportsConfig getCustomReportsConfig() {
    return customReportsConfig;
  }

  public void setCustomReportsConfig(CustomReportsConfig customReportsConfig) {
    this.customReportsConfig = customReportsConfig;
  }

  public PaymentsConfig getPaymentsConfig() {
    return paymentsConfig;
  }

  public void setPaymentsConfig(PaymentsConfig paymentsConfig) {
    this.paymentsConfig = paymentsConfig;
  }

  public boolean isPaymentsEnabled() {
    return (paymentsConfig != null && paymentsConfig.isPaymentsEnabled());
  }

  public AssetConfig getAssetConfig() {
    return assetConfig;
  }

  public void setAssetConfig(AssetConfig assetConfig) {
    this.assetConfig = assetConfig;
  }

  public boolean isTemperatureMonitoringWithLogisticsEnabled() {
    return (assetConfig != null && assetConfig.isTemperatureMonitoringWithLogisticsEnabled());
  }

  public boolean isTemperatureMonitoringEnabled() {
    return (assetConfig != null && assetConfig.isTemperatureMonitoringEnabled());
  }

  public DashboardConfig getDashboardConfig() {
    if (dashboardConfig == null) {
      return new DashboardConfig();
    }
    return this.dashboardConfig;
  }

  public void setDashboardConfig(DashboardConfig dashboardConfig) {
    this.dashboardConfig = dashboardConfig;
  }

  public ApprovalsConfig getApprovalsConfig() {
    return approvalsConfig;
  }

  public void setApprovalsConfig(ApprovalsConfig approvalsConfig) {
    this.approvalsConfig = approvalsConfig;
  }

  public boolean getUiPreference() {
    return uiPref;
  }

  public void setUiPreference(boolean uiPref) {
    this.uiPref = uiPref;
  }

  public boolean isOnlyNewUIEnabled() {
    return onlyNewUI;
  }

  public void setOnlyNewUI(boolean onlyNewUI) {
    this.onlyNewUI = onlyNewUI;
  }

  public Map<String, SupportConfig> getSupportConfigMapByRole() {
    return supportConfigMap;
  }

  public SupportConfig getSupportConfigByRole(String role) {
    if (supportConfigMap == null) {
      return null;
    }
    return supportConfigMap.get(role);
  }

  public void setSupportConfigByRole(String role, SupportConfig sc) {
    if (supportConfigMap == null) {
      supportConfigMap = new HashMap<String, SupportConfig>();
    }
    supportConfigMap.put(role, sc);
  }

  public void setAdminContactByType(String ty, AdminContactConfig ac) {
    if (adminContactConfigMap == null) {
      adminContactConfigMap = new HashMap<>();
    }
    adminContactConfigMap.put(ty, ac);
  }

  public Map<String, AdminContactConfig> getAdminContactConfigMap() {
    return adminContactConfigMap;
  }

  public void setAdminContactConfigMap(
      Map<String, AdminContactConfig> adminContactConfigMap) {
    this.adminContactConfigMap = adminContactConfigMap;
  }

  public boolean isEnableShippingOnMobile() {
    return enableShippingOnMobile;
  }

  public void setEnableShippingOnMobile(boolean enableShippingOnMobile) {
    this.enableShippingOnMobile = enableShippingOnMobile;
  }

  public boolean isEnableSwitchToNewHost() {
    return enableSwitchToNewHost;
  }

  public void setEnableSwitchToNewHost(boolean enableSwitchToNewHost) {
    this.enableSwitchToNewHost = enableSwitchToNewHost;
  }

  public String getNewHostName() {
    return newHostName;
  }

  public void setNewHostName(String newHostName) {
    this.newHostName = newHostName;
  }

  public void settagInvByOperation(Map<String, String> tagsInvByOperation) {
    this.tagsInvByOperation = tagsInvByOperation;
  }

  public Map<String, String> gettagsInvByOperation() {
    return tagsInvByOperation;
  }

  public int getAuthenticationTokenExpiry() {
    return authenticationTokenExpiry;
  }

  public void setAuthenticationTokenExpiry(int authenticationTokenExpiry) {
    this.authenticationTokenExpiry = authenticationTokenExpiry;
  }

  public boolean isLocalLoginRequired() {
    return localLoginRequired;
  }

  public void setLocalLoginRequired(boolean localLoginRequired) {
    this.localLoginRequired = localLoginRequired;
  }

  public SyncConfig getSyncConfig() {
    return syncConfig;
  }

  public void setSyncConfig(SyncConfig syncConfig) {
    this.syncConfig = syncConfig;
  }

  public boolean isDisableOrdersPricing() {
    return disableOrdersPricing;
  }

  public void setDisableOrdersPricing(boolean disableOrdersPricing) {
    this.disableOrdersPricing = disableOrdersPricing;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  public String getStateId() {
    return stateId;
  }

  public void setStateId(String stateId) {
    this.stateId = stateId;
  }

  public String getDistrictId() {
    return districtId;
  }

  public void setDistrictId(String districtId) {
    this.districtId = districtId;
  }

  public int getStoreAppTheme() { return storeAppTheme; }

  public void setStoreAppTheme(int storeAppTheme) { this.storeAppTheme = storeAppTheme; }

}
