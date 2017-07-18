package com.logistimo.locations.model;

import java.io.Serializable;

/**
 * Created by kumargaurav on 13/07/17.
 */
public class LocationResponseModel extends LocationBaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public LocationResponseModel() {
        super();
    }

    private String cityId;

    private String talukId;

    private String districtId;

    private String stateId;

    private String countryId;

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getTalukId() {
        return talukId;
    }

    public void setTalukId(String talukId) {
        this.talukId = talukId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }
}
