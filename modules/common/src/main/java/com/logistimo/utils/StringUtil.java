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

import com.logistimo.constants.CharacterConstants;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.CharacterIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author Arun
 */
public class StringUtil {

  private static final String COMMA_ESCAPER = "__COMMA__";

  public static String getCSV(String[] values) {
    String csv = "";
    if (values == null) {
      return csv;
    }
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        csv += ",";
      }
      csv += values[i];
    }
    return csv;
  }

  /**
   * Given csv string, this method will remove duplicate values and will construct unique csv string
   * @param csv - Comma separated value
   * @return - csv value without duplicates
   */
  public static String getUniqueValueCSV(String csv){
    return getCSV(new LinkedHashSet<>(
        Arrays.asList(csv.split(CharacterConstants.COMMA))));
  }

  public static String getCSV(Collection<String> list) {
    return getCSV(list, ",");
  }

  public static String getCSV(Collection<String> list, String separator) {
    String str = null;
    if (list != null) {
      Iterator<String> it = list.iterator();
      if (it.hasNext()) {
        str = it.next();
      }
      while (it.hasNext()) {
        str += separator + it.next();
      }
    }
    return str;
  }

  public static String[] getArray(String csv) {
    return getArray(csv, ",");
  }

  public static String[] getArray(String csv, String separator) {
    if (csv == null || csv.isEmpty()) {
      return null;
    }
    String[] a = csv.split(separator);
    if (a.length == 0) {
      a = new String[1];
      a[0] = csv;
    }

    return a;
  }

  public static List<String> getList(String[] array) {
    if (array == null) {
      return null;
    }
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < array.length; i++) {
      list.add(array[i]);
    }
    return list;
  }

  public static List<String> getList(String csv) {
    return getList(csv, false);
  }

  public static List<String> getList(String csv,
                                     boolean unique) { // get a list with unique entries only, even if duplicated in CSV
    List<String> list = null;
    if (csv != null && !csv.isEmpty()) {
      String[] itemsArray = csv.split(",");
      if (itemsArray == null || itemsArray.length == 0) {
        itemsArray = new String[1];
        itemsArray[0] = csv.trim();
      }
      list = new ArrayList<String>();
      for (int i = 0; i < itemsArray.length; i++) {
        if (unique && list.contains(itemsArray[i])) {
          continue;
        }
        list.add(itemsArray[i].trim());
      }
    }
    return list;
  }

  // Is email valid
  public static boolean isEmailValid(String email) {
    if (email == null || email.isEmpty()) {
      return false;
    }
    String[] tokens = email.split("@");
    if (tokens.length < 2) {
      return false;
    }
    return (tokens[1].contains("."));
  }

  // Escape the character c in the string s
  public static String escapeForHTML(String s, char c) {
    if (s == null || s.isEmpty()) {
      return s;
    }
    StringBuilder b = new StringBuilder();
    StringCharacterIterator it = new StringCharacterIterator(s);
    char ch = it.current();
    while (ch != CharacterIterator.DONE) {
      if (ch == '<') {
        b.append("&lt;");
      } else if (ch == '>') {
        b.append("&gt;");
      } else if (ch == '&') {
        b.append("&amp;");
      } else if (ch == '\"') {
        b.append("&quot;");
      } else {
        b.append(ch);
      }
      ch = it.next();
    }
    return b.toString();
  }

  // Get a string as required for TextListBox
  public static String getTextListBoxString(String[] entries) {
    if (entries == null || entries.length == 0) {
      return "";
    }
    String str = "";
    for (int i = 0; i < entries.length; i++) {
      str += "[\"" + entries[i] + "\",\"" + entries[i] + "\",null,null]";
      if (i != (entries.length - 1)) {
        str += ",";
      }
    }
    return str;
  }

  // Get a string as required for Autocomplete of TextListBox
  public static String getTextListBoxString(String var, String[] entries) {
    if (entries == null || entries.length == 0) {
      return "";
    }
    String str = var;
    if (str == null) {
      str = "";
    }
    for (int i = 0; i < entries.length; i++) {
      str += ".add( \"" + entries[i] + "\",\"" + entries[i] + "\",null )";
    }
    return str;
  }

  // Trim all elements of the array
  public static String[] trim(String[] array) {
    if (array == null) {
      return null;
    }
    String[] a = new String[array.length];
    for (int i = 0; i < array.length; i++) {
      a[i] = array[i].trim();
    }
    return a;
  }

  // Get the string, given an input stream
  public static String fromInputStream(InputStream stream) throws IOException {
    BufferedReader buf = new BufferedReader(new InputStreamReader(stream));
    String str = "", line = null;
    while ((line = buf.readLine()) != null) {
      str += line;
    }
    buf.close();
    return str;
  }

  // Get the diff. between two lists; List[ 0 ] has newly added items, if any, and List[ 1 ] has removed items, if any
  @SuppressWarnings("rawtypes")
  public static List[] getDiffs(List<String> oldItems, List<String> newItems) {
    List[] tagDiffs = new ArrayList[2];
    if (newItems == null || newItems.isEmpty()) {
      tagDiffs[0] = new ArrayList<String>();
      tagDiffs[1] = oldItems;
      return tagDiffs;
    }
    if (oldItems == null || oldItems.isEmpty()) {
      tagDiffs[0] = newItems;
      tagDiffs[1] = new ArrayList<String>();
      return tagDiffs;
    }
    Iterator<String> newIt = newItems.iterator();
    Iterator<String> oldIt = oldItems.iterator();
    List<String> tagsToAdd = new ArrayList<String>();
    List<String> tagsToRemove = new ArrayList<String>();
    // Get tags to be added, if any
    while (newIt.hasNext()) {
      String tag = newIt.next();
      if (!oldItems.contains(tag)) {
        tagsToAdd.add(tag);
      }
    }
    // Get tags to be removed, if any
    while (oldIt.hasNext()) {
      String tag = oldIt.next();
      if (!newItems.contains(tag)) {
        tagsToRemove.add(tag);
      }
    }
    tagDiffs[0] = tagsToAdd;
    tagDiffs[1] = tagsToRemove;

    return tagDiffs;
  }

  // In a given string, escape commas that appear in between quoted strings, with the escape string (typically used for CSV)
  public static String escapeCommasWithinQuotedSubstrings(String str, String escapeStr) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    String thisStr = str;
    String newStr = "";
    int quoteStart = thisStr.indexOf('"');
    if (quoteStart == -1) {
      return str;
    }
    int quoteEnd = thisStr.indexOf('"', quoteStart + 1);
    if (quoteEnd == -1) {
      return str;
    }
    while (quoteEnd != -1) {
      newStr += thisStr.substring(0, quoteStart);
      String quotedStr = thisStr.substring(quoteStart + 1, quoteEnd);
      quotedStr = quotedStr.replaceAll(",", escapeStr);
      newStr += quotedStr;
      thisStr = thisStr.substring(quoteEnd + 1);
      // Update indices
      quoteStart = thisStr.indexOf('"');
      if (quoteStart == -1) {
        break;
      }
      quoteEnd = thisStr.indexOf('"', quoteStart + 1);
    }
    if (!thisStr.isEmpty()) {
      newStr += thisStr;
    }
    return newStr;
  }

  // Given a CSV with quoted values (say, like Windows CSV with quotes values with commas within them), return a token list of values
  public static String[] getCSVTokens(String csvLine) {
    if (csvLine == null || csvLine.isEmpty()) {
      return null;
    }
    String newCsvLine = escapeCommasWithinQuotedSubstrings(csvLine, COMMA_ESCAPER);
    // Tokenize
    String[] tokens = newCsvLine.split(",", -1);
    // Fixed escaped commas, if any
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].contains(COMMA_ESCAPER)) {
        tokens[i] = tokens[i].replaceAll(COMMA_ESCAPER, ",");
      }
    }
    return tokens;
  }

  // Check if the input string has a Long value
  public static boolean isStringLong(String str) {
    if (str == null || str.isEmpty()) {
      return false;
    }
    try {
      Long.parseLong(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // Check if the  input string has a Double value
  public static boolean isStringDouble(String str) {
    if (str == null || str.isEmpty()) {
      return false;
    }
    try {
      Double.parseDouble(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // Check if the input string has a date in the specified date format.
  public static boolean isStringDate(String inputString, String dateFormat) {
    if (parseDateString(inputString, dateFormat) == null) {
      return false;
    }
    return true;
  }

  // Parses the string containing the date in the specified date format and returns the date object.
  public static Date parseDateString(String str, String dateFormat) {
    if (str == null || str.isEmpty() || dateFormat == null || dateFormat.isEmpty()) {
      return null;
    }
    SimpleDateFormat format = new SimpleDateFormat(dateFormat);
    try {
      Date date = format.parse(str);
      return date;
    } catch (ParseException pe) {
      return null;
    }
  }

  // Checks if the input string has a boolean value.
  public static boolean isStringBoolean(String inputString) {
    if (inputString == null || inputString.isEmpty()) {
      return false;
    }
    return (Boolean.parseBoolean(inputString));
  }

  public static String escapeSingleQuotes(String str) {
    String newStr = str.replaceAll("'", "\\\\'");
    return newStr;
  }

  // Check if the  input string has a Integer value
  public static boolean isStringInteger(String str) {
    if (str == null || str.isEmpty()) {
      return false;
    }
    try {
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // Escape quotes in a given string
  public static String quote(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.replace("\"", "\\\"");
  }

  public static String unquote(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.replace("\\\"", "\"");
  }

  public static String trimCSV(String reasonsCSV) {
    String csv = reasonsCSV;
    if (csv != null) {
      csv = csv.trim();
      if (csv.isEmpty()) {
        csv = null;
      } else {
        // Compact spaces between reasons
        csv = StringUtil.getCSV(StringUtil.trim(StringUtil.getArray(csv)));
      }
    }
    if (csv == null) {
      csv = "";
    }
    return csv;
  }

  public static String getTrimmedName(String str) {
    if (StringUtils.isNotEmpty(str)) {
      return str.trim().replaceAll("\\s+", " ");
    }
    return str;
  }

  public static String trimCommas(String str) {
    if(StringUtils.isNotEmpty(str)) {
      str = str.replaceAll("[,]{2,}", CharacterConstants.COMMA).replaceAll("^[,]|[,]$",
          CharacterConstants.EMPTY);
      return str;
    }
    return str;
  }
}
