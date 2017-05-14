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

import com.logistimo.services.datastore.DataStoreService;
import com.logistimo.services.datastore.DataStoreServiceFactory;

import com.logistimo.services.impl.PMF;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Charan, Mohan Raja
 */
public class DatastoreMutationPool {

  private final ArrayList<Entity> entities;
  private final Map<String, List<String>> delEntities;
  private int flushSize;

  public DatastoreMutationPool(int flushSize) {
    this.flushSize = flushSize;
    this.entities = new ArrayList<>(flushSize);
    this.delEntities = new HashMap<>();
  }

  public void put(Entity entity) throws SQLException {
    entities.add(entity);
    if (this.entities.size() == flushSize) {
      synchronized (entities) {
        persistEntities();
        entities.clear();
      }
    }
  }

  private void persistEntities() throws SQLException {
    DataStoreService dataStoreService = DataStoreServiceFactory.getDataStoreService();
    dataStoreService.persist(entities);
  }

  public void close() throws SQLException {
    if (!entities.isEmpty()) {
      persistEntities();
    }
    if (!delEntities.isEmpty()) {
      deleteEntities();
    }
    PMF.get().getDataStoreCache().evictAll();
    //PMF.getReportsPM().getDataStoreCache().evictAll();
  }

  public void delete(Entity entity) {
    List<String> de = delEntities.get(entity.getName());
    if (de == null) {
      de = new ArrayList<>();
      de.add(entity.getKey().getId());
      delEntities.put(entity.getName(), de);
    } else {
      de.add(entity.getKey().getId());
    }
    if (de.size() == flushSize) {
      synchronized (delEntities) {
        deleteEntities();
        delEntities.clear();
      }
    }
  }

  public void deleteEntities() {
    DataStoreService dataStoreService = DataStoreServiceFactory.getDataStoreService();
    dataStoreService.delete(delEntities);
  }
}
