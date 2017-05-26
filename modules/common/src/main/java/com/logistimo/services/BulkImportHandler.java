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

import com.logistimo.entity.IUploaded;

import com.logistimo.services.impl.UploadServiceImpl;
import com.logistimo.logger.XLog;

/**
 * Created by charan on 25/09/14.
 */
public class BulkImportHandler {

  private static final XLog xLogger = XLog.getLog(BulkImportHandler.class);

  public static void handleBulkImportMRCallback(String jobId) {
    xLogger.info("Finalizing bulk-import MR job {0}...", jobId);
    try {
      // Get the Uploaded object
      UploadService svc = Services.getService(UploadServiceImpl.class);
      IUploaded u = svc.getUploadedByJobId(jobId);
      if (u != null) {
        u.setJobStatus(IUploaded.STATUS_DONE);
        svc.updateUploaded(u);
      } else {
        xLogger.severe(
            "MR Bulk Import Completion Callback: Unable to find Uploaded object corresponding to job Id {0}",
            jobId);
      }
    } catch (Exception e) {
      xLogger.severe("{0} when handling bulk-import MR completion-callback for job-Id {1}: {2}",
          e.getClass().getName(), jobId, e.getMessage());
    }
    xLogger.fine("Exiting handleBulkImportMRCallback");
  }
}
