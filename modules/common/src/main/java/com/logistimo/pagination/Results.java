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
package com.logistimo.pagination;

import java.util.List;
import java.util.Locale;

/**
 * Represents a set of results along with a cursor to the data store (the cursor is a datastore pointer to the end of current result set)
 *
 * @author Arun
 */
public class Results <T> {

  protected String cursor = null;
  @SuppressWarnings("rawtypes")
  protected List<T> results = null;
  protected Locale
      locale =
      null;
  // required for any date formatting as per locale; uses Java default otherwise
  protected String
      timezone =
      null;
  // required for time conversion as per timezone; uses Java default otherwise
  protected int numFound;
  protected int offset;

  @SuppressWarnings("rawtypes")
  public Results(List<T> results, String cursor) {
    this.results = results;
    this.cursor = cursor;
  }

  @SuppressWarnings("rawtypes")
  public Results(List<T> results, String cursor,
                 int numFound, int offset) {
    this(results, cursor);
    this.numFound = numFound;
    this.offset = offset;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getNumFound() {
    return numFound;
  }

  public void setNumFound(int numFound) {
    this.numFound = numFound;
  }

  public String getCursor() {
    return cursor;
  }

  @SuppressWarnings("rawtypes")
  public List<T> getResults() {
    return results;
  }

  public int getSize() {
    if (results != null) {
      return results.size();
    } else {
      return 0;
    }
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }
}
