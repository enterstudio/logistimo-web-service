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
