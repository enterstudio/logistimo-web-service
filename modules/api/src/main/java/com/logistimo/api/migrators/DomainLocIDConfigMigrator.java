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

package com.logistimo.api.migrators;

import com.logistimo.AppFactory;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.constants.LocationConstants;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.locations.LocationServiceUtil;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainLocIDConfigMigrator {

  private static final XLog xlogger = XLog.getLog(DomainLocIDConfigMigrator.class);

  /**
   * Migrate the events config
   */
  public void updateDomainLocConfig() throws ServiceException {
    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    Results domains = ds.getAllDomains(null);
    List domainList = domains.getResults();
    if (domainList != null && domainList.size() > 0) {
      int total = domainList.size();
      int limit = 25;
      int plimit = limit;
      int noOfPages = total / limit;
      if (total % limit != 0) {
        noOfPages = noOfPages + 1;
      }
      int p = 0;
      int k = 0;
      while (p < noOfPages - 1) {
        process(domainList.subList(k, limit));
        p++;
        k += plimit;
        limit += plimit;
        try {
          Thread.sleep(5000l);
        } catch (InterruptedException e) {
          xlogger.warn("Issue with domain location config update {}", e);
        }
      }
      process(domainList.subList(k, total));
    }
    xlogger.info("Migration of events config completed");
  }

  private void process(List domainList) {
    for (Object domainObj : domainList) {
      try {
        updateLocId((IDomain) domainObj);
      } catch (Exception e) {
        xlogger.warn("Issue with domain location config update {}", ((IDomain) domainObj).getId());
      }
    }
  }


  private void updateLocId(IDomain domain) throws ServiceException {
    DomainConfig domainConfig = DomainConfig.getInstance(domain.getId());
    Map<String, Object> map = prepareParam(domainConfig);
    //call location service to get country id, state id and district id
    Map res = LocationServiceUtil.getInstance().getLocationIds(map);
    if (res.get(LocationConstants.STATUS_TYPE_LITERAL).equals(LocationConstants.SUCCESS_LITERAL)) {
      domainConfig.setCountryId((String) res.get(LocationConstants.COUNTRYID_LITERAL));
      domainConfig.setStateId((String) res.get(LocationConstants.STATEID_LITERAL));
      domainConfig.setDistrictId((String) res.get(LocationConstants.DISTID_LITERAL));
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
        xlogger.info("Location id updated for domain {0}:{1}", domain.getId(), domain.getName());
      } catch (Exception e) {
        xlogger.severe("{2}: Failed to update loc config for {0}:{1}", domain.getId(),
            domain.getName(),
            e);
      }
    }
  }

  private Map<String, Object> prepareParam(DomainConfig dc) {
    Map<String, Object> lcMap = new HashMap<>();
    if (StringUtils.isNotEmpty(dc.getCountry())) {
      lcMap.put(LocationConstants.COUNTRY_LITERAL, dc.getCountry());
    }
    if (StringUtils.isNotEmpty(dc.getState())) {
      lcMap.put(LocationConstants.STATE_LITERAL, dc.getState());
    }
    if (StringUtils.isNotEmpty(dc.getDistrict())) {
      lcMap.put(LocationConstants.DIST_LITERAL, dc.getDistrict());
    }
    //add app name and user name
    lcMap.put(LocationConstants.APP_LITERAL, LocationConstants.APP_NAME);
    lcMap.put(LocationConstants.USER_LITERAL, "migration-user");
    return lcMap;
  }
}
