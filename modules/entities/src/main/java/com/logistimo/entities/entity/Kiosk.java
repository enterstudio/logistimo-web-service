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

package com.logistimo.entities.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.Permissions;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.UserAccount;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.NumberUtil;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Kiosk implements IKiosk {

  // Logger
  private static final XLog xLogger = XLog.getLog(Kiosk.class);
  @Persistent
  String
      nName;
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @Expose
  private Long kioskId;
  @Persistent(table = "KIOSK_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain Id
  @Persistent
  @Expose
  private String name;
  @Persistent
  private double longitude;
  @Persistent
  private double latitude;
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Double gacc; // geo-accuracy (TODO: capture via bulk upload yet, Mar. 2013)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String gerr; // geo-error, if any (TODO: capture via bulk upload yet, Mar. 2013)
  @Persistent
  private String street;
  @Persistent
  private String city;
  @Persistent
  private String taluk;
  @Persistent
  private String district;
  @Persistent
  private String state;
  @Persistent
  private String country;
  @Persistent
  private String pinCode;
  @Persistent
  private String vertical;
  @Persistent
  private int serviceLevel = 85;
  @Persistent
  private String invModel = "";
  @Persistent
  private String orderingMode = ORDERMODE_AUTO;
  @Persistent
  private Date timeStamp;
  @Persistent
  private Date lastUpdated;
  @Persistent
  private String type = TYPE_RETAIL;
  @Persistent
  private boolean optimizationOn = false;
  @Persistent
  private String currency = Constants.CURRENCY_DEFAULT;
  @Persistent
  private BigDecimal tax;
  @Persistent
  private Integer nlnk; // number of links this kiosk is associated with - DEPRECATED
  @Persistent
  private String txId; // tax identification number
  @Persistent
  private String prms; // permissions on kiosk-links
  // normalized name (all lowercase) for the kiosk - to facilitate searching/uniqueness-check
  // non-indexed field
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Boolean rte = false; // Indicates if the kiosk uses manual route for Customers
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Boolean rtev = false; // Indicates if the kiosk uses manual route for Vendors
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String rgdBy; // registered by userId
  @Persistent(table = "KIOSK_TAGS", defaultFetchGroup = "true")
  @Join(column = "kioskId")
  @Element(column = "id")
  private List<Tag> tgs; // list of kiosk tags
  @Persistent
  private String cId; // Custom ID

  //non-persistent derived fields
  @NotPersistent
  private List<PoolGroup> poolGroups; // updates to list are NOT persisted
  @NotPersistent
  private List<UserAccount> users; // updates to list are persisted
  @NotPersistent
  private int rtIndex = -1; // route index
  @NotPersistent
  private String rtTag; // route segment/tag

  @NotPersistent //Used to manage tags internally..
  private List<String> tags;

  @Persistent
  private String ub;

  @Persistent
  private Boolean be = true;

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;
  @Persistent
  private int cPerm = 0;
  @Persistent
  private int vPerm = 0;
  @Persistent
  private Date iat; // Time of the first inventory transaction that happens on the entity
  @Persistent
  private Date oat; // Time of the first order that happens on the entity
  @Persistent
  private Date at; // iat or oat (whichever is earlier)
  @Persistent(column = "COUNTRY_ID")
  private String countryId;
  @Persistent(column = "STATE_ID")
  private String stateId;
  @Persistent(column = "DISTRICT_ID")
  private String districtId;
  @Persistent(column = "SUBDISTRICT_ID")
  private String talukId;
  @Persistent(column = "CITY_ID")
  private String cityId;

  @Override
  public boolean equals(Object o) {
    if (o instanceof IKiosk) {
      return kioskId.equals(((IKiosk) o).getKioskId());
    }
    return false;
  }

  /**
   * Get map of kiosks in newer, compressed format
   */
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Hashtable getMapZ(boolean includeOperators, String createdByUserId) {
    Hashtable kioskMap = new Hashtable();
    // NOTE: Hashtable does NOT allow NULL values!
    kioskMap.put(JsonTagsZ.KIOSK_ID, kioskId.toString());
    kioskMap.put(JsonTagsZ.NAME, name);
    kioskMap.put(JsonTagsZ.CITY, city);
    if (state != null && !state.isEmpty()) {
      kioskMap.put(JsonTagsZ.STATE, state);
    }
    // Add location info., if present
    if (district != null && !district.isEmpty()) {
      kioskMap.put(JsonTagsZ.DISTRICT, district);
    }
    if (taluk != null && !taluk.isEmpty()) {
      kioskMap.put(JsonTagsZ.TALUK, taluk);
    }
    if (street != null && !street.isEmpty()) {
      kioskMap.put(JsonTagsZ.STREET_ADDRESS, street);
    }
    if (pinCode != null && !pinCode.isEmpty()) {
      kioskMap.put(JsonTagsZ.PINCODE, pinCode);
    }
    // Add lat/lng, if present
    if (!(latitude == 0D && longitude == 0D)) {
      kioskMap.put(JsonTagsZ.LATITUDE, String.valueOf(latitude));
      kioskMap.put(JsonTagsZ.LONGITUDE, String.valueOf(longitude));
      if (gacc != null) {
        kioskMap.put(JsonTagsZ.GEO_ACCURACY, String.valueOf(gacc));
      }
    }
    if (gerr != null && !gerr.isEmpty()) {
      kioskMap.put(JsonTagsZ.GEO_ERROR_CODE, gerr);
    }
    // Add currency, if present
    if (currency != null) {
      kioskMap.put(JsonTagsZ.CURRENCY, currency);
    }
    // Add tax info. if present
    if (tax != null && BigUtil.notEqualsZero(tax)) {
      kioskMap.put(JsonTagsZ.TAX, String.valueOf(tax));
    }
    // Add route index and tag, if any
    if (rtIndex >= 0) {
      kioskMap.put(JsonTagsZ.ROUTE_INDEX, String.valueOf(rtIndex));
    }
    if (rtTag != null && !rtTag.isEmpty()) {
      kioskMap.put(JsonTagsZ.ROUTE_TAG, rtTag);
    }
    // Add route enablement, if any
    if (rte != null && rte.booleanValue()) {
      kioskMap.put(JsonTagsZ.ROUTE_ENABLED_CUSTOMERS, "true");
    }
    if (rtev != null && rtev.booleanValue()) {
      kioskMap.put(JsonTagsZ.ROUTE_ENABLED_VENDORS, "true");
    }
    // Custom Kiosk ID if any
    if (cId != null && !cId.isEmpty()) {
      kioskMap.put(JsonTagsZ.CUSTOM_KIOSKID, cId);
    }
    if (!isBatchMgmtEnabled()) {
      kioskMap.put(JsonTagsZ.DISABLE_BATCH_MGMT, String.valueOf(!isBatchMgmtEnabled()));
    }
    // Add operator data, if any
    if (includeOperators && users != null && !users.isEmpty()) {
      Vector<Hashtable<String, String>> usersVector = new Vector<>();
      Iterator<UserAccount> it = users.iterator();
      while (it.hasNext()) {
        IUserAccount u = it.next();
        Hashtable<String, String> uht = new Hashtable<>();
        uht.put(JsonTagsZ.USER_ID, u.getUserId());
        uht.put(JsonTagsZ.FIRST_NAME, u.getFirstName());
        String lastName = u.getLastName();
        if (lastName != null && !lastName.isEmpty()) {
          uht.put(JsonTagsZ.LAST_NAME, lastName);
        }
        uht.put(JsonTagsZ.MOBILE, u.getMobilePhoneNumber());
        if (StringUtils.isNotEmpty(u.getLandPhoneNumber())) {
          uht.put(JsonTagsZ.LANDLINE, u.getLandPhoneNumber());
        }
        uht.put(JsonTagsZ.ROLE, u.getRole());
        String state = u.getState();
        if (state != null && !state.isEmpty()) {
          uht.put(JsonTagsZ.STATE, state);
        }
        // Add view-only permisions to users that createdByUserId did NOT register
        String
            perms =
            (createdByUserId != null && createdByUserId.equals(u.getRegisteredBy()) ? "e"
                : ""); // allow edit permissions only for users created by createdByUserId
        if (!perms.isEmpty()) {
          uht.put(JsonTagsZ.PERMISSIONS, perms);
        }
        // Add Custom User ID if present
        String uCid = u.getCustomId();
        if (uCid != null && !uCid.isEmpty()) {
          uht.put(JsonTagsZ.CUSTOM_USERID, uCid);
        }
        // Add user's email, if present
        if (u.getEmail() != null && !u.getEmail().isEmpty()) {
          uht.put(JsonTagsZ.EMAIL, u.getEmail());
        }
        // Add to users vector
        usersVector.add(uht);
      }
      if (!usersVector.isEmpty()) {
        kioskMap.put(JsonTagsZ.USERS, usersVector);
      }
    }
    return kioskMap;
  }

  @Override
  public Long getKioskId() {
    return kioskId;
  }

  @Override
  public void setKioskId(Long kioskId) {
    this.kioskId = kioskId;
  }

  public Long getDomainId() {
    return sdId;
  }

  public void setDomainId(Long domainId) {
    sdId = domainId;
  }

  public List<Long> getDomainIds() {
    return dId;
  }

  public void setDomainIds(List<Long> domainIds) {
    this.dId.clear();
    this.dId.addAll(domainIds);
  }

  public void addDomainIds(List<Long> domainIds) {
    if (domainIds == null || domainIds.isEmpty()) {
      return;
    }
    if (this.dId == null) {
      this.dId = new ArrayList<>();
    }
    for (Long dId : domainIds) {
      if (!this.dId.contains(dId)) {
        this.dId.add(dId);
      }
    }
  }

  public void removeDomainId(Long domainId) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.remove(domainId);
    }
  }

  public void removeDomainIds(List<Long> domainIds) {
    if (this.dId != null && !this.dId.isEmpty()) {
      for (Long domainId : domainIds) {
        this.dId.remove(domainId);
      }

    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
    if (name != null) {
      nName = name.toLowerCase(); // normalized name for searching/uniqueness-check
    }
  }

  @Override
  public String getUniqueName() {
    return nName;
  }

  @Override
  public String getTruncatedName(int numChars) {
    if (name != null && name.length() > numChars) {
      return name.substring(0, numChars) + "...";
    }
    return name;
  }

  @Override
  public String getCustomId() {
    return cId;
  }

  @Override
  public void setCustomId(String customId) {
    this.cId = customId;
  }

  @Override
  public double getLatitude() {
    return latitude;
  }

  @Override
  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  @Override
  public double getLongitude() {
    return longitude;
  }

  @Override
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  @Override
  public String getStreet() {
    return street;
  }

  @Override
  public void setStreet(String street) {
    this.street = street;
  }

  @Override
  public String getCity() {
    return city;
  }

  @Override
  public void setCity(String city) {
    this.city = city;
  }

  @Override
  public String getTaluk() {
    return taluk;
  }

  @Override
  public void setTaluk(String taluk) {
    this.taluk = taluk;
  }

  @Override
  public String getDistrict() {
    return district;
  }

  @Override
  public void setDistrict(String district) {
    this.district = district;
  }

  @Override
  public String getState() {
    return state;
  }

  @Override
  public void setState(String state) {
    this.state = state;
  }

  @Override
  public String getCountry() {
    return country;
  }

  @Override
  public void setCountry(String country) {
    this.country = country;
  }

  @Override
  public String getPinCode() {
    return pinCode;
  }

  @Override
  public void setPinCode(String pinCode) {
    this.pinCode = pinCode;
  }

  @Override
  public String getVertical() {
    return vertical;
  }

  @Override
  public void setVertical(String vertical) {
    this.vertical = vertical;
  }

  @Override
  public int getServiceLevel() {
    return serviceLevel;
  }

  @Override
  public void setServiceLevel(int serviceLevel) {
    this.serviceLevel = serviceLevel;
  }

  @Override
  public String getInventoryModel() {
    return invModel;
  }

  @Override
  public void setInventoryModel(String inventoryModel) {
    this.invModel = inventoryModel;
    if (invModel == null || invModel.isEmpty()) {
      this.optimizationOn = false;
    } else {
      this.optimizationOn = true;
    }
  }

  @Override
  public String getOrderingMode() {
    return orderingMode;
  }

  @Override
  public void setOrderingMode(String orderingMode) {
    this.orderingMode = orderingMode;
  }

  @Override
  public void setOrderingMode(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  @Override
  public Date getTimeStamp() {
    return timeStamp;
  }

  @Override
  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  @Override
  public Date getLastUpdated() {
    return lastUpdated;
  }

  @Override
  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  @Override
  public Permissions getPermissions() {
    if (prms != null) {
      try {
        return new Permissions(prms);
      } catch (ConfigurationException e) {
        xLogger.warn("ConfigurationException when getting permissions for kiosk {0}: {1}", kioskId,
            e.getMessage());
      }
    }
    return null;
  }

  @Override
  public void setPermissions(Permissions prms) {
    if (prms != null) {
      this.prms = prms.toJSONString();
    } else {
      this.prms = null;
    }
  }

  @Override
  public List<? extends IUserAccount> getUsers() {
    return users;
  }

  @Override
  public void setUsers(List<? extends IUserAccount> users) {
    this.users = (List<UserAccount>) users;
  }

  // Return the user with a specific role
  @Override
  public List<IUserAccount> getUsers(String role) {
    if (users == null || users.isEmpty()) {
      return null;
    }
    List<IUserAccount> filtered = new ArrayList<IUserAccount>();
    Iterator<UserAccount> it = users.iterator();
    while (it.hasNext()) {
      IUserAccount user = it.next();
      if (user.getRole().equals(role)) {
        filtered.add(user);
      }
    }
    return filtered;
  }

  @Override
  public List<? extends IPoolGroup> getPoolGroups() {
    return poolGroups;
  }

  @Override
  public void setPoolGroups(List<? extends IPoolGroup> poolGroups) {
    this.poolGroups = (List<PoolGroup>) poolGroups;
  }

  @Override
  public boolean isOptimizationOn() {
    return optimizationOn;
  }

  @Override
  public void setOptimizationOn(boolean flag) {
    this.optimizationOn = flag;
  }

  @Override
  public String getCurrency() {
    return currency;
  }

  @Override
  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Override
  public BigDecimal getTax() {
    return tax != null ? tax : BigDecimal.ZERO;
  }

  @Override
  public void setTax(BigDecimal tax) {
    this.tax = tax;
  }

  @Override
  public String getTaxId() {
    return txId;
  }

  @Override
  public void setTaxId(String txId) {
    this.txId = txId;
  }

  @Override
  public double getGeoAccuracy() {
    return NumberUtil.getDoubleValue(gacc);
  }

  @Override
  public void setGeoAccuracy(double accuracy) {
    gacc = new Double(accuracy);
  }

  @Override
  public String getGeoError() {
    return gerr;
  }

  @Override
  public void setGeoError(String geoError) {
    gerr = geoError;
  }

  @Override
  public String getRegisteredBy() {
    return rgdBy;
  }

  @Override
  public void setRegisteredBy(String userId) {
    rgdBy = userId;
  }

  @Override
  public List<String> getTags() {
    if (tags == null) {
      tags = TagUtil.getList(tgs);
    }
    return tags;
  }

  @Override
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  @Override
  public void setTgs(List<? extends ITag> tags) {
    if (this.tgs != null) {
      this.tgs.clear();
      if (tags != null) {
        this.tgs.addAll((Collection<Tag>) tags);
      }
    } else {
      this.tgs = (List<Tag>) tags;
    }
    this.tags = null;
  }

  // Returns location in format: city, taluk, district, state - convenience function
  @Override
  public String getLocation() {
    String location = "";
    boolean needsComma = false;
    if (city != null && !city.isEmpty()) {
      location += city;
      needsComma = true;
    }
    if (district != null && !district.isEmpty()) {
      location += (needsComma ? ", " : "") + district;
      needsComma = true;
    }
    if (state != null && !state.isEmpty()) {
      location += (needsComma ? ", " : "") + state;
    }

    return location;
  }

  // Return the first user (typically, only one user may be associated with a kiosk) - conveniene funtion
  @Override
  public IUserAccount getUser() {
    if (users != null && users.size() > 0) {
      return users.get(0);
    } else {
      return null;
    }
  }

  // Gets a formatted address of this kiosk: street, city, zip, state
  @Override
  public String getFormattedAddress() {
    String addr = "";
    if (getStreet() != null && !getStreet().isEmpty()) {
      addr += getStreet() + ", ";
    }
    addr += getCity() + ", ";
    if (getPinCode() != null && !getPinCode().isEmpty()) {
      addr += getPinCode() + ", ";
    }
    if (getState() != null) {
      addr += getState();
    }
    return addr;
  }

  // Get the filter names and values for this kiosk (esp. in the context of reporting)
  @Override
  public Map<String, Object> getDimensions() {
    Map<String, Object> map = new HashMap<String, Object>();
    if (country != null && !country.isEmpty()) {
      map.put(ReportsConstants.FILTER_COUNTRY, country);
    }
    if (state != null && !state.isEmpty()) {
      map.put(ReportsConstants.FILTER_STATE, state);
    }
    if (district != null && !district.isEmpty()) {
      map.put(ReportsConstants.FILTER_DISTRICT, district);
    }
    if (taluk != null && !taluk.isEmpty()) {
      map.put(ReportsConstants.FILTER_TALUK, taluk);
    }
    if (city != null && !city.isEmpty()) {
      map.put(ReportsConstants.FILTER_CITY, city);
    }
    if (pinCode != null && !pinCode.isEmpty()) {
      map.put(ReportsConstants.FILTER_PINCODE, pinCode);
    }
    if (tgs != null && !tgs.isEmpty()) {
      getTags();
      map.put(ReportsConstants.FILTER_KIOSKTAG, tags);
    }
    return map;
  }

  @Override
  public boolean isRouteEnabled(String linkType) {
    if (IKioskLink.TYPE_CUSTOMER.equals(linkType)) {
      return rte != null && rte.booleanValue();
    } else {
      return rtev != null && rtev.booleanValue();
    }
  }

  @Override
  public void setRouteEnabled(String linkType, boolean routeEnabled) {
    if (IKioskLink.TYPE_CUSTOMER.equals(linkType)) {
      this.rte = routeEnabled;
    } else {
      this.rtev = routeEnabled;
    }
  }

  @Override
  public int getRouteIndex() {
    return rtIndex;
  }

  @Override
  public void setRouteIndex(int routeIndex) {
    rtIndex = routeIndex;
  }

  @Override
  public String getRouteTag() {
    return rtTag;
  }

  @Override
  public void setRouteTag(String tag) {
    rtTag = tag;
  }

  public String getUpdatedBy() {
    return ub;
  }

  public void setUpdatedBy(String ub) {
    this.ub = ub;
  }

  @Override
  public boolean isBatchMgmtEnabled() {
    return be != null ? be : true;
  }

  @Override
  public void setBatchMgmtEnabled(Boolean bte) {
    this.be = bte;
  }

  @Override
  public Date getInventoryActiveTime() {
    return this.iat;
  }

  @Override
  public void setInventoryActiveTime(Date time) {
    this.iat = time;
    setActiveTime(time);
  }

  @Override
  public Date getOrderActiveTime() {
    return this.oat;
  }

  @Override
  public void setOrderActiveTime(Date time) {
    this.oat = time;
    setActiveTime(time);
  }

  @Override
  public Date getActiveTime() {
    return this.at;
  }

  @Override
  public void setActiveTime(Date time) {
    if (time != null && this.at == null) {
      this.at = time;
    }
  }

  @Override
  public String getEntityJSON() {
    Map<String, Object> values = new HashMap<String, Object>(1);
    values.put(JsonTagsZ.NAME, getName());
    values.put(JsonTagsZ.LATITUDE, getLatitude());
    values.put(JsonTagsZ.LONGITUDE, getLongitude());
    List<Map<String, Object>> userList = new ArrayList<Map<String, Object>>(1);
    for (IUserAccount userAccount : getUsers()) {
      Map<String, Object> userValues = new HashMap<String, Object>(1);
      userValues.put(JsonTagsZ.USER_ID, userAccount.getUserId());
      userValues.put(JsonTagsZ.USER, userAccount.getFullName());
      userValues.put(JsonTagsZ.MOBILE, userAccount.getMobilePhoneNumber());
      userList.add(userValues);
    }
    values.put(JsonTagsZ.USERS, userList);
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    String jsonStr = gson.toJson(values);
    return jsonStr;
  }

  public Date getArchivedAt() {
    return arcAt;
  }

  public void setArchivedAt(Date archivedAt) {
    arcAt = archivedAt;
  }

  public String getArchivedBy() {
    return arcBy;
  }

  public void setArchivedBy(String archivedBy) {
    arcBy = archivedBy;
  }

  public int getCustomerPerm() {
    return cPerm;
  }

  public void setCustomerPerm(int cPerm) {
    this.cPerm = cPerm;
  }

  public int getVendorPerm() {
    return vPerm;
  }

  public void setVendorPerm(int vPerm) {
    this.vPerm = vPerm;
  }

  @Override
  public String getCountryId() {
    return countryId;
  }

  @Override
  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  @Override
  public String getStateId() {
    return stateId;
  }

  @Override
  public void setStateId(String stateId) {
    this.stateId = stateId;
  }

  @Override
  public String getDistrictId() {
    return districtId;
  }

  @Override
  public void setDistrictId(String districtId) {
    this.districtId = districtId;
  }

  @Override
  public String getTalukId() {
    return talukId;
  }

  @Override
  public void setTalukId(String talukId) {
    this.talukId = talukId;
  }

  @Override
  public String getCityId() {
    return cityId;
  }

  @Override
  public void setCityId(String cityId) {
    this.cityId = cityId;
  }

}

