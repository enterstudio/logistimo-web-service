/**
 *
 */
package com.logistimo.api.servlets;

import com.logistimo.api.communications.MessageHandler;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.api.security.SecurityMgr;
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
