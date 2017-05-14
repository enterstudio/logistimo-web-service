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

import com.logistimo.services.cron.CronLeaderElection;
import com.logistimo.services.taskqueue.QueueFactory;
import com.logistimo.services.taskqueue.TaskServer;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.services.utils.ZkDataMonitor;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

import java.io.IOException;

/**
 * Created by charan on 01/10/15.
 */
public class AppDomainHandler implements Watcher, ZkDataMonitor.ZkNodeDataListener {

  private static AppDomainHandler _instance;
  private static XLog _logger = XLog.getLog(AppDomainHandler.class);
  private final String currentDomain;
  // The ZooKeeper API handle
  protected ZooKeeper hZooKeeper = null;
  ZkDataMonitor zkDataMonitor = null;
  private boolean isDefault = false;
  private String defaultDomainPath = "/lsdefault";
  // A string of server hosts
  private String zooHosts = "localhost:2181";
  // The time out interval in milliseconds
  private int timeout = 3000;
  private boolean shutDown = false;

  private AppDomainHandler() throws KeeperException, IOException, InterruptedException {
    this.zooHosts = ConfigUtil.get("zoo.servers", "localhost:2181");
    this.timeout = ConfigUtil.getInt("zoo.timeout", 3000);
    this.defaultDomainPath = ConfigUtil.get("zoo.default.domain.path", "/lsdefault");
    this.currentDomain = ConfigUtil.getDomain();
    init();
  }

  public static void start() {
    if (_instance == null) {
      synchronized (AppDomainHandler.class) {
        if (_instance == null) {
          try {
            _instance = new AppDomainHandler();
          } catch (Exception e) {
            _logger.severe("Unrecoverable startup failure.. Failed to connect to  Zoo Keeper {0}",
                e.getLocalizedMessage(), e);
          }
        }
      }
    }

  }

  public static void stop() {
    if (_instance != null) {
      _instance.shutDown = true;
      if (ConfigUtil.getBoolean("task.server", false)) {
        if (_instance.isDefault) {
          try {
            CronLeaderElection.stop();
          } catch (Exception e) {
            _logger.warn("Exception while closing cron leader", e);
          }
        }
        try {
          TaskServer.close();
        } catch (Exception e) {
          _logger.warn("Exception while closing task server", e);
        }
      }
      if (_instance.hZooKeeper != null) {
        try {
          _logger.info("Shutting down Zookeeper client");
          _instance.hZooKeeper.close();
        } catch (Exception e) {
          _logger.warn("Failed to close zookeeper on shutdown", e);
        }
      }
    }
  }

  private void init() throws IOException, KeeperException, InterruptedException {
    // Initialize the ZooKeeper api
    hZooKeeper = new ZooKeeper(this.zooHosts, this.timeout, this);
    // Attempt to create the election znode parent
//        conditionalCreateElectionZNode();
    zkDataMonitor = new ZkDataMonitor(hZooKeeper, defaultDomainPath, this);
  }

  @Override
  public void process(WatchedEvent watchedEvent) {
    if (zkDataMonitor != null) {
      zkDataMonitor.process(watchedEvent);
    }
  }

  @Override
  public void onZNodeDeleted() {
    _logger.info("Default version node deleted in zoo keeper");

  }

  @Override
  public void onZooKeeperDisconnected() {
    //do nothing
  }

  @Override
  public void onZooKeeperReconnected() {
    //do nothing
  }

  @Override
  public void onZooKeeperSessionClosed() {
    if (!shutDown) {
      _logger.info("Session closed.. Re init connections");
      if (hZooKeeper != null) {
        try {
          hZooKeeper.close();
        } catch (InterruptedException e) {
          _logger.warn("Exception while closing session {0}", e.getMessage());
        }
      }
      try {
        init();
      } catch (Exception e) {
        _logger.severe("Fatal error while initializing zoo keeper connections ", e);
      }
    }
  }

  @Override
  public void exists(byte[] b) {
    if (b != null) {
      String domain = new String(b);
      if (domain.equals(currentDomain)) {
        promoteToDefault();
      } else {
        demoteFromDefault();
      }
    }
  }

  private void promoteToDefault() {
    if (!isDefault) {
      _logger.info("We are default version, Firing up Task servers on default queues");
      QueueFactory.getDefaultQueue().startDefault();
      if (ConfigUtil.getBoolean("task.server", false)) {
        TaskServer.getInstance(Constants.DEFAULT);
        _logger.info("We are default version, Start cron leader election {0}", currentDomain);
        CronLeaderElection.start();
      }
      isDefault = true;
    } else {
      _logger.info("False alarm?? we are already default doing nothing, current domain: {0}",
          currentDomain);
    }
  }

  private void demoteFromDefault() {
    if (isDefault) {
      _logger.info("We are no longer default version, switching back to domain queue {0}",
          currentDomain);
      QueueFactory.getDefaultQueue().stopDefault();
      if (ConfigUtil.getBoolean("task.server", false)) {
        _logger.info("We are no longer default version, switching Task server to domain queue {0}",
            currentDomain);
        TaskServer.close(Constants.DEFAULT);
        _logger
            .info("We are no longer default version, Stop cron leader election {0}", currentDomain);
        CronLeaderElection.stop();
        _logger.info("Ensure task server is up for current domain: {0}", currentDomain);
        TaskServer.getInstance(currentDomain);
      }
      isDefault = false;
    } else {
      _logger.info("Start task server , current domain: {0}", currentDomain);
      if (ConfigUtil.getBoolean("task.server", false)) {
        TaskServer.getInstance(currentDomain);
      }
    }
  }
}
