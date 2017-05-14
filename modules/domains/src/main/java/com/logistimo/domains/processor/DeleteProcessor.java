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

package com.logistimo.domains.processor;

import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.logger.XLog;

import java.util.List;

import javax.jdo.PersistenceManager;

/**
 * @author Arun
 */
public class DeleteProcessor implements Processor {

  private static final XLog xLogger = XLog.getLog(DeleteProcessor.class);

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public String process(Long domainId, Results results, String skipCounting, PersistenceManager pm)
      throws ProcessingException {
    try {
      if (results == null) {
        return null;
      }
      // Get results
      List list = results.getResults();

      if (list == null || list.isEmpty()) {
        return null;
      }

      pm.deletePersistentAll(list);
      xLogger
          .info("Deleted {0} objects of type {1} ", list.size(), list.get(0).getClass().getName());
    } catch (Exception e) {
      xLogger
          .severe("{0} when deleting entities in domain {1}: {2}", e.getClass().getName(), domainId,
              e.getMessage(), e);
    }
    return skipCounting;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_DEFAULT;
  }
}
