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

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.OptimizerConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.optimization.service.InventoryOptimizerService;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * @author Arun
 */
public class InvOptimizationDQProcessor implements Processor {

  private static final XLog xLogger = XLog.getLog(InvOptimizationDQProcessor.class);

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
    OptimizerConfig oc = dc.getOptimizerConfig();
    Locale locale = dc.getLocale();
    if (locale == null) {
      locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    }
    // Filter inventories for kiosks where optimization is not enabled
    List<IInvntry> filteredInventories = getFilteredInventory(domainId, inventories, oc, pm);
    if (filteredInventories == null || filteredInventories.isEmpty()) {
      xLogger.info("Nothing to compute DQ on in domain {0}", domainId);
      return prevOutput;
    }
    try {
      // Get service
      InventoryOptimizerService
          ios =
          Services.getService("optimizer", locale);
      xLogger.info("InvOptimizationDQProcessor: Computing DQ for domain = {0}...", domainId);
      // Optimize
      ios.optimize(domainId, filteredInventories, dc, false, true, locale, pm);
    } catch (Exception e) {
      throw new ProcessingException(e.getClass().getName() + ": " + e.getMessage());
    }
    xLogger.fine("Exiting process");
    return prevOutput;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_OPTIMZER;
  }

  // Get inventory filtered by kiosks where it is really required
  private List<IInvntry> getFilteredInventory(Long domainId, List<IInvntry> inventories,
                                              OptimizerConfig oc, PersistenceManager pm) {
    xLogger.fine("Entered getFilteredInventory");
    boolean isDemandForecasting = oc.getCompute() == OptimizerConfig.COMPUTE_FORECASTEDDEMAND;
    boolean isEOQ = oc.getCompute() == OptimizerConfig.COMPUTE_EOQ;
    if (!isDemandForecasting && !isEOQ) {
      return null;
    }
    Iterator<IInvntry> it = inventories.iterator();
    List<IInvntry> filteredInventories = new ArrayList<IInvntry>();
    Map<Long, Boolean>
        optMap =
        new HashMap<Long, Boolean>(); // kioskId --> boolean (true, if opt. reqd)
    while (it.hasNext()) {
      IInvntry inv = it.next();
      if (!domainId.equals(inv.getDomainId())) {
        continue; // if this inv. item was not created in this domain, then do not optimize it (it will be optimized in its leaf domain, i.e. domain of creation) (superdomains)
      }
      Long kioskId = inv.getKioskId();
      Boolean isOptReqd = optMap.get(kioskId);
      if (isOptReqd != null) {
        if (!isOptReqd) {
          continue; // DO NOT add this inventory to the filtered list (opt. not required for this kiosk)
        }
      } else { // opt. reqd. exists
        try {
          IKiosk k = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
          // Optimization not required, if the kiosk is not configured for optimization
          boolean noOptRequired = isEOQ && !k.isOptimizationOn();
          optMap.put(kioskId, Boolean.valueOf(!noOptRequired));
          if (noOptRequired) {
            xLogger.fine("Skipping EOQ optimization for kiosk {0}", k.getKioskId());
            continue;
          }
        } catch (Exception e) {
          xLogger.warn("Could not find kiosk {0} during inv. optimization: {1} : {2}",
              inv.getKioskId(), e.getClass().getName(), e.getMessage());
          continue;
        }
      }
      // Add to filtered list
      filteredInventories.add(inv);
    }
    xLogger.fine("Exiting getFilteredInventory");
    return filteredInventories;
  }
}
