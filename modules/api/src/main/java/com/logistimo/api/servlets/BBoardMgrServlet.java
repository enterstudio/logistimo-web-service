/**
 *
 */
package com.logistimo.api.servlets;

import com.logistimo.dao.JDOUtils;

import com.logistimo.entity.IBBoard;
import com.logistimo.events.handlers.BBHandler;

import com.logistimo.security.SecureUserDetails;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.ServiceException;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Arun
 */
public class BBoardMgrServlet extends JsonRestServlet {

  private static final long serialVersionUID = 1L;
  // Logger
  private static final XLog xLogger = XLog.getLog(BBoardMgrServlet.class);
  // Actions
  private static final String ACTION_POSTMESSAGE = "post";
  private static final String ACTION_REMOVE = "rm";

  // Remove messages from a bulletin board
  private static void removeMessages(HttpServletRequest request) {
    xLogger.fine("Entered removeMessages");
    // Get the item Ids
    String itemIdsCSV = request.getParameter("itemids");
    if (itemIdsCSV == null || itemIdsCSV.isEmpty()) {
      xLogger.severe("Nothing to remove from bulletin board");
      return;
    }
    List<Long> itemIds = new ArrayList<Long>();
    String[] itemIdsArray = itemIdsCSV.split(",");
    for (int i = 0; i < itemIdsArray.length; i++) {
      itemIds.add(Long.valueOf(itemIdsArray[i]));
    }
    // Remove
    BBHandler.remove(itemIds);
    xLogger.fine("Exiting removeMessages");
  }

  @Override
  protected void processGet(HttpServletRequest request, HttpServletResponse response,
                            ResourceBundle backendMessages,
                            ResourceBundle messages) throws ServletException, IOException,
      ServiceException {
    processPost(request, response, backendMessages, messages);
  }

  @Override
  protected void processPost(HttpServletRequest request, HttpServletResponse response,
                             ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entered processPost");
    String action = request.getParameter("action");
    if (ACTION_POSTMESSAGE.equals(action)) {
      postMessage(request, response);
    } else if (ACTION_REMOVE.equals(action)) {
      removeMessages(request);
    } else {
      xLogger.severe("Invalid action: {0}", action);
    }
    xLogger.fine("Exiting processPost");
  }

  // Post message to BBoard
  private void postMessage(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    xLogger.fine("Entered postMessage");
    String message = request.getParameter("message");
    if (message != null && !message.isEmpty()) {
      message = URLDecoder.decode(message, "UTF-8");
    } else {
      xLogger.severe("No message to post to Bulletin Board");
      sendJsonResponse(response, 200, "{\"st\": \"0\", \"ms\": \"No message to post\" }");
    }
    String jsonResp = null;
    try {
      // Get the user Id
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      xLogger.fine("sUser: {0}", sUser);
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      // Create the BBoard message
      IBBoard bb = JDOUtils.createInstance(IBBoard.class);
      bb.setDomainId(domainId);
      bb.setMessage(message);
      bb.setTimestamp(new Date());
      bb.setType(IBBoard.TYPE_POST);
      bb.setUserId(userId);
      BBHandler.add(bb);
      // Get back to user
      jsonResp = "{ \"st\": \"1\" }"; // success
    } catch (Exception e) {
      jsonResp = "{\"st\": \"0\", \"ms\": \"" + e.getMessage() + "\" }";
    }
    xLogger.fine("Exiting postMessage: {0}", jsonResp);
    sendJsonResponse(response, 200, jsonResp);
  }
}
