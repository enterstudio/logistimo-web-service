/**
 *
 */
package com.logistimo.api.communications;

import com.logistimo.AppFactory;
import com.logistimo.communications.service.MessageService;
import com.logistimo.communications.service.SMSService;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.utils.HttpUtil;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Routes messages to DEV or to the 'message' task queue on the local server (prod or dev)
 *
 * @author Arun
 */
public class MessageRouter {
  // Message routing
  public static final String DEV = "d"; // send to dev.
  public static final String PROD = "p"; // send to prod.
  // URL
  private static final String DEV_URL = "http://samaanguru-dev.appspot.com/pub/receiver";
  private static final String MSGHANDLER_URL = "/s2/api/sms";
  // Logger
  private static final XLog xLogger = XLog.getLog(MessageRouter.class);

  private static ITaskService taskService = AppFactory.get().getTaskService();

  // Properties
  private String wireType = MessageService.SMS;
  private String message = null;
  private String address = null;
  private String recdOn = null;
  private String routing = PROD;

  public MessageRouter(String wireType, String message, String address, String recdOn) {
    this.wireType = wireType;
    this.message = message;
    this.address = address;
    this.recdOn = recdOn;
    // Set the dev/prod routing info.
    setRoutingInfo();
  }

  public String getWireType() {
    return wireType;
  }

  public String getMessage() {
    return message;
  }

  public String getAddress() {
    return address;
  }

  public String getReceivedOn() {
    return recdOn;
  }

  public void route() throws MessageHandlingException {
    if (message == null || message.isEmpty()) {
      throw new MessageHandlingException("No message specified");
    }
    if (address == null || address.isEmpty()) {
      throw new MessageHandlingException("No address specified");
    }
    if (DEV.equals(routing)) {
      sendMsgToDev();
    } else {
      scheduleMessageProcessing();
    }
  }

  // Get the dev/prod. routing and protocol-type info. from the message
  private void setRoutingInfo() {
    if (message == null || message.isEmpty()) {
      return;
    }
    // Check if message is to routed to DEV server
    if (message.endsWith(" " + DEV)) {
      routing = DEV;
      // Remove the dev. flag
      message = stripDevFlag(message);
    }
  }

  // Send a message to DEV
  private void sendMsgToDev() throws MessageHandlingException {
    xLogger.fine("Entered sendMsgToDev");
    try {
      Map<String, String> paramMap = null;
      if (MessageService.SMS.equals(wireType)) {
        // Get the parameters
        paramMap = getSMSParameterMap();
        // Send to dev. server's /pub/receiver
        String result = HttpUtil.get(DEV_URL, paramMap, null);
        xLogger.fine("Got result: {0}", result);
      } else {
        xLogger.warn("Unsupported wire type: {0}", wireType);
      }
    } catch (MalformedURLException e) {
      xLogger.warn("MalformedURLException when sending to dev: {0}", e.getMessage());
    } catch (IOException e) {
      xLogger.warn("IOException when sending to dev: {0}", e.getMessage());
    }
    xLogger.fine("Exiting sendMsgToDev");
  }

  // Get the parameter map for sending to DEV
  private Map<String, String> getSMSParameterMap() throws MessageHandlingException {
    Map<String, String> params = new HashMap<String, String>();
    // Get the provider-specific parameter name mapping
    SMSService smsService = SMSService.getInstance();
    try {
      params.put(smsService.getParameterName(SMSService.PARAM_MOBILENO),
          URLEncoder.encode(address, "UTF-8"));
      params.put(smsService.getParameterName(SMSService.PARAM_MESSAGE),
          URLEncoder.encode(message, "UTF-8"));
      params.put(smsService.getParameterName(SMSService.PARAM_RECEIVEDON),
          URLEncoder.encode(recdOn, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      xLogger.severe("Unsupported Encoding: {0}", e.getMessage());
    }

    return params;
  }

  // Get the generic parameter map
  private Map<String, String> getGenericParameterMap() {
    Map<String, String> paramMap = new HashMap<String, String>();
    // Strip message of its routing keyword
    message = stripKeyword(message);
    try {
      paramMap.put(MessageHandler.WIRETYPE, wireType);
      paramMap.put(MessageHandler.ADDRESS, URLEncoder.encode(address, "UTF-8"));
      paramMap.put(MessageHandler.MESSAGE, URLEncoder.encode(message, "UTF-8"));
      paramMap.put(MessageHandler.RECEIVEDON, URLEncoder.encode(recdOn, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      xLogger.severe("Unsuppported Encoding: {0}", e.getMessage());
    }
    return paramMap;
  }

  // Process the human protocol message
  private void scheduleMessageProcessing() {
    xLogger.fine("Entered scheduleMessageProcessing");
    Map<String, String> paramMap = getGenericParameterMap();
    try {
      taskService
          .schedule(taskService.QUEUE_DEFAULT, MSGHANDLER_URL, paramMap, taskService.METHOD_POST);
    } catch (TaskSchedulingException e) {
      xLogger.warn("Unable to schedule task for dev. sending: {0}", e.getMessage());
    }
    xLogger.fine("Exiting scheduleMessageProcessing");
  }

  // Strip the dev. flag from the message
  private String stripDevFlag(String message) {
    if (message == null || message.isEmpty()) {
      return null;
    }
    return message.substring(0, message.length() - (DEV.length() + 1));
  }

  // Strip the keyword from the message
  public String stripKeyword(String message) {
    if (message == null || message.isEmpty()) {
      return null;
    }
    return message.substring(MessageHandler.KEYWORD.length() + 1);
  }
}
