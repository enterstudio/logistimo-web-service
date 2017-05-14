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

package com.logistimo.mappers.migrators;

import com.logistimo.services.mapper.DatastoreMutationPool;
import com.logistimo.services.mapper.Entity;
import com.logistimo.services.mapper.GenericMapper;
import com.logistimo.services.mapper.Key;

import org.apache.hadoop.io.NullWritable;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.List;

public class DomainIdListMigrator extends GenericMapper<Key, Entity, NullWritable, NullWritable> {

  private static final XLog xLogger = XLog.getLog(DomainIdListMigrator.class);

  @Override
  public void map(Key key, Entity entity, Context context) {
    try {
      // Get the current domain Id
      Long dId = (Long) entity.getProperty("dId");
      List<Long> dIds = new ArrayList<Long>();
      dIds.add(dId);
      entity.setProperty("dId", dIds);
      DatastoreMutationPool mutationPool = this.getContext(context).getMutationPool();
      mutationPool.put(entity);
    } catch (Exception e) {
      xLogger.warn("{0} when trying to update Entity object (Name:{1} Key:{2}) : {3}",
          e.getClass().getName(), entity.getName(), entity.getKey(), e.getMessage());
    }
  }
}
