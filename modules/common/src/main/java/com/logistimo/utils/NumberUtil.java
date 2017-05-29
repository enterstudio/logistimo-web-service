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

/**
 *
 */
package com.logistimo.utils;

import com.logistimo.logger.XLog;

import java.util.Random;

/**
 * @author Arun
 */
public class NumberUtil {

  private static final XLog xLogger = XLog.getLog(NumberUtil.class);


  public static float getFloatValue(Float f) {
    if (f == null || Float.isNaN(f.floatValue())) {
      return 0F;
    }
    return f.floatValue();
  }

  // Round to 2 decimal places
  public static float round2(float f) {
    return (Math.round(f * 100.0)) / 100.0F;
  }

  public static double getDoubleValue(Double d) {
    if (d == null || Double.isNaN(d.doubleValue())) {
      return 0D;
    }
    return d.doubleValue();
  }

  public static int getIntegerValue(Integer i) {
    if (i == null) {
      return 0;
    }
    return i.intValue();
  }

  // Returns an integer (if the float does not have decimal places), or a float rounded to 2nd decimal
  public static String getFormattedValue(float f) {
    // Check if this is integer
    if (Math.ceil((double) f) == f) {
      return String.valueOf((int) f);
    } else {
      float f1 = ((float) Math.round(f * 100F)) / 100F;
      return String.format("%.2f", f1);
    }
  }

  // Returns a five digit random number
  public static int generateFiveDigitRandomNumber() {
    Random r = new Random(System.currentTimeMillis());
    return (1 + r.nextInt(2)) * 10000 + r.nextInt(10000);
  }

  /**
   * Ret
   */
  public static String getFormattedValue(String min) {
    if (min != null) {
      try {
        return getFormattedValue(Float.valueOf(min));
      } catch (Exception e) {
        xLogger.warn("Cannot format a non number {0}", min);
      }
    }
    return null;
  }
}
