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
package com.logistimo.inventory.optimization.pagination.processor;

import com.logistimo.inventory.optimization.service.InventoryOptimizerService;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

/**
 * @author Arun
 */
public class InvOptimizationPSProcessor implements Processor {

  private static final XLog xLogger = XLog.getLog(InvOptimizationPSProcessor.class);

  // Filter inventory items to determine if processing is required or not
  private static List<IInvntry> getFilteredInventories(Long domainId, List<IInvntry> inventories) {
    List<IInvntry> filteredInvs = new ArrayList<IInvntry>();
    Iterator<IInvntry> it = inventories.iterator();
    while (it.hasNext()) {
      IInvntry inv = it.next();
      // Check if this inv. item was created in this domain; if so, then process further; else, it will be processed in the domain of its creation (superdomains)
      if (domainId.equals(inv.getDomainId())) {
        filteredInvs.add(inv);
      }
    }
    return filteredInvs;
  }

  @SuppressWarnings("unchecked")
  @Override
  public String process(Long domainId, Results results, String prevOutput, PersistenceManager pm)
      throws ProcessingException {
    xLogger.fine("Entered process");
    if (results == null) {
      return prevOutput;
    }
    List<IInvntry> inventories = results.getResults();
    if (inventories == null || inventories.isEmpty()) {
      return prevOutput;
    }
    // Get the optimizer config and locale
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    Locale locale = dc.getLocale();
    if (locale == null) {
      locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    }
    // Check if PS computation is required
    if (ic.getConsumptionRate() < InventoryConfig.CR_AUTOMATIC) {
      xLogger
          .info("Inventory optimization (PS computation) not configured for domain {0}", domainId);
      return prevOutput;
    }
    // Filter inventories to ensure optimization is required for them
    inventories = getFilteredInventories(domainId, inventories);
    if (inventories.isEmpty()) {
      return prevOutput;
    }
    try {
      // Get service
      InventoryOptimizerService
          ios =
          Services.getService("optimizer", locale);
      xLogger.info("InvOptimizationPSProcessor: Computing PS for domain = {0}", domainId);
      // Optimize
      ios.optimize(domainId, inventories, dc, true, true, locale, pm);
    } catch (Exception e) {
      xLogger.warn("Error in processing data:", e);
      throw new ProcessingException(e);
    }
    xLogger.fine("Exiting process");
    return prevOutput;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_OPTIMZER;
  }
}
