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

package com.logistimo.services.utils;
/**
 * A simple class that monitors the data and existence of a ZooKeeper
 * node. It uses asynchronous ZooKeeper APIs.
 */

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import com.logistimo.logger.XLog;

import java.util.Arrays;

// See http://zookeeper.apache.org/doc/current/javaExample.html#ch_Introduction
public class ZkDataMonitor implements Watcher, StatCallback {

  XLog _logger = XLog.getLog(ZkDataMonitor.class);


  // The ZooKeeper API handle
  ZooKeeper hZooKeeper = null;

  // The ZNode that we're monitoring
  String zNodePath = null;

  // True if we're dead, False otherwise
  boolean isZooKeeperSessionClosed = false;

  // The listener object interested in changes to our monitored znode
  ZkNodeDataListener listener = null;

  byte prevData[];

  /**
   * Constructor
   */
  public ZkDataMonitor(ZooKeeper hZooKeeper, String zNodePath, ZkNodeDataListener listener) {
    this.hZooKeeper = hZooKeeper;
    this.zNodePath = zNodePath;
    this.listener = listener;
    // Get things started by checking if the node exists. We are going
    // to be completely event driven
    checkZNodeExistsAsync();
  }

  /**
   * @return handle to ZooKeeper API
   */
  private ZooKeeper getZooKeeper() {
    assert null != hZooKeeper;
    return hZooKeeper;
  }

  /**
   * @return Path to zNode that we're monitoring for existence
   */
  private final String getZNodePath() {
    return zNodePath;
  }

  /**
   * @return True if we're dead, False otherwise
   */
  public boolean getIsZooKeeperSessionClosed() {
    return isZooKeeperSessionClosed;
  }

  /**
   * @return Listener to callback when data changes
   */
  private ZkNodeDataListener getListener() {
    assert null != listener;
    return listener;
  }

  /**
   * Utility function to close monitor and inform listener on session close
   */
  private void onZooKeeperSessionClosed() {
    isZooKeeperSessionClosed = true;
    getListener().onZooKeeperSessionClosed();
  }

  /**
   * Utility function to close monitor and inform listener on connection loss
   */
  private void onZooKeeperDisconnected() {
    getListener().onZooKeeperDisconnected();
  }

  private void onZooKeeperReconnected() {
    getListener().onZooKeeperReconnected();
  }

  /**
   * Utility function to check on znode existence
   */
  private void checkZNodeExistsAsync() {
    getZooKeeper().exists(getZNodePath(), true /*bWatch*/, this /*AsyncCallback.StatCallback*/, null /*Object ctx*/);
  }

  /**
   * Watcher callback
   */
  public void process(WatchedEvent event) {
    String path = event.getPath();
    if (event.getType() == Event.EventType.None) {
      // We are are being told that the state of the
      // connection has changed
      switch (event.getState()) {
        case SyncConnected:
          // In this particular example we don't need to do anything
          // here - watches are automatically re-registered with
          // server and any watches triggered while the client was
          // disconnected will be delivered (in order of course)
          onZooKeeperReconnected();
          break;
        case Expired:
          // It's all over
          onZooKeeperSessionClosed();
          break;

        // It's all over
        case Disconnected:
          onZooKeeperDisconnected();
          break;
      }
    } else {
      if (path != null && path.equals(getZNodePath())) {
        // Something has changed on the node, let's find out
        checkZNodeExistsAsync();
      }
    }
  }

  /**
   * AsyncCallback.StatCallback
   */
  @SuppressWarnings("deprecation")
  public void processResult(int rc, String path, Object ctx, Stat stat) {
    _logger.info("ZooZNodeDeletionMonitor::processResult:: rc is " + rc);
    if (rc == Code.NoNode /*Code.NONODE.ordinal()*/) {
      getListener().onZNodeDeleted();
    } else if (rc == Code.Ok /*Code.OK.ordinal()*/) {
      // put logging or handle this case separately (zNode exists)
      byte b[] = null;
      try {
        b = getZooKeeper().getData(getZNodePath(), false, null);
      } catch (KeeperException e) {
        // We don't need to worry about recovering now. The watch
        // callbacks will kick off any exception handling
        _logger.warn("Keeper exception", e);
      } catch (InterruptedException e) {
        return;
      }
      if (b != null && !Arrays.equals(prevData, b)) {
        listener.exists(b);
        prevData = b;
      }
    } else if (
        (rc == Code.SessionExpired /*Code.SESSIONEXPIRED.ordinal()*/)
            || (rc == Code.NoAuth /*Code.NOAUTH.ordinal()*/)
        ) {
      onZooKeeperSessionClosed();
    } else {
      // Retry errors
      checkZNodeExistsAsync();
    }
  }

  /**
   * Other classes use the DataMonitor by implementing this method
   */
  public interface ZkNodeDataListener {
    /**
     * The existence status of the node has changed.
     */
    void onZNodeDeleted();

    /**
     * The ZooKeeper session is no longer valid.
     */
    void onZooKeeperSessionClosed();

    /**
     * The ZooKeeper connection lost
     */
    void onZooKeeperDisconnected();

    /**
     * Listener for zookeeper reconnect.
     */
    void onZooKeeperReconnected();

    /**
     * The existence status of the node has changed.
     */
    void exists(byte[] b);
  }
}