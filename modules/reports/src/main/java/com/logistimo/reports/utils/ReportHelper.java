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

package com.logistimo.reports.utils;

import com.logistimo.reports.constants.ReportType;
import com.logistimo.reports.constants.ReportViewType;

/** @author Mohan Raja */
public class ReportHelper {
  public static String getColumnsForReport(ReportType type) {
    switch (type) {
      case INV_ABNORMAL_STOCK:
        return "soec,lmec,gmec,soed,lmed,gmed,so100,so90,so80,so70,lm100,lm90,lm80,lm70"+
                ",gm100,gm90,gm80,gm70,lic,sosc,lmsc,gmsc";
      case INV_REPLENISHMENT:
        return "sod, soc, gmd, gmc, lmd, lmc";
      case INV_TRANSACTION_COUNT:
        return "ic, rc, sc, wc, trc, tc, iec, rec, scec, wec, trec, tec, lic";
      case AS_ASSET_STATUS:
        return "wkec, urec, brec, cdec, dec, stbec, wked, ured, bred, cded, " +
                "stbed, ded, wkecs, urecs, brecs, cdecs, stbecs, decs, miac, mac";
      default:
        return null;
    }
  }

  public static String getColumnsForTableReport(ReportType type,ReportViewType viewType) {
    switch (type) {
      case INV_ABNORMAL_STOCK:
      case INV_REPLENISHMENT:
      case AS_ASSET_STATUS:
        return getColumnsForReport(type);
      case INV_TRANSACTION_COUNT:
        switch (viewType) {
          case BY_ENTITY:
            return "ic, rc, sc, wc, trc, tc";
          default:
            return getColumnsForReport(type);
        }
      default:
        return null;
    }
  }
}
