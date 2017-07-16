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

package com.logistimo.services;

import com.logistimo.AppFactory;
import com.logistimo.accounting.entity.Account;
import com.logistimo.assets.entity.Asset;
import com.logistimo.assets.entity.AssetAttribute;
import com.logistimo.assets.entity.AssetRelation;
import com.logistimo.assets.entity.AssetStatus;
import com.logistimo.config.entity.Config;
import com.logistimo.constants.Constants;
import com.logistimo.domains.entity.Domain;
import com.logistimo.domains.entity.DomainLink;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.entities.entity.KioskLink;
import com.logistimo.entities.entity.KioskToPoolGroup;
import com.logistimo.entities.entity.PoolGroup;
import com.logistimo.entities.entity.UserToKiosk;
import com.logistimo.entity.ALog;
import com.logistimo.entity.BBoard;
import com.logistimo.entity.Downloaded;
import com.logistimo.entity.MessageLog;
import com.logistimo.entity.MultipartMsg;
import com.logistimo.entity.Task;
import com.logistimo.entity.Uploaded;
import com.logistimo.entity.UploadedMsgLog;
import com.logistimo.events.entity.Event;
import com.logistimo.hystrix.SecurityHystrixConcurrencyStrategy;
import com.logistimo.inventory.entity.Invntry;
import com.logistimo.inventory.entity.InvntryBatch;
import com.logistimo.inventory.entity.InvntryEvntLog;
import com.logistimo.inventory.entity.InvntryLog;
import com.logistimo.inventory.entity.Transaction;
import com.logistimo.inventory.optimization.entity.OptimizerLog;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.Material;
import com.logistimo.mnltransactions.entity.MnlTransaction;
import com.logistimo.orders.entity.DemandItem;
import com.logistimo.orders.entity.DemandItemBatch;
import com.logistimo.orders.entity.Order;
import com.logistimo.pagination.StreamingExecutor;
import com.logistimo.services.blobstore.HDFSBlobStoreService;
import com.logistimo.services.cron.CronJobScheduler;
import com.logistimo.services.cron.CronLeaderElection;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.storage.HDFSStorageUtil;
import com.logistimo.services.taskqueue.TaskServer;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.tags.entity.Tag;
import com.logistimo.users.entity.UserAccount;
import com.logistimo.utils.MetricsUtil;
import com.netflix.hystrix.strategy.HystrixPlugins;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by charan on 08/10/14.
 */
public class LogistimoServicesListener implements ServletContextListener {


  private static final XLog _logger = XLog.getLog(LogistimoServicesListener.class);

  private void lookup(Class clazz, PersistenceManager pm) {
    try {
      pm.getObjectById(clazz, 1);
    } catch (Exception e) {
      //Ignore just for initializing
    }

  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    _logger.info("Start application context initialization");
    MetricsUtil.startReporter();
    _logger.info("Started metrics reporting {0}", MetricsUtil.getRegistry());
    if (!ConfigUtil.getBoolean("gae.deployment", true)) {
      //Initialize persistence manager before starting task service.
      Class[]
          classes =
          new Class[]{Account.class, ALog.class, BBoard.class, Config.class, DemandItem.class,
              DemandItemBatch.class, Domain.class, DomainLink.class, Downloaded.class, Event.class,
              Invntry.class,
              InvntryBatch.class, InvntryEvntLog.class, InvntryLog.class,
              Kiosk.class,
              KioskLink.class, KioskToPoolGroup.class, Material.class, MessageLog.class,
              MnlTransaction.class,
              MultipartMsg.class, OptimizerLog.class, Order.class, PoolGroup.class,
              Tag.class,
              Task.class, Transaction.class, Uploaded.class, UploadedMsgLog.class,
              UserAccount.class, UserToKiosk.class, Asset.class, AssetAttribute.class,
              AssetStatus.class,
              AssetRelation.class};

      PersistenceManager pm = null;
      try {
        pm = PMF.get().getPersistenceManager();
        for (Class c : classes) {
          lookup(c, pm);
        }
      } catch (Exception e) {
        //Ignore
      } finally {
        if (pm != null) {
          try {
            pm.close();
          } catch (Exception ignored) {
            _logger.warn("Exception while closing pm", ignored);
          }
        }
      }
      //Initialising camel context
      AppFactory.get().getTaskService().initContext();
      //Set HystrixStrategy
      HystrixPlugins.getInstance()
          .registerConcurrencyStrategy(new SecurityHystrixConcurrencyStrategy());

      //start the task server
      if (ConfigUtil.getBoolean("task.server", false)) {
        TaskServer.getInstance(Constants.DEFAULT);
        if (ConfigUtil.isLocal()) {
          _logger.info("Local mode.. starting Cron scheduler" + this + " scheduler"
              + CronLeaderElection.scheduler);
          CronLeaderElection.scheduler = new CronJobScheduler();
        } else {
          CronLeaderElection.start();
        }
      }
    }
    _logger.info("Complete application context initialization");
  }


  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    try {
      AppDomainHandler.stop();
    } catch (Exception e) {
      _logger.warn("Exception while stopping AppDomainHandler", e);
    }
    try {
      AppFactory.get().getMemcacheService().close();
    } catch (Exception e) {
      _logger.warn("Exception while closing Memcache service", e);
    }
    try {
      PMF.get().close();
    } catch (Exception e) {
      _logger.warn("Exception while closing PMF", e);
    }
    try {
      ((HDFSBlobStoreService) AppFactory.get().getBlobstoreService()).close();
    } catch (Exception e) {
      _logger.warn("Exception while closing HDFSBlobstoreService", e);
    }
    try {
      ((HDFSStorageUtil) AppFactory.get().getStorageUtil()).close();
    } catch (Exception e) {
      _logger.warn("Exception while closing HDFSStorageUtil", e);
    }
    try {
      StreamingExecutor.poolExecutor.shutdown();
    } catch (Exception ignored) {
      _logger.warn("Exception while closing StreamingExecutor", ignored);
    }
    try {
      if (ConfigUtil.getBoolean("task.server",true)) {
        TaskServer.close();
      }
    } catch (Exception ignored) {
      _logger.warn("Exception while closing StreamingExecutor", ignored);
    }
  }
}
