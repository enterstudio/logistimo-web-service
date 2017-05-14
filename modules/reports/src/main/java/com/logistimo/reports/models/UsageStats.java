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

package com.logistimo.reports.models;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import com.logistimo.reports.entity.slices.IMonthSlice;

import com.logistimo.pagination.Results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UsageStats {
  @Expose
  List<DomainUsageStats> domainUsageStatsList;
  String cursor; // cursor

  @SuppressWarnings("unchecked")
  public UsageStats(Results results) {
    this(results.getResults());
    this.cursor = results.getCursor();
  }

  public UsageStats(List<IMonthSlice> monthSlices) {
    if (monthSlices != null && !monthSlices.isEmpty()) {
      domainUsageStatsList = new ArrayList<DomainUsageStats>();
      // Iterate through monthSlices
      Iterator<IMonthSlice> monthSlicesIter = monthSlices.iterator();
      while (monthSlicesIter.hasNext()) {
        IMonthSlice monthSlice = monthSlicesIter.next();
        DomainUsageStats domainUsageStats = new DomainUsageStats(monthSlice);
        // Set the other members of domainUsageStats
        // Add the domainUsageStats to
        domainUsageStatsList.add(domainUsageStats);
      }
    }
  }

  public String toJSONString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public List<DomainUsageStats> getDomainUsageStatsList() {
    return domainUsageStatsList;
  }
}
