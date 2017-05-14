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

package com.logistimo.services.datastore;

import com.logistimo.AppFactory;
import com.logistimo.accounting.entity.Account;
import com.logistimo.config.entity.Config;
import com.logistimo.domains.entity.Domain;
import com.logistimo.domains.entity.DomainLink;
import com.logistimo.domains.entity.DomainPermission;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.entities.entity.KioskLink;
import com.logistimo.entities.entity.KioskToPoolGroup;
import com.logistimo.entities.entity.PoolGroup;
import com.logistimo.entities.entity.UserToKiosk;
import com.logistimo.events.entity.Event;
import com.logistimo.inventory.entity.Invntry;
import com.logistimo.inventory.entity.InvntryBatch;
import com.logistimo.inventory.entity.InvntryEvntLog;
import com.logistimo.inventory.entity.InvntryLog;
import com.logistimo.inventory.entity.Transaction;
import com.logistimo.inventory.optimization.entity.OptimizerLog;
import com.logistimo.materials.entity.Material;
import com.logistimo.mnltransactions.entity.MnlTransaction;
import com.logistimo.orders.entity.DemandItem;
import com.logistimo.orders.entity.DemandItemBatch;
import com.logistimo.orders.entity.Order;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.mapper.Entity;
import com.logistimo.services.mapper.EntityCursor;
import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.entity.ALog;
import com.logistimo.entity.BBoard;
import com.logistimo.entity.Downloaded;
import com.logistimo.entity.MessageLog;
import com.logistimo.entity.MultipartMsg;
import com.logistimo.entity.Task;
import com.logistimo.entity.TaskLog;
import com.logistimo.entity.Uploaded;
import com.logistimo.entity.UploadedMsgLog;
import com.logistimo.services.impl.PMF;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.logger.XLog;
import com.logistimo.tags.entity.Tag;
import com.logistimo.users.entity.UserAccount;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

/**
 * @author Charan, Mohan Raja
 */
public class MySQLDataStoreService extends DataStoreService {

  private static final XLog logger = XLog.getLog(MySQLDataStoreService.class);
  MemcacheService memcacheService = AppFactory.get().getMemcacheService();
  private Connection conn;
  private Map<String, Class> classNames;

  public void checkAndInitConnection() {
    Statement statement = null;
    try {
      initConnection();
      //validate connection still valid.
      statement = conn.createStatement();
      final String testQuery = "SELECT 1";
      statement.executeQuery(testQuery);
    } catch (SQLException e) {
      initConnection();
    } finally {
      try {
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private void initConnection() {
    try {
      Class.forName("org.mariadb.jdbc.Driver");
      if (conn == null || conn.isClosed()) {
        conn =
            DriverManager.getConnection(ConfigUtil.get("db.url"), ConfigUtil.get("db.user"),
                ConfigUtil.get("db.password"));
        conn.setAutoCommit(false);
      }
    } catch (SQLException e) {
      logger.severe("Failed to Initialize MySQL connection", e);
    } catch (ClassNotFoundException e) {
      logger.severe("Unable to initialize jdbc", e);
    }
  }

  private Connection getConnection(String entityType) {
    if (isReportTable(entityType)) {
      throw new UnsupportedOperationException("Reports database no longer exists");
    } else {
      return conn;
    }
  }

  private String getSchema(String entityType) {
    if (isReportTable(entityType)) {
      return ConfigUtil.get("reportsdb.schema", "logireports");
    } else {
      return ConfigUtil.get("db.schema", "logistimo");
    }
  }

  private boolean isReportTable(String entityType) {
    return ConfigUtil.get("reportsdb.tables",
        "DAYACTIVECOUNTSSTATSSTORE,DAYSLICE,MONTHACTIVECOUNTSSTATSSTORE,MONTHSLICE").toLowerCase()
        .contains(entityType.toLowerCase());
  }

  @Override
  public void persist(Collection<Entity> entities) throws SQLException {
    checkAndInitConnection();
    if (entities != null && !entities.isEmpty()) {
      try {
        Map<String, PreparedStatement> statementMap = new HashMap<>();
        for (Entity entity : entities) {
          PreparedStatement statement = statementMap.get(entity.getName());
          EntityMetadata entityMetadata = buildStatement(entity);
          if (statement == null) {
            statement =
                getConnection(entity.getName())
                    .prepareStatement(entityMetadata.getPreparedStatement());
            statementMap.put(entity.getName(), statement);
          }
          prepareStatement(statement, entityMetadata, entity);
        }
        for (PreparedStatement statement : statementMap.values()) {
          statement.executeBatch();
          statement.close();
        }
        conn.commit();
      } catch (SQLException e) {
        logger.severe("Failed to Store Entities", e);
        throw e;
      }
    }


  }

  @Override
  public EntityCursor readAll(String entityType) throws SQLException {
    checkAndInitConnection();
    EntityCursor entityCursor = null;
    if (entityType != null) {
      List<String> columns = getColumns(entityType);
      Statement statement = getConnection(entityType).createStatement();
      statement.setFetchSize(20);
      ResultSet
          resultSet =
          statement.executeQuery("SELECT * FROM `" + entityType + CharacterConstants.ACUTE);
      entityCursor =
          new EntityCursor(statement, resultSet, columns, getPrimaryKey(entityType), entityType);
    }
    return entityCursor;
  }

  @Override
  public void delete(Map<String, List<String>> entities) {
    if (classNames == null) {
      initClassNames();
    }
    for (String entity : entities.keySet()) {
      Class clz = classNames.get(entity);
      if (clz == null) {
        logger
            .severe("Error in deleting entities for class {0}. Unknown class identified.", entity);
        continue;
      }
      PersistenceManager pm = null;
      try {
        pm = isReportTable(entity) ?
            PMF.getReportsPM().getPersistenceManager() : PMF.get().getPersistenceManager();
        List<String> keys = entities.get(entity);
        List<Object> objs = new ArrayList<>(keys.size());
        for (String key : keys) {
          try {
            Object o = pm.getObjectById(clz, key);
            if (o != null) {
              objs.add(o);
            }
          } catch (JDOObjectNotFoundException e) {
            logger.severe("Error in deleting entity {0} from {1}", key, entity, e);
          }
        }
        pm.deletePersistentAll(objs);
      } catch (Exception e) {
        logger.severe("Error in deleting entity {0}", entity, e);
      } finally {
        if (pm != null) {
          pm.close();
        }
      }
    }
  }

  private void initClassNames() {
    classNames = new HashMap<>(43);
    classNames.put("ACCOUNT", Account.class);
    classNames.put("ALOG", ALog.class);
    classNames.put("BBOARD", BBoard.class);
    classNames.put("CONFIG", Config.class);
    classNames.put("DEMANDITEM", DemandItem.class);
    classNames.put("DEMANDITEMBATCH", DemandItemBatch.class);
    classNames.put("DOMAIN", Domain.class);
    classNames.put("DOMAINLINK", DomainLink.class);
    classNames.put("DOMAINPERMISSION", DomainPermission.class);
    classNames.put("DOWNLOADED", Downloaded.class);
    classNames.put("EVENT", Event.class);
    classNames.put("INVNTRY", Invntry.class);
    classNames.put("INVNTRYBATCH", InvntryBatch.class);
    classNames.put("INVNTRYEVNTLOG", InvntryEvntLog.class);
    classNames.put("INVNTRYLOG", InvntryLog.class);
    classNames.put("KIOSK", Kiosk.class);
    classNames.put("KIOSKLINK", KioskLink.class);
    classNames.put("KIOSKTOPOOLGROUP", KioskToPoolGroup.class);
    classNames.put("MATERIAL", Material.class);
    classNames.put("MESSAGELOG", MessageLog.class);
    classNames.put("MNLTRANSACTION", MnlTransaction.class);
    classNames.put("MULTIPARTMSG", MultipartMsg.class);
    classNames.put("OPTIMIZERLOG", OptimizerLog.class);
    classNames.put("ORDER", Order.class);
    classNames.put("POOLGROUP", PoolGroup.class);
    classNames.put("TAG", Tag.class);
    classNames.put("TASK", Task.class);
    classNames.put("TASKLOG", TaskLog.class);
    classNames.put("TRANSACTION", Transaction.class);
    classNames.put("UPLOADED", Uploaded.class);
    classNames.put("UPLOADEDMSGLOG", UploadedMsgLog.class);
    classNames.put("USERACCOUNT", UserAccount.class);
    classNames.put("USERTOKIOSK", UserToKiosk.class);
  }

  private String getPrimaryKey(String entityType) throws SQLException {
    DatabaseMetaData meta = getConnection(entityType).getMetaData();
    ResultSet rs = meta.getPrimaryKeys(null, getSchema(entityType), entityType);
    if (rs.next()) {
      return rs.getString("COLUMN_NAME");
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private List<String> getColumns(String entityType) throws SQLException {
    final String key = "entitymeta_" + entityType;
    List<String> columns = (List<String>) memcacheService.get(key);
    if (columns == null) {
      synchronized ("entitymeta_" + entityType) {
        columns = (List<String>) memcacheService.get(key);
        if (columns == null) {
          columns = new ArrayList<>();
          DatabaseMetaData meta = getConnection(entityType).getMetaData();
          ResultSet columnsRS = meta.getColumns(null, getSchema(entityType), entityType, null);
          final String column_name = "COLUMN_NAME";
          while (columnsRS.next()) {
            columns.add(columnsRS.getString(column_name));
          }
          memcacheService.put(key, columns, 1800);
        }
      }
    }
    return columns;
  }

  private void prepareStatement(PreparedStatement statement, EntityMetadata entityMetadata,
                                Entity entity) throws SQLException {
    int index = 1;
    for (String column : entityMetadata.getColumns()) {
      if (entity.getProperty(column) != null) {
        Object value = entity.getProperty(column);
        statement.setObject(index++, value);
      }
    }
    statement.addBatch();
  }

  private EntityMetadata buildStatement(Entity entity) throws SQLException {
    List<String> columns = getColumns(entity.getName());

    StringBuilder builder = new StringBuilder();
    builder.append("INSERT INTO ").append(CharacterConstants.ACUTE).append(entity.getName())
        .append(CharacterConstants.ACUTE).append(CharacterConstants.O_BRACKET);
    int i = 1;
    for (String columnName : columns) {
      if (entity.getProperty(columnName) != null) {
        i++;
        builder.append(CharacterConstants.ACUTE).append(columnName).append(CharacterConstants.ACUTE)
            .append(CharacterConstants.COMMA);
      }
    }
    builder.setLength(builder.length() - 1);
    builder.append(") VALUES (");
    for (; i > 1; i--) {
      builder.append(CharacterConstants.QUESTION).append(CharacterConstants.COMMA);
    }
    builder.setLength(builder.length() - 1);
    builder.append(CharacterConstants.C_BRACKET);

    return new EntityMetadata(columns, builder.toString());
  }
}
