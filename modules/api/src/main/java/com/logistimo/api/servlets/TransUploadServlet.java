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


import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.bulkuploads.MnlTransactionUtil;

import com.logistimo.services.ServiceException;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class TransUploadServlet extends SgServlet {
  // Actions
  public static final String ACTION_TRANSACTIONIMPORT = "ti";
  // Logger
  private static final XLog xLogger = XLog.getLog(TransUploadServlet.class);

  @Override
  protected void processGet(HttpServletRequest request, HttpServletResponse response,
                            ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    // TODO Auto-generated method stub

  }

  @Override
  protected void processPost(HttpServletRequest request, HttpServletResponse response,
                             ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entering processPost");
    String actionStr = request.getParameter("action");
    if (TransUploadServlet.ACTION_TRANSACTIONIMPORT.equals(actionStr)) {
      importTransactions(request, response, backendMessages, messages);
    } else {
      xLogger.severe("Invalid actionStr. {0}", actionStr);
    }

    xLogger.fine("Exiting processPost");
  }

  private void importTransactions(HttpServletRequest request, HttpServletResponse response,
                                  ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entering transactionImport");
    String userIdStr = request.getParameter("userid");
    String domainIdStr = request.getParameter("domainid");
    String blobKeyStr = request.getParameter("blobkey");
    String kioskIdStr = request.getParameter("kioskid");

    String type = request.getParameter("type");

    if (userIdStr == null || userIdStr.isEmpty() || domainIdStr == null || domainIdStr.isEmpty()
        || blobKeyStr == null || blobKeyStr.isEmpty() || type == null || type.isEmpty()) {
      xLogger.severe("Invalid or null parameters while scheduling transaction import");
      return;
    }
    Long domainId = Long.valueOf(domainIdStr);
    Long kioskId = null;
    if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
      kioskId = Long.valueOf(kioskIdStr);
    }
    if (BulkUploadMgr.TYPE_TRANSACTIONS.equals(type)) {
      MnlTransactionUtil
          .parseUploadedTransactions(backendMessages, messages, domainId, kioskId, userIdStr,
              blobKeyStr);
    } else if (BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA.equals(type)) {
      MnlTransactionUtil
          .parseUploadedManualTransactions(backendMessages, messages, domainId, kioskId, userIdStr,
              blobKeyStr);
    }
    xLogger.fine("Exiting transactionImport");
  }

}
