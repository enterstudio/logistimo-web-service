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
import com.logistimo.services.mapred.IMapredService;

import java.sql.SQLException;

/**
 * Created by charan on 29/09/14.
 */
public class EntityInputFormat extends InputFormat<Key, Entity> {


  private DataStoreService dataStoreService;
  private String entityType;
  private EntityCursor cursor;

  public EntityInputFormat() {

  }

  @Override
  protected void init() throws Exception {
    dataStoreService = DataStoreServiceFactory.getDataStoreService();
    entityType = getConfiguration().get(IMapredService.PARAM_ENTITYKIND);
    try {
      cursor = dataStoreService.readAll(entityType.toUpperCase());
    } catch (SQLException e) {
      throw new Exception(e);
    }
  }

  @Override
  public KeyVal<Key, Entity> readNext() {

    if (cursor != null) {
      Entity entity = cursor.readNext();
      if (entity != null) {
        return new KeyVal(entity.getKey(), entity);
      }
    }
    return null;
  }

  @Override
  public void close() {
    if (cursor != null) {
      cursor.close();
    }
  }
}
