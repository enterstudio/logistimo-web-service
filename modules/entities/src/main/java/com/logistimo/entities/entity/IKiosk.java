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

import com.logistimo.config.models.Permissions;
import com.logistimo.entity.ILocation;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.entity.IUserAccount;

import com.logistimo.domains.ISuperDomain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by charan on 20/05/15.
 */
public interface IKiosk extends ISuperDomain, ILocation {
  // Kiosk types
  String TYPE_RETAIL = "r";
  String TYPE_DISTRIBUTOR = "d";
  String TYPE_WHOLESALER = "w";
  String TYPE_TRANSPORTER = "t";
  String TYPE_MANUFACTURER = "m";
  // Ordering modes
  String ORDERMODE_AUTO = "a";
  String ORDERMODE_MANUAL = "m";
  // Link types
  String CUSTOMER = IKioskLink.TYPE_CUSTOMER;
  String VENDOR = IKioskLink.TYPE_VENDOR;
  // Activity Type
  int TYPE_INVENTORYACTIVITY = 0;
  int TYPE_ORDERACTIVITY = 1;

  @SuppressWarnings({"unchecked", "rawtypes"})
  Hashtable getMapZ(boolean includeOperators, String createdByUserId);

  Long getKioskId();

  void setKioskId(Long kioskId);

  Long getDomainId();

  void setDomainId(Long dId);

  String getName();

  void setName(String name);

  String getUniqueName();

  String getTruncatedName(int numChars);

  String getCustomId();

  void setCustomId(String customId);

  double getLatitude();

  void setLatitude(double latitude);

  double getLongitude();

  void setLongitude(double longitude);

  String getStreet();

  void setStreet(String street);

  String getCity();

  void setCity(String city);

  String getTaluk();

  void setTaluk(String taluk);

  String getDistrict();

  void setDistrict(String district);

  String getState();

  void setState(String state);

  String getCountry();

  void setCountry(String country);

  String getPinCode();

  void setPinCode(String pinCode);

  String getVertical();

  void setVertical(String vertical);

  int getServiceLevel();

  void setServiceLevel(int serviceLevel);

  String getInventoryModel();

  void setInventoryModel(String inventoryModel);

  String getOrderingMode();

  void setOrderingMode(String orderingMode);

  void setOrderingMode(Date timeStamp);

  Date getTimeStamp();

  void setTimeStamp(Date timeStamp);

  Date getLastUpdated();

  void setLastUpdated(Date lastUpdated);

  String getType();

  void setType(String type);

  Permissions getPermissions();

  void setPermissions(Permissions prms);

  List<? extends IUserAccount> getUsers();

  void setUsers(List<? extends IUserAccount> users);

  // Return the user with a specific role
  List<? extends IUserAccount> getUsers(String role);

  List<? extends IPoolGroup> getPoolGroups();

  void setPoolGroups(List<? extends IPoolGroup> poolGroups);

  boolean isOptimizationOn();

  void setOptimizationOn(boolean flag);

  String getCurrency();

  void setCurrency(String currency);

  BigDecimal getTax();

  void setTax(BigDecimal tax);

  String getTaxId();

  void setTaxId(String txId);

  double getGeoAccuracy();

  void setGeoAccuracy(double accuracy);

  String getGeoError();

  void setGeoError(String geoError);

  String getRegisteredBy();

  void setRegisteredBy(String userId);

  List<String> getTags();

  void setTags(List<String> tags);

  void setTgs(List<? extends ITag> tags);

  // Returns location in format: city, taluk, district, state - convenience function
  String getLocation();

  // Return the first user (typically, only one user may be associated with a kiosk) - conveniene funtion
  IUserAccount getUser();

  // Gets a formatted address of this kiosk: street, city, zip, state
  String getFormattedAddress();

  // Get the filter names and values for this kiosk (esp. in the context of reporting)
  Map<String, Object> getDimensions();

  boolean isRouteEnabled(String linkType);

  void setRouteEnabled(String linkType, boolean routeEnabled);

  int getRouteIndex();

  void setRouteIndex(int routeIndex);

  String getRouteTag();

  void setRouteTag(String tag);

  String getEntityJSON();

  String getUpdatedBy();

  void setUpdatedBy(String username);

  boolean isBatchMgmtEnabled();

  void setBatchMgmtEnabled(Boolean batchMgmtEnabled);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);

  int getCustomerPerm();

  void setCustomerPerm(int custPerm);

  int getVendorPerm();

  void setVendorPerm(int vendPerm);

  Date getInventoryActiveTime();

  void setInventoryActiveTime(Date time);

  Date getOrderActiveTime();

  void setOrderActiveTime(Date time);

  Date getActiveTime();

  void setActiveTime(Date time);
}
