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
package com.logistimo.api.servlets;

import com.logistimo.api.communications.MessageHandler;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles received message, as per the protocol or request type
 *
 * @author Arun
 */
@SuppressWarnings("serial")
public class MsgHandlerServlet extends HttpServlet {

  // Logger
  private static final XLog xLogger = XLog.getLog(MsgHandlerServlet.class);
  // Date format
  private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss a";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    xLogger.fine("Entered doPost");
    try {
      // Get the request parameters
      String wireType = request.getParameter(MessageHandler.WIRETYPE);
      String address = request.getParameter(MessageHandler.ADDRESS);
      String message = request.getParameter(MessageHandler.MESSAGE);
      String recdOn = request.getParameter(MessageHandler.RECEIVEDON);
      // Decode parameters
      if (address != null) {
        address = URLDecoder.decode(address, "UTF-8");
      }
      if (message != null) {
        message = URLDecoder.decode(message, "UTF-8");
      }
      if (recdOn != null) {
        recdOn = URLDecoder.decode(recdOn, "UTF-8");
      }
      // Convert date
      Date recd = null;
      if (recdOn != null && !recdOn.isEmpty()) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        try {
          recd = df.parse(recdOn);
        } catch (ParseException e) {
          xLogger.warn("Unable to parse date: {0}", e.getMessage());
        }
      }
      // Invoke the message handler to handle the message
      MessageHandler mh = MessageHandler.getInstance(wireType, message, address, recd);
      // Set the URL base
      mh.setUrlBase(
          "https://" + request.getServerName() + (SecurityMgr.isDevServer() ? ":" + request
              .getServerPort() : ""));
      // Process the message
      mh.process();
    } catch (MessageHandlingException e) {
      xLogger.severe("MessageHandlingException: {0}", e.getMessage());
    } catch (Exception e) {
      xLogger.severe("Exception: {0} : {1}", e.getClass().getName(), e.getMessage());
    }
    xLogger.fine("Exiting doPost");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }
}
