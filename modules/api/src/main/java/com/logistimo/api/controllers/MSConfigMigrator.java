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

package com.logistimo.api.controllers;

import com.logistimo.AppFactory;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smriti on 2/6/17.
 */
public class MSConfigMigrator {

  private static final String
      CONFIG_READ_QUERY =
      "SELECT `KEY`, cast(CONF as CHAR) as CONF FROM CONFIG WHERE `KEY`";
  private static final String CONFIG_UPDATE_QUERY = "UPDATE CONFIG SET CONF = ? WHERE `KEY` = ?";
  private static final XLog XLOGGER = XLog.getLog(MSConfigMigrator.class);
  private static final String CONF_KEY = "config.";
  private static final String[]
      TRANSACTION_TYPE =
      new String[]{ITransaction.TYPE_ISSUE, ITransaction.TYPE_PHYSICALCOUNT,
          ITransaction.TYPE_RECEIPT, ITransaction.TYPE_WASTAGE, ITransaction.TYPE_TRANSFER};
  private static PreparedStatement ps;
  private static Connection connection;

  /**
   * Updates configuration for all domains
   *
   * @param force Force the migration to run for all domains even migration already ran for them
   * @return true/false based on configuration got updated for all domains or not
   */

  public static boolean update(String force) {
    return update((List<String>) null, force);
  }

  /**
   * Updates configuration for a single key
   *
   * @param key   Domain id
   * @param force Force the migration to run for all domains even migration already ran for them
   * @return true/false based on configuration for single key got updated or not
   */
  public static boolean update(String key, String force) {
    return update(Collections.singletonList(key), force);
  }

  /**
   * Updates configuration for a list of keys
   *
   * @param keys  List of domain ids
   * @param force Force the migration to run for all domains even migration already ran for them
   * @return true/false based on configuration got updated for all keys or not
   */
  public static boolean update(List<String> keys, String force) {
    try {
      Map<String, String> config;
      List<String> configKeys = null;
      if (keys != null && keys.size() > 0) {
        configKeys = new ArrayList<>(keys.size());
        for (String key : keys) {
          if (key.startsWith(CONF_KEY)) {
            configKeys.add(key);
          } else {
            configKeys.add(CONF_KEY + key);
          }
        }
      }
      config = readConfiguration(configKeys);
      if (config == null || config.size() == 0) {
        XLOGGER.warn("No configuration found");
        return false;
      }
      boolean configUpdated = false;
      Map<String, String> updateDomains = new HashMap<>();
      for (String domain : config.keySet()) {
        String dId = domain.replace("config.", "");
        XLOGGER.info("Getting configuration for domain: {0}", dId);
        JSONObject configuration = new JSONObject(config.get(domain));
        if(!configuration.has("invntry")) {
          XLOGGER.info("Inventory configuration not found for domain:{0} ", dId);
          continue;
        }
        JSONObject invntry =  (JSONObject) configuration.get("invntry");
        JSONObject status = null;
        if(invntry.has("mtst")) {
          status = (JSONObject) invntry.get("mtst");
          for (String type : TRANSACTION_TYPE) {
            JSONObject transStatus = (JSONObject) status.get(type);
            if (transStatus.has("sm") && force == null) {
              XLOGGER.info(
                  "Configuration for status mandatory already updated for domain {0}, type: {1} ",
                  dId, type);
              break;
            } else {
              transStatus.put("sm", false);
              String matstatus = transStatus.has("df") ? (String) transStatus.get("df") : CharacterConstants.EMPTY;
              String tempmatstatus = transStatus.has("etsm") ? (String) transStatus.get("etsm") : CharacterConstants.EMPTY;
              if ((StringUtils.isNotBlank(matstatus) && !matstatus
                  .startsWith(CharacterConstants.COMMA)) || (
                  StringUtils.isNotBlank(tempmatstatus) && !tempmatstatus
                      .startsWith(CharacterConstants.COMMA))) {
                transStatus.put("sm", true);
              }
              transStatus.put("df", StringUtil.trimCommas(matstatus));
              transStatus.put("etsm", StringUtil.trimCommas(tempmatstatus));
              status.put(type, transStatus);
              configUpdated = true;
              XLOGGER.info("Updating status mandatory configuration for type {0} as {1}", type,
                  transStatus.get("sm"));
            }
          }
        }
        if (configUpdated) {
          invntry.put("mtst", status);
          configuration.put("invntry", invntry);
          updateDomains.put(domain, configuration.toString());
        }
        XLOGGER.info("Exiting domain: " + dId);
      }
      MemcacheService cache = AppFactory.get().getMemcacheService();
      // update configuration
      int[] count = new int[0];
      if (updateDomains.size() > 0) {
        ps = connection.prepareStatement(CONFIG_UPDATE_QUERY);
        for (String domain : updateDomains.keySet()) {
          ps.setString(1, updateDomains.get(domain));
          ps.setString(2, domain);
          ps.addBatch();
          if (cache != null) {
            cache.delete(domain);
          }
        }
        count = ps.executeBatch();
      }
      XLOGGER.info("Configuration updated for {0} domains out of {1} domains", count.length,
          updateDomains.size());
      return true;
    } catch (Exception e) {
      XLOGGER.warn("Error while updating configuration: " + e);
    } finally {
      if (ps != null) {
        try {
          ps.close();
        } catch (SQLException e) {
          XLOGGER.warn("Error while closing the connection: " + e);
        }
      }
    }
    return false;
  }

  /**
   * Reads configuration for a given list of keys
   *
   * @param keys List of domain ids
   * @return Map of key,value for entire configuration
   */
  private static Map<String, String> readConfiguration(List<String> keys)
      throws ClassNotFoundException, SQLException {
    Map<String, String> config = new HashMap<>();
    Class.forName("org.mariadb.jdbc.Driver");
    connection =
        DriverManager.getConnection(ConfigUtil.get("db.url"), ConfigUtil.get("db.user"),
            ConfigUtil.get("db.password"));
    StringBuilder sb = new StringBuilder();
    String query;
    if (keys != null && keys.size() > 0) {
      for (String key : keys) {
        if (key.startsWith("config.")) {
          sb = sb.append(CharacterConstants.SINGLE_QUOTES).append(key)
              .append(CharacterConstants.SINGLE_QUOTES).append(
                  CharacterConstants.COMMA);
        }
      }
      sb.setLength(sb.length() - 1);
      query = CONFIG_READ_QUERY + " IN(" + sb.toString() + CharacterConstants.C_BRACKET;
    } else {
      query = CONFIG_READ_QUERY + " LIKE 'config.%' AND `KEY` NOT LIKE 'config.kiosk.%'";
    }
    ps = connection.prepareStatement(query);
    ResultSet rs = ps.executeQuery();
    if (rs == null) {
      XLOGGER.warn("No config object found for keys: {0}", sb);
      return null;
    }
    while (rs.next()) {
      config.put(rs.getString("key"), rs.getString("conf"));
    }
    return config;
  }

}
