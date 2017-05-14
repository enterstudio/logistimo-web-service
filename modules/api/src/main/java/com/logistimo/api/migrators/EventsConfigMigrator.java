package com.logistimo.api.migrators;

import com.logistimo.AppFactory;
import com.logistimo.config.entity.Config;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.dao.IUserDao;
import com.logistimo.users.dao.UserDao;
import com.logistimo.users.entity.IUserAccount;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Created by Charan.
 */
public class EventsConfigMigrator {
  private static final XLog xlogger = XLog.getLog(EventsConfigMigrator.class);

  /**
   * Migrate the events config
   */
  public void migrateEventsConfig() throws ServiceException {
    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    Results domains = ds.getAllDomains(null);
    List domainList = domains.getResults();
    if (domainList != null && domainList.size() > 0) {
      for(Object domainObj: domainList){
        migrateDomain((IDomain)domainObj);
      }
    }
    xlogger.info("Migration of events config completed");
  }


  private void migrateDomain(IDomain domain) throws ServiceException {
    DomainConfig domainConfig = DomainConfig.getInstance(domain.getId());
    EventsConfig ec = domainConfig.getEventsConfig();
    Map<String, EventSpec> fullSpec = ec.getFullEventSpec();
    Map<String, EventSpec> newSpecs = new LinkedHashMap<>();
    boolean changed = false;
    for (String key : fullSpec.keySet()) {
      String tokens[] = key.split(":");
      String newKey = null;
      changed = false;
      switch (tokens[0]) {
        case "org.lggi.samaanguru.entity.UserAccount":
          newKey = "com.logistimo.users.entity.UserAccount";
          break;
        case "org.lggi.samaanguru.entity.Invntry":
          newKey = "com.logistimo.inventory.entity.Invntry";
          break;
        case "org.lggi.samaanguru.entity.Transaction":
          newKey = "com.logistimo.inventory.entity.Transaction";
          break;
        case "org.lggi.samaanguru.entity.InvntryBatch":
          newKey = "com.logistimo.inventory.entity.InvntryBatch";
          break;
        case "org.lggi.samaanguru.entity.Order":
          newKey = "com.logistimo.orders.entity.Order";
          break;
        case "org.lggi.samaanguru.entity.Kiosk":
          newKey = "com.logistimo.entities.entity.Kiosk";
          break;
        case "org.lggi.samaanguru.entity.Material":
          newKey = "com.logistimo.materials.entity.Material";
          break;
        case "org.lggi.samaanguru.entity.AssetStatus":
          newKey = "com.logistimo.assets.entity.AssetStatus";
          break;
        case "com.logistimo.shipment.entity.Shipment":
          newKey = "com.logistimo.shipments.entity.Shipment";
      }
      if (newKey != null) {
        newSpecs
            .put(EventsConfig.createKey(Integer.parseInt(tokens[1]), newKey), fullSpec.get(key));
        changed = true;
      } else {
        newSpecs.put(key, fullSpec.get(key));
      }
    }
    if (changed) {
      ec.setEventSpecs(newSpecs);
      domainConfig.setEventsConfig(ec);
      ConfigurationMgmtService
          cms =
          Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig config;
      try {
        config = cms.getConfiguration(IConfig.CONFIG_PREFIX + domain.getId());
        config.setConfig(domainConfig.toJSONSring());
        cms.updateConfiguration(config);
        MemcacheService cache = AppFactory.get().getMemcacheService();
        if (cache != null) {
          cache.put(DomainConfig.getCacheKey(domain.getId()), domainConfig);
        }
        xlogger.info("Events config changed for domain {0}:{1}",domain.getId(), domain.getName());
      } catch (Exception e) {
        xlogger.severe("{2}: Failed to update config for {0}:{1}",domain.getId(),domain.getName(),e);
      }
    }

  }

}
