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

package com.logistimo.api.models;

import com.logistimo.utils.CommonUtils;

/**
 * Created by charan on 22/06/17.
 */
public class AddressModel {

  private String country;

  private String state;

  private String district;

  private String city;

  private String taluk;

  private String address;

  public AddressModel(String country, String state, String district, String city, String taluk) {
    this.country = country;
    this.state = state;
    this.district = district;
    this.city = city;
    this.taluk = taluk;
    updateAddress();
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
    updateAddress();
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    updateAddress();
    this.state = state;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    updateAddress();
    this.district = district;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    updateAddress();
    this.city = city;
  }

  public String getTaluk() {
    return taluk;
  }

  public void setTaluk(String taluk) {
    updateAddress();
    this.taluk = taluk;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void updateAddress() {
    this.address = CommonUtils.getAddress(city, taluk, district, state);
  }
}
