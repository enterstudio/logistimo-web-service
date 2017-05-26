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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

public class Properties {
  private Hashtable propTable = new Hashtable();

  public static Properties loadProperties(String filePath) throws IOException {
    Properties result = new Properties();
    InputStream stream = result.getClass().getResourceAsStream(filePath);
    // Read contents as string
    String contents = StringUtil.read(stream, "UTF-8");
    // Close stream
    stream.close();
    // Get Properties map
    String[] lines = StringUtil.split(contents, "\r\n");
    for (int i = 0; i < lines.length; i++) {
      String[] kv = StringUtil.split(lines[i], "=");
      if (kv.length == 1) {
        result.setProperty(kv[0], "");
      }
      if (kv.length == 2) {
        result.setProperty(kv[0], kv[1]);
      }
    }
    return result;
  }

  public void setProperty(String key, String val) {
    this.propTable.put(key, val);
  }

  public String getProperty(String key) {
    return (String) this.propTable.get(key);
  }

  public int getPropertyCount() {
    return this.propTable.size();
  }

  public Enumeration getEnumeratedNames() {
    return this.propTable.keys();
  }

  public String[] getPropertyNames() {
    String[] result = new String[this.propTable.size()];
    int c = 0;
    for (Enumeration e = this.propTable.keys(); e.hasMoreElements(); ) {
      result[c] = ((String) e.nextElement());
      c++;
    }

    return result;
  }
}