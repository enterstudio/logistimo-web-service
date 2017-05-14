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
package com.logistimo.config.models;

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.utils.StringUtil;

import java.io.Serializable;

/**
 * Configuration for a demand board
 *
 * @author Arun
 */
public class DemandBoardConfig implements Serializable {


  // Tags
  public static final String BANNER = "bnn";
  public static final String HEADING = "hdn";
  public static final String COPYRIGHT = "cpy";
  public static final String ISPUBLIC = "spb";
  public static final String SHOWSTOCK = "sst";
  private static final long serialVersionUID = 5512331322276022818L;
  private String banner = null;
  private String heading = null;
  private String copyright = null;
  private boolean isPublic = false;
  private boolean showStock = true;

  public DemandBoardConfig() {
  }

  public DemandBoardConfig(String jsonString) throws ConfigurationException {
    if (jsonString == null) {
      return;
    }
    JSONObject json = null;
    try {
      json = new JSONObject(jsonString);
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    try {
      isPublic = "true".equals((String) json.get(ISPUBLIC));
    } catch (JSONException e) {
      isPublic = false; // default
    }
    try {
      banner = StringUtil.unquote((String) json.get(BANNER));
    } catch (JSONException e) {
      // do nothing
    }
    try {
      heading = StringUtil.unquote((String) json.get(HEADING));
    } catch (JSONException e) {
      // do nothing
    }
    try {
      copyright = StringUtil.unquote((String) json.get(COPYRIGHT));
    } catch (JSONException e) {
      // do nothing
    }
    try {
      showStock = "true".equals((String) json.getString(SHOWSTOCK));
    } catch (JSONException e) {
      // do nothing
    }
  }

  public String getBanner() {
    return banner;
  }

  public void setBanner(String bannerDiv) {
    this.banner = bannerDiv;
  }

  public String getHeading() {
    return heading;
  }

  public void setHeading(String headingDiv) {
    this.heading = headingDiv;
  }

  public String getCopyright() {
    return copyright;
  }

  public void setCopyright(String copyrightDiv) {
    this.copyright = copyrightDiv;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setIsPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public boolean showStock() {
    return showStock;
  }

  public void setShowStock(boolean showStock) {
    this.showStock = showStock;
  }

  public String toJSONString() throws ConfigurationException {
    String jsonStr = null;
    try {
      JSONObject json = new JSONObject();
      json.put(ISPUBLIC, String.valueOf(isPublic));
      if (banner != null && !banner.isEmpty()) {
        json.put(BANNER, StringUtil.quote(banner));
      }
      if (heading != null && !heading.isEmpty()) {
        json.put(HEADING, StringUtil.quote(heading));
      }
      if (copyright != null && !copyright.isEmpty()) {
        json.put(COPYRIGHT, StringUtil.quote(copyright));
      }
      json.put(SHOWSTOCK, String.valueOf(showStock));
      jsonStr = json.toString();
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return jsonStr;
  }
}
