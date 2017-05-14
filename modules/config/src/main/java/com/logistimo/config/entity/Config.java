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
package com.logistimo.config.entity;

import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.JsonConfig;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * @author arun
 *
 *         Stores various configurations in the system.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Cacheable("false")
public class Config implements IConfig {

  public static final String UTF_8 = "utf-8";
  @PrimaryKey
  @Persistent
  private String key; // one of the above values as the key
  @Persistent(defaultFetchGroup = "true")
  @Column(jdbcType = "BLOB")
  private byte[] conf; // serialized string (typically, JSON) of a particular configuration
  @Persistent
  private Date lastUpd; // Last updated time
  @Persistent
  private String uId; // user who last edited the configuration
  @Persistent
  private Long
      dId;
  // domainId; configuration is globally applicable, if domainId is null; else, it is a domain-specific configuration
  @Persistent(defaultFetchGroup = "true")
  @Column(jdbcType = "BLOB")
  private byte[] prevConf; // previous version of the configuration string (just for backup)

  public Config() {
  }

  // Escape quotes in a given string
  public static String quote(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.replace("\"", "\\\"");
  }

  public static String unquote(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.replace("\\\"", "\"");
  }

  public IConfig init(
      IConfig iConfig) { // copies everything except the key, given this is a new object
    Config config = (Config) iConfig;
    conf = config.conf;
    lastUpd = new Date(config.lastUpd.getTime());
    uId = config.uId;
    dId = config.dId;
    prevConf = config.prevConf;
    return this;
  }

  public IConfig copyFrom(IConfig config) { // copies everything, except key
    return init(config);
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public String getConfig() {
    if (conf != null) {
      try {
        return new String(conf, UTF_8);
      } catch (UnsupportedEncodingException e) {
        // ignore
      }
    }
    return null;
  }

  @Override
  public void setConfig(String configStr) {
    if (configStr != null) {
      try {
        this.conf = configStr.getBytes(UTF_8);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    } else {
      this.conf = null;
    }

  }

  @Override
  public Date getLastUpdated() {
    return lastUpd;
  }

  @Override
  public void setLastUpdated(Date lastUpdated) {
    this.lastUpd = lastUpdated;
  }

  @Override
  public String getUserId() {
    return uId;
  }

  @Override
  public void setUserId(String userId) {
    this.uId = userId;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long domainId) {
    this.dId = domainId;
  }

  @Override
  public String getPrevConfig() {
    if (prevConf != null) {
      try {
        return new String(prevConf, UTF_8);

      } catch (UnsupportedEncodingException e) {
        // ignore
      }
    }
    return null;
  }

  @Override
  public void setPrevConfig(String prevConfigStr) {
    if (prevConfigStr != null) {

      try {
        this.prevConf = prevConfigStr.getBytes(UTF_8);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    } else {
      this.prevConf = null;
    }
  }

  /**
   * Get the actual value of a configuration attribute, given a key.
   */
  @Override
  public String getString(String attribute) {
    if (conf == null) {
      return null;
    }
    try {
      JsonConfig jsonC = new JsonConfig(getConfig());
      return jsonC.getStringValue(attribute);
    } catch (ConfigurationException e) {
      return null;
    }
  }
}
