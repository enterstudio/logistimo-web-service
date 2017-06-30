package com.logistimo.entity;

/**
 * Created by yuvaraj on 16/03/17.
 */
public interface ILocationConfig {
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

    String getCountryId();

    void setCountryId(String countryId);

    String getStateId();

    void setStateId(String stateId);

    String getDistrictId();

    void setDistrictId(String districtId);

    String getTalukId();

    void setTalukId(String talukId);

    String getCityId();

    void setCityId(String cityId);

}
