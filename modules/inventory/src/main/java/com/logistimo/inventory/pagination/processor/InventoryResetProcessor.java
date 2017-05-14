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

/**
 *
 */
package com.logistimo.inventory.pagination.processor;

import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.logger.XLog;

import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;

/**
 * Resets inventory counts
 *
 * @author Arun
 */
public class InventoryResetProcessor implements Processor {

  private static final XLog xLogger = XLog.getLog(InventoryResetProcessor.class);


  @SuppressWarnings("unchecked")
  @Override
  public String process(Long domainId, Results results, String prevOutput, PersistenceManager pm)
      throws ProcessingException {
    xLogger.fine("Entered InventoryResetProcessor.process");
    // Process results
    List<IInvntry> inventories = results.getResults();
    if (inventories == null || inventories.isEmpty()) {
      xLogger.warn("No inventory to reset");
      return null;
    }
    // Reset inventories
    Iterator<IInvntry> it = inventories.iterator();
    while (it.hasNext()) {
      it.next().reset();
    }
    return null;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_DEFAULT;
  }
}
