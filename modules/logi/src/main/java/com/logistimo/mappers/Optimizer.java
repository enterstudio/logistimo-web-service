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

import com.logistimo.AppFactory;
import com.logistimo.services.mapper.Entity;
import com.logistimo.services.mapper.GenericMapper;
import com.logistimo.services.mapper.Key;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.lang.StringUtils;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.constants.PropertyConstants;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Optimize all domains that require such.
 *
 * @author Arun
 */
public class Optimizer extends GenericMapper<Key, Entity, NullWritable, NullWritable> {

  private final static XLog xLogger = XLog.getLog(Optimizer.class);


  public Optimizer() {
  }

  @Override
  public void map(Key key, Entity entity, Context context) {
    Long domainId = new Long(key.getId());
    // Get the domain config, and check if optimization is required
    DomainConfig dc = DomainConfig.getInstance(domainId);
    if (dc.getInventoryConfig().getConsumptionRate() == InventoryConfig.CR_AUTOMATIC) {
      Calendar GMTZero = new GregorianCalendar();
      LocalDateUtil.resetTimeFields(GMTZero);

      Calendar dCal = new GregorianCalendar();
      if (StringUtils.isNotEmpty(dc.getTimezone())) {
        dCal.setTimeZone(TimeZone.getTimeZone(dc.getTimezone()));
      }
      LocalDateUtil.resetTimeFields(dCal);
      dCal.set(Calendar.DAY_OF_MONTH, new GregorianCalendar().get(Calendar.DAY_OF_MONTH));

      long etaMillis = dCal.getTimeInMillis() - GMTZero.getTimeInMillis();
      if (etaMillis <= 0) { // If current day's time passed, schedule for next day
        dCal.add(Calendar.DAY_OF_MONTH, 1);
      }
      String freq = dc.getOptimizerConfig().getComputeFrequency();
      boolean proceed = false;
      if (StringUtils.isBlank(freq) || Constants.FREQ_DAILY.equals(freq)) {
        proceed = true;
      } else if (Constants.FREQ_WEEKLY.equals(freq)
          && dCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
        proceed = true;
      } else if (Constants.FREQ_MONTHLY.equals(freq) && dCal.get(Calendar.DAY_OF_MONTH) == 1) {
        proceed = true;
      }
      if (proceed) {
        // Schedule optimization task
        Map<String, String> params = new HashMap<>(2);
        params.put("domainid", domainId.toString());
        params.put("compute", "PS");
        try {
          long
              delayTime =
              ConfigUtil.getInt(PropertyConstants.OPTIMIZER_JOB_DELAY_MINUTES, 180) * 60_000;
          xLogger.info("Scheduling optimizer (PS computation) for domain {0}", domainId);
          AppFactory.get().getTaskService()
              .schedule(ITaskService.QUEUE_OPTIMZER, Constants.URL_OPTIMIZE, params, null,
                  ITaskService.METHOD_POST, dCal.getTimeInMillis() + delayTime, domainId, null,
                  "OPTIMIZER");
        } catch (TaskSchedulingException e) {
          xLogger.severe(
              "TaskSchedulingExcpetion: unable to schedule optimization task for domain {0}: {1}",
              domainId, e);
        }
      }
    }
  }
}
