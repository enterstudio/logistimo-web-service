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
