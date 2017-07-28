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

package com.logistimo.config.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


public class AdminContactConfig implements Serializable {

  public static final String PRIMARY_ADMIN_CONTACT = "pac";
  public static final String SECONDARY_ADMIN_CONTACT = "sac";
  protected static final long serialVersionUID = 1l;

  private String pac;

  private String sac;

  public AdminContactConfig() {
  }

  public AdminContactConfig(JSONObject json) {
    try {
      pac = json.getString(PRIMARY_ADMIN_CONTACT);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      sac = json.getString(SECONDARY_ADMIN_CONTACT);
    } catch (JSONException e) {
      // do nothing
    }
  }

  public String getPrimaryAdminContact() {
    return pac;
  }

  public void setPrimaryAdminContact(String pac) {
    this.pac = pac;
  }

  public String getSecondaryAdminContact() {
    return sac;
  }

  public void setSecondaryAdminContact(String sac) {
    this.sac = sac;
  }

  public Object toJSONObject() throws ConfigurationException {
    JSONObject json = null;
    try {
      json = new JSONObject();
      if (pac != null) {
        json.put(PRIMARY_ADMIN_CONTACT, pac);
      }
      if (sac != null) {
        json.put(SECONDARY_ADMIN_CONTACT, sac);
      }
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return json;
  }

}
