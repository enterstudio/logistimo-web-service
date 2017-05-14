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
