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

import com.logistimo.logger.XLog;

/**
 * Drops an entire datastore table.
 *
 * @author Arun
 */
public class TableDropper extends GenericMapper<Key, Entity, NullWritable, NullWritable> {

  private final static XLog xLogger = XLog.getLog(TableDropper.class);

  public TableDropper() {
  }

  @Override
  public void map(Key key, Entity entity, Context context) {
    xLogger.fine("Entered TableDropper.map");

    Long domainIdParam = getDomainIdParam(context);

    if (domainIdParam == null || domainIdParam.equals(getDomainId(entity))) {
      // Delete this entity
      DatastoreMutationPool mutationPool = this.getContext(context).getMutationPool();
      mutationPool.delete(entity);
    }
  }

  private Long getDomainId(Entity entity) {
    Long domainId = (Long) entity.getProperty("sdId");
    if (domainId == null) {
      // Get the domain Id
      try {
        domainId = (Long) entity.getProperty("dId");
      } catch (Exception e) {
        xLogger.warn("Entity is neither multi domain nor regular: ", entity, e);
      }
    }
    return domainId;
  }

  // Get domain ID parameter, if specified by user
  private Long getDomainIdParam(Context context) {
    String domainIdStr = context.getConfiguration().get("mapreduce.mapper.counter.domainid");
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      return Long.valueOf(domainIdStr);
    }
    return null;
  }
}
