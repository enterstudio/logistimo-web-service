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

package com.logistimo.locations.client.impl;

import com.logistimo.entity.ILocation;
import com.logistimo.locations.client.LocationClient;
import com.logistimo.locations.command.GetLocationCommand;
import com.logistimo.locations.constants.LocationConstants;
import com.logistimo.locations.model.LocationRequestModel;
import com.logistimo.locations.model.LocationResponseModel;
import com.logistimo.logger.XLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Service
public class LocationServiceImpl implements LocationClient {

  private static final XLog log = XLog.getLog(LocationServiceImpl.class);

  @Autowired
  @Qualifier("locationsRestTemplate")
  private RestTemplate restTemplate;

  @Override
  public LocationResponseModel getLocationIds(ILocation location) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("x-app-name", "logistimo");
    LocationRequestModel model = convert(location);
    GetLocationCommand locationCommand = new GetLocationCommand(restTemplate, model, headers);
    return locationCommand.execute();
  }

  private LocationRequestModel convert(ILocation location) {
    LocationRequestModel model = new LocationRequestModel();
    model.setAppName(LocationConstants.APP_NAME);
    model.setUserName(location.user());
    model.setCountryCode(location.getCountry());
    model.setState(location.getState());
    model.setDistrict(location.getDistrict());
    model.setTaluk(location.getTaluk());
    model.setCity(location.getCity());
    model.setPincode(location.getPinCode());
    model.setLongitude(location.getLongitude());
    model.setLatitude(location.getLatitude());
    return model;
  }

}
