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

package com.logistimo.utils;

import com.logistimo.logger.XLog;

import java.util.List;

public class ParamChecker {
  public static <T> T notNull(T obj, String name) {
    if (obj == null) {
      throw new IllegalArgumentException(name + " cannot be null.");
    }

    return obj;
  }

  public static String notEmpty(String value, String name) {
    notNull(value, name).toString();

    if (value.length() == 0) {
      throw new IllegalArgumentException(name + " cannot be empty.");
    }

    return value;
  }

  public static <T> T[] notNullElements(T[] array, String name) {
    notNull(array, name);

    for (int i = 0; i < array.length; i++) {
      notNull(array[i], XLog.format("list [{0}] element [{1}]", name, i));
    }

    return array;
  }

  public static <T> List<T> notNullElements(List<T> list, String name) {
    notNull(list, name);

    for (int i = 0; i < list.size(); i++) {
      notNull(list.get(i), XLog.format("list [{0}] element [{1}]", name, i));
    }

    return list;
  }
}
