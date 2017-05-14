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

package com.logistimo.reports.entity.slices;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mohansrinivas on 10/20/16.
 */

public class DomainStatisticsSlice implements IDomainStats {

  public Map domainsStatsRow = new HashMap<>();

  @Override
  public Long getDomainId() {
    return getIfNotExists("did");
  }

  public Long getActiveKioskCount() {
    return getIfNotExists("akc");
  }

  public Long getLiveKioskCount() {
    return getIfNotExists("lkc");
  }

  public Long getKioskCount() {
    return getIfNotExists("kc");
  }

  public Long getMaterialCount() {
    return getIfNotExists("mc");
  }

  public Long getUserCount() {
    return getIfNotExists("uc");
  }

  public Long getMonitoredLiveWorkingAssets() {
    return getIfNotExists("mlwa");
  }

  public Long getMonitoredActiveWorkingAssets() {
    return getIfNotExists("mawa");
  }

  public Long getMonitoredWorkingAssets() {
    return getIfNotExists("mwa");
  }

  public Long getMonitoredAssetCounts() {
    return getIfNotExists("mac");
  }

  public Long getMonitoringAssetCounts() {
    return getIfNotExists("miac");
  }

  Long getIfNotExists(String key) {
    return domainsStatsRow.get(key) != null ? Long.parseLong(domainsStatsRow.get(key).toString())
        : 0;
  }

}
