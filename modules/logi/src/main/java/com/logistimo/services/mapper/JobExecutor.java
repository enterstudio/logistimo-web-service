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

package com.logistimo.services.mapper;

import com.logistimo.AppFactory;
import com.logistimo.services.mapred.IMapredService;
import com.logistimo.services.taskqueue.TaskService;

import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by charan on 29/09/14.
 */
public class JobExecutor {

  private static XLog xLogger = XLog.getLog(JobExecutor.class);

  private final Job job;

  public JobExecutor(Job job) {
    this.job = job;

  }

  public void execute() {
    Configuration configuration = job.getConfiguration();
    String inputFormatStr = configuration.get("mapreduce.inputformat.class");
    String mapperClass = configuration.get("mapreduce.map.class");

    GenericMapper genericMapper = null;
    GenericMapper.Context context = null;
    InputFormat inputFormat = null;
    boolean failed = false;
    try {
      genericMapper = (GenericMapper) Class.forName(mapperClass).newInstance();
      context = new GenericMapper.Context(configuration);
      genericMapper.taskSetup(context);
      inputFormat = InputFormatFactory.getInputFormat(inputFormatStr);
      inputFormat.setConfiguration(configuration);
      KeyVal keyVal;
      while ((keyVal = inputFormat.readNext()) != null) {
        genericMapper.map(keyVal.getKey(), keyVal.getValue(), context);
      }

    } catch (Exception e) {
      failed = true;
      xLogger.severe("Failed to start job", e);
    } finally {
      if (genericMapper != null) {
        try {
          genericMapper.taskCleanup(context);
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (inputFormat != null) {
          inputFormat.close();
        }
      }
    }
    String callback = configuration.get(IMapredService.PARAM_DONECALLBACK);
    if (callback != null) {
      Map<String, String> params = new HashMap<String, String>();
      params.put(IMapredService.JOBID_PARAM, job.getId());
      params.put(IMapredService.JOB_STATUS, String.valueOf(!failed));
      job.getConfiguration().get("domainId");
      try {
        AppFactory.get().getTaskService()
            .schedule("default", callback, params, null, TaskService.METHOD_GET, -1, null,
                "MAPRED_CONFIRM");
      } catch (TaskSchedulingException e) {
        xLogger.severe("Failed to call Done call back ", e);
      }
    }

  }
}
