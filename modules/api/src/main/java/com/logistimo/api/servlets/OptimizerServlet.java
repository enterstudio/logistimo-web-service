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

package com.logistimo.api.servlets;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.context.StaticApplicationContext;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.models.InventoryFilters;
import com.logistimo.inventory.optimization.pagination.processor.InvOptimizationDQProcessor;
import com.logistimo.inventory.optimization.pagination.processor.InvOptimizationPSProcessor;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.QueryParams;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class OptimizerServlet extends JsonRestServlet {

  private static final XLog xLogger = XLog.getLog(OptimizerServlet.class);
  private static final int MAX_ENTITIES_PER_TASK = 10;
  // Computation types
  private static final String COMPUTE_PS = "PS";
  private static final String COMPUTE_DQ = "DQ";

  // Access via the data/test GUI
  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    processPost(req, resp, backendMessages, messages);
  }

  // Accessed via a task
  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    xLogger.fine("Entered processPost");
    // Get parameters
    String domainIdStr = req.getParameter("domainid");
    String
        kioskIdStr =
        req.getParameter("kioskid"); // optional; domain Id is ignored, if kioskId is specified
    String
        materialIdStr =
        req.getParameter(
            "materialid"); // optional: domain Id is ignored and used alongside kiosk Id, if specified
    String compute = req.getParameter("compute"); // either PS or DQ
    if (compute == null || compute.isEmpty()) {
      compute = COMPUTE_DQ;
    }
    Long domainId = null;
    Long kioskId = null;
    Long materialId = null;
    try {
      if (domainIdStr != null && !domainIdStr.isEmpty()) {
        domainId = Long.parseLong(domainIdStr);
      } else {
        xLogger.severe("No domain Id provided");
        return;
      }
      if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
        kioskId = Long.valueOf(kioskIdStr);
      }
      if (materialIdStr != null && !materialIdStr.isEmpty()) {
        materialId = Long.valueOf(materialIdStr);
      }
      // Get the optimizer config.
      DomainConfig dc = DomainConfig.getInstance(domainId);
      // Check if optimization is enabled for this domain
      if (dc.getInventoryConfig().getConsumptionRate() != InventoryConfig.CR_AUTOMATIC) {
        xLogger.info("No optimization required for domain {0}", domainId);
        return;
      }
      InvntryDao
          invntryDao =
          StaticApplicationContext.getApplicationContext().getBean(InvntryDao.class);
      InventoryFilters filters = new InventoryFilters();
      // Form query and query params
      if (kioskId != null) {
        filters.withKioskId(kioskId);
        if (materialId != null) {
          filters.withMaterialId(materialId);
        }
      } else {
        filters.withSourceDomainId(domainId);
      }
      QueryParams qp = invntryDao.buildInventoryQuery(filters, false);
      // Get page params
      PageParams pageParams = new PageParams(null, MAX_ENTITIES_PER_TASK);
      // Get the processor
      String processor = InvOptimizationDQProcessor.class.getName();
      if (COMPUTE_PS.equals(compute)) {
        processor = InvOptimizationPSProcessor.class.getName();
      }
      // *** Execute optimization process ***
      PagedExec.exec(domainId, qp, pageParams, processor, null, null);
      // *** Done execution ***
    } catch (Exception e) {
      xLogger.severe("Exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      // TODO: if email is set then send exception via email
    }
    xLogger.fine("Exiting processPost");
  }
}
