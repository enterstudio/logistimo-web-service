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
public class Results {

  protected String cursor = null;
  @SuppressWarnings("rawtypes")
  protected List results = null;
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
  public Results(List results, String cursor) {
    this.results = results;
    this.cursor = cursor;
  }

  @SuppressWarnings("rawtypes")
  public Results(List results, String cursor,
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
  public List getResults() {
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
