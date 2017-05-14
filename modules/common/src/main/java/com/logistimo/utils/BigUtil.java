package com.logistimo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Mohan Raja
 */
public class BigUtil {

  public static final java.math.BigDecimal HUNDRED = new BigDecimal(100);

  public static final BigDecimal MAX_VALUE = new BigDecimal("999999999999.9999");

  public static boolean notEquals(BigDecimal o1, BigDecimal o2) {
    return !equals(o1, o2);
  }

  public static boolean equals(BigDecimal o1, BigDecimal o2) {
    if (o1 != null && o2 != null) {
      return o1.compareTo(o2) == 0;
    } else if (o1 == null && o2 == null) {
      return true;
    }
    return false;
  }

  public static boolean greaterThan(BigDecimal o1, BigDecimal o2) {
    return o1.compareTo(o2) > 0;
  }

  public static boolean greaterThanEquals(BigDecimal o1, BigDecimal o2) {
    return o1.compareTo(o2) >= 0;
  }

  public static boolean lesserThan(BigDecimal o1, BigDecimal o2) {
    return o1.compareTo(o2) < 0;
  }

  public static boolean lesserThanEquals(BigDecimal o1, BigDecimal o2) {
    return o1.compareTo(o2) <= 0;
  }

  public static boolean notEquals(BigDecimal o1, float o2) {
    return !equals(o1, o2);
  }

  public static boolean equals(BigDecimal o1, float o2) {
    return o1.compareTo(BigDecimal.valueOf(o2)) == 0;
  }

  public static boolean greaterThan(BigDecimal o1, float o2) {
    return o1.compareTo(BigDecimal.valueOf(o2)) > 0;
  }

  public static boolean greaterThanEquals(BigDecimal o1, float o2) {
    return o1.compareTo(BigDecimal.valueOf(o2)) >= 0;
  }

  public static boolean lesserThan(BigDecimal o1, float o2) {
    return o1.compareTo(BigDecimal.valueOf(o2)) < 0;
  }

  public static boolean lesserThanEquals(BigDecimal o1, float o2) {
    return o1.compareTo(BigDecimal.valueOf(o2)) <= 0;
  }

  public static boolean notEqualsZero(BigDecimal o1) {
    return !equals(o1, BigDecimal.ZERO);
  }

  public static boolean equalsZero(BigDecimal o1) {
    return o1 != null && o1.compareTo(BigDecimal.ZERO) == 0;
  }

  public static boolean greaterThanZero(BigDecimal o1) {
    return o1.compareTo(BigDecimal.ZERO) > 0;
  }

  public static boolean greaterThanEqualsZero(BigDecimal o1) {
    return o1.compareTo(BigDecimal.ZERO) >= 0;
  }

  public static boolean lesserThanZero(BigDecimal o1) {
    return o1.compareTo(BigDecimal.ZERO) < 0;
  }

  public static boolean lesserThanEqualsZero(BigDecimal o1) {
    return o1.compareTo(BigDecimal.ZERO) <= 0;
  }

  public static String getFormattedValue(BigDecimal b) {
    BigDecimal number = b.setScale(0, RoundingMode.FLOOR);
    boolean hasDecimal = number.compareTo(b) != 0;
    if (hasDecimal) {
      return String.format("%.2f", b);
    } else {
      return number.toPlainString();
    }
  }

  public static BigDecimal getZeroIfNull(BigDecimal b) {
    return b == null ? BigDecimal.ZERO : b;
  }

  public static BigDecimal round2(BigDecimal b) {
    return b.setScale(2, RoundingMode.HALF_UP);
  }

  public static boolean gtMax(BigDecimal quantity) {
    return greaterThan(quantity, MAX_VALUE);
  }

  /**
   * Checks if value is null or less than zero or greater than 999,999,999,999.9999
   */
  public static boolean isInvalidQ(BigDecimal quantity) {
    return quantity == null || lesserThanZero(BigUtil.getZeroIfNull(quantity)) || gtMax(quantity);
  }

  public static BigDecimal round2NoTrailZeroes(BigDecimal minMax) {
    return minMax.setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.ONE);
  }
}
