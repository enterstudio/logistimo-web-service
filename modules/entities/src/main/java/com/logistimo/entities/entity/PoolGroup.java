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


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class PoolGroup implements IPoolGroup { // implements JsonBean {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long groupId;
  @Persistent
  private Long dId; // Domain ID
  @Persistent
  private String name;
  @Persistent
  private String description;
  @Persistent
  private String ownerId;
  @Persistent
  private Date timestamp;
  //Derived non-persistent fields
  @NotPersistent
  private String street;
  @NotPersistent
  private String city;
  @NotPersistent
  private String taluk;
  @NotPersistent
  private String district;
  @NotPersistent
  private String state;
  @NotPersistent
  private String country;
  @NotPersistent
  private String pinCode;
  @NotPersistent
  private List<Kiosk> kiosks;

  @Persistent
  private String ub;
  @Persistent
  private Date uo;
  @Persistent
  private String cb;

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;


  @Override
  public Long getGroupId() {
    return groupId;
  }

  @Override
  public void setGroupId(Long groupId) {
    this.groupId = groupId;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long dId) {
    this.dId = dId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getOwnerId() {
    return ownerId;
  }

  @Override
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  @Override
  public Date getTimeStamp() {
    return timestamp;
  }

  @Override
  public void setTimeStamp(Date timeStamp) {
    this.timestamp = timeStamp;
  }

  @Override
  public List<? extends IKiosk> getKiosks() {
    return kiosks;
  }

  @Override
  public void setKiosks(List<? extends IKiosk> kiosks) {
    this.kiosks = (List<Kiosk>) kiosks;
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
  public String getStreet() {
    return street;
  }

  @Override
  public void setStreet(String street) {
    this.street = street;
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
    }

    return location;
  }

  @Override
  public Date getUpdatedOn() {
    return uo;
  }

  @Override
  public void setUpdatedOn(Date uo) {
    this.uo = uo;
  }

  @Override
  public String getUpdatedBy() {
    return ub;
  }

  @Override
  public void setUpdatedBy(String ub) {
    this.ub = ub;
  }

  @Override
  public String getCreatedBy() {
    return cb;
  }

  @Override
  public void setCreatedBy(String cb) {
    this.cb = cb;
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


}

