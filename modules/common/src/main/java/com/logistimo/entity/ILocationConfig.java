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
