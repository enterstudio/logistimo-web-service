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

package com.logistimo.assets.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by charan on 27/11/15.
 */
public class AssetStatusModel {
  /**
   * Vendor Id
   */
  public String vId;
  /**
   * Device Id
   */
  public String dId;
  /**
   * Status
   */
  public int st;
  /**
   * Event type
   */
  public int type;
  /**
   * UTC time in seconds
   */
  public int time; // UTC time in seconds
  /**
   * Temperature
   */
  public double tmp;
  /**
   * Abnormality type. Low excursion/High excursion
   */
  public int aSt;
  /**
   * Monitoring point Id
   */
  public int mpId;

  /**
   * Sensor Id
   */
  public String sId;

  /**
   * Asset meta data. Attributes like min, max sent based on the event type.
   */
  public Map<String, String> attrs = new LinkedHashMap<>(1);

  public List<String> tags;
}
