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
package com.logistimo.config.service;

import com.logistimo.config.entity.IConfig;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

/**
 * @author arun
 *
 *         Provides methods for storage and retrieval of various configuration elements in the system
 *         (such as locations, languages, currencies, entity defaults, optimization configurations, and so on).
 */
public interface ConfigurationMgmtService extends Service {

  /**
   * Add a new configuration to the data store
   *
   * @param config The configuration object along with its key
   * @throws ServiceException Thrown if the object or its key is invalid
   */
  void addConfiguration(String key, IConfig config) throws ServiceException;

  /**
   * Add domain-specific configuration
   */
  void addConfiguration(String key, Long domainId, IConfig config) throws ServiceException;

  /**
   * Get the configuration object, given a key
   *
   * @param key The key for the config. object (key names are defined in Config.java)
   * @return The config. object corresponding to the given key
   * @throws ObjectNotFoundException If the config. object for the given key was not found
   * @throws ServiceException        Any invalid parameter or data retrieval exceptions
   */
  IConfig getConfiguration(String key) throws ObjectNotFoundException, ServiceException;

  /**
   * Get a domain specific configuration
   */
  IConfig getConfiguration(String key, Long domainId)
      throws ObjectNotFoundException, ServiceException;

  /**
   * Update a given configuration object.
   *
   * @param config The configuration object to be updated.
   * @throws ServiceException Thrown if an invalid object or key was passed.
   */
  void updateConfiguration(IConfig config) throws ServiceException;

  /**
   * Copy configuration from one domain to another
   */
  void copyConfiguration(Long srcDomainId, Long destDomainId) throws ServiceException;
}
