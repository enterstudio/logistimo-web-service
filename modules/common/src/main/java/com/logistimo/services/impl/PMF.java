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

package com.logistimo.services.impl;

import com.logistimo.logger.XLog;

import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public final class PMF {

  private static final XLog xlogger = XLog.getLog(PMF.class);

  private static final PersistenceManagerFactory pmfInstance =
      JDOHelper.getPersistenceManagerFactory("Optimistic");


  private PMF() {
  }

  public static PersistenceManagerFactory get() {
    return pmfInstance;
  }


  public static PersistenceManagerFactory getReportsPM() {
    throw new UnsupportedOperationException();
  }

  /**
   * Close with eviction of cache for these objects, if there is a parallel access/dirty read during this time it could get cached.
   */
  public static void close(PersistenceManager pm) {
    evictObjects(pm);
    pm.close();
  }

  public static void evictObjects(PersistenceManager pm) {
    try {
      Collection objects = pm.getManagedObjects();
      if (objects != null) {
        pm.evictAll(objects);
      }
    } catch (Exception e) {
      xlogger.warn("Error while evicting cached object", e);
    }
  }
}
