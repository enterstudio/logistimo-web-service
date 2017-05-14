/**
 *
 */
package com.logistimo.utils;

import com.logistimo.AppFactory;
import com.logistimo.dao.IDaoUtil;

import com.logistimo.pagination.PageParams;

import java.util.List;

import javax.jdo.Query;


/**
 * @author Arun
 */
public class QueryUtil {

  private static IDaoUtil daoUtil = AppFactory.get().getDaoUtil();

  // Set the pagination parameters to a given query
  public static void setPageParams(Query q, PageParams pageParams) {
    // Add pagination parameters, if needed
    if (q != null && pageParams != null) {
      String cursorStr = pageParams.getCursor();
      boolean hasCursor = (cursorStr != null && !cursorStr.isEmpty());
      int size = pageParams.getSize();
      int startOffset = pageParams.getOffset();
      int endOffset = startOffset + size;
      if (hasCursor) {
        daoUtil.setCursorExtensions(cursorStr, q);
        startOffset =
            0; // ensure that the offset is always zero, when cursor is present (given the range is starting from this cursor)
        endOffset =
            size; // ensure that end offset is size (and does not include startOffset within)
      }
      // Set the range
      if (endOffset > 0) {
        q.setRange(startOffset, endOffset);
      }
    }
  }

  // Get the web-safe cursor from a given result set
  @SuppressWarnings("rawtypes")
  public static String getCursor(List results) {
    return daoUtil.getCursor(results);
  }
}
