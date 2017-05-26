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

package com.logistimo.proto.utils;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author kkalyan
 */
public class LinkedHashtable extends Hashtable {

  Vector list;

  public LinkedHashtable() {
    list = new Vector();
  }

  public Object put(String key, Object o) {
    if (!list.contains(key)) {
      list.addElement(key);
    }
    return super.put(key, o);
  }

  public Object putFirst(String key, Object o) {
    if (list.contains(key)) {
      list.removeElement(key);
    }
    list.insertElementAt(key, 0);
    return super.put(key, o);
  }

  public Object remove(String key) {
    for (int i = 0; i < list.size(); i++) {
      String elem = (String) list.elementAt(i);
      if (elem.equals(key)) {
        list.removeElementAt(i);
        break;
      }
    }
    return super.remove(key);
  }

  public Vector getKeys() {
    return list;
  }

  public void clear() {
    super.clear();
    list.removeAllElements();
  }
}
