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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.logistimo.constants.Constants;
import com.logistimo.entity.ILocation;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;

import javax.jdo.annotations.*;
import java.util.*;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class UserAccount implements IUserAccount, ILocation {

  // NOTE: All constants are represented as String with 3-5 chars, given that it minimizes GAE storage cost and is readable

  @Persistent
  String
      nName;
  @Persistent(table = "USER_ACC_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  List<Long> accDids; // List of domainIds accessible to this user
  @PrimaryKey
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.USER_ID)
  private String userId;
  @Persistent(table = "USERACCOUNT_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source Domain ID
  @Persistent
  private String encodedPassword;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.ROLE)
  private String role;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.CUSTOM_USERID)
  private String cId; // Custom ID
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.FIRST_NAME)
  private String firstName;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.LAST_NAME)
  private String lastName;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.MOBILE)
  private String mobilePhoneNumber;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.EMAIL)
  private String email;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.LANDLINE)
  private String landPhoneNumber;
  @Persistent
  private String gender;
  @Persistent
  private Date birthdate;
  @Persistent
  private int age;
  @Persistent
  private String ageType;
  @Expose
  @SerializedName(JsonTagsZ.STREET_ADDRESS)
  @Persistent
  private String street;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.CITY)
  private String city;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.TALUK)
  private String taluk;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.DISTRICT)
  private String district;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.STATE)
  private String state;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.COUNTRY)
  private String country;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.PINCODE)
  private String pinCode;
  @Persistent
  private Date lastLogin;
  @Persistent
  private Date memberSince;
  @Persistent
  private String registeredBy;
  @Persistent
  private Date uo;
  @Persistent
  private String ub;
  @Persistent
  private int atexp;
  @Persistent
  private boolean isEnabled;
  @Persistent
  @SerializedName(JsonTagsZ.LANGUAGE)
  @Expose
  private String language = Constants.LANG_ENGLISH;
  @Persistent
  private String phoneBrand;
  @Persistent
  private String phoneModelNumber;
  @Persistent
  private String phoneServiceProvider;
  @Persistent
  private String imei;
  @Persistent
  private String simId;
  @Persistent
  @SerializedName(JsonTagsZ.TIMEZONE)
  @Expose
  private String timezone = Constants.TIMEZONE_DEFAULT; // preferred timezone of user
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String
      usrAgnt;
  // User-agent string sent from devices/browsers as part of the http headers
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String
      prevUsrAgnt;
  // previous user agent string, should it change - this helps in tracking changes to devices by users
  @Persistent
  private String ipAddr; // IP address of clients device
  @Persistent
  private String v; // app version
  // Normalized full name of user: fistName lastName (all lowercase) - to facilitate searching
  // non-indexed field
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  @Expose
  @SerializedName(JsonTagsZ.ROUTE_ENABLED_USER)
  private Boolean rte = false; // Indicates if the user uses manual route
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Date lre; // last reconnected from mobile
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Integer ast; // aggregation status
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  @Expose
  @SerializedName(JsonTagsZ.KIOSK_ID)
  private Long pkId; // primary kiosk ID
  // UI preference for the user. false indicates preference is old UI and true indicates preference is new UI
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Boolean
      uiPref =
      false;

  // non-persistent derived fields
  @NotPersistent
  @Expose
  @SerializedName(JsonTagsZ.CONFIGURATION)

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;
  @Persistent
  @Expose
  @SerializedName(JsonTagsZ.PERMISSIONS)
  private String per = PERMISSION_DEFAULT; // User permission

  /**
   * Login on reconnect value to be used, if override is specified
   */
  @Persistent
  private Integer lgr = LR_DOMAIN_DEFAULT;

  @Persistent(table = "USER_TAGS", defaultFetchGroup = "true")
  @Join(column = "userId")
  @Element(column = "id")
  private List<Tag> tgs; // list of user tags

  @NotPersistent //Used to manage tags internally..
  private List<String> tags;


  @Persistent
  private Integer lgSrc;

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
  @Persistent
  private Integer theme;

  // Get the difference of two user lists: a - b
  public static List<IUserAccount> getDifference(List<IUserAccount> a, List<IUserAccount> b) {
    if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
      return a;
    }
    List<IUserAccount> c = new ArrayList<IUserAccount>();
    Iterator<IUserAccount> itA = a.iterator();
    while (itA.hasNext()) {
      IUserAccount uA = itA.next();
      String userId = uA.getUserId();
      Iterator<IUserAccount> itB = b.iterator();
      boolean isInB = false;
      while (itB.hasNext()) {
        IUserAccount u = itB.next();
        if (userId.equals(u.getUserId())) {
          isInB = true;
          break;
        }
      }
      if (!isInB) {
        c.add(uA);
      }
    }
    return c;
  }

  /**
   * Get the map in newer, compressed format
   */
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Hashtable getMapZ() {
    Hashtable userMap = new Hashtable();
    if (userId != null) {
      userMap.put(JsonTagsZ.USER_ID, userId);
    }
    if (firstName != null) {
      userMap.put(JsonTagsZ.FIRST_NAME, firstName);
    }
    if (lastName != null && !lastName.isEmpty()) {
      userMap.put(JsonTagsZ.LAST_NAME, lastName);
    }
    if (role != null && !role.isEmpty()) {
      userMap.put(JsonTagsZ.ROLE, role);
    }
    if (timezone != null) {
      userMap.put(JsonTagsZ.TIMEZONE, timezone);
    }
    if (language != null) {
      userMap.put(JsonTagsZ.LANGUAGE, language);
    }
    if (country != null) {
      userMap.put(JsonTagsZ.COUNTRY, country);
    }
    if (state != null && !state.isEmpty()) {
      userMap.put(JsonTagsZ.STATE, state);
    }
    if (district != null && !district.isEmpty()) {
      userMap.put(JsonTagsZ.DISTRICT, district);
    }
    if (taluk != null && !taluk.isEmpty()) {
      userMap.put(JsonTagsZ.TALUK, taluk);
    }
    if (city != null && !city.isEmpty()) {
      userMap.put(JsonTagsZ.CITY, city);
    }
    if (pinCode != null && !pinCode.isEmpty()) {
      userMap.put(JsonTagsZ.PINCODE, pinCode);
    }
    if (street != null && !street.isEmpty()) {
      userMap.put(JsonTagsZ.STREET_ADDRESS, street);
    }
    if (email != null && !email.isEmpty()) {
      userMap.put(JsonTagsZ.EMAIL, email);
    }
    if (mobilePhoneNumber != null && !mobilePhoneNumber.isEmpty()) {
      userMap.put(JsonTagsZ.MOBILE, mobilePhoneNumber);
    }
    if (landPhoneNumber != null && !landPhoneNumber.isEmpty()) {
      userMap.put(JsonTagsZ.LANDLINE, landPhoneNumber);
    }
    if (rte != null && rte.booleanValue()) {
      userMap.put(JsonTagsZ.ROUTE_ENABLED_USER, "true");
    }
    if (pkId != null) {
      userMap.put(JsonTagsZ.KIOSK_ID, pkId.toString());
    }
    if (cId != null && !cId.isEmpty()) {
      userMap.put(JsonTagsZ.CUSTOM_USERID, cId.toString());
    }
    return userMap;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof IUserAccount && ((IUserAccount) o).getUserId().equals(userId);
  }

  @Override
  public Long getDomainId() {
    return sdId;
  }

  @Override
  public void setDomainId(Long dId) {
    this.sdId = dId;
  }

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }

  @Override
  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  @Override
  public String getEncodedPassword() {
    return encodedPassword;
  }

  @Override
  public void setEncodedPassword(String encodedPassword) {
    this.encodedPassword = encodedPassword;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public void setUserId(String userId) {
    this.userId = userId;
  }

  @Override
  public String getRole() {
    return role;
  }

  @Override
  public void setRole(String role) {
    this.role = role;
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
  public String getFirstName() {
    return firstName;
  }

  @Override
  public void setFirstName(String firstName) {
    this.firstName = firstName;
    if (firstName != null && !firstName.isEmpty()) {
      this.nName = firstName.toLowerCase();
    }
  }

  @Override
  public String getLastName() {
    return lastName;
  }

  @Override
  public void setLastName(String lastName) {
    this.lastName = lastName;
    if (lastName != null && !lastName.isEmpty()) {
      if (nName != null) {
        nName += " " + lastName.toLowerCase();
      } else {
        nName = lastName.toLowerCase();
      }
    }
  }

  @Override
  public String getMobilePhoneNumber() {
    return mobilePhoneNumber;
  }

  @Override
  public void setMobilePhoneNumber(String mobilePhoneNumber) {
    this.mobilePhoneNumber = mobilePhoneNumber;
  }

  public String getSimId() {
    return simId;
  }

  public void setSimId(String simId) {
    this.simId = simId;
  }

  public String getImei() {
    return imei;
  }

  public void setImei(String imei) {
    this.imei = imei;
  }

  @Override
  public String getLandPhoneNumber() {
    return landPhoneNumber;
  }

  @Override
  public void setLandPhoneNumber(String landPhoneNumber) {
    this.landPhoneNumber = landPhoneNumber;
  }

  @Override
  public String getGender() {
    return gender;
  }

  @Override
  public void setGender(String gender) {
    this.gender = gender;
  }

  @Override
  public Date getBirthdate() {
    return birthdate;
  }

  @Override
  public void setBirthdate(Date birthdate) {
    this.birthdate = birthdate;
  }

  @Override
  public int getAge() {
    return age;
  }

  @Override
  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public String getAgeType() {
    return ageType;
  }

  @Override
  public void setAgeType(String ageType) {
    this.ageType = ageType;
  }

  @Override
  public double getLatitude() {
    return 0;
  }

  @Override
  public void setLatitude(double latitude) {
    //todo
  }

  @Override
  public double getLongitude() {
    return 0;
  }

  @Override
  public void setLongitude(double longitude) {
    //todo
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
  public String getEmail() {
    return email;
  }

  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public Date getLastLogin() {
    return lastLogin;
  }

  @Override
  public void setLastLogin(Date timeStamp) {
    this.lastLogin = timeStamp;
  }

  @Override
  public String getRegisteredBy() {
    return registeredBy;
  }

  @Override
  public void setRegisteredBy(String registeredBy) {
    this.registeredBy = registeredBy;
  }

  @Override
  public Date getMemberSince() {
    return memberSince;
  }

  @Override
  public void setMemberSince(Date date) {
    this.memberSince = date;
  }

  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public String getPhoneBrand() {
    return phoneBrand;
  }

  @Override
  public void setPhoneBrand(String phoneBrand) {
    this.phoneBrand = phoneBrand;
  }

  @Override
  public String getPhoneModelNumber() {
    return phoneModelNumber;
  }

  @Override
  public void setPhoneModelNumber(String phoneModelNumber) {
    this.phoneModelNumber = phoneModelNumber;
  }

  @Override
  public String getPhoneServiceProvider() {
    return phoneServiceProvider;
  }

  @Override
  public void setPhoneServiceProvider(String phoneServiceProvider) {
    this.phoneServiceProvider = phoneServiceProvider;
  }

  @Override
  public String getTimezone() {
    return timezone;
  }

  @Override
  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  @Override
  public String getUserAgent() {
    return usrAgnt;
  }

  ;

  @Override
  public void setUserAgent(String userAgentStr) {
    this.usrAgnt = userAgentStr;
  }

  @Override
  public String getPreviousUserAgent() {
    return prevUsrAgnt;
  }

  @Override
  public void setPreviousUserAgent(String userAgentStr) {
    this.prevUsrAgnt = userAgentStr;
  }

  @Override
  public String getIPAddress() {
    return ipAddr;
  }

  @Override
  public void setIPAddress(String ipAddress) {
    this.ipAddr = ipAddress;
  }

  @Override
  public String getAppVersion() {
    return v;
  }

  @Override
  public void setAppVersion(String v) {
    this.v = v;
  }

  @Override
  public Date getLastMobileAccessed() {
    return lre;
  }

  @Override
  public void setLastMobileAccessed(Date d) {
    lre = d;
  }

  @Override
  public Long getPrimaryKiosk() {
    return pkId;
  }

  @Override
  public void setPrimaryKiosk(Long kioskId) {
    pkId = kioskId;
  }

  @Override
  public String getFullName() {
    String fullName = "";
    if (this.firstName != null) {
      fullName = this.firstName;
    }
    if (this.lastName != null && !this.lastName.isEmpty()) {
      if (this.firstName == null || this.firstName.isEmpty()) {
        fullName = this.lastName;
      } else {
        fullName += " " + this.lastName;
      }
    }

    return fullName;
  }

  // Returns location in format: city, taluk, district, state
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
      needsComma = true;
    }
    if (country != null && !country.isEmpty()) {
      location += (needsComma ? ", " : "") + new Locale(language, country).getDisplayCountry();
    }

    return location;
  }

  @Override
  public Locale getLocale() {
    if (country == null) {
      return new Locale(language, "");
    } else {
      return new Locale(language, country);
    }
  }

  @Override
  public boolean isRteEnabled() {
    return rte != null && rte.booleanValue();
  }

  @Override
  public void setRteEnabled(boolean rte) {
    this.rte = rte;
  }

  @Override
  public boolean getUiPref() {
    return uiPref != null && uiPref.booleanValue();
  }

  @Override
  public void setUiPref(boolean uiPref) {
    this.uiPref = uiPref;
  }

  @Override
  public String getKeyString() {
    return userId;
  }

  public Object getParams() {
    return null;
  }

  public Date getUpdatedOn() {
    return uo;
  }

  public void setUpdatedOn(Date uo) {
    this.uo = uo;
  }

  public String getUpdatedBy() {
    return ub;
  }

  public void setUpdatedBy(String ub) {
    this.ub = ub;
  }

  public List<Long> getAccessibleDomainIds() {
    return this.accDids;
  }

  public void setAccessibleDomainIds(List<Long> accDids) {
    this.accDids = accDids;
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

  public Integer getLoginReconnect() {
    return lgr == null ? LR_DOMAIN_DEFAULT : lgr;
  }

  public void setLoginReconnect(Integer lgr) {
    this.lgr = lgr;
  }

  public int getAuthenticationTokenExpiry() {
    return atexp;
  }

  public void setAuthenticationTokenExpiry(int atexp) {
    this.atexp = atexp;
  }

  @Override
  public String getPermission() {
    return per;
  }

  public void setPermission(String per) {
    this.per = per;
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


  public Integer getLoginSource() {
    return lgSrc;
  }

  public void setLoginSource(Integer lgSrc) {
    this.lgSrc = lgSrc;
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

  @Override
  public Integer getStoreAppTheme() { return theme; }

  @Override
  public void setStoreAppTheme(Integer theme) { this.theme = theme; }

  @Override
  public String user () {
    return this.getUpdatedBy();
  }

}
