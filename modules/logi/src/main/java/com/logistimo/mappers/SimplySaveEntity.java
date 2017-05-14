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
package com.logistimo.mappers;

import com.logistimo.services.mapper.DatastoreMutationPool;
import com.logistimo.services.mapper.Entity;
import com.logistimo.services.mapper.GenericMapper;
import com.logistimo.services.mapper.Key;

import java.sql.SQLException;


/**
 * Simply re-persist an entity (useful when a column type changes in any model)
 *
 * @author Arun
 */
public class SimplySaveEntity extends GenericMapper<Key, Entity, NullWritable, NullWritable> {

  @Override
  public void map(Key key, Entity entity, Context context) {
    String kind = entity.getKind();
    boolean changed = true;
    if ("Event".equals(kind)) { // Migrate message from String to Text - April 2013
      Object msgObj = entity.getProperty("ms");
      String msg = null;
      if (msgObj != null) {
        msg = (String) msgObj;
      }
      if (msg != null) {
        entity.setProperty("msg", msg);
        entity.setProperty("ms", null);
      } else {
        changed = false;
      }
    } else if ("BBoard".equals(kind)) { // Migrate message from String to Text - April 2013
      Object msgObj = entity.getProperty("msg");
      String msg = null;
      if (msgObj != null) {
        msg = (String) msgObj;
      }
      if (msg != null) {
        entity.setProperty("ms", msg);
        entity.setProperty("msg", null);
      } else {
        changed = false;
      }
    }
    // Persist
    if (changed) {
      DatastoreMutationPool mutationPool = this.getContext(context).getMutationPool();
      try {
        mutationPool.put(entity);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
