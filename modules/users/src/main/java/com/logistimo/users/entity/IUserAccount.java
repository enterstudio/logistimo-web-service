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

package com.logistimo.users.entity;

import com.logistimo.entity.ILocation;
import com.logistimo.tags.entity.ITag;

import com.logistimo.domains.ISuperDomain;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

/**
 * Created by charan on 20/05/15.
 */
public interface IUserAccount extends ISuperDomain, ILocation {
  // Gender
  String GENDER_MALE = "m";
  String GENDER_FEMALE = "f";
  // Age
  String AGETYPE_BIRTHDATE = "b";
  String AGETYPE_YEARS = "y";
  // User attribute names (esp. used for searching)
  String REGISTERED_BY = "registeredBy";

  String ROLE = "role";
  Integer LR_DOMAIN_DEFAULT = -1;
  Integer LR_LOGIN_RECONNECT = 0;
  Integer LR_LOGIN_DONT_RECONNECT = 1;

  String PERMISSION_VIEW = "v";
  String PERMISSION_ASSET = "a";
  String PERMISSION_DEFAULT = "d";

  @SuppressWarnings({"rawtypes", "unchecked"})
  Hashtable getMapZ();

  Long getDomainId();

  void setDomainId(Long dId);

  boolean isEnabled();

  void setEnabled(boolean isEnabled);

  String getEncodedPassword();

  void setEncodedPassword(String encodedPassword);

  String getUserId();

  void setUserId(String userId);

  String getRole();

  void setRole(String role);

  String getCustomId();

  void setCustomId(String customId);

  String getFirstName();

  void setFirstName(String firstName);

  String getLastName();

  void setLastName(String lastName);

  String getMobilePhoneNumber();

  void setMobilePhoneNumber(String mobilePhoneNumber);

  String getSimId();

  void setSimId(String simId);

  String getImei();

  void setImei(String imei);

  String getLandPhoneNumber();

  void setLandPhoneNumber(String landPhoneNumber);

  String getGender();

  void setGender(String gender);

  Date getBirthdate();

  void setBirthdate(Date birthdate);

  int getAge();

  void setAge(int age);

  String getAgeType();

  void setAgeType(String ageType);

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

  String getEmail();

  void setEmail(String email);

  Date getLastLogin();

  void setLastLogin(Date timeStamp);

  String getRegisteredBy();

  void setRegisteredBy(String registeredBy);

  Date getMemberSince();

  void setMemberSince(Date date);

  String getLanguage();

  void setLanguage(String language);

  String getPhoneBrand();

  void setPhoneBrand(String phoneBrand);

  String getPhoneModelNumber();

  void setPhoneModelNumber(String phoneModelNumber);

  String getPhoneServiceProvider();

  void setPhoneServiceProvider(String phoneServiceProvider);

  String getTimezone();

  void setTimezone(String timezone);

  String getUserAgent();

  void setUserAgent(String userAgentStr);

  String getPreviousUserAgent();

  void setPreviousUserAgent(String userAgentStr);

  String getIPAddress();

  void setIPAddress(String ipAddress);

  String getAppVersion();

  void setAppVersion(String v);

  Date getLastMobileAccessed();

  void setLastMobileAccessed(Date d);

  Long getPrimaryKiosk();

  void setPrimaryKiosk(Long kioskId);

  String getFullName();

  // Returns location in format: city, taluk, district, state
  String getLocation();

  Locale getLocale();

  boolean isRteEnabled();

  void setRteEnabled(boolean rte);

  boolean getUiPref();

  void setUiPref(boolean uiPref);

  String getKeyString();

  String getUpdatedBy();

  void setUpdatedBy(String username);

  Date getUpdatedOn();

  void setUpdatedOn(Date updatedOn);

  List<Long> getAccessibleDomainIds();

  void setAccessibleDomainIds(List<Long> accDIds);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);

  Integer getLoginReconnect();

  void setLoginReconnect(Integer lgr);

  int getAuthenticationTokenExpiry();

  void setAuthenticationTokenExpiry(int atexp);

  String getPermission();

  void setPermission(String per);

  List<String> getTags();

  void setTags(List<String> tags);

  void setTgs(List<? extends ITag> tags);

  Integer getLoginSource();

  void setLoginSource(Integer lgSrc);

  Integer getStoreAppTheme();

  void setStoreAppTheme(Integer theme);
}
