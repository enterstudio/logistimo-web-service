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

package com.logistimo.inventory.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Mohan Raja
 */
public interface IInventoryMinMaxLog {
  Long getId();

  void setId(Long key);

  Long getInventoryId();

  void setInventoryId(Long iKey);

  Long getMaterialId();

  void setMaterialId(Long mId);

  Long getKioskId();

  void setKioskId(Long kId);

  BigDecimal getMin();

  void setMin(BigDecimal min);

  BigDecimal getMax();

  void setMax(BigDecimal max);

  BigDecimal getMinDuration();

  void setMinDuration(BigDecimal minDur);

  BigDecimal getMaxDuration();

  void setMaxDuration(BigDecimal maxDur);

  Integer getType();

  void setType(Integer type);

  BigDecimal getConsumptionRate();

  void setConsumptionRate(BigDecimal cr);

  Integer getSource();

  void setSource(Integer src);

  String getUser();

  void setUser(String user);

  Date getCreatedTime();

  void setCreatedTime(Date t);

  Integer getMinMaxFrequency();

  void setMinMaxFrequency(Frequency freq);

  enum Frequency {
    DAILY("days"), WEEKLY("weeks"), MONTHLY("months");
    private String d;

    Frequency(String d) {
      this.d = d;
    }

    public static String getDisplayFrequency(Integer fr) {
      return fr != null ? Frequency.values()[fr].toString() : null;
    }

    @Override
    public String toString() {
      return d;
    }
  }
}
