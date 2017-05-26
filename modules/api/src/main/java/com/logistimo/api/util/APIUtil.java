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

package com.logistimo.api.util;

import com.logistimo.users.entity.IUserAccount;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by charan on 09/03/17.
 */
public class APIUtil {
  // Get the difference of two user lists: a - b
  public static List<IUserAccount> getDifference(List<IUserAccount> a, List<IUserAccount> b) {
    if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
      return a;
    }
    List<IUserAccount> c = new ArrayList<IUserAccount>();
    Iterator<IUserAccount> itA = a.iterator();
    while (itA.hasNext()) {
      IUserAccount uA = itA.next();
      String userId = uA.getUserId();
      Iterator<IUserAccount> itB = b.iterator();
      boolean isInB = false;
      while (itB.hasNext()) {
        IUserAccount u = itB.next();
        if (userId.equals(u.getUserId())) {
          isInB = true;
          break;
        }
      }
      if (!isInB) {
        c.add(uA);
      }
    }
    return c;
  }
}
