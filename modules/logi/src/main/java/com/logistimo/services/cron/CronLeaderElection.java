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

package com.logistimo.services.cron;

import com.logistimo.services.taskqueue.DelayScheduler;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.services.utils.ZooElectableClient;

import org.apache.zookeeper.KeeperException;
import com.logistimo.logger.XLog;

import java.io.IOException;

/**
 * Created by charan on 24/11/14.
 */
public class CronLeaderElection extends ZooElectableClient {

  private static final XLog _logger = XLog.getLog(CronLeaderElection.class);

  public static CronJobScheduler scheduler = null;

  private static CronLeaderElection _instance;
  private boolean shutDown = false;

  protected CronLeaderElection() throws InterruptedException, IOException, KeeperException {
    super(ConfigUtil.get("cron.zoo.path", "/logisticsCronLeader"));
  }

  public static void start() {
    if (_instance == null) {
      synchronized (CronLeaderElection.class) {
        if (_instance == null) {
          try {
            _instance = new CronLeaderElection();
          } catch (Exception e) {
            _logger.severe("Failed to start Zoo Keeper Leader Election for Cron scheduler {0}",
                e.getLocalizedMessage(), e);
          }
        }
      }
    }
  }

  public static void stop() {
    if (null != _instance) {
      _instance.shutDown = true;
      if (scheduler != null) {
        _logger.info("Shutting down Cron scheduler");
        scheduler.shutdown();
        scheduler = null;
      }
      if (null != _instance.hZooKeeper) {
        try {
          _logger.info("Shutting down Zookeeper client");
          _instance.hZooKeeper.close();
        } catch (Exception e) {
          _logger.warn("Failed to close zookeeper on shutdown", e);
        }
      }
      _instance = null;
    }

  }

  @Override
  public void performRole() {
    if (getCachedIsLeader()) {
      if (scheduler != null && !scheduler.isShuttingdown()) {
        scheduler.shutdown();
      }
      scheduler = new CronJobScheduler();
      _logger.info(
          "We are chosen as leader .. started Cron scheduler" + this + " scheduler" + scheduler
              + " instance sched" + _instance.scheduler);
      if (!ConfigUtil.isLocal()) {
        _logger.info("We are leader, Move any tasks from current domain queue to default queue {0}",
            ConfigUtil.getDomain());
        DelayScheduler.getInstance().moveScheduledTasksToDefault(ConfigUtil.getDomain());
      }

    }
  }

  @Override
  public void onZooKeeperDisconnected() {
    _logger.info("Zookeeper disconnected event");
    if (scheduler != null) {
      _logger.info("Zookeeper Disconnected: Shutting down Cron scheduler");
      scheduler.shutdown();
      scheduler = null;
    }
  }

  @Override
  public void onZooKeeperSessionClosed() {
    if (!shutDown) {
      _logger.info("Session closed.. Re init connections");

      if (scheduler != null) {
        _logger.info("Zookeeper session closed: Shutting down Cron scheduler");
        scheduler.shutdown();
        scheduler = null;
      }

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
}
