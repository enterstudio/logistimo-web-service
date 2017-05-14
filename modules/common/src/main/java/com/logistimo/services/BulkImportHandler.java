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
