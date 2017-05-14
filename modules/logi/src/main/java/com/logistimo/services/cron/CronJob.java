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

package com.logistimo.services.cron;

import com.logistimo.AppFactory;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.codec.digest.DigestUtils;
import com.logistimo.utils.HttpUtil;
import com.logistimo.logger.XLog;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by charan on 08/10/14.
 */
public class CronJob implements Job {

  private static final XLog _logger = XLog.getLog(CronJob.class);

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    JobDetail jobDetail = jobExecutionContext.getJobDetail();
    _logger.info("Cron triggered for {0}", jobDetail.getDescription());
    JobDataMap dataMap = jobDetail.getJobDataMap();
    try {
      //Creating entry in redis to avoid duplicate job scheduling
      MemcacheService cache = AppFactory.get().getMemcacheService();
      String jobKey = "SJOB_" + DigestUtils.md5Hex(dataMap.getString("url"));
      if (cache.get(jobKey) == null) {
        HttpUtil
            .connectMulti(HttpUtil.GET, ConfigUtil.get("task.url") + dataMap.getString("url"), null,
                null, null, null);
        cache.put(jobKey, true, 1800);
      } else {
        _logger.warn("Job with url {0} is already scheduled. Doing nothing!!",
            dataMap.getString("url"));
      }
    } catch (Exception e) {
      _logger.warn("Scheduled job failed for url {0} with exception {1}", dataMap.getString("url"),
          e.getMessage(), e);
    }

  }
}
