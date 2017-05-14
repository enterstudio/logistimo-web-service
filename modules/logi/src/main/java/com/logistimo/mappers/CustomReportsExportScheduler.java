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

package com.logistimo.mappers;

import com.logistimo.customreports.CustomReportsExportMgr;
import com.logistimo.services.mapper.Entity;
import com.logistimo.services.mapper.GenericMapper;
import com.logistimo.services.mapper.Key;

import com.logistimo.services.impl.PMF;
import com.logistimo.logger.XLog;

import java.io.IOException;

import javax.jdo.PersistenceManager;

/**
 * Schedules custom report generation for a given domain (report could be generated for inventory, orders or transactions)
 *
 * @author vani
 */
public class CustomReportsExportScheduler
    extends GenericMapper<Key, Entity, NullWritable, NullWritable> {
  public static final String DEFAULT_EXPORT_TIME = "00:00";
  // Logger
  private static final XLog xLogger = XLog.getLog(CustomReportsExportScheduler.class);
  PersistenceManager pm = null;

  @Override
  public void taskSetup(Context context) throws IOException, InterruptedException {
    super.taskSetup(context);
    pm = PMF.get().getPersistenceManager();
  }

  @Override
  public void taskCleanup(Context context) throws IOException, InterruptedException {
    super.taskCleanup(context);
    pm.close();
  }

  @Override
  public void map(Key key, Entity entity, Context context) {
    xLogger.fine("Entered CustomReportsExportScheduler.map");
    // Get domain Id
    Long domainId = Long.valueOf(key.getId());
    xLogger.info("Processing domain: {0}", domainId);
    // Handle Custom reports export for the whole domain
    CustomReportsExportMgr.handleCustomReportsExport(domainId, null, null);
    xLogger.fine("Exiting CustomReportsExportScheduler.map");
  }
}
