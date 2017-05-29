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

import java.io.Serializable;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public class DashboardChartModel implements Serializable {

  public Long value;
  public Double per; //Percentage
  public Long num;
  public Long den;
  public Long kid;
  public Long mid;
  public Map<String, DashboardChartModel> materials;

  public DashboardChartModel() {
  }

  public DashboardChartModel(Long value) {
    this.value = value;
  }

  public DashboardChartModel(Long value, Long kid) {
    this.value = value;
    this.kid = kid;
  }

  public DashboardChartModel(Long value, Double per, Long num, Long den) {
    this.value = value;
    this.per = per;
    this.num = num;
    this.den = den;
  }
}
