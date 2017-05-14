package com.logistimo.api.controllers;

import com.logistimo.services.utils.ConfigUtil;

import org.json.JSONObject;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.logger.XLog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public class IRPostConfigMigrator {

  public static final String
      CONFIG_QUERY =
      "SELECT `KEY`, cast(CONF as CHAR) as conf FROM CONFIG WHERE `KEY`";
  public static final String CONFIG_UPDATE_QUERY = "UPDATE CONFIG SET CONF=? WHERE `KEY`=?";
  public static final XLog xLog = XLog.getLog(IRPostConfigMigrator.class);

  public static Connection connection;
  public static PreparedStatement ps;

  /**
   * Update configuration for all keys
   */
  public static boolean update() {
    return update((List<String>) null);
  }

  /**
   * Update configuration for a key.
   *
   * @param key - Config key to migrate
   */
  public static boolean update(String key) {
    return update(Collections.singletonList(key));
  }

  /**
   * Update configurations for a list of keys
   *
   * @param keys - list of config key to migrate
   */
  public static boolean update(List<String> keys) {
    try {
      Map<String, String> conf = readConfig(keys);
      if (conf == null) {
        return false;
      }
      //process
      for (String domainId : conf.keySet()) {
        xLog.info("Parsing config for domain {0}", domainId);
        JSONObject config = new JSONObject(conf.get(domainId));
        if (config.has("ogr")) {
          boolean isPostIssue = config.getBoolean("ogi");
          boolean isPostReceipt = config.getBoolean("ogr");
          config.remove("ogr");
          if (!isPostIssue && isPostReceipt) {
            xLog.info("Config updated for domain {0}", domainId);
            config.put("ogi", true);
          }
          conf.put(domainId, String.valueOf(config));
        }
      }
      //update
      ps = connection.prepareStatement(CONFIG_UPDATE_QUERY);
      for (String confKeys : conf.keySet()) {
        ps.setString(1, conf.get(confKeys));
        ps.setString(2, confKeys);
        ps.addBatch();
      }
      int[] count = ps.executeBatch();
      xLog.info("{0} domains updated out of {1}", count.length, conf.size());
    } catch (Exception e) {
      xLog.warn("Error in updating configuration: " + e);
      return false;
    } finally {
      if (ps != null) {
        try {
          ps.close();
        } catch (SQLException ignored) {
          xLog.warn("Exception while closing prepared statement", ignored);
        }
      }
    }
    return true;
  }

  public static Map<String, String> readConfig(List<String> keys)
      throws ClassNotFoundException, SQLException {
    Map<String, String> conf = new HashMap<>();
    Class.forName("org.mariadb.jdbc.Driver");
    connection =
        DriverManager.getConnection(ConfigUtil.get("db.url"), ConfigUtil.get("db.user"),
            ConfigUtil.get("db.password"));
    String sql;
    if (keys == null) {
      sql =
          CONFIG_QUERY
              + " LIKE 'config.%' AND `KEY` NOT LIKE 'config.kiosk.%'"; // for updating all configs
    } else {
      boolean isFirst = true;
      StringBuilder str = new StringBuilder();
      for (String key : keys) {
        if (key.startsWith("config.") && !key.startsWith("config.kiosk.")) {
          if (!isFirst) {
            str.append(CharacterConstants.COMMA);
          }
          isFirst = false;
          str.append(CharacterConstants.SINGLE_QUOTES).append(key)
              .append(CharacterConstants.SINGLE_QUOTES);
        } else {
          xLog.info("Migration config: Invalid key found ", key);
        }
      }
      if (str.length() == 0) {
        return null;
      }
      sql = CONFIG_QUERY + " IN (" + str.toString() + ")";  // for specific domain ids.
    }
    ps = connection.prepareStatement(sql);
    ResultSet resultset = ps.executeQuery();
    while (resultset.next()) {
      conf.put(resultset.getString("key"), resultset.getString("conf"));
    }
    return conf;
  }
}
