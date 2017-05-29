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


/**
 * Represents the needs of a page, when querying datastore and forming a ResultSet
 *
 * @author Arun
 */
public class PageParams {

  // Default size of results set
  public static final int DEFAULT_SIZE = 50;

  public static final String DEFAULT_SIZE_STR = "50";

  public static final String DEFAULT_OFFSET_STR = "0";

  private String cursor = null; // starting cursor
  private int size = DEFAULT_SIZE; // number of results to be returned
  private int offset = 0;

  public PageParams(String cursor, int offset, int size) {
    this.cursor = cursor;
    this.offset = offset;
    this.size = size;
  }

  public PageParams(String cursor, int size) {
    this.cursor = cursor;
    this.size = size;
  }

  public PageParams(int offset, int size) {
    this.offset = offset;
    this.size = size;
  }

  /**
   * @param maxResults
   */
  public PageParams(int size) {
    this.size = size;
  }

  public int getOffset() {
    return offset;
  }

  public int getSize() {
    return size;
  }

  public String getCursor() {
    return cursor;
  }

  public String toString() {
    return "{ cursor=" + cursor + ", size=" + size + "}";
  }
}
