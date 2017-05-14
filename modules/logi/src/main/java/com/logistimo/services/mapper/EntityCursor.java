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

package com.logistimo.services.mapper;

import com.logistimo.logger.XLog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by charan on 30/09/14.
 */
public class EntityCursor {

  private static final XLog logger = XLog.getLog(EntityCursor.class);

  private final ResultSet resultSet;
  private final List<String> columns;
  private final String primaryKey;
  private final String entityType;
  private final Statement statement;

  public EntityCursor(Statement statement, ResultSet resultSet, List<String> columns,
                      String primaryKey, String entityType) {
    this.statement = statement;
    this.resultSet = resultSet;
    this.columns = columns;
    this.primaryKey = primaryKey;
    this.entityType = entityType;
  }

  public Entity readNext() {
    Entity entity = null;
    try {
      if (resultSet.next()) {
        Object pk = resultSet.getObject(primaryKey);
        if (pk instanceof Long) {
          entity = new Entity(entityType, (Long) pk);
        } else {
          entity = new Entity(entityType, new Key(String.valueOf(pk)));
        }
        for (String column : columns) {
          entity.setProperty(column, resultSet.getObject(column));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return entity;
  }

  public void close() {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (SQLException e) {
        logger.warn("Error while closing resultset", e);
      }
    }
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        logger.warn("Error while closing statement", e);
      }
    }
  }
}
