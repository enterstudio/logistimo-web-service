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

package com.logistimo.reports.constants;

public enum ReportType {

  INV_ABNORMAL_STOCK("ias"),
  INV_REPELISHMENT("ir"),
  INV_TRANSACTION_COUNT("itc"),
  INV_CONSUMPTION("ic"),
  INV_DISCARDS("id"),
  INV_STOCK_AVAILABILITY("isa"),
  INV_STOCK_TREND("ist"),
  INV_SUPPLY("is"),
  INV_UTILISATION("iu"),
  AS_CAPACITY("asa"),
  AS_POWER_AVAILABILITY("apa"),
  AS_RESPONSE_TIME("art"),
  AS_SICKNESS_RATE("asr"),
  AS_TEMPERATURE_EXCURSION("ate"),
  AS_UP_TIME("aut"),
  AS_ASSET_STATUS("aas"),
  ACTIVITY_USER("ua");

  private String value;

  ReportType(String value) {
    this.value = value;
  }

  public static ReportType getReportType(String value) {
    for (ReportType type : ReportType.values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return value;
  }
}
