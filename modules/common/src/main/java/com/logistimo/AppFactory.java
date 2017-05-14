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
