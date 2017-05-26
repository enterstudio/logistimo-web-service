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

package com.logistimo.api.mappers;

import com.logistimo.AppFactory;
import com.logistimo.api.builders.EntityBuilder;
import com.logistimo.api.request.NetworkViewResponseObj;
import com.logistimo.entities.models.EntityLinkModel;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.mapper.Entity;
import com.logistimo.services.mapper.GenericMapper;
import com.logistimo.services.mapper.Key;

import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.services.utils.StopWatch;
import com.logistimo.logger.XLog;

import com.logistimo.mappers.NullWritable;

import java.util.List;

public class EntityHierarchy extends GenericMapper<Key, Entity, NullWritable, NullWritable> {

  private static final XLog xLogger = XLog.getLog(EntityHierarchy.class);

  EntityBuilder builder = new EntityBuilder();

  @Override
  public void map(Key key, Entity entity, Context context) {
    Long domainId = Long.parseLong(key.getId());
    StopWatch stopWatch = new StopWatch();
    try {
      MemcacheService cache = AppFactory.get().getMemcacheService();
      if (cache == null) {
        xLogger.warn("Error in caching entity hierarchy data for domain {0}", domainId);
        return;
      }
      String name = String.valueOf(entity.getProperty("name"));
      xLogger.info("Creating Hierarchy for domain {0} {1}", domainId, name);
      NetworkViewResponseObj obj;
      String cacheKey = Constants.NW_HIERARCHY_CACHE_PREFIX + domainId;
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      List<EntityLinkModel> entityLinks = as.getKioskLinksInDomain(domainId, null, null);
      obj = builder.buildNetworkViewRequestObject(entityLinks, domainId);
      cache.put(cacheKey, obj, 24 * 60 * 60); // 1 day expiry
      xLogger.info("Hierarchy created for domain {0} {1} in {2}", domainId, name,
          (stopWatch.getElapsedTime()));
    } catch (Exception e) {
      //TODO
    }
  }
}
