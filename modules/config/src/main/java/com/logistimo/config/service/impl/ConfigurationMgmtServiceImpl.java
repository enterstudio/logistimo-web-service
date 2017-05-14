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
package com.logistimo.config.service.impl;

import com.logistimo.AppFactory;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.dao.JDOUtils;
import com.logistimo.services.cache.MemcacheService;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.LocationConfig;
import com.logistimo.config.entity.IConfig;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.logger.XLog;

import java.util.Date;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

/**
 * @author arun
 */
public class ConfigurationMgmtServiceImpl extends ServiceImpl implements ConfigurationMgmtService {

  private static final XLog xLogger = XLog.getLog(ConfigurationMgmtServiceImpl.class);

  /**
   * Add a new configuration to the data store
   *
   * @param config The configuration object along with its key
   * @throws ServiceException Thrown if the object or its key is invalid
   */
  public void addConfiguration(String key, IConfig config) throws ServiceException {
    addConfiguration(key, null, config);
  }

  /**
   * Add a domain-specific configuration, if domainId is not null
   */
  public void addConfiguration(String key, Long domainId, IConfig config) throws ServiceException {
    if (config == null || config.getConfig() == null) {
      throw new ServiceException("Invalid configuration object - the object or its value are null");
    }

    if (key == null || key.isEmpty()) {
      throw new ServiceException(
          "Invalid configuration key - key cannot be null or an empty string");
    }

    // Save the config object to data store
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Check if a config with this key already exists
      try {
        String realKey = getRealKey(key, domainId);
        JDOUtils.getObjectById(IConfig.class, realKey, pm);
        // If it comes here, then this object is found, so throw an exception, given duplication
        throw new ServiceException("An entry with exists for key: " + key);
      } catch (JDOObjectNotFoundException e) {
        // This key does not exist; so it is valid to add a config with this key
      }

      config.setKey(key);
      config.setConfig(getCleanString(config.getConfig()));
      config.setLastUpdated(new Date());
      pm.makePersistent(config);
      pm.detachCopy(config);
    } catch (Exception e) {
      xLogger.fine("Exception while adding configuation object: {0}", e.getMessage());
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }
  }

  /**
   * Get the configuration object, given a key
   *
   * @param key The key for the config. object
   * @return The config. object corresponding to the given key
   * @throws ObjectNotFoundException If the config. object for the given key was not found
   * @throws ServiceException        Any invalid parameter or data retrieval exceptions
   */
  public IConfig getConfiguration(String key) throws ObjectNotFoundException, ServiceException {
    return getConfiguration(key, null);
  }

  /**
   * Get domain-specific configurtion, if domain is specified
   */
  public IConfig getConfiguration(String key, Long domainId)
      throws ObjectNotFoundException, ServiceException {
    if (key == null || key.isEmpty()) {
      throw new ServiceException("Invalid key: " + key);
    }
    IConfig config = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      String realKey = getRealKey(key, domainId);
      config = JDOUtils.getObjectById(IConfig.class, realKey, pm);
      config = pm.detachCopy(config);
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("Config object not found for key: {0}", key);
      throw new ObjectNotFoundException(e);
    } finally {
      pm.close();
    }

    return config;
  }

  /**
   * Update a given configuration object.
   *
   * @param config The configuration object to be updated.
   * @throws ServiceException Thrown if an invalid object or key was passed.
   */
  public void updateConfiguration(IConfig config) throws ServiceException {
    if (config == null || config.getConfig() == null) {
      throw new ServiceException("Invalid configuration object - the object or its value are null");
    }

    if (config.getKey() == null || config.getKey().isEmpty()) {
      throw new ServiceException(
          "Invalid configuration key - key cannot be null or an empty string");
    }

    // Save the config object to data store
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IConfig c = JDOUtils.getObjectById(IConfig.class, config.getKey(), pm);
      c.setPrevConfig(c.getConfig()); // backup the current configuration before updating
      c.setConfig(
          getCleanString(config.getConfig())); // update the current configuration with the new one
      c.setUserId(config.getUserId());
      c.setDomainId(config.getDomainId());
      c.setLastUpdated(new Date());
      // whenever there is a change in the location configuration, re-initialize it
      if (c.getKey().equals(IConfig.LOCATIONS)) {
        LocationConfig.initialize();
      }
//			pm.makePersistent( c );
    } catch (Exception e) {
      xLogger
          .fine("Exception while updating configuration object with key {0}: {1}", config.getKey(),
              e.getMessage(), e);
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }
  }


  /**
   * Copy configuration from one domain to another
   */
  public void copyConfiguration(Long srcDomainId, Long destDomainId) throws ServiceException {
    xLogger.fine("Entered copyConfiguration");
    // Get destination configuration
    IConfig srcConfig = null;
    // Check if source configuration exists
    try {
      srcConfig = getConfiguration(createConfigKey(srcDomainId));
    } catch (ObjectNotFoundException e) {
      xLogger.warn("No configuration found to copy (to dest. domain {0}) in source domain {1}",
          destDomainId, srcDomainId);
      return; // nothing to do, really
    }
    // Create or update destination domain configuration with source domain configuration
    IConfig destConfig = null;
    try {
      destConfig = getConfiguration(createConfigKey(destDomainId)).copyFrom(srcConfig);
      destConfig.setDomainId(destDomainId);
      destConfig.setLastUpdated(new Date());
      updateConfiguration(destConfig);
    } catch (ObjectNotFoundException e) { // destination configuration not found; add one
      destConfig = JDOUtils.createInstance(IConfig.class).init(srcConfig);
      destConfig.setDomainId(destDomainId);
      destConfig.setLastUpdated(new Date());
      addConfiguration(createConfigKey(destDomainId), destConfig);
    }
    MemcacheService cache = AppFactory.get().getMemcacheService();
    if (cache != null) {
      try {
        destConfig = getConfiguration(createConfigKey(destDomainId));
        cache.put(DomainConfig.getCacheKey(destDomainId), new DomainConfig(destConfig.getConfig()));
      } catch (Exception e) {
        xLogger.severe("Failed to update config cache for domain  {0}", destDomainId, e);
      }
    }
    xLogger.fine("Exiting copyConfiguration");
  }

  public static String createConfigKey(Long domainId) {
    return IConfig.CONFIG_PREFIX + domainId;
  }


  public void destroy() throws ServiceException {
    xLogger.fine("Entering destroy");
    // TODO Auto-generated method stub
    xLogger.fine("Exiting destroy");
  }

  public Class<? extends Service> getInterface() {
    xLogger.fine("Entering getInterface");
    xLogger.fine("Exiting getInterface");
    return ConfigurationMgmtService.class;
  }

  public void init(Services services) throws ServiceException {
    xLogger.fine("Entering init");
    // TODO Auto-generated method stub
    xLogger.fine("Exiting init");
  }

  // Private method to remove new line characters
  private String getCleanString(String inputString) {
    xLogger.fine("Entering getCleanString");
    String str = null;
    if (inputString != null) {
      str = inputString.replaceAll("\r\n|\n", "");
    }
    xLogger.fine("Exiting getCleanString");
    return str;
  }

  // Get a domain-specific or generic configuration key, depending on whether domainId was specified
  private String getRealKey(String key, Long domainId) {
    String realKey = key;
    if (domainId != null) {
      realKey += "." + domainId.toString();
    }
    return realKey;
  }
}
