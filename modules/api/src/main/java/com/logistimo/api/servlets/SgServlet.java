/**
 *
 */
package com.logistimo.api.servlets;

import com.logistimo.security.SecureUserDetails;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Arun
 */
@SuppressWarnings("serial")
public abstract class SgServlet extends HttpServlet {

  // Logger
  private static final XLog xLogger = XLog.getLog(SgServlet.class);
  // Constants
  private static final String GET = "GET";
  private static final String POST = "POST";

  // Write plain text back on response
  protected static void writeText(HttpServletResponse resp, String message) throws IOException {
    resp.setContentType("text/plain; charset=UTF-8");
    PrintWriter pw = resp.getWriter();
    pw.write(message);
    pw.close();
  }

  // Get operation
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    process(GET, request, response);
  }

  // Post operation
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    process(POST, request, response);
  }

  // Process GET/POST with the necessary GAE exceptions handled.
  private void process(String method, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String msg = null;
    // Get the user's locale
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = null;
    if (sUser == null) {
      // Check if locale params. are passed
      String country = request.getParameter("country");
      String language = request.getParameter("language");
      if (country == null) {
        country = Constants.COUNTRY_DEFAULT;
      }
      if (language == null) {
        language = Constants.LANG_DEFAULT;
      }
      locale = new Locale(language, country);
    } else {
      locale = sUser.getLocale();
    }
    // Get the resource bundles
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    xLogger.fine("BackendMessages: {0}, Messages: {1}, Locale: {2}, sUser: {3}", backendMessages,
        messages, locale, sUser);
    try {
      // Process GET/POST
      if (GET.equals(method)) {
        processGet(request, response, backendMessages, messages);
      } else {
        processPost(request, response, backendMessages, messages);
      }
    } catch (MissingResourceException e) {
      xLogger.severe("Missing resources: {0}", e.getMessage());
      msg = "Unable to load the required resources";
//		} catch ( DeadlineExceededException e ) {
//			msg = backendMessages.getString("error.deadlineexceeded");
//    		xLogger.severe(  "DeadlineExceeded: " + e.getMessage() );
//    	} catch ( CapabilityDisabledException e ) {
//    		msg = backendMessages.getString("error.capabilitydisabled");
//    		xLogger.severe( "CapabilityDisabled: " + e.getMessage() );
    } catch (Exception e) {
      xLogger.severe("Exception: {0} : {1}", e.getClass().getName(), e.getMessage(), e);
      msg = backendMessages.getString("error") + ": " + e.getMessage();
    }
    // Send response (typically, error messages) - error response in this case
    if (msg != null) {
      sendResponse(request, response, msg);
    }
  }

  // Send response to client (can be overridden by children, e.g. JsonServlet will send a response as Json and not HTML)
  protected void sendResponse(HttpServletRequest request, HttpServletResponse response, String msg)
      throws IOException {
    if (msg == null) {
      return;
    }
    writeToScreen(request, response, msg, null, null, "/s/message.jsp");
  }

  // Write response to screen through a dispatch to another URL
  protected void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                               String mode,
                               String view, String url) throws IOException {
    writeToScreen(req, resp, message, mode, view, null, url);
  }

  protected void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                               String mode,
                               String view, String subview, String url) throws IOException {
    xLogger.fine("Entered writeToScreen: view = {0}", view);
    resp.setStatus(200);
    resp.setContentType("text/html; charset=UTF-8");
    message = "<p style=\"font-size:12px;\">" + message + "</p>";
    req.setAttribute("message", message);
    if (mode != null && !mode.isEmpty()) {
      req.setAttribute("mode", mode);
    }
    if (view != null && !view.isEmpty()) {
      req.setAttribute("view", view);
    }
    if (subview != null && !subview.isEmpty()) {
      req.setAttribute("subview", subview);
    }
    try {
      RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
      dispatcher.include(req, resp);
    } catch (ServletException e) {
      xLogger.severe("Exception when writing to screen: {0}", e.getMessage());
      e.printStackTrace();
    }

    xLogger.fine("Exiting writeToScreen");
  }

  // GET/POST Methods to be implmented by children
  protected abstract void processGet(HttpServletRequest request, HttpServletResponse response,
                                     ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException;

  protected abstract void processPost(HttpServletRequest request, HttpServletResponse response,
                                      ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException;
}
