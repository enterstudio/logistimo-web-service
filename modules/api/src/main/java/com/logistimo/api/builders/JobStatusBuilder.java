package com.logistimo.api.builders;

import com.logistimo.entity.IJobStatus;
import com.logistimo.exports.handlers.ExportHandlerUtil;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;
import com.logistimo.api.models.JobStatusModel;
import com.logistimo.exports.BulkExportMgr;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by vani on 27/10/15.
 */
public class JobStatusBuilder {
  private static final XLog xLogger = XLog.getLog(JobStatusBuilder.class);

  public Results buildJobs(Results results, SecureUserDetails user) throws ServiceException {
    List<JobStatusModel> models = null;
    if (results != null && results.getSize() > 0) {
      models =
          buildJobStatusModels(results.getResults(), user.getLocale(), user.getTimezone(),
              results.getOffset());
    }
    return new Results(models, results.getCursor(), results.getNumFound(), results.getOffset());
  }

  public List<JobStatusModel> buildJobStatusModels(List jobs, Locale locale, String timeZone,
                                                   int offset) throws ServiceException {
    List<JobStatusModel> models = null;
    if (jobs != null) {
      models = new ArrayList<JobStatusModel>(jobs.size());
      int count = offset + 1;
      for (Object job : jobs) {
        JobStatusModel model = buildJobStatusModel((IJobStatus) job, locale, timeZone);
        if (model != null) {
          model.sno = count++;
          models.add(model);
        }
      }
    }
    return models;
  }

  public JobStatusModel buildJobStatusModel(IJobStatus job, Locale locale, String timeZone)
      throws ServiceException {
    if (job.getStatus() == IJobStatus.INQUEUE) {
      return null;
    }
    JobStatusModel model = new JobStatusModel();
    model.id = job.getJobId();
    model.ty = job.getType();
    model.sbty = job.getSubType();
    model.sbtyd = ExportHandlerUtil.getExportTypeDisplay(model.sbty, locale);
    model.st = job.getStatus();
    model.std = BulkExportMgr.getExportJobStatusDisplay(model.st, locale);
    model.rsn = job.getReason();
    model.stt = job.getStartTime();
    if (job.getStartTime() != null) {
      model.sttStr = LocalDateUtil.format(job.getStartTime(), locale, timeZone);
    }
    model.ut = job.getUpdatedTime();
    if (job.getUpdatedTime() != null) {
      model.utStr = LocalDateUtil.format(job.getUpdatedTime(), locale, timeZone);
    }
    model.nr = job.getNumberOfRecordsCompleted();
    model.rsn = job.getReason();
    model.on = job.getOutputName();
    model.ofl = job.getOutputFileLocation();
    model.crby = job.getCreatedBy();
    UsersService as = Services.getService(UsersServiceImpl.class, locale);
    if (model.crby != null) {
      try {
        IUserAccount cbyUsr = as.getUserAccount(model.crby);
        model.crbyFn = cbyUsr.getFullName();
      } catch (ObjectNotFoundException e) {
        xLogger.warn("ObjectNotFoundException occurred while finding user for export job {0}",
            model.crby);
      }
    }

    Map<String, String> mMap = job.getMetadataMap();
    if (mMap != null) {
      List<String> rcpList = StringUtil.getList(mMap.get("userids"));
      if (rcpList != null && !rcpList.isEmpty()) {
        model.rcp = new HashMap<>(rcpList.size());
        // Iterate through the model.rcp list and get a map of recepient user ids and their full names
        for (String rcpId : rcpList) {
          try {
            IUserAccount ua = as.getUserAccount(rcpId);
            model.rcp.put(rcpId, ua.getFullName());
          } catch (ObjectNotFoundException e) {
            xLogger.warn("ObjectNotFoundException occurred while finding user for export job {0}",
                model.crby);
          }
        }
      }
      List<String> emList = StringUtil.getList(mMap.get("emids"));
      if (emList != null && !emList.isEmpty()) {
        if (model.rcp == null) {
          model.rcp = new HashMap<>(emList.size());
        }
        for (String emId : emList) {
          model.rcp.put(emId, "__email");
        }
      }
    }
    return model;
  }
}
