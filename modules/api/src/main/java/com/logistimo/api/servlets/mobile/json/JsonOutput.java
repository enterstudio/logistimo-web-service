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

package com.logistimo.api.servlets.mobile.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import com.logistimo.constants.Constants;

public class JsonOutput {

  public static final String VERSION_DEFAULT = "01";

  @Expose
  protected String v = VERSION_DEFAULT; // version
  @Expose
  protected String st = null; // status: 0 = success, 1 = failure
  @Expose
  protected String ms = null; // message
  @Expose
  protected String cs = null; // cursor

  public JsonOutput(String version, boolean status, String message) {
    this.st = (status ? "0" : "1");
    this.ms = message;
    this.v = version;
  }

  public boolean getStatus() {
    return (st != null && "0".equals(st));
  }

  public String getMessage() {
    return ms;
  }

  public String getVersion() {
    return v;
  }

  public String getCursor() {
    return cs;
  }

  public void setCursor(String cursor) {
    cs = cursor;
  }

  public String toJSONString() {
    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();
    return gson.toJson(this);
  }
}
