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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import com.logistimo.proto.utils.ResourceBundle;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Arun
 */
public class Tags {

  public String NOTAGS = "{Others}";

  private Hashtable tagMap = null; // map of tag and vector(hashtable) of material data

  public Tags() {
    this(null);
  }

  public Tags(ResourceBundle bundle) {
    tagMap = new Hashtable();
    if (bundle != null) {
      NOTAGS = "{" + bundle.getString("notags") + "}";
    }
  }

  public Enumeration getTags() {
    return tagMap.keys();
  }

  public Vector getData(String tag) {
    return (Vector) tagMap.get(tag);
  }

  public void addData(String tag, Hashtable data) {
    Vector v = getData(tag);
    if (v == null) {
      v = new Vector();
      tagMap.put(tag, v);
    }
    v.addElement(data);
  }

  public Hashtable getTagMap() {
    return this.tagMap;
  }

  public boolean hasTags() {
    return !tagMap.isEmpty();
  }

  public int size() {
    return tagMap.size();
  }

  public void clear() {
    tagMap.clear();
  }

  public boolean hasOnlyNOTAGStag() { // check if only NOTAGS tag is present
    return tagMap.size() == 1 && tagMap.containsKey(NOTAGS);
  }
}
