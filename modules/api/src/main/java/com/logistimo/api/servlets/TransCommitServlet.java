/**
 *
 */
package com.logistimo.api.servlets;

import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvntry;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.Results;
import com.logistimo.inventory.optimization.pagination.processor.InvOptimizationDQProcessor;
import com.logistimo.services.impl.PMF;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Arun
 */
@SuppressWarnings("serial")
public class TransCommitServlet extends SgServlet {

  // Logger
  private static final XLog xLogger = XLog.getLog(TransCommitServlet.class);
  // Actions
  private static final String ACTION_TRANSCOMMIT = "transcommit";

  private IInvntryDao invntryDao = new InvntryDao();

  public void processPost(HttpServletRequest request, HttpServletResponse response,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException {
    xLogger.fine("Entered doPost");
    // Get parameters
    String action = request.getParameter("action");
    if (ACTION_TRANSCOMMIT.equals(action)) {
      doPostTransactionCommitHook(request, response, backendMessages, messages);
    } else {
      xLogger.severe("Invalid action: {0}", action);
    }
    xLogger.fine("Exiting doPost");
  }

  public void processGet(HttpServletRequest request, HttpServletResponse response,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException {
    processPost(request, response, backendMessages, messages);
  }

  // Process actions required to be done after a transaction commit
  private void doPostTransactionCommitHook(HttpServletRequest request, HttpServletResponse response,
                                           ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException {
    xLogger.fine("Entered doPostTransactionCommitHook");
    String domainIdStr = request.getParameter("domainid");
    String transType = request.getParameter("transtype");
    if (domainIdStr == null || domainIdStr.isEmpty()) {
      throw new IllegalArgumentException("Domain ID not specified");
    }
    Long domainId = Long.valueOf(domainIdStr);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    /************ Optimization - DQ computation *********/
    // Check if inventory optimization (D & Q computation) has to be run (only for inventory transactions)
    if (TransactionUtil.isPostTransOptimizationReqd(dc, transType)) {
      optimize(domainId, request, response, backendMessages, messages);
    }
    xLogger.fine("Exiting doPostTransactionCommitHook");
  }

  // Optimize based on DQ computation
  private void optimize(Long domainId, HttpServletRequest request, HttpServletResponse response,
                        ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException {
    xLogger.fine("Entered optimize");
    String invIdsCSV = request.getParameter("inventoryids");
    if (invIdsCSV == null || invIdsCSV.isEmpty()) {
      return;
    }
    // Get the inventory objects
    String[] invIds = invIdsCSV.split(",");
    if (invIds == null || invIds.length == 0) {
      return;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      List<IInvntry> inventories = new ArrayList<IInvntry>();
      for (int i = 0; i < invIds.length; i++) {
        try {
          inventories.add(invntryDao.getById(invIds[i], pm));
        } catch (JDOObjectNotFoundException e) {
          xLogger.warn(
              "Could not find inventory with ID {0} when optimizing post-transaction-commit in domain {1}",
              invIds[i], domainId);
        }
      }
      // Optimize (esp. DQ computation)
      if (!inventories.isEmpty()) {
        InvOptimizationDQProcessor proc = new InvOptimizationDQProcessor();
        Results results = new Results(inventories, null);
        proc.process(domainId, results, null, pm);
      }
    } catch (Exception e) {
      xLogger.severe("{0} when optimizing (DQ) for inventory items in domain {1}: {2}",
          e.getClass().getName(), domainId, e.getMessage());
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting optimize");
  }
}
