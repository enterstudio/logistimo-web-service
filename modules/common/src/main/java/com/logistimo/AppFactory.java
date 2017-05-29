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
import com.logistimo.dao.DaoException;
import com.logistimo.dao.IDaoUtil;
import com.logistimo.logger.ILogger;
import com.logistimo.reports.dao.IReportsDao;
import com.logistimo.services.IBackendService;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.http.URLFetchService;
import com.logistimo.services.mapred.IMapredService;
import com.logistimo.services.storage.StorageUtil;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.models.ICounter;


/**
 * Created by charan on 04/03/15.
 */
public abstract class AppFactory {

  private static final String APPFACTORY = "AppFactory";
  private static AppFactory _instance = (AppFactory) createInstance(APPFACTORY);
  protected ITaskService taskService;
  protected StorageUtil storageUtil;
  protected IMapredService mapredService;
  protected MemcacheService memCacheService;
  protected BlobstoreService blobStoreService;
  protected URLFetchService urlFetchService;
  protected IBackendService backendService;

  public static AppFactory get() {
    return _instance;
  }

  public static Object createInstance(String type) {
    String
        implClazzName =
        ConfigUtil.get("appfactory.class","com.logistimo.LogiAppFactory");
    try {
      return Class.forName(implClazzName).newInstance();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract ILogger getLogger(String name);

  public abstract ITaskService getTaskService();

  public abstract StorageUtil getStorageUtil();

  public abstract IMapredService getMapredService();

  public abstract MemcacheService getMemcacheService();

  public abstract BlobstoreService getBlobstoreService();

  public abstract URLFetchService getURLFetchService();

  public abstract IBackendService getBackendService();

  public abstract ICounter getCounter();

  public abstract AuthorizationService getAuthorizationService();

  public abstract AuthenticationService getAuthenticationService();

  public abstract IDaoUtil getDaoUtil() throws DaoException;

  public abstract IReportsDao getReportsDao() throws DaoException;

}
