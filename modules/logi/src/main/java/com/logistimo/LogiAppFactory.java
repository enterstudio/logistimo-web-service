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

package com.logistimo;

import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.AuthorizationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;
import com.logistimo.auth.services.impl.AuthorizationServiceImpl;
import com.logistimo.dao.DaoException;
import com.logistimo.dao.IDaoUtil;
import com.logistimo.dao.impl.DaoUtil;
import com.logistimo.logger.ILogger;
import com.logistimo.logger.LogiLogger;
import com.logistimo.reports.dao.IReportsDao;
import com.logistimo.reports.dao.impl.ReportsDaoImpl;
import com.logistimo.services.IBackendService;
import com.logistimo.services.LogiBackendService;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.blobstore.HDFSBlobStoreService;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.cache.RedisMemcacheService;
import com.logistimo.services.http.LogiURLFetchService;
import com.logistimo.services.http.URLFetchService;
import com.logistimo.services.mapper.MapredService;
import com.logistimo.services.mapred.IMapredService;
import com.logistimo.services.storage.HDFSStorageUtil;
import com.logistimo.services.storage.StorageUtil;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.taskqueue.TaskService;

import com.logistimo.models.ICounter;
import com.logistimo.services.utils.LogiCounter;

/**
 * Created by charan on 04/03/15.
 */
public class LogiAppFactory extends AppFactory {

  private IReportsDao reportsDao;
  private IDaoUtil daoUtil;

  @Override
  public ILogger getLogger(String name) {
    return new LogiLogger(name);
  }

  @Override
  public ITaskService getTaskService() {
    if (taskService == null) {
      taskService = new TaskService();
    }
    return taskService;
  }

  @Override
  public StorageUtil getStorageUtil() {
    if (storageUtil == null) {
      storageUtil = new HDFSStorageUtil();
    }
    return storageUtil;
  }

  @Override
  public IMapredService getMapredService() {
    if (mapredService == null) {
      mapredService = new MapredService();
    }
    return mapredService;
  }

  @Override
  public MemcacheService getMemcacheService() {
    if (memCacheService == null) {
      memCacheService = new RedisMemcacheService();
    }
    return memCacheService;
  }

  @Override
  public BlobstoreService getBlobstoreService() {
    if (blobStoreService == null) {
      blobStoreService = new HDFSBlobStoreService();
    }
    return blobStoreService;
  }

  @Override
  public URLFetchService getURLFetchService() {
    if (urlFetchService == null) {
      urlFetchService = new LogiURLFetchService();
    }
    return urlFetchService;
  }

  @Override
  public IBackendService getBackendService() {
    if (backendService == null) {
      backendService = new LogiBackendService();
    }
    return backendService;
  }

  @Override
  public ICounter getCounter() {
    return new LogiCounter();
  }

  @Override
  public AuthorizationService getAuthorizationService() {
    return new AuthorizationServiceImpl();
  }

  @Override
  public AuthenticationService getAuthenticationService() {
    return new AuthenticationServiceImpl();
  }

  @Override
  public IDaoUtil getDaoUtil() throws DaoException {
    if(daoUtil == null){
      daoUtil = new DaoUtil();
    }
    return daoUtil;
  }

  @Override
  public IReportsDao getReportsDao() throws DaoException {
    if(reportsDao == null){
      reportsDao = new ReportsDaoImpl();
    }
    return reportsDao;
  }


}
