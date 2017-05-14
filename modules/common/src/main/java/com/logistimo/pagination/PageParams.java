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
