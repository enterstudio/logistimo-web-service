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
