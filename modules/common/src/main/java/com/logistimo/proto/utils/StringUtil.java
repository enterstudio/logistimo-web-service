package com.logistimo.proto.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Vector;

/**
 * String Utility Methods
 */
public class StringUtil {

  public static String[] split(String original, String separator) {
    Vector nodes = new Vector();
    int index = original.indexOf(separator);
    while (index >= 0) {
      nodes.addElement(original.substring(0, index));
      original = original.substring(index + separator.length());
      index = original.indexOf(separator);
    }
    nodes.addElement(original);
    String[] result = new String[nodes.size()];
    if (nodes.size() > 0) {
      for (int loop = 0; loop < nodes.size(); loop++) {
        result[loop] = (String) nodes.elementAt(loop);
      }
    }
    return result;
  }

  public static String getDate() {
    String date = new Date().toString();
    String[] fields = StringUtil.split(date, " ");
    String formatted = fields[2] + "/" + getMonth(fields[1]) + "/" + fields[5];
    return formatted;
  }

  private static String getMonth(String mon) {
    mon = mon.toLowerCase();
    if (mon.equals("jan")) {
      return "1";
    }
    if (mon.equals("feb")) {
      return "2";
    }
    if (mon.equals("mar")) {
      return "3";
    }
    if (mon.equals("apr")) {
      return "4";
    }
    if (mon.equals("may")) {
      return "5";
    }
    if (mon.equals("jun")) {
      return "6";
    }
    if (mon.equals("jul")) {
      return "7";
    }
    if (mon.equals("aug")) {
      return "8";
    }
    if (mon.equals("sep")) {
      return "9";
    }
    if (mon.equals("oct")) {
      return "10";
    }
    if (mon.equals("nov")) {
      return "11";
    }
    if (mon.equals("dec")) {
      return "12";
    }
    return "1";

  }

  public static float getNumber(String input) {
    try {
      float n = Float.parseFloat(input);
      return n;
    } catch (Exception ex) {
      return -1F;
    }
  }

  // Get an integer string, or a decimal formatted to the specified number of digits
  public static String getFormattedNumber(String number) {
    if (number == null || number.equals("")) {
      return null;
    }
    String newNum = null;
    try {
      Double D = Double.valueOf(number);
      double d10 = D.doubleValue() * 10D;
      int i10 = D.intValue() * 10;
      if (i10 == (int) d10) // then this number is an integer
      {
        newNum = String.valueOf(D.intValue());
      } else // to two decimal digits
      {
        newNum = String.valueOf((double) (((int) (D.doubleValue() * 100D)) / 100D));
      }
    } catch (NumberFormatException e) {
      return null;
    }
    return newNum;
  }

  public static String read(InputStream stream, String charEncoding)
      throws UnsupportedEncodingException, IOException {
    byte[] buff = new byte[1024];
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    int pos = stream.read(buff, 0, 1024);
    while (pos != -1) {
      bout.write(buff, 0, pos);
      pos = stream.read(buff, 0, 1024);
    }
    String contents = new String(bout.toByteArray(), charEncoding);
    bout.close();
    return contents;
  }
}
