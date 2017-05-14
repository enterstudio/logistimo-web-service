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

import com.logistimo.AppFactory;

import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;


/**
 * Created by charan on 10/10/14.
 */
public class JMSTaskQueue implements Queue {

  XLog xLog = XLog.getLog(JMSTaskQueue.class);


  @Override
  public Task add(TaskOptions task) {
    long etaMillis = task.getEtaMillis() - System.currentTimeMillis();
    if (etaMillis > 0) {
      DelayScheduler.getInstance().schedule(task, Constants.DEFAULT);
    } else {
      // get the camel template for Spring template style sending of messages (= producer)
      ProducerTemplate
          camelTemplate =
          AppFactory.get().getTaskService().getContext()
              .getBean("camel-client", ProducerTemplate.class);
      camelTemplate.sendBody("direct:tasks", ExchangePattern.InOnly, task);
    }
    return new SimpleTask(task);
  }



  @Override
  public void startDefault() {
    xLog.info("Processing request to switch to default queue");
  }

  @Override
  public void stopDefault() {
    xLog.info("Processing request to close default queue");
  }

}
