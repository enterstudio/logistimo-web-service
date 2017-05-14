/**
 *
 */
package com.logistimo.api.servlets;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.api.communications.MessageRouter;
import com.logistimo.communications.service.MessageService;
import com.logistimo.communications.service.SMSService;
import com.logistimo.entity.IMessageLog;
import com.logistimo.utils.MessageUtil;
import com.logistimo.logger.XLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author Arun
 */
@SuppressWarnings("serial")
public class MsgReceiverServlet extends HttpServlet {

  // Logger
  private static final XLog xLogger = XLog.getLog(MsgReceiverServlet.class);
  // Providers
  private static final String PROVIDER_INFOBIP = "infobip";

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    xLogger.fine("Entered doGet");
    try {
      // Get the provider ID, if present
      String providerId = request.getParameter("pid");
      if (providerId != null && !providerId.isEmpty()) {
        handleProviderRequest(providerId, request, response);
        return;
      }

      // Get the provider instance
      SMSService smsService = SMSService.getInstance();
      // Get the provider-specific request parameter names
      String jobIdParam = smsService.getParameterName(SMSService.PARAM_JOBID);
      String mobileNoParam = smsService.getParameterName(SMSService.PARAM_MOBILENO);
      String messageParam = smsService.getParameterName(SMSService.PARAM_MESSAGE);
      ;
      String recdOnParam = smsService.getParameterName(SMSService.PARAM_RECEIVEDON);
      ;
      // Get the jobId, if present
      String jobId = request.getParameter(jobIdParam);
      String mobileNo = request.getParameter(mobileNoParam); // from the provider
      xLogger.fine("JobID = {0}, mobileNO = {1}", jobId, mobileNo);
      xLogger.fine("JobId = {0}, mobileNo = {1}, Status = {2}, DoneTime = {3}, MessagePart = {4}",
          request.getParameter("jobno"), mobileNo, request.getParameter("status"),
          request.getParameter("DoneTime"), request.getParameter("messagepart"));
      boolean isMessage = (jobId == null || jobId.isEmpty());
      if (isMessage) { // receive the message sent from some phone
        // Get the request parameter values
        String message = request.getParameter(messageParam); // from the provider
        String recdOn = request.getParameter(recdOnParam); // from the provider
        // Decode parameters
        if (mobileNo != null) {
          mobileNo = URLDecoder.decode(mobileNo, "UTF-8");
        }
        if (message != null) {
          message = URLDecoder.decode(message, "UTF-8");
        }
        if (recdOn != null) {
          recdOn = URLDecoder.decode(recdOn, "UTF-8");
        }
        xLogger.fine("MsgReceiver: mobileNo = {0}, message = {1}, recdOn = {2}", mobileNo, message,
            recdOn);
        // Process the message
        MessageRouter router = new MessageRouter(MessageService.SMS, message, mobileNo, recdOn);
        router.route();
      } else {
        // Process status update from the gateway
        String status = request.getParameter(smsService.getParameterName(SMSService.PARAM_STATUS));
        String
            doneTime =
            request.getParameter(smsService.getParameterName(SMSService.PARAM_DONETIME));
        xLogger
            .fine("Jobno = {0}, mobileNo = {1}, status = {2}, done time = {3}; mobileNoParam = {4}",
                jobId, mobileNo, status, doneTime, mobileNoParam);
        if (mobileNo != null) {
          mobileNo = URLDecoder.decode(mobileNo, "UTF-8");
        }
        if (status != null) {
          status = URLDecoder.decode(status, "UTF-8");
        }
        if (doneTime != null) {
          doneTime = URLDecoder.decode(doneTime, "UTF-8");
        }
        Date doneDate = smsService.parseDate(doneTime);
        updateMessageStatus(jobId, mobileNo, status, doneDate);
      }
    } catch (MessageHandlingException e) {
      xLogger.warn("MessageHandlingException: {0}", e.getMessage());
    } catch (Exception e) {
      xLogger.severe("Exception: {0} : {1}", e.getClass().getName(), e.getMessage());
    }
    xLogger.fine("Exiting doGet");
  }

  // Post is same as get
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  // Update the status of a sent message
  private void updateMessageStatus(String jobId, String mobileNo, String status, Date doneDate)
      throws MessageHandlingException {
    xLogger.fine("Entered updateMessageStatus");
    xLogger.info("JobId: {0}, mobileNo: {1}, Status: {2}, Done-date: {3}", jobId, mobileNo, status,
        doneDate);
    // Create a message log
    IMessageLog
        mlog =
        MessageUtil.getLog(jobId, mobileNo); // throws exception, if log entry not present
    mlog.setStatus(status);
    mlog.setDoneTime(doneDate);
    MessageUtil.log(mlog);
    xLogger.fine("Exiting updateMessageStatus");
  }

  // Handle the specific request of a given provider
  private void handleProviderRequest(String pid, HttpServletRequest request,
                                     HttpServletResponse resp) throws IOException {
    xLogger.fine("Entered handleProviderRequest");
    if (PROVIDER_INFOBIP.equals(pid)) {
      handleInfobipRequest(request, resp);
    } else {
      xLogger.severe("Unknown providers {0}", pid);
    }
    xLogger.fine("Exiting handleProviderRequest");
  }

  // Handle the delivery status report from Infobip
  private void handleInfobipRequest(HttpServletRequest request, HttpServletResponse resp) {
    xLogger.fine("Entered handleInfobipRequest");
    // Get the XML data
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(request.getInputStream());
      // Init. service
      SMSService smsService = SMSService.getInstanceByProvider(PROVIDER_INFOBIP);
      // Get the root - message - element
      Element root = doc.getDocumentElement();
      NodeList nl = root.getElementsByTagName("message");
      if (nl != null && nl.getLength() > 0) {
        for (int i = 0; i < nl.getLength(); i++) {
          Element e = (Element) nl.item(i);
          String jobId = e.getAttribute("id");
          String status = e.getAttribute("status");
          String doneDateStr = e.getAttribute("donedate");
          Date doneDate = smsService.parseDate(doneDateStr);
          updateMessageStatus(jobId, null, status, doneDate);
        }
      }
    } catch (Exception e) {
      xLogger.warn("{0} when handling Infobip delivery report request: {1}", e.getClass().getName(),
          e.getMessage());
    }
    xLogger.fine("Exiting handleInfobipRequest");
  }
}
