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
package com.logistimo.orders;

import com.logistimo.orders.entity.IOrder;

import com.logistimo.pagination.Results;

import java.util.List;

/**
 * @author Arun
 */
public class OrderResults extends Results {

  private IOrder order = null;

  /**
   * @param results
   * @param cursor
   */
  @SuppressWarnings("rawtypes")
  public OrderResults(List results, String cursor, IOrder order) {
    super(results, cursor);
    this.order = order;
  }

  /**
   * @param items
   * @param cursorStr
   */
  @SuppressWarnings("rawtypes")
  public OrderResults(List results, String cursor) {
    super(results, cursor);
  }

  public IOrder getOrder() {
    return order;
  }
}
