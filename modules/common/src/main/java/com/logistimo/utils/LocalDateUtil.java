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
import com.logistimo.constants.Constants;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.services.Resources;
import com.logistimo.proto.JsonTagsZ;

import com.logistimo.logger.XLog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * @author arun
 */
public class LocalDateUtil {
  public static final String GMT = "GMT";
  public static final String IST = "IST";
  public static final String PST = "PST";
  public static final String EST = "EST";
  public static final long MILLISECS_PER_DAY = 86400000;
  private static final XLog xLogger = XLog.getLog(LocalDateUtil.class);
  // Regex of timezone prefixes - to filter duplicate time zones in TimeZone.getAvailableIDs()
  private static String
      TIMEZONE_ID_PREFIXES =
      "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

  //private static final XLog xLogger = XLog.getLog(LocalDateUtil.class);

  public static String format(String strTimestamp, Locale locale, String timezone)
      throws ParseException {
    // Convert to Date object
    DateFormat df = DateFormat.getDateTimeInstance();
    return format(df.parse(strTimestamp), locale, timezone);
  }

  public static String format(Date timestamp, Locale locale, String timezone) {
    return format(timestamp, locale, timezone, false);
  }

  public static String format(Date timestamp, Locale locale, String timezone, boolean dateOnly) {
    // Get the date formatter according to style and locale
    DateFormat dateFormat = null;
    if (locale != null) {
      if (dateOnly) {
        dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
      } else {
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
      }
    } else {
      if (dateOnly) {
        dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
      } else {
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
      }
    }
    if (timezone != null) {
      dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
    }
    return dateFormat.format(timestamp);
  }

  /**
   * Gets the date time short format for the given locale.
   *
   * @param locale
   * @param dateOnly  If true, sends only date ignoring time field
   * @return          a short date format
   */
  public static String getDateTimePattern(Locale locale, boolean dateOnly) {
    DateFormat formatter;
    if(dateOnly) {
      formatter = DateFormat.getDateInstance(DateFormat.SHORT,locale);
    } else {
      formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    }
    return ((SimpleDateFormat)formatter).toLocalizedPattern();
  }

  // Format date according to a specified date format
  public static String formatCustom(Date timestamp, String dateFormat, String timezone) {
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    if (timezone != null && !timezone.isEmpty()) {
      sdf.setTimeZone(TimeZone.getTimeZone(timezone));
    }
    return sdf.format(timestamp);
  }

  // Parse according to specified date format
  public static Date parseCustom(String timestampStr, String dateFormat, String timezone)
      throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    if (timezone != null && !timezone.isEmpty()) {
      sdf.setTimeZone(TimeZone.getTimeZone(timezone));
    }
    return sdf.parse(timestampStr);
  }
  public static Date parseDate(String timestampStr, List<String> dateFormats, String timezone)
          throws ParseException {
    Date date=null;
    SimpleDateFormat sdf = new SimpleDateFormat();
    for(String s:dateFormats){
      sdf.applyPattern(s);
      if (timezone != null && !timezone.isEmpty()) {
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
      }
      try {
        date = sdf.parse(timestampStr);
      }catch (ParseException ignored){
      }
    }
    if(date==null){
      throw new ParseException(timestampStr,0);
    }
    return date;
  }

  public static Date parse(String timestampStr, Locale locale, String timezone)
      throws ParseException {
    DateFormat dateFormat = null;
    if (locale != null) {
      dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    } else {
      dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }
    if (timezone != null) {
      dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
    }
    return dateFormat.parse(timestampStr);
  }

  // Find the difference (in days) between two dates
  public static int daysBetweenDates(Calendar start, Calendar end) {
    if (start == null || end == null) {
      return 0;
    }
    // Get the millisecond times, with the daylight savings time offsets (read more at: http://www.xmission.com/~goodhill/dates/deltaDates.html)
    long endL = end.getTimeInMillis() + end.getTimeZone().getOffset(end.getTimeInMillis());
    long startL = start.getTimeInMillis() + start.getTimeZone().getOffset(start.getTimeInMillis());

    return (int) ((endL - startL)/ MILLISECS_PER_DAY);
  }

  // Get formatted time range
  public static String getFormattedTimeRange(String timeRange) {
    if (timeRange == null || timeRange.isEmpty()) {
      return null;
    }
    String[] times = timeRange.split("-"); // split start and end time in the range, if any
    String str = times[0];
    if (times.length == 2) {
      String[] startDate = times[0].split(" ");
      String[] endDate = times[1].split(" ");
      if (startDate[0].equals(endDate[0]) && endDate.length > 1) {
        str += " - " + endDate[1];
        if (endDate.length == 3) {
          str += " " + endDate[2];
        }
      } else {
        str += " - " + times[1];
      }
    }
    return str;
  }

  // Find the difference (in days) between two dates - date objects as inputs.
  public static int daysBetweenDates(Date start, Date end, Locale locale) {
    // Get the calendar objects
    Calendar startCal = GregorianCalendar.getInstance(locale);
    Calendar endCal = GregorianCalendar.getInstance(locale);

    return daysBetweenDates(startCal, endCal);
  }

  // Get a date numDays days previous to the given date
  public static Date getOffsetDate(Date d, int numDays) {
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(d);
    cal.add(Calendar.DATE, numDays);
    return cal.getTime();
  }

  // Get a date X units previous to the given date; units are determined by type (e.g. Calendar.DATE, Calendar.HOUR, and so on)
  public static Date getOffsetDate(Date d, int amount, int type) {
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(d);
    cal.add(type, amount);
    return cal.getTime();
  }

  // Get a time duration in Milliseconds in hours, to second decimal point
  public static float getMillisInHours(long duration) {
    float hours = ((float) duration) / 3600000F;
    return ((float) Math.round(hours * 100.0)) / 100F;
  }

  // Get millis in hour and day when value is 1

  public static String getFormattedMillisInHoursDays(long duration, Locale locale) {
    return getFormattedMillisInHoursDays(duration, locale, false);
  }

  // Get millis in hours and days, in string format (if > 1 day, only days are returned)
  public static String getFormattedMillisInHoursDays(long duration, Locale locale,
                                                     Boolean forOrders) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    float hours = getMillisInHours(duration);
    if (hours < 24.0) {
      if (forOrders && (hours == 1f || hours == 0f)) {
        return String.valueOf(Math.round(hours)) + " " + messages.getString("order.hours");
      }
      return String.format("%.2f", hours) + " " + messages.getString("hours");
    }
    float days = Math.round(hours / 24.0);
    String str;
    if (forOrders && (days == 1f || days == 0f)) {
      str = String.valueOf(days) + " " + messages.getString("day");
    } else {
      str = String.valueOf(days) + " " + messages.getString("days");
    }
    ///float remHours = ( hours % 24 );
    ///if ( remHours > 0 )
    ///	str += " " + String.format( "%.2f", remHours ) + " " + messages.getString( "hours" );
    return str;
  }

  // Compare two dates on year, month and date
  // Returns 0, if date1 == date2
  //         1, if date1 > date2
  //		  -1, if date1 < date2
  public static int compareDates(Calendar date1, Calendar date2) {
    int val = 0;
    int y1 = date1.get(Calendar.YEAR);
    int m1 = date1.get(Calendar.MONTH);
    int d1 = date1.get(Calendar.DATE);
    int y2 = date2.get(Calendar.YEAR);
    int m2 = date2.get(Calendar.MONTH);
    int d2 = date2.get(Calendar.DATE);
    if (y1 > y2) {
      val = 1;
    } else if (y1 < y2) {
      val = -1;
    } else { // years are equal, compare months
      if (m1 > m2) {
        val = 1;
      } else if (m1 < m2) {
        val = -1;
      } else { // months equal, compare days
        if (d1 > d2) {
          val = 1;
        } else if (d1 < d2) {
          val = -1;
        } else {
          val = 0;
        }
      }
    }

    return val;
  }

  // Get the map of timezone display names, and ids (note: key is display name, and value is ID)
  public static Map<String, String> getTimeZoneNames() {
    String[] timezones = TimeZone.getAvailableIDs();
    Map<String, String> timezoneMap = new HashMap<String, String>();
    for (int i = 0; i < timezones.length; i++) {
      if (timezones[i].matches(TIMEZONE_ID_PREFIXES)) {
        timezoneMap.put(TimeZone.getTimeZone(timezones[i]).getDisplayName(), timezones[i]);
      }
    }
    return timezoneMap;
  }

  // Get the map of time zone ids and timezone display names (note: key is the ID and value is display name)
  public static Map<String, String> getTimeZoneNamesKVReversed() {
    String[] timezones = TimeZone.getAvailableIDs();
    Map<String, String> timezoneMap = new HashMap<String, String>();
    for (int i = 0; i < timezones.length; i++) {
      if (timezones[i].matches(TIMEZONE_ID_PREFIXES)) {
        timezoneMap.put(timezones[i], TimeZone.getTimeZone(timezones[i]).getDisplayName());
      }
    }
    return timezoneMap;
  }

  // Get the map of timezone display names, and ids (note: key is display name, and value is ID)
  public static Map<String, String> getTimeZoneNamesWithOffset(Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String[] timezones = TimeZone.getAvailableIDs();
    Map<String, String> timezoneMap = new HashMap<String, String>();
    for (int i = 0; i < timezones.length; i++) {
      if (timezones[i].matches(TIMEZONE_ID_PREFIXES)) {
        long
            offset =
            TimeZone.getTimeZone(timezones[i]).getRawOffset() + TimeZone.getTimeZone(timezones[i])
                .getDSTSavings();
        String
            text =
            String.format("%s%s%02d:%02d", GMT, offset >= 0 ? "+" : "-", Math.abs(offset) / 3600000,
                (Math.abs(offset) / 60000) % 60);
        timezoneMap.put(timezones[i],
            TimeZone.getTimeZone(timezones[i]).getDisplayName() + " (" + text + ")");
      }
    }
    return timezoneMap;
  }

  // Get the current year
  public static int getCurrentYear() {
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(new Date());
    return cal.get(Calendar.YEAR);
  }

  // Get the month
  public static boolean datesOfSameMonth(Date d1, Date d2) {
    if (d1 == null || d2 == null) {
      return false;
    }
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(d1);
    int m1 = cal.get(Calendar.MONTH);
    int y1 = cal.get(Calendar.YEAR);
    cal.setTime(d2);
    int m2 = cal.get(Calendar.MONTH);
    int y2 = cal.get(Calendar.YEAR);
    return ((y1 == y2) && (m1 == m2));
  }

  // Reset time fields from hour to ms.
  public static Calendar resetTimeFields(Calendar cal) {
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal;
  }

  // Parse time ranges in the format <from-Date>-<toDate>,..., each date being of the form dd/MM/yy hh:mm:ss
  public static List<Map<String, Date>> parseTimeRanges(String timeRangesStr, Locale locale,
                                                        String timezone) throws ParseException {
    StringTokenizer st = new StringTokenizer(timeRangesStr, ",");
    List<Map<String, Date>> timeRanges = new ArrayList<Map<String, Date>>();
    while (st.hasMoreTokens()) {
      timeRanges.add(parseTimeRange(st.nextToken(), locale, timezone));
    }
    return timeRanges;
  }

  // Parse a given time range of the form <fromDate>-<toDate>, each date being of the format dd/MM/yy hh:mm:ss
  public static Map<String, Date> parseTimeRange(String timeRange, Locale locale, String timezone)
      throws ParseException {
    if (timeRange == null || timeRange.isEmpty()) {
      return null;
    }
    int index = timeRange.indexOf('-');
    String startTime = null, endTime = null;
    if (index == -1) {
      startTime = timeRange;
    } else {
      startTime = timeRange.substring(0, index);
      endTime = timeRange.substring(index + 1);
    }
    if (startTime == null || startTime.isEmpty()) {
      return null;
    }
    Map<String, Date> timeRangeMap = new HashMap<String, Date>();
    timeRangeMap.put(JsonTagsZ.TIME_START, parse(startTime, locale, timezone));
    if (endTime != null) {
      timeRangeMap.put(JsonTagsZ.TIME_END, parse(endTime, locale, timezone));
    }
    return timeRangeMap;
  }

  // Format a time range (<startTime>-<endTime>)
  public static String formatTimeRange(Map<String, Date> timeRange, Locale locale,
                                       String timezone) {
    if (timeRange == null || timeRange.isEmpty()) {
      return null;
    }
    Date start = timeRange.get(JsonTagsZ.TIME_START);
    if (start == null) {
      return null;
    }
    String str = format(start, locale, timezone);
    Date end = timeRange.get(JsonTagsZ.TIME_END);
    if (end != null) {
      str += "-" + format(end, locale, timezone);
    }
    return str;
  }

  // Format time ranges (CSV of time-ranges formatted as above)
  public static String formatTimeRanges(List<Map<String, Date>> timeRanges, Locale locale,
                                        String timezone) {
    Iterator<Map<String, Date>> it = timeRanges.iterator();
    String str = "";
    while (it.hasNext()) {
      if (str.length() > 0) {
        str += ",";
      }
      str += formatTimeRange(it.next(), locale, timezone);
    }
    return str;
  }

  // Get file name from date
  public static String getNameWithDate(String name, Date date, Locale locale, String timezone) {
    Calendar cal = null;
    if (locale != null && timezone != null) {
      cal = GregorianCalendar.getInstance(TimeZone.getTimeZone(timezone), locale);
    } else if (locale != null) {
      cal = GregorianCalendar.getInstance(locale);
    } else if (timezone != null) {
      cal = GregorianCalendar.getInstance(TimeZone.getTimeZone(timezone));
    } else {
      cal = GregorianCalendar.getInstance();
    }
    cal.setTime(date);
    String day = String.valueOf(cal.get(Calendar.DATE));
    if (day.length() == 1) {
      day = "0" + day;
    }
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1) {
      month = "0" + month;
    }
    String hour = String.valueOf(cal.get(Calendar.HOUR));
    if (hour.length() == 1) {
      hour = "0" + hour;
    }
    String minute = String.valueOf(cal.get(Calendar.MINUTE));
    if (minute.length() == 1) {
      minute = "0" + minute;
    }
    return name + "_" + day + "_" + month + "_" + cal.get(Calendar.YEAR) + "_" +
        hour + "_" + minute + "_" +
        (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
  }

  public static int monthsBetweenDates(Calendar start, Calendar end) {
    int diffYear = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
    int diffMonth = diffYear * 12 + end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
    return diffMonth;
  }

  //Get timeago from date
  public static String getTimeago(long time, String timezone, Locale locale, boolean appendSuffix) {
    final int SECOND_MILLIS = 1000;
    final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    final long DAY_MILLIS = 24 * HOUR_MILLIS;
    final long MONTH_MILLIS = 30 * DAY_MILLIS;
    final long YEAR_MILLIS = 12 * MONTH_MILLIS;
    String timeString, duration;
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);

    // if time is given in seconds, convert to millis
    if (time < 1000000000000L) {
      time *= 1000;
    }

    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone(timezone));
    final long now = calendar.getTimeInMillis();
    long diff = now - time;
    boolean isTimeAgo = true;

    if (diff < 0) {
      diff *= -1;
      isTimeAgo = false;
    }

    if (diff < 2 * MINUTE_MILLIS) {
      timeString = "1";
      duration = backendMessages.getString("minute");
    } else if (diff < 60 * MINUTE_MILLIS) {
      timeString = String.valueOf(Math.round(diff / MINUTE_MILLIS));
      duration = backendMessages.getString("minutes");
    } else if (diff < 120 * MINUTE_MILLIS) {
      timeString = "1";
      duration = backendMessages.getString("hour");
    } else if (diff < 24 * HOUR_MILLIS) {
      timeString = String.valueOf(Math.round(diff / HOUR_MILLIS));
      duration = backendMessages.getString("hours");
    } else if (diff < 48 * HOUR_MILLIS) {
      timeString = "1";
      duration = backendMessages.getString("day");
    } else {
      timeString = String.valueOf(Math.round(diff / DAY_MILLIS));
      duration = backendMessages.getString("days");
    }

    timeString += " " + duration;

    if (appendSuffix) {
      if (isTimeAgo) {
        timeString += " " + backendMessages.getString("ago");
      } else {
        timeString += " " + backendMessages.getString("ahead");
      }
    }

    return timeString;
  }

  /**
   * Convert given date to epoch milliseconds with timezone
   */
  public static long convertDateToEpoch(Date date, String timezone) {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone(timezone));
    calendar.setTimeInMillis(date.getTime());

    return calendar.getTimeInMillis();
  }

  public static String convertDateToTimeAgo(Date date, String timezone, Locale locale,
                                            boolean appendSuffix) {
    return getTimeago(convertDateToEpoch(date, timezone), timezone, locale, appendSuffix);
  }

  public static Calendar resetTimeFields(Calendar cal, String timezone) {
    if (timezone != null) {
      Calendar newcal = GregorianCalendar.getInstance(TimeZone.getTimeZone(timezone));
      newcal.setTime(cal.getTime());
      cal = newcal;
    }
    return resetTimeFields(cal);
  }

  public static Calendar getZeroTime(String timezone) {
    Calendar
        cal =
        timezone != null ? GregorianCalendar.getInstance(TimeZone.getTimeZone(timezone))
            : GregorianCalendar.getInstance();
    return resetTimeFields(cal);
  }

  public static Date getGMTZeroTimeFromCal(Calendar cal) {
    Calendar sCal = Calendar.getInstance();
    sCal = resetTimeFields(sCal);
    sCal.set(Calendar.DATE, cal.get(Calendar.DATE));
    sCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
    sCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
    return sCal.getTime();
  }

  /**
   * Converts a string in hh:mm format in a particular timezone to hh:mm string in UTC, if domainTimezoneToUTC is true
   * If domainTimezoneToUTC is false, it converts string in hh:mm format in UTC to hh:mm format in domain specific timezone
   * If timezone is null, then the string is returned as is.
   */
  public static String convertTimeString(String timeStr, String timezone,
                                         boolean domainTimezoneToUTC) {
    Calendar cal = null;
    if (domainTimezoneToUTC && timezone != null && !timezone.isEmpty()) {
      cal = GregorianCalendar.getInstance(TimeZone.getTimeZone(timezone));
    } else {
      cal = GregorianCalendar.getInstance(TimeZone.getDefault());
    }
    cal.setTime(new Date());
    LocalDateUtil.resetTimeFields(cal);

    // Get hour/min.
    String[] timeOfDay = timeStr.split(":");
    String returnTimeStr = "00:00";
    try {
      if (timeOfDay.length > 0) {
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeOfDay[0]));
      }
      if (timeOfDay.length > 1) {
        cal.set(Calendar.MINUTE, Integer.parseInt(timeOfDay[1]));
      }
      cal.getTimeInMillis();
      if (domainTimezoneToUTC || timezone == null || timezone.isEmpty()) {
        cal.setTimeZone(TimeZone.getDefault());
      } else {
        cal.setTimeZone(TimeZone.getTimeZone(timezone));
      }
      cal.getTimeInMillis();
      int hr = cal.get(Calendar.HOUR_OF_DAY);
      int min = cal.get(Calendar.MINUTE);
      returnTimeStr =
          ((hr < 10) ? "0" : "") + Integer.toString(hr) + ":" + ((min < 10) ? "0" : "") + Integer
              .toString(min);
    } catch (NumberFormatException e) {
      xLogger
          .warn("{0} when converting timeStr. Message: {1}", e.getClass().getName(), e.getMessage(),
              e);
    }
    return returnTimeStr;
  }

  public static List<String> convertTimeStringList(List<String> timeStrList, String timezone,
                                                   boolean domainTimezoneToUTC) {
    List<String> returnList = new ArrayList<>();
    if (timeStrList != null && !timeStrList.isEmpty()) {
      for (String timeStr : timeStrList) {
        String str = convertTimeString(timeStr, timezone, domainTimezoneToUTC);
        if (str != null) {
          returnList.add(str);
        }
      }
    }
    return returnList;
  }

  /**
   * for cassandra for monthly freq - date is in "YYYY-MM" format where as for Day in "YYYY-MM-DD" format
   * this method will accept the date and freq and send the appropriate format
   */

  public static String formatDateForReports(Date date, String freq) {
    String dateParamStr;
    Calendar cal = Calendar.getInstance();
    if (date != null) {
      cal.setTime(date);
    } else {
      cal.setTime(new Date());
    }
    SimpleDateFormat dateFormat;
    if (freq.equals(ReportsConstants.FREQ_DAILY)) {
      dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_CSV);
      dateParamStr = dateFormat.format(date);
    } else {
      dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_MONTH);
      dateParamStr = dateFormat.format(date);
    }
    return dateParamStr;
  }

  public static long getCurrentTimeInSeconds(String timezone) {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.add(Calendar.MINUTE, 1);
    return calendar.getTimeInMillis() / 1000;
  }

  public static int daysBetweenDates(Date date1, Date date2) {
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(date1);
    resetTimeFields(cal1);
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(date2);
    resetTimeFields(cal2);
    return LocalDateUtil.daysBetweenDates(cal1,cal2);
  }

  public static String formatDateForQueryingDatabase(Date date) {
    if (date == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return (String.valueOf(cal.get(Calendar.YEAR)) + CharacterConstants.HYPHEN + String.valueOf(cal.get(Calendar.MONTH) + 1) + CharacterConstants.HYPHEN + String.valueOf(
        cal.get(Calendar.DATE)));

  }
}
