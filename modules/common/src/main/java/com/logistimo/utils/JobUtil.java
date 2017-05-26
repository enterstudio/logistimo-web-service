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

package com.logistimo.utils;

import com.google.gson.Gson;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.entity.IJobStatus;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;

import com.logistimo.constants.QueryConstants;

import com.logistimo.logger.XLog;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Vani
 */
public class JobUtil {
  private static final XLog xLogger = XLog.getLog(JobUtil.class);

  public static long createJob(long domainId, String userName, String description, String jobType,
                               String subType, Map<String, String> metadataMap) {
    return createJob(domainId, userName, description, jobType, subType, metadataMap,
        IJobStatus.INPROGRESS);
  }

  public static long createJob(long domainId, String userName, String description, String jobType,
                               String subType, Map<String, String> metadataMap, int status) {
    if (jobType == null || jobType.isEmpty()) {
      throw new IllegalArgumentException("Invalid job parameters");
    }
    IJobStatus job = JDOUtils.createInstance(IJobStatus.class);
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      job.setDomainId(domainId);
      job.setCreatedBy(userName);
      job.setDescription(description);
      job.setType(jobType);
      job.setSubType(subType);
      job.setMetadata(getMetadataString(metadataMap));
      job.setStatus(status);
      job.setStartTime(new Date());
      pm.makePersistent(job);
      job = pm.detachCopy(job);
    } catch (Exception e) {
      xLogger.severe("Error while creating job", e);
    } finally {
      pm.close();
    }
    return job.getJobId();
  }


  @SuppressWarnings("unchecked")
  public static Results getRecentJobs(String type, String createdBy, Long domainId,
                                      PageParams pageParams) throws ServiceException {
    if (domainId == null || type == null) {
      throw new ServiceException("Invalid input parameters while getting recent jobs.");
    }

    Results results = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(JDOUtils.getImplClass(IJobStatus.class));
    StringBuilder filter = new StringBuilder();
    StringBuilder declaration = new StringBuilder();
    filter.append("ty == typeParam");
    declaration.append("String typeParam");
    filter.append(QueryConstants.AND);
    filter.append("dId == dIdParam");
    declaration.append(CharacterConstants.COMMA).append("Long dIdParam");

    if (createdBy != null && !createdBy.isEmpty()) {
      filter.append(QueryConstants.AND);
      filter.append("crBy == crByParam");
      declaration.append(CharacterConstants.COMMA).append("String crByParam");
    }
    Map<String, Object> params = new HashMap<>();
    if (createdBy != null && !createdBy.isEmpty()) {
      params.put("crByParam", createdBy);
    }
    params.put("dIdParam", domainId);
    params.put("typeParam", type);
    q.setFilter(filter.toString());
    q.declareParameters(declaration.toString());
    q.setOrdering("stt desc");
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      List<IJobStatus> jobs = (List<IJobStatus>) q.executeWithMap(params);
      String cursor = null;
      if (jobs != null) {
        jobs.size();
        cursor = QueryUtil.getCursor(jobs);
        jobs = (List<IJobStatus>) pm.detachCopyAll(jobs);
      }
      // Get the results along with cursor, if present
      results = new Results(jobs, cursor);
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    return results;
  }

  public static String getMetadataString(Map<String, String> metadataMap) {
    if (metadataMap == null || metadataMap.isEmpty()) {
      xLogger.warn("No metadata for specified for the job");
      return null;
    }
    return new Gson().toJson(metadataMap);
  }


  public static void setJobFailed(Long jobId, String msg) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IJobStatus job = JDOUtils.getObjectById(IJobStatus.class, jobId, pm);
      job.setStatus(IJobStatus.FAILED);
      job.setReason(msg);
      pm.makePersistent(job);
    } catch (Exception e) {
      xLogger.severe("{0} while setting job status to failed. Message: {1}", e.getClass().getName(),
          e.getMessage(), e);
    } finally {
      pm.close();
    }

  }

  public static void setNumberOfRecordsCompleted(Long jobId, int numberOfRecords) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IJobStatus job = JDOUtils.getObjectById(IJobStatus.class, jobId, pm);
      job.setNumberOfRecordsCompleted(numberOfRecords);
      job.setUpdatedTime(new Date());
      pm.makePersistent(job);
    } catch (Exception e) {
      xLogger.severe("{0} while setting number of records completed. Message: {1}",
          e.getClass().getName(), e.getMessage(), e);
    } finally {
      pm.close();
    }

  }

  // Update the job status to completed, in case where there is exported data and also when there is no data to export.
  public static void setJobCompleted(Long jobId, String jobType, int size, String fileName,
                                     ResourceBundle backendMessages) {
    // Get the Job ID and update.
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IJobStatus jobStatus = JDOUtils.getObjectById(IJobStatus.class, jobId, pm);
      jobStatus.setStatus(IJobStatus.COMPLETED);
      jobStatus.setUpdatedTime(new Date());
      if (size == 0 && jobType.equals(IJobStatus.TYPE_EXPORT)) {
        jobStatus.setReason(backendMessages.getString("export.nodata"));
      } else {
        jobStatus.setOutputName(fileName);
        // Form the url for the exported file.
        String host = ConfigUtil.get("logi.host.server");
        String path = host == null ? "http://localhost:50070/webhdfs/v1" : "/media";
        String localStr = host == null ? "?op=OPEN" : "";
        String
            jobTypeLocStr =
            jobType.equals(IJobStatus.TYPE_EXPORT) ? "dataexport" : "customreports";
        String
            outputFileLoc =
            (host == null ? "" : "https://" + host) + path + "/user/logistimoapp/" + jobTypeLocStr
                + "/" + fileName + localStr;
        jobStatus.setOutputFileLocation(outputFileLoc);
      }
      pm.makePersistent(jobStatus);
    } catch (Exception e) {
      xLogger.warn("{0} when updating job with ID {1}: {2}", e.getClass().getName(), jobId,
          e.getMessage());
    } finally {
      pm.close();
    }
  }

  // Update the job metadata info.
  public static void setJobMetaInfo(Long jobId, Map<String, String> metadataMap) {
    // Get the Job ID and update.
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IJobStatus jobStatus = JDOUtils.getObjectById(IJobStatus.class, jobId, pm);
      if (metadataMap != null && !metadataMap.isEmpty()) {
        jobStatus.setMetadata(getMetadataString(metadataMap));
        pm.makePersistent(jobStatus);
      }
    } catch (Exception e) {
      xLogger
          .warn("{0} when updating metadata info for job with ID {1}: {2}", e.getClass().getName(),
              jobId, e.getMessage());
    } finally {
      pm.close();
    }
  }
}
