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
 * ZooElectableClient
 */
package com.logistimo.services.utils;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

// See http://zookeeper.apache.org/doc/trunk/recipes.html#sc_leaderElection
public abstract class ZooElectableClient implements Watcher, ZkDataMonitor.ZkNodeDataListener {

  // The ZooKeeper API handle
  protected ZooKeeper hZooKeeper = null;
  XLog _logger = XLog.getLog(ZooElectableClient.class);
  // Monitors existence of a parameter zNode
  ZkDataMonitor zNodeDeletionMonitor = null;
  // The path to the election znode
  private String electionZNodePath = "/election";
  // A string of server hosts
  private String zooHosts = "localhost:2181";
  // The time out interval in milliseconds
  private int timeout = 3000;
  // True if we are the leader, False otherwise
  private boolean isLeader = false;
  // The path to our election GUID znode
  private String electionGUIDZNodePath = null;

  // Constructor
  protected ZooElectableClient() throws KeeperException, IOException, InterruptedException {
    this.zooHosts = ConfigUtil.get("zoo.servers", "localhost:2181");
    this.timeout = ConfigUtil.getInt("zoo.timeout", 3000);
    this.electionZNodePath = ConfigUtil.get("zoo.path", "/election");
    init();
  }

  protected ZooElectableClient(String zooHosts, String electionZNodePath, int timeout)
      throws InterruptedException, IOException, KeeperException {
    this.zooHosts = zooHosts;
    this.electionZNodePath = electionZNodePath;
    this.timeout = timeout;
    init();
  }

  protected ZooElectableClient(String electionZNodePath)
      throws InterruptedException, IOException, KeeperException {
    this.electionZNodePath = electionZNodePath;
    this.zooHosts = ConfigUtil.get("zoo.servers", "localhost:2181");
    this.timeout = ConfigUtil.getInt("zoo.timeout", 3000);
    init();
  }

  protected void init() throws IOException, KeeperException, InterruptedException {
    // Initialize the ZooKeeper api
    hZooKeeper = new ZooKeeper(this.zooHosts, this.timeout, this);
    // Attempt to create the election znode parent
    conditionalCreateElectionZNode();
    // Create our election GUID
    conditionalCreateElectionGUIDZNode();

    determineAndPerformRole();
  }

  // @return String containing path to persistent election znode
  private final String getElectionZNodePath() {
    return electionZNodePath;
  }


  // @return handle to ZooKeeper API
  private ZooKeeper getZooKeeper() {
    assert null != hZooKeeper;
    return hZooKeeper;
  }

  // @return True if the we are the current leader, False otherwise
  protected boolean getCachedIsLeader() {
    return isLeader;
  }

  // @return handle to our deletion monitor
  private ZkDataMonitor getZNodeDeletionMonitor() {
    assert null != zNodeDeletionMonitor;
    return zNodeDeletionMonitor;
  }

  // @return the path of this client's election GUID
  protected final String getElectionGUIDZNodePath() {
    assert null != electionGUIDZNodePath;
    return electionGUIDZNodePath;
  }

  // Utility function to convert a GUID to a full path
  private String formatElectionGUIDZNodePath(String zNodeGUID) {
    return getElectionZNodePath() + "/" + zNodeGUID;
  }

  // @return the path of the leader's election GUID
  private String getLeaderElectionGUIDZNodePath(List<String> optionalGUIDs)
      throws KeeperException, InterruptedException {
    List<String> guids = ((optionalGUIDs == null) || optionalGUIDs.isEmpty()) ?
        getZooKeeper().getChildren(getElectionZNodePath(), false /*bWatch*/) : optionalGUIDs;
    if (!guids.isEmpty()) {
      String leaderGUID = formatElectionGUIDZNodePath(Collections.min(guids));
      _logger.info("ZooElectableClient::getLeaderElectionGUIDZNodePath:: " + leaderGUID);
      return leaderGUID;
    } else {
      _logger.info("ZooElectableClient::getLeaderElectionGUIDZNodePath:: no GUIDS exist!");
      return null;
    }
  }

  // @return largest guid znode that is less than our guid (unless we are the leader, then return leader znode path)
  private String getZNodePathToWatch(List<String> optionalGUIDs)
      throws KeeperException, InterruptedException {
    // Early out if we are the leader
    if (getCachedIsLeader()) {
      _logger.info(
          "ZooElectableClient::getZNodePathToWatch:: (" + getElectionGUIDZNodePath() + ") -> "
              + getElectionGUIDZNodePath());
      return getElectionGUIDZNodePath();
    }

    List<String> guids = ((optionalGUIDs == null) || optionalGUIDs.isEmpty()) ?
        getZooKeeper().getChildren(getElectionZNodePath(), false /*bWatch*/) : optionalGUIDs;

    if (!guids.isEmpty()) {
      // Initialize to first path less than our znode
      String zNodePathToWatch = null;
      int itrGUID = 0;
      for (; itrGUID < guids.size(); ++itrGUID) {
        String guid = formatElectionGUIDZNodePath(guids.get(itrGUID));
        if (guid.compareTo(getElectionGUIDZNodePath()) < 0) {
          zNodePathToWatch = guid;
          break;
        }
      }

      // There should be at least one znode less than us
      assert null != zNodePathToWatch;

      // Find largest znode that's less than our znode
      for (; itrGUID < guids.size(); ++itrGUID) {
        String guid = formatElectionGUIDZNodePath(guids.get(itrGUID));
        if (
            (guid.compareTo(zNodePathToWatch) > 0)
                && (guid.compareTo(getElectionGUIDZNodePath()) < 0)
            ) {
          zNodePathToWatch = guid;
        }
      }
      _logger.info(
          "ZooElectableClient::getZNodePathToWatch:: (" + getElectionGUIDZNodePath() + ") -> "
              + zNodePathToWatch);
      return zNodePathToWatch;
    } else {
      _logger.info("ZooElectableClient::getZNodePathToWatch:: no GUIDS exist!");
      return null;
    }
  }


  // Attempts to create an election znode if it doesn't already exist
  private void conditionalCreateElectionZNode() throws KeeperException, InterruptedException {
    if (null == getZooKeeper().exists(getElectionZNodePath(), false /*bWatch*/)) {
      try {
        final String
            path =
            getZooKeeper().create(getElectionZNodePath(), null /*data*/, Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        _logger
            .info("ZooElectableClient::conditionalCreateElectionZNode:: created with path:" + path);
      } catch (KeeperException.NodeExistsException ne) {
        _logger.info(
            "ZooElectableClient::conditionalCreateElectionZNode:: failed (NodeExistsException)");
      }
    } else {
      _logger.info("ZooElectableClient::conditionalCreateElectionZNode:: already created.");
    }
  }

  // Creates a sequential znode with a lifetime of these client process
  private void conditionalCreateElectionGUIDZNode() throws KeeperException, InterruptedException {
    electionGUIDZNodePath =
        getZooKeeper().create(getElectionZNodePath() + "/guid-", null /*data*/, Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
    _logger.info("ZooElectableClient::conditionalCreateElectionGUIDZNode:: created with path:"
        + electionGUIDZNodePath);
  }

  // Elects a leader and caches the results for this client
  private void electAndCacheLeader() throws KeeperException, InterruptedException {
    isLeader = getElectionGUIDZNodePath().equals(getLeaderElectionGUIDZNodePath(null));
    _logger.info("ZooElectableClient::electAndCacheLeader:: " + isLeader);
  }

  // Sets a deletion monitor on the next lowest election GUID (if we are the leader, then we listen to ourselves)
  private void resetZNodeDeletionMonitor() throws KeeperException, InterruptedException {
    // TODO: possible race condition:
    // If zNodeDeletionMonitor is not null and a watch event comes in, we may get some sort of null exception or
    // watch/processResult calls on a stale monitor.
    zNodeDeletionMonitor = new ZkDataMonitor(getZooKeeper(), getZNodePathToWatch(null), this);
  }

  // Watcher callback
  public void process(WatchedEvent event) {
    // znode monitor can be null if we haven't initialized it yet
    if (null != zNodeDeletionMonitor) {
      // Forward to znode monitor
      getZNodeDeletionMonitor().process(event);
    }
  }

  // Callback when monitored znode is deleted
  public void onZNodeDeleted() {
    try {
      determineAndPerformRole();
    } catch (Exception e) {
      _logger.severe("Failed to perform role ", e);
    }
  }

  // Callback received when znode monitor dies
  public void onZooKeeperSessionClosed() {
    synchronized (this) {
      notifyAll();
    }
  }


  // Callback received when znode monitor dies
  public void onZooKeeperDisconnected() {
    synchronized (this) {
      notifyAll();
    }
  }

  @Override
  public void onZooKeeperReconnected() {
    try {
      _logger.info("Zookeeper reconnected: performing role");

      //Determining role on reconnect
      determineAndPerformRole();
    } catch (Exception e) {
      _logger.severe("{0} while performing role on zookeeper reconnect.", e.getMessage(), e);
    }
  }

  @Override
  public void exists(byte[] b) {
    //Nothing to do here. We only track deleted nodes.
  }

  // Holds leader election, performs work based on results, and watches on a GUID
  private void determineAndPerformRole() throws KeeperException, InterruptedException {
    electAndCacheLeader();
    // Do work based on whether or not we are the leader
    performRole();
    // Set a deletion monitor if we are not the leader
    resetZNodeDeletionMonitor();
  }

  // Wait until monitor is dead

  public void run() throws KeeperException, IOException, InterruptedException {

    // Perform initial work based on whether we are the leader or not
    determineAndPerformRole();

    try {
      synchronized (this) {
        while (!getZNodeDeletionMonitor().getIsZooKeeperSessionClosed()) {
          wait();
        }
      }
    } catch (InterruptedException e) {

    }
  }

  // Override this function to determine what work should be done
  public abstract void performRole();
}