/**
 *
 */
package com.logistimo.api.communications;

import com.logistimo.api.util.RESTUtil;
import com.logistimo.communications.service.MessageService;
import com.logistimo.dao.JDOUtils;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.entity.IMultipartMsg;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;

import com.logistimo.materials.entity.IMaterial;
import com.logistimo.proto.AuthenticateInput;
import com.logistimo.proto.AuthenticateOutput;
import com.logistimo.proto.GetInventoryInput;
import com.logistimo.proto.GetInventoryOutput;
import com.logistimo.proto.GetOrderInput;
import com.logistimo.proto.GetOrdersInput;
import com.logistimo.proto.GetOrdersOutput;
import com.logistimo.proto.InputMessageBean;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.proto.MessageHeader;
import com.logistimo.proto.MessageUtil;
import com.logistimo.proto.OrderOutput;
import com.logistimo.proto.OutputMessageBean;
import com.logistimo.proto.ProtocolException;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.proto.UpdateInventoryInput;
import com.logistimo.proto.UpdateInventoryOutput;
import com.logistimo.proto.UpdateOrderInput;
import com.logistimo.proto.UpdateOrderStatusInput;
import com.logistimo.users.entity.IUserAccount;

import com.logistimo.constants.Constants;
import com.logistimo.utils.HttpUtil;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author Arun
 */
public class MachineProtocolMessageHandler extends MessageHandler {

  // Port for SMS
  public static final String SMS_PORT = "16500";
  // REST API commands
  private static final String LOGIN = "l";
  private static final String INVENTORY = "i";
  private static final String ORDER = "o";
  // Re-trials
  private static int NUM_RETRIALS = 3;

  public MachineProtocolMessageHandler(String wireType, String message,
                                       String address, Date recdOn)
      throws MessageHandlingException {
    super(wireType, message, address, recdOn);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void process() throws MessageHandlingException, IOException {
    xLogger.fine("Entered process: message = {0}", message);
    try {
      // Check the command
      MessageHeader header = new MessageHeader(message);
      String cmd = header.getCommand();
      // Get the input messages messageId - this has to be the ID for the output messages as well
      String messageId = header.getMessageId();
      xLogger.fine("process(): messageId = {0}", messageId);
      // Persist the message until all the messages have arrived
      Vector<String> messages = waitOrProcess(address, message, header);
      if (messages == null || messages.isEmpty()) { // implies persist and wait for more messages
        xLogger.info("Did not get complete response yet: mobileNo = {0}, message = {1}", address,
            message);
        return;
      }
      // All message have arrived
      // Get the input message bean
      InputMessageBean iBean = InputMessageBean.getInstance(messages);
      // Get user Id
      String userId = iBean.getUserId();
      // Initialize services
      initMessageService(userId);
      // Check if the user's phone on record, and the source phone are the same; if not, respond with relevant message
      if (!isPhoneValid()) {
        xLogger
            .warn("Source phone {0} does not match user {1}'s phone", address, iBean.getUserId());
        String locale = Constants.LANG_DEFAULT;
        if (user != null) {
          locale = user.getLocale().toString();
        }
        Vector<String>
            errMessages =
            getErrorMessages(iBean.getVersion(),
                backendMessages.getString("error.sourcephonenotmatching"), messageId, locale);
        sendMessages(address, errMessages, MessageService.BINARY, SMS_PORT);
        return;
      }
      // Retrial? If this is request for partial response, then retrieve last response and send
      if (iBean.hasResponseMessageNumbers()) {
        try {
          sendPartialResponse(address, iBean.getResponseMessageNumbers());
          return;
        } catch (ObjectNotFoundException e) {
          // Log info. and re-do the operation (it appears that the operation could not execute and store the message - so re-doing should be ok)
          xLogger.info("Multipart message with ID {0}.{1} was not found. Re-doing the operation...",
              address, messageId);
        } catch (MessageHandlingException e) {
          xLogger.severe(
              "Unable to send partial response messages for address {0} and missing responses {1}: {2}",
              address, iBean.getResponseMessageNumbers(), e.getMessage());
          return; // NOTE: Given some error in processing the buffered missing responses, we are NOT going to re-do the whole op, and re-send for now
        }
      }
      // Process and get the output bean (this is fresh command, not a retrial)
      OutputMessageBean oBean = null;
      if (RestConstantsZ.ACTION_LOGIN.equals(cmd)) {
        oBean = processLogin(iBean);
      } else if (RestConstantsZ.ACTION_GETINVENTORY.equals(cmd)) {
        oBean = processGetInventory(iBean);
      } else if (RestConstantsZ.ACTION_UPDINVENTORY.equals(cmd)) {
        oBean = processUpdateInventory(iBean);
      } else if (RestConstantsZ.ACTION_GETORDERS.equals(cmd)) {
        oBean = processGetOrders(iBean);
      } else if (RestConstantsZ.ACTION_GETORDER.equals(cmd) || RestConstantsZ.ACTION_CANCELORDER
          .equals(cmd)) {
        oBean =
            processGetOrCancelOrder(iBean,
                cmd); // TODO: Deprecated; use processUpdateOrderStatus instead
      } else if (RestConstantsZ.ACTION_UPDATEORDER.equals(cmd)) {
        oBean = processUpdateOrder(iBean);
      } else if (RestConstantsZ.ACTION_UPDATEORDERSTATUS.equals(cmd)) {
        oBean = processUpdateOrderStatus(iBean);
      } else {
        xLogger.warn("Unsupported command: {0}", cmd);
      }

      // NOTE: countryCode SHOULD be set by now by the processXXX methods above!

      // Send the output message
      if (oBean != null) {
        // Set the input message Id for the output messages
        oBean.setMessageId(messageId);
        // Update version (e.g. 01, 02, as passed in by input messages)
        oBean.setVersion(iBean.getVersion());
        // Get the response messages
        Vector<String> respMessages = oBean.toMessageString();
        // Store the response messages
        try {
          storeResponseMessages(address, messageId, respMessages, countryCode);
        } catch (MessageHandlingException e) {
          xLogger.warn("Exception when storing response messages with Id {0} from address {1}: {2}",
              messageId, address, e.getMessage());
        }
        // Send messages
        sendMessages(respMessages, null, MessageService.BINARY, SMS_PORT);
      }
    } catch (ProtocolException e) {
      throw new MessageHandlingException(e.getMessage());
    }
    xLogger.fine("Exiting process");
  }

  // Send SMS messages, which is what the machine protocol is designed for
  @SuppressWarnings("unchecked")
  protected void sendPartialResponse(String address, String missingResponseMsgNos)
      throws ObjectNotFoundException, MessageHandlingException, IOException, ProtocolException {
    xLogger.fine("Entered sendPartialResponse: address = {0}, missingResponseMsgNos = {1}", address,
        missingResponseMsgNos);
    MessageHeader[] msgHeaders = null;
    // First check whether there are missing responses requested
    if (missingResponseMsgNos != null && !missingResponseMsgNos.isEmpty()) {
      msgHeaders = MessageUtil.getResponseMessageHeaders(missingResponseMsgNos);
    }
    // Filter messages to only contain those missing
    Vector<String> messages = null;
    if (msgHeaders != null && msgHeaders.length > 0) {
      xLogger.fine("msgHeaders: msgId = {0}, numMsgs = {1}", msgHeaders[0].getMessageId(),
          msgHeaders.length);
      messages = getResponseMessages(address, msgHeaders[0].getMessageId());
      // NOTE: countryCode SHOULD be set by now, based on what is stored in the MultipartMsg!
      messages = MessageUtil.filterMessages(messages, msgHeaders);
    }
    xLogger.fine("Messages to be sent: {0}", messages.toString());
    // Send messages
    sendMessages(messages, null, MessageService.BINARY, SMS_PORT);
    xLogger.fine("Exiting sendPartialResponse");
  }

  // Wait for more messages (of a given seqeunce), or process a given (full) message
  private Vector<String> waitOrProcess(String address, String message, MessageHeader header)
      throws MessageHandlingException {
    xLogger.fine("Entered waitOrProcess");
    int numMessages = header.getNumberOfMessages();
    if (numMessages == 1) {
      Vector<String> v = new Vector<String>();
      v.add(message);
      return v;
    }
    // Form the persistence key
    String key = JDOUtils.createMultipartMsgKey(address, header.getMessageId());
    // Get the message
    IMultipartMsg mmsg = null;
    try {
      // Get the msg. in storage, if any
      mmsg = com.logistimo.utils.MessageUtil.getMultipartMsg(key);
      // Add the new message
      mmsg.addMessage(message);
      xLogger.fine("Accumulated messages: {0}", mmsg.getMessages());
      // Check if message is complete; if so, delete from storage and return full message
      if (numMessages == mmsg.size()) {
        Vector<String> msgs = toVector(mmsg.getMessages());
        // Delete the multi-part message from temp. storage
        com.logistimo.utils.MessageUtil.removeMultipartMsg(mmsg);
        // Return assembled message
        return msgs;
      }
    } catch (ObjectNotFoundException e) {
      mmsg = JDOUtils.createInstance(IMultipartMsg.class);
      mmsg.setId(key);
      mmsg.addMessage(message);
      mmsg.setTimestamp(new Date());
    }
    if (mmsg != null) {
      // Store message
      com.logistimo.utils.MessageUtil.storeMultipartMsg(mmsg);
    }
    xLogger.fine("Exiting waitOrProcess with null");
    return null;
  }

  // Process login command
  @SuppressWarnings("unchecked")
  private OutputMessageBean processLogin(InputMessageBean iBean) throws MessageHandlingException {
    xLogger.fine("Entered processLogin");
    AuthenticateOutput ao = null;
    try {
      // Get the authentication inputs
      AuthenticateInput ai = (AuthenticateInput) iBean;
      // Form the REST API URL
      String url = getUrlBase() + "/" + LOGIN;
      Map<String, String> params = new HashMap<String, String>();
      params.put(RestConstantsZ.ACTION, RestConstantsZ.ACTION_LOGIN);
      params.put(RestConstantsZ.USER_ID, ai.getUserId());
      params.put(RestConstantsZ.PASSWORD, ai.getPassword());
      if (ai.isMinResponse()) {
        params.put(RestConstantsZ.MIN_RESPONSE, "1");
      }
      String locale = ai.getLocale();
      if (locale != null && !locale.isEmpty()) {
        params.put(RestConstantsZ.LOCALE, locale);
      }
      // Check whether onlyAuthenticate flag is set
      if (ai.isOnlyAuthenticate()) {
        params.put(RestConstantsZ.ONLY_AUTHENTICATE, "true");
      }
      // Invoke REST API and get the response
      xLogger.fine("Invoking REST API: url = {0}, params = {1}", url, params);
      String response = invokeREST(url, params, false);
      xLogger.fine("REST response: {0}", response);
      // Get JSON response back
      ao =
          new AuthenticateOutput(
              Constants.LANG_DEFAULT); // TODO: only protocol exceptions will be in en, all other server-side messages will be in local language
      ao.fromJSONString(response);
      if (ao.getStatus()) {
        // Replace material Ids with short-codes, before sending
        Vector<Hashtable<String, Object>> kiosks = ao.getKiosks();
        if (kiosks != null) {
          Enumeration<Hashtable<String, Object>> en = kiosks.elements();
          while (en.hasMoreElements()) {
            Hashtable<String, Object> kiosk = en.nextElement();
            replaceIdsWithShortcodes(
                (Vector<Hashtable<String, String>>) kiosk.get(JsonTagsZ.MATERIALS));
          }
        }
      }
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    }
    xLogger.fine("Exting processLogin");
    return ao;
  }

  // Process the get inventory message
  @SuppressWarnings("unchecked")
  private OutputMessageBean processGetInventory(InputMessageBean iBean)
      throws MessageHandlingException {
    xLogger.fine("Entered processGetInventory");
    GetInventoryOutput giOut = null;
    try {
      // Get the GetInventory data
      GetInventoryInput giIn = (GetInventoryInput) iBean;
      String userId = giIn.getUserId();
      String password = giIn.getPassword();
      String kioskId = giIn.getKioskId();
      // Get the REST API URL
      String url = getUrlBase() + "/" + INVENTORY;
      Map<String, String> params = new HashMap<String, String>();
      params.put(RestConstantsZ.ACTION, RestConstantsZ.ACTION_GETINVENTORY);
      params.put(RestConstantsZ.USER_ID, userId);
      params.put(RestConstantsZ.PASSWORD, password);
      if (giIn.isOnlyStock()) {
        params.put(RestConstantsZ.FILTER, RestConstantsZ.FILTER_ONLYSTOCK);
      }
      if (kioskId != null && !kioskId.isEmpty()) {
        params.put(RestConstantsZ.KIOSK_ID, kioskId);
      }
      // Invoke REST API
      String response = invokeREST(url, params, false);
      xLogger.fine("REST response: {0}", response);
      // Process JSON response
      giOut =
          new GetInventoryOutput(
              Constants.LANG_DEFAULT); // TODO: only protocol exceptions will be in en, all other server-side messages will be in local language
      giOut.fromJSONString(response);
      if (giOut.getStatus()) {
        giOut.setOnlyStock(giIn.isOnlyStock());
        // Convert materialIds to short-codes
        replaceIdsWithShortcodes((Vector<Hashtable<String, String>>) giOut.getMaterials());
      }
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    }
    xLogger.fine("Exting processGetInventory");
    return giOut;
  }

  // Process update inventory request
  @SuppressWarnings("unchecked")
  private OutputMessageBean processUpdateInventory(InputMessageBean iBean)
      throws MessageHandlingException {
    xLogger.fine("Entered processUpdateInventory");
    UpdateInventoryOutput uiOut = null;
    try {
      // Get the input object
      UpdateInventoryInput uiIn = (UpdateInventoryInput) iBean;
      // Translate short-codes to materialIds
      replaceShortcodesWithIds(uiIn.getMaterials(), uiIn.getUserId());
      // Form the REST API URL
      String url = getUrlBase() + "/" + INVENTORY;
      Map<String, String> params = new HashMap<String, String>();
      params.put(RestConstantsZ.ACTION, RestConstantsZ.ACTION_UPDINVENTORY);
      params.put(RestConstantsZ.TRANS_TYPE, uiIn.getType());
      params.put(RestConstantsZ.PASSWORD, uiIn.getPassword());
      String jsonString = uiIn.toJSONString();
      xLogger.fine("JSON String: {0}", jsonString);
      params.put(RestConstantsZ.JSON_STRING, jsonString);
      // Invoke REST API
      String response = null;
      try {
        response =
            invokeREST(url, params, true); // try this only once, given this is a write operation
        xLogger.fine("REST response: {0}", response);
        // Process response
        uiOut =
            new UpdateInventoryOutput(
                Constants.LANG_DEFAULT); // TODO: only protocol exceptions will be in en, all other server-side messages will be in local language
        uiOut.fromJSONString(response);
        if (uiOut.getStatus()) {
          // Replace materialIds with short-codes
          replaceIdsWithShortcodes(uiOut.getMaterials());
        }
      } catch (IOException e) {
        // Send back a message to the user asking him to verify and try again
        String locale = Constants.LANG_DEFAULT;
        if (user != null) {
          locale = user.getLocale().toString();
        }
        uiOut =
            new UpdateInventoryOutput(false, null,
                backendMessages.getString("error.incompleterequest"), null, null, null, locale,
                RESTUtil.VERSION_01);
      }
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    }
    xLogger.fine("Exiting processUpdateInventory");
    return uiOut;
  }

  // Process update order request
  @SuppressWarnings("unchecked")
  private OutputMessageBean processUpdateOrder(InputMessageBean iBean)
      throws MessageHandlingException {
    xLogger.fine("Entered processUpdateOrder");
    OrderOutput oOut = null;
    try {
      // Get the input object
      UpdateOrderInput uoIn = (UpdateOrderInput) iBean;
      // Translate short-codes to materialIds
      replaceShortcodesWithIds(uoIn.getMaterials(), uoIn.getUserId());
      xLogger.fine("Materials with long codes: {0}", uoIn.getMaterials());
      // Form the REST API URL
      String url = getUrlBase() + "/" + ORDER;
      Map<String, String> params = new HashMap<String, String>();
      params.put(RestConstantsZ.ACTION, RestConstantsZ.ACTION_UPDATEORDER);
      params.put(RestConstantsZ.TRANS_TYPE, uoIn.getType());
      params.put(RestConstantsZ.PASSWORD, uoIn.getPassword());
      String jsonString = uoIn.toJSONString();
      xLogger.fine("JSON String: {0}", jsonString);
      params.put(RestConstantsZ.JSON_STRING, jsonString);
      // Invoke REST API
      String response = null;
      try {
        response = invokeREST(url, params, true); // call it only once, given it is a write op.
        xLogger.fine("REST response: {0}", response);
        // Process response
        oOut =
            new OrderOutput(
                Constants.LANG_DEFAULT); // TODO: only protocol exceptions will be in en, all other server-side messages will be in local language
        oOut.fromJSONString(response);
        if (oOut.getStatus()) {
          // Replace materialIds with short-codes
          replaceIdsWithShortcodes(oOut.getMaterials());
        }
      } catch (IOException e) {
        // Send back a message to the user asking him to verify and try again
        String locale = Constants.LANG_DEFAULT;
        if (user != null) {
          locale = user.getLocale().toString();
        }
        oOut =
            new OrderOutput(false, null, backendMessages.getString("error.incompleterequest"),
                locale, RESTUtil.VERSION_01);
      }
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    }
    xLogger.fine("Exiting processUpdateOrder");
    return oOut;
  }

  // Process the get orders request
  @SuppressWarnings("unchecked")
  private OutputMessageBean processGetOrders(InputMessageBean iBean)
      throws MessageHandlingException {
    xLogger.fine("Entered processGetOrders");
    GetOrdersOutput gosOut = null;
    try {
      // Get the GetInventory data
      GetOrdersInput gosIn = (GetOrdersInput) iBean;
      // Get the REST API URL
      String url = getUrlBase() + "/" + ORDER;
      Map<String, String> params = new HashMap<String, String>();
      params.put(RestConstantsZ.ACTION, RestConstantsZ.ACTION_GETORDERS);
      params.put(RestConstantsZ.USER_ID, gosIn.getUserId());
      params.put(RestConstantsZ.PASSWORD, gosIn.getPassword());
      params.put(RestConstantsZ.KIOSK_ID, gosIn.getKioskId());
      params.put(RestConstantsZ.NUM_RESULTS, String.valueOf(gosIn.getMaxResults()));
      params.put(RestConstantsZ.LOADALL, gosIn.getLoad());
      String otype = gosIn.getOrderType();
      if (otype != null && !otype.isEmpty()) {
        params.put(RestConstantsZ.ORDER_TYPE, gosIn.getOrderType());
      }
      String status = gosIn.getStatus();
      if (status != null && !status.isEmpty()) {
        params.put(RestConstantsZ.STATUS, status);
      }
      // Invoke REST API
      String response = invokeREST(url, params, false);
      xLogger.fine("REST response: {0}", response);
      // Process JSON response
      gosOut =
          new GetOrdersOutput(
              Constants.LANG_DEFAULT); // TODO: only protocol exceptions will be in en, all other server-side messages will be in local language
      gosOut.fromJSONString(response);
      xLogger.fine("Got GetOrdersOutput from JSON String...");
      // Convert materialIds to short-codes
      if (gosOut.hasOrders()) {
        Vector<Hashtable<String, Object>> orders = gosOut.getOrders();
        xLogger.fine("orders: {0}", orders);
        Enumeration<Hashtable<String, Object>> en = orders.elements();
        while (en.hasMoreElements()) {
          Hashtable<String, Object> o = en.nextElement();
          replaceIdsWithShortcodes((Vector<Hashtable<String, String>>) o.get(JsonTagsZ.MATERIALS));
        }
      } else {
        replaceIdsWithShortcodes((Vector<Hashtable<String, String>>) gosOut.getMaterials());
      }
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    }
    xLogger.fine("Exting processGetOrders");
    return gosOut;
  }

  // Process get or cancel order request
  // TODO: Cancel Order deprecated; used processUpdateOrderStatus instead
  @SuppressWarnings("unchecked")
  private OutputMessageBean processGetOrCancelOrder(InputMessageBean iBean, String cmd)
      throws MessageHandlingException {
    xLogger.fine("Entered processGetOrCancelOrder");
    OrderOutput oOut = null;
    try {
      // Get the input object
      GetOrderInput oIn = (GetOrderInput) iBean;
      // Form the REST API URL
      String url = getUrlBase() + "/" + ORDER;
      Map<String, String> params = new HashMap<String, String>();
      params.put(RestConstantsZ.ACTION, cmd);
      params.put(RestConstantsZ.USER_ID, oIn.getUserId());
      params.put(RestConstantsZ.PASSWORD, oIn.getPassword());
      params.put(RestConstantsZ.ORDER_ID, oIn.getOrderId());
      // Invoke REST API
      String response = invokeREST(url, params, false);
      xLogger.fine("REST response: {0}", response);
      // Process response
      oOut =
          new OrderOutput(
              Constants.LANG_DEFAULT); // TODO: only protocol exceptions will be in en, all other server-side messages will be in local language
      oOut.fromJSONString(response);
      // Replace materialIds with short-codes
      replaceIdsWithShortcodes(oOut.getMaterials());
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    }
    xLogger.fine("Exiting processGetOrCancelOrder");
    return oOut;
  }

  // Update the status of an order
  @SuppressWarnings("unchecked")
  private OutputMessageBean processUpdateOrderStatus(InputMessageBean iBean)
      throws MessageHandlingException {
    xLogger.fine("Entered processUpdateOrderStatus");
    OrderOutput oOut = null;
    try {
      // Get the input object
      UpdateOrderStatusInput uosIn = (UpdateOrderStatusInput) iBean;
      // Form the REST API URL
      String url = getUrlBase() + "/" + ORDER;
      Map<String, String> params = new HashMap<String, String>();
      params.put(RestConstantsZ.ACTION, RestConstantsZ.ACTION_UPDATEORDERSTATUS);
      params.put(RestConstantsZ.USER_ID, uosIn.getUserId());
      params.put(RestConstantsZ.PASSWORD, uosIn.getPassword());
      params.put(RestConstantsZ.ORDER_ID, uosIn.getOrderId());
      params.put(RestConstantsZ.STATUS, uosIn.getStatus());
      // Invoke REST API
      String response = null;
      try {
        response = invokeREST(url, params, true);
        xLogger.fine("REST response: {0}", response);
        // Process response
        oOut =
            new OrderOutput(
                Constants.LANG_DEFAULT); // TODO: only protocol exceptions will be in en, all other server-side messages will be in local language
        oOut.fromJSONString(response);
        // Replace materialIds with short-codes
        if (oOut.getStatus()) {
          replaceIdsWithShortcodes(oOut.getMaterials());
        }
      } catch (IOException e) {
        // Send back a message to the user asking him to verify and try again
        String locale = Constants.LANG_DEFAULT;
        if (user != null) {
          locale = user.getLocale().toString();
        }
        oOut =
            new OrderOutput(false, null, backendMessages.getString("error.incompleterequest"),
                locale, RESTUtil.VERSION_01);
      }
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    }
    xLogger.fine("Exiting processUpdateOrderStatus");
    return oOut;
  }

  // Persist the response messages for later use
  private void storeResponseMessages(String address, String messageId, Vector<String> messages,
                                     String countryCode) throws MessageHandlingException {
    xLogger.fine("Entered storeResponseMessages");
    IMultipartMsg mmsg = JDOUtils.createInstance(IMultipartMsg.class);
    mmsg.setId(JDOUtils.createMultipartMsgKey(address, messageId));
    mmsg.setTimestamp(new Date());
    mmsg.setCountry(countryCode);
    Iterator<String> it = messages.iterator();
    while (it.hasNext()) {
      mmsg.addMessage(it.next());
    }
    com.logistimo.utils.MessageUtil.storeMultipartMsg(mmsg);
    xLogger.fine("Exiting storeResponseMessages");
  }

  // Get the response messages stored previously
  private Vector<String> getResponseMessages(String address, String messageId)
      throws ObjectNotFoundException, MessageHandlingException {
    xLogger.fine("Entered getResponseMessages: address = {0}, messageId = {1}", address, messageId);
    String id = JDOUtils.createMultipartMsgKey(address, messageId);
    IMultipartMsg mmsg = com.logistimo.utils.MessageUtil.getMultipartMsg(id);
    // Set the country code (for use in determine g/w provider)
    countryCode = mmsg.getCountry();
    xLogger.fine("Got multi-part msg with ID {0}: {1}", id,
        (mmsg == null ? "NULL" : mmsg.getMessages()));
    List<String> msgList = mmsg.getMessages();
    if (msgList == null || msgList.isEmpty()) {
      throw new MessageHandlingException("No messages in multi-part message with Id: " + id);
    }
    Vector<String> messages = new Vector<String>();
    Iterator<String> it = msgList.iterator();
    while (it.hasNext()) {
      messages.add(it.next());
    }
    xLogger.fine("Exiting getResponseMessages");
    return messages;
  }

  // Convert from string to vector
  private Vector<String> toVector(List<String> list) {
    Vector<String> v = new Vector<String>();
    Iterator<String> it = list.iterator();
    while (it.hasNext()) {
      v.add(it.next());
    }
    return v;
  }

  // Replace materialIds with material short-codes
  private void replaceIdsWithShortcodes(Vector<Hashtable<String, String>> materials) {
    xLogger.fine("Entered replaceIdsWithShortcodes");
    if (materials == null || materials.isEmpty()) {
      return;
    }
    Enumeration<Hashtable<String, String>> en = materials.elements();
    while (en.hasMoreElements()) {
      Hashtable<String, String> ht = en.nextElement();
      Long materialId = Long.valueOf(ht.get(JsonTagsZ.MATERIAL_ID));
      try {
        IMaterial m = mcs.getMaterial(materialId);
        String shortCode = m.getShortCode();
        String shortName = m.getShortName();
        if (shortCode != null) {
          ht.put(JsonTagsZ.MATERIAL_ID, shortCode);
          xLogger.fine("replaceIdsWithShortcodes: replaced {0} with {1}", materialId, shortCode);
        }
        if (shortName != null && !shortName.isEmpty()) {
          ht.put(JsonTagsZ.NAME, shortName);
        }
      } catch (ServiceException e) {
        xLogger.warn("Unable to get material with ID: {0}", materialId);
      }
    }
    xLogger.fine("Exiting replaceIdsWithShortcodes");
  }

  // Replace the short codes with material Ids
  private void replaceShortcodesWithIds(Vector<Hashtable<String, String>> materials,
                                        String userId) {
    if (materials == null || materials.isEmpty()) {
      return;
    }
    // Get the domain Id
    try {
      IUserAccount u = as.getUserAccount(userId);
      Long domainId = u.getDomainId();
      Enumeration<Hashtable<String, String>> en = materials.elements();
      while (en.hasMoreElements()) {
        Hashtable<String, String> material = en.nextElement();
        String shortCode = material.get(JsonTagsZ.MATERIAL_ID);
        Long materialId = null;
        if (shortCode != null) {
          materialId = mcs.getMaterialId(domainId, shortCode);
        }
        if (materialId != null) {
          material.put(JsonTagsZ.MATERIAL_ID, materialId.toString());
        }
      }
    } catch (ServiceException e) {
      xLogger.severe("ServiceException: {0}", e.getMessage());
    } catch (ObjectNotFoundException e) {
      xLogger.severe("User not found: {0}", e.getMessage());
    }
  }

  // Invoke rest API
  private String invokeREST(String url, Map<String, String> params, boolean tryOnlyOnce)
      throws IOException {
    String response = null;
    int numTrials = NUM_RETRIALS;
    if (tryOnlyOnce) {
      numTrials = 1;
    }
    for (int i = 0; i < numTrials; i++) {
      try {
        response =
            HttpUtil.post(url, params, null); // post, given only post is supported from task queue
        break;
      } catch (IOException e) {
        xLogger.warn("IOException while invoking REST: {0}", e.getMessage());
        if (tryOnlyOnce) {
          throw new IOException(e.getMessage());
        }
      }
    }
    if (response == null) {
      throw new IOException("Unable to get a response from REST API");
    }

    return response;
  }

  // Initialize the message service
  private void initMessageService(String userId) throws MessageHandlingException {
    // Set the user details - country code, language, domainId, etc. (required for SMS sending) [TODO: later get this from the header itself]
    try {
      setUserDetails(userId);
      // Set the message service
      msgservice = getMessageService();
      xLogger.fine("Got message service: {0}", msgservice);
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    }
  }

  // Check if the source phone is same as user's phone
  private boolean isPhoneValid() {
    if (user == null) {
      return false;
    }
    xLogger.fine("IsPhoneValid: address = {0}", address);
    return address.equals(msgservice.getFormattedAddress(user.getMobilePhoneNumber()));
  }

  // Get the error messages in the required format
  @SuppressWarnings("rawtypes")
  private Vector getErrorMessages(String version, String message, String msgId, String locale)
      throws ProtocolException {
    xLogger.fine("Entered getErrorMessages");
    // Use any of the proto. output classes for error
    AuthenticateOutput
        ao =
        new AuthenticateOutput(false, null, null, null, null, message, locale, RESTUtil.VERSION_01);
    ao.setMessageId(msgId);
    ao.setVersion(version);
    xLogger.fine("Exiting getErrorMessages");
    return ao.toMessageString();
  }
}
