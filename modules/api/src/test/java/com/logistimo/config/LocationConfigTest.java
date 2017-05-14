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

package com.logistimo.config;

import com.google.gson.Gson;

import org.junit.Test;

import com.logistimo.config.models.LocationConfig;
import com.logistimo.config.models.LocationModel;
import com.logistimo.config.models.StateModel;

import static org.junit.Assert.assertEquals;

/**
 * Created by mohansrinivas on 12/16/16.
 */
public class LocationConfigTest {
  String lConfigStr = "{\n" +
      "    \"data\": {\n" +
      "        \"IN\": {\n" +
      "            \"name\": \"India\",\n" +
      "            \"states\": {\n" +
      "                \"Andhra Pradesh\": {\n" +
      "                    \"districts\": {\n" +
      "                        \"Nellore\": {}\n" +
      "                    }\n" +
      "                }\n" +
      "            }\n" +
      "        }\n" +
      "    }\n" +
      "}";

  @Test
  public void getStateTest() {
    LocationModel lc = new Gson().fromJson(lConfigStr, LocationModel.class);
    LocationConfig.constructStateDistrictMaps(lc);
    StateModel sm = LocationConfig.getState("Nellore");
    assertEquals("Andhra Pradesh", sm.state);
    assertEquals("IN", sm.country);
  }

  @Test
  public void getCountryTest() {
    LocationModel lc = new Gson().fromJson(lConfigStr, LocationModel.class);
    LocationConfig.constructStateDistrictMaps(lc);
    assertEquals("IN", LocationConfig.getCountry("Andhra Pradesh"));
  }
}
