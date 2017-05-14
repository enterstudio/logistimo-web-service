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

package com.logistimo.services.taskqueue;

import com.codahale.metrics.Meter;
import com.logistimo.utils.MetricsUtil;

import org.apache.camel.Handler;
import com.logistimo.logger.XLog;

/**
 * Created by charan on 10/10/14.
 */
public class JMSTaskProcessor {

  private static Meter jmsMeter = MetricsUtil.getMeter(JMSTaskProcessor.class, "taskMeter");
  XLog log = XLog.getLog(JMSTaskProcessor.class);

  public JMSTaskProcessor() {
  }

  @Handler
  public void execute(TaskOptions taskOptions) {
    jmsMeter.mark();
    log.info("Submitting task with parameter{0}", taskOptions);
    //localQueue.add(taskOptions);
    Task simpleTask = new SimpleTask(taskOptions);
    try {
      ((Runnable) simpleTask).run();
    } catch (Exception e) {
      log.severe("Issue with submitted task {0} with error {1}", taskOptions, e.getMessage(), e);
      throw e;
    }
  }
}
