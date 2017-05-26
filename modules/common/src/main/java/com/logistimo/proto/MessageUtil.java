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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;


import org.apache.commons.codec.binary.Hex;
import com.logistimo.proto.utils.StringTokenizer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

/**
 * Helps in creating messages for SMS transmission
 *
 * @author Arun
 */
public class MessageUtil {

  // Dummy currency
  public static final String DUMMY = "*";
  // Field including flags
  public static final int QUANTITY_ONLY = 0;
  public static final int ALL_FIELDS = 1;
  public static final int QUANTITY_AND_PRICE = 2;
  // Charset default
  public static final String CHARSET_DEFAULT = "UTF-8";
  // Separator
  private static final String SEP = "$"; // material separator
  private static final String SEP2 = " "; // order separator
  // Max. message size
  private static final int MAX_SIZE = 151; // 160 max ascii chars minus 9 chars for msg header

  // Create a string representation of the material metadata
  public static String getMaterialsString(Vector materials, int includeFields,
                                          boolean excludeTime) {
    if (materials == null || materials.isEmpty()) {
      return "0";
    }
    ///return "";
    String msg = materials.size() + " ";
    Enumeration en = materials.elements();
    while (en.hasMoreElements()) {
      Hashtable material = (Hashtable) en.nextElement();
      msg += (String) material.get(JsonTagsZ.MATERIAL_ID); // material short-code
      msg += SEP + (String) material.get(JsonTagsZ.QUANTITY); // stock level
      String time = (String) material.get(JsonTagsZ.TIMESTAMP);
      msg += SEP;
      if (!excludeTime && time != null && time.length() > 0) {
        msg += encode(time);
      } else {
        msg += DUMMY;
      }
      String value = null;
      if (includeFields == QUANTITY_AND_PRICE || includeFields == ALL_FIELDS) {
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.RETAILER_PRICE)) != null && !value
            .equals("")) // retailer price
        {
          msg += value;
        } else {
          msg += MessageUtil.DUMMY;
        }
      }
      if (includeFields == ALL_FIELDS) {
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.NAME)) != null && !value.equals("")) {
          msg += encode(value); // material name
        } else {
          msg += MessageUtil.DUMMY;
        }
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.TAGS)) != null && !value.equals("")) // tags
        {
          msg += encode(value);
        } else {
          msg += MessageUtil.DUMMY;
        }
        // Optimization parameters, if any
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.CR_DAILY)) != null && !value
            .equals("")) // daily consumption rate
        {
          msg += value;
        } else {
          msg += MessageUtil.DUMMY;
        }
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.CR_WEEKLY)) != null && !value
            .equals("")) // weekly consumption rate
        {
          msg += value;
        } else {
          msg += MessageUtil.DUMMY;
        }
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.CR_MONTHLY)) != null && !value
            .equals("")) // monthly consumption rate
        {
          msg += value;
        } else {
          msg += MessageUtil.DUMMY;
        }
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.DEMAND_FORECAST)) != null && !value
            .equals("")) // demand forecast
        {
          msg += value;
        } else {
          msg += MessageUtil.DUMMY;
        }
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.ORDER_PERIODICITY)) != null && !value
            .equals("")) // order periodicity
        {
          msg += value;
        } else {
          msg += MessageUtil.DUMMY;
        }
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.EOQ)) != null && !value
            .equals("")) // economic order quantity
        {
          msg += value;
        } else {
          msg += MessageUtil.DUMMY;
        }
        // Add type
        msg += SEP;
        if ((value = (String) material.get(JsonTagsZ.DATA_TYPE)) != null && !value
            .equals("")) // material data type
        {
          msg += value;
        } else {
          msg += MessageUtil.DUMMY;
        }
      }
      // Add reason code, if present
      String reason = (String) material.get(JsonTagsZ.REASON);
      if (reason != null && !reason.equals("")) {
        msg += SEP + encode(reason);
      } else {
        msg += SEP + DUMMY;
      }
      // Add space separator for next material
      if (en.hasMoreElements()) {
        msg += " ";
      }
    }
    return msg;
  }

  // Get the material metadata from its string representation
  public static Vector getMaterialsVector(String materialsString, Tags tags) {
    if (materialsString == null || materialsString.equals("")) {
      return null;
    }
    // Get the number of materials
    StringTokenizer st = new StringTokenizer(materialsString, " ");
    return getMaterialsVector(st, tags);
  }

  public static Vector getMaterialsVector(StringTokenizer st, Tags tags) {
    Vector v = new Vector();
    // Ignore number of materials
    int numMaterials = 0;
    if (st.hasNext()) {
      numMaterials = Integer.parseInt(st.nextToken());
    }
    int i = 0;
    while (st.hasMoreTokens() && i < numMaterials) {
      String materialStr = st.nextToken();
      StringTokenizer stm = new StringTokenizer(materialStr, SEP);
      Hashtable m = new Hashtable();
      if (stm.hasMoreTokens()) {
        m.put(JsonTagsZ.MATERIAL_ID, stm.nextToken());
      }
      if (stm.hasMoreTokens()) {
        m.put(JsonTagsZ.QUANTITY, stm.nextToken());
      }
      if (stm.hasMoreElements()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.TIMESTAMP, decode(val));
        }
      }
      // Price
      if (stm.hasMoreTokens()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.RETAILER_PRICE, val);
        }
      }
      // Check if reason is present here, in case only QUANTITY_AND_PRICE or no extra fields was set
      int numTokens = stm.countTokens();
      if (numTokens == 4 || numTokens == 5) {
        // Reason
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.REASON, decode(val));
        }
      }
      // Name
      if (stm.hasMoreTokens()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.NAME, decode(val));
        }
      }
      // Tags
      if (stm.hasMoreTokens()) {
        String val = stm.nextToken();
        if (DUMMY.equals(val) || val.equals("")) {
          val = (tags != null ? tags.NOTAGS : "");
        } else {
          val = decode(val);
        }
        m.put(JsonTagsZ.TAGS, val);
        if (tags != null) {
          JsonUtil.updateTagMap(tags, val, m);
        }
      }
      // Optimization parameters, if any
      // Daily consumption rate
      if (stm.hasMoreElements()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.CR_DAILY, val);
        }
      }
      // Weekly consumption rate
      if (stm.hasMoreElements()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.CR_WEEKLY, val);
        }
      }
      // Monthly consumption rate
      if (stm.hasMoreElements()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.CR_MONTHLY, val);
        }
      }
      // Demand forecast
      if (stm.hasMoreElements()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.DEMAND_FORECAST, val);
        }
      }
      // Order periodicity
      if (stm.hasMoreElements()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.ORDER_PERIODICITY, val);
        }
      }
      // Economic order quantity
      if (stm.hasMoreElements()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.EOQ, val);
        }
      }
      // Material data type (e.g. binary)
      if (stm.hasMoreElements()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.DATA_TYPE, val);
        }
      }
      // Reason code, if any (present here if ALL_FIELDS was selected when set)
      if (stm.hasMoreElements()) {
        String val = stm.nextToken();
        if (val != null && val.length() > 0 && !DUMMY.equals(val)) {
          m.put(JsonTagsZ.REASON, decode(val));
        }
      }
      // Add to vector
      v.addElement(m);
      i++;
    }
    // Clear tags, if only 1 tag is present
    if (tags != null && tags.hasOnlyNOTAGStag()) {
      tags.clear();
    }

    return v;
  }

  // Get the vendors string
  public static String getRelatedKioskString(Vector vendors) {
    String msg = "";
    if (vendors == null || vendors.isEmpty()) {
      return "0";
    }
    ////return null;
    Enumeration en = vendors.elements();
    msg = String.valueOf(vendors.size()); // no. of related kiosks
    while (en.hasMoreElements()) {
      Hashtable vendor = (Hashtable) en.nextElement();
      msg += " " + (String) vendor.get(JsonTagsZ.KIOSK_ID) +
          " " + encode((String) vendor.get(JsonTagsZ.NAME)) +
          " " + encode((String) vendor.get(JsonTagsZ.CITY));
    }
    return msg;
  }

  // Get the Vector of vendors from a kiosk JSON
  public static Vector getRelatedKioskVector(StringTokenizer st) {
    Vector vendors = new Vector();
    int size = Integer.parseInt((String) st.nextElement()); // no. of related kiosks
    int i = 0;
    while (st.hasMoreElements() && i < size) {
      Hashtable vendor = new Hashtable();
      vendor.put(JsonTagsZ.KIOSK_ID, (String) st.nextElement());
      vendor.put(JsonTagsZ.NAME, decode((String) st.nextElement()));
      vendor.put(JsonTagsZ.CITY, decode((String) st.nextElement()));
      // Add to vector
      vendors.addElement(vendor);
      ++i;
    }
    return vendors;
  }

  // Clean up messages to ensure only messages of the given message Id exists
  public static Vector getCleanMessages(Vector messages, String messageId)
      throws ProtocolException {
    if (messages == null || messageId == null) {
      return messages;
    }
    Vector cleanMessages = new Vector();
    Enumeration en = messages.elements();
    while (en.hasMoreElements()) {
      String msg = (String) en.nextElement();
      MessageHeader hd = new MessageHeader(msg);
      if (messageId.equals(hd.getMessageId())) {
        cleanMessages.addElement(msg);
      }
    }
    return cleanMessages;
  }

  // Get the progress in the form of <num-msgs-received>/<total-expected-messages>
  public static String getProgress(Vector messages) throws ProtocolException {
    String progress = "";
    if (messages == null || messages.isEmpty()) {
      return null;
    }
    String firstMesg = (String) messages.elementAt(0);
    MessageHeader header = new MessageHeader(firstMesg);
    progress = messages.size() + "/" + header.getNumberOfMessages();
    return progress;
  }

  // Split a given message into multiple messages of relevant sizes, each with a header
  public static Vector split(String message) throws ProtocolException {
    return split(message, null);
  }

  public static Vector split(String message, String msgId) throws ProtocolException {
    try {
      Vector v = new Vector();
      StringTokenizer st = new StringTokenizer(message, " ");
      String msg = "";
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if ((msg + " " + token).length() <= MAX_SIZE) {
          if (!msg.equals("")) {
            msg += " ";
          }
          msg += token;
        } else {
          v.addElement(msg);
          msg = token;
        }
      }
      // Add the last message
      v.addElement(msg);
      // Create message Id
      if (msgId == null || msgId.length() == 0) {
        msgId = createMessageId();
      }
      // Add headers to the messages
      return addHeaders(v, msgId);
    } catch (Exception e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  // Assemble a split message
  public static String assemble(Vector msgs) throws ProtocolException {
    if (msgs == null) {
      return null;
    }
    String msg = "";
    String msgId = null;
    Hashtable ht = new Hashtable();
    int numMsgs = 0;
    Enumeration en = msgs.elements();
    while (en.hasMoreElements()) {
      String m = (String) en.nextElement();
      StringTokenizer st = new StringTokenizer(m, " ");
      String slNo = null;
      if (st.hasMoreTokens()) {
        slNo = st.nextToken();
      }
      if (st.hasMoreTokens()) {
        numMsgs = Integer.parseInt(st.nextToken());
      }
      if (st.hasMoreTokens()) {
        if (msgId == null) {
          msgId = st.nextToken();
        } else if (!msgId.equals(st.nextToken())) {
          throw new ProtocolException(
              "Message Id mismatch - found a messages with different ID other than " + msgId);
        }
      }
      // Form the string
      msg += tokensToString(st);
      // Add the header-stripped message to a hastable, indexed by its msgNo
      ht.put(slNo, msg);
      msg = ""; // reset msg
    }
    // Assemble message in sequence
    int size = ht.size();
    if (size != numMsgs) {
      throw new ProtocolException(
          "Number of messages (" + size + ") does not match expected number of messages (" + numMsgs
              + ")");
    }
    msg = "";
    for (int i = 1; i <= size; i++) {
      String m = (String) ht.get(String.valueOf(i));
      if (m != null) {
        msg += m;
      }
      if (i != size) {
        msg += " ";
      }
    }
    return msg;
  }

  // Check whether all messages are present - i.e. all messages are received
  public static boolean isComplete(Vector messages, String messageId) throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      return false;
    }
    int size = messages.size();
    int expectedSize = 0;
    String expectedMsgId = messageId;
    MessageHeader header = new MessageHeader((String) messages.elementAt(0));
    if (messageId == null) {
      expectedMsgId = header.getMessageId();
    }
    Enumeration en = messages.elements();
    while (en.hasMoreElements()) {
      String msg = (String) en.nextElement();
      header = new MessageHeader(msg);
      if (expectedSize == 0 && header.getMessageId().equals(expectedMsgId)) {
        expectedSize = header.getNumberOfMessages();
      }
      if (!header.getMessageId().equals(expectedMsgId)) {
        size--; // decrement size
        continue; // ignore this message and go to the next one
      }
    }
    return messages.size() > 0 && expectedSize == size;
  }

  // Get the message header of the message set
  public static MessageHeader getMessageHeader(Vector messages) throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      throw new ProtocolException("No messages specified");
    }
    return new MessageHeader((String) messages.elementAt(0));
  }

  // Get the missing message serial nos., as a CSV
  public static String getMissingMessageNos(Vector messages, String outstandingMsgId)
      throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      if (outstandingMsgId == null || outstandingMsgId.length() == 0) {
        return null;
      } else {
        return outstandingMsgId;
      }
    }
    MessageHeader hd = new MessageHeader((String) messages.elementAt(0));
    String msgId = hd.getMessageId();
    int numMsgs = hd.getNumberOfMessages();
    if (numMsgs == messages.size()) {
      return ""; // no messages missing
    }
    int[] msgNos = new int[numMsgs];
    // Fill the array with 0s
    for (int i = 0; i < msgNos.length; i++) {
      msgNos[i] = 0;
    }
    // For all msg. nos. that are present, fill in 1s
    for (int i = 0; i < messages.size(); i++) {
      hd = new MessageHeader((String) messages.elementAt(i));
      if (msgId.equals(hd.getMessageId())) {
        msgNos[hd.getMessageNo() - 1] =
            1; // note: the index of message nos. starts from 1; so it is necessary to minus 1 here
      }
    }
    // Now, the array indicies with 0s are the missing message nos.
    String missingMsgNos = "";
    for (int i = 0; i < numMsgs; i++) {
      if (msgNos[i] == 0) {
        if (missingMsgNos.length() > 0) {
          missingMsgNos += ",";
        }
        missingMsgNos +=
            String.valueOf(i
                + 1); // note: the index of messages starts from 1, whereas array index starts from 0; so add 1 to index here
      }
    }
    return msgId + ";" + missingMsgNos;
  }

  // Get a MessageHeader based on missing message numbers
  public static MessageHeader[] getResponseMessageHeaders(String messageNumbersCSV) {
    if (messageNumbersCSV == null || messageNumbersCSV.length() == 0) {
      return null;
    }
    MessageHeader[] msgHeaders = null;
    StringTokenizer st = new StringTokenizer(messageNumbersCSV, ";"); // splits msgId and msgNos
    String msgId = st.nextToken();
    if (st.hasMoreTokens()) {
      String msgNos = st.nextToken();
      st = new StringTokenizer(msgNos, ","); // splits msgNos further
      int numTokens = st.countTokens();
      msgHeaders = new MessageHeader[numTokens];
      for (int i = 0; i < numTokens; i++) {
        msgHeaders[i] = new MessageHeader(msgId, Integer.parseInt(st.nextToken()), 0);
      }
    } else {
      msgHeaders = new MessageHeader[1];
      msgHeaders[0] =
          new MessageHeader(msgId, -1,
              0); // seq. no. of -1 implies that all messages are to be sent back
    }
    return msgHeaders;
  }

  // Filter messages to contain only a given set of msg. numbers
  public static Vector filterMessages(Vector messages, MessageHeader[] messageHeaders)
      throws ProtocolException {
    if (messages == null || messageHeaders == null || messageHeaders.length == 0) {
      return messages;
    }
    if (messageHeaders.length == 1
        && messageHeaders[0].getMessageNo() < 0) { // implies send all messages, no filtering
      return messages;
    }
    // Get a vector of message nos.
    Vector msgNos = new Vector();
    for (int i = 0; i < messageHeaders.length; i++) {
      msgNos.addElement(new Integer(messageHeaders[i].getMessageNo()));
    }
    // Get only messages which have a msgNo that is part of msgNos
    Vector filteredMsgs = new Vector();
    Enumeration en = messages.elements();
    while (en.hasMoreElements()) {
      String msg = (String) en.nextElement();
      MessageHeader hd = new MessageHeader(msg);
      if (msgNos.contains(new Integer(hd.getMessageNo()))) {
        filteredMsgs.addElement(msg);
      }
    }
    return filteredMsgs;
  }

  // Get the orders string
  public static String getOrderString(Hashtable order, String version) throws ProtocolException {
    if (order == null || order.isEmpty()) {
      throw new ProtocolException("No order specified");
    }
    String msg = (String) order.get(JsonTagsZ.TRACKING_ID) + SEP2 +
        (String) order.get(JsonTagsZ.ORDER_STATUS) + SEP2 +
        (String) order.get(JsonTagsZ.QUANTITY) + SEP2 +
        encode((String) order.get(JsonTagsZ.TIMESTAMP));
    String value = null;
    if ((value = (String) order.get(JsonTagsZ.UPDATED_TIME)) != null && !value.equals("")) {
      msg += SEP2 + encode(value);
    } else {
      msg += SEP2 + DUMMY;
    }
    if ((value = (String) order.get(JsonTagsZ.TOTAL_PRICE)) != null && !value.equals("")) {
      msg += SEP2 + value;
    } else {
      msg += SEP2 + DUMMY;
    }
    if ((value = (String) order.get(JsonTagsZ.CURRENCY)) != null && !value.equals("")) {
      msg += SEP2 + value;
    } else {
      msg += SEP2 + DUMMY;
    }
    if ((value = (String) order.get(JsonTagsZ.MESSAGE)) != null && !value.equals("")) {
      msg += SEP2 + encode(value);
    } else {
      msg += SEP2 + DUMMY;
    }
    if (Integer.parseInt(version) >= Integer
        .parseInt(MessageHeader.VERSION02)) { // MessageHeader.VERSION02.equals( version ) ) {
      // Vendor ID
      if ((value = (String) order.get(JsonTagsZ.VENDORID)) != null && !value.equals("")) {
        msg += SEP2 + value;
      } else {
        msg += SEP2 + DUMMY;
      }
    }
    if (Integer.parseInt(version) >= Integer
        .parseInt(MessageHeader.VERSION05)) { // version 05 onwards
      // Discount
      if ((value = (String) order.get(JsonTagsZ.DISCOUNT)) != null && !value.equals("")) {
        msg += SEP2 + value;
      } else {
        msg += SEP2 + DUMMY;
      }
    }
    if (Integer.parseInt(version) >= Integer
        .parseInt(MessageHeader.VERSION06)) { // version 06 onwards
      // Kiosk Id (Customer)
      if ((value = (String) order.get(JsonTagsZ.KIOSK_ID)) != null && !value.equals("")) {
        msg += SEP2 + value;
      } else {
        msg += SEP2 + DUMMY;
      }
    }
    // Add materials
    msg +=
        SEP2 + getMaterialsString((Vector) order.get(JsonTagsZ.MATERIALS), QUANTITY_AND_PRICE,
            true);
    // Split message
    return msg;
  }

  // Get an order object from string
  public static Hashtable getOrderObject(String order, String version) throws ProtocolException {
    StringTokenizer st = new StringTokenizer(order, SEP2);
    return getOrderObject(st, version);
  }

  public static Hashtable getOrderObject(StringTokenizer st, String version)
      throws ProtocolException {
    Hashtable o = new Hashtable();
    if (st.countTokens() < 5) {
      throw new ProtocolException("At least 5 tokens expected in an order message");
    }
    o.put(JsonTagsZ.TRACKING_ID, st.nextToken());
    o.put(JsonTagsZ.ORDER_STATUS, st.nextToken());
    o.put(JsonTagsZ.QUANTITY, st.nextToken());
    o.put(JsonTagsZ.TIMESTAMP, decode(st.nextToken()));
    String value = null;
    if (st.hasMoreTokens() && !DUMMY.equals(value = st.nextToken())) {
      o.put(JsonTagsZ.UPDATED_TIME, decode(value));
    }
    if (st.hasMoreTokens() && !DUMMY.equals(value = st.nextToken())) {
      o.put(JsonTagsZ.TOTAL_PRICE, value);
    }
    if (st.hasMoreTokens() && !DUMMY.equals(value = st.nextToken())) {
      o.put(JsonTagsZ.CURRENCY, value);
    }
    if (st.hasMoreTokens() && !DUMMY.equals(value = st.nextToken())) {
      o.put(JsonTagsZ.MESSAGE, decode(value));
    }
    if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION02) && st.hasMoreTokens()
        && !DUMMY.equals(value = st.nextToken())) { // MessageHeader.VERSION02.equals( version ) ) {
      // Vendor ID
      o.put(JsonTagsZ.VENDORID, value);
    }
    if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION05) && st.hasMoreTokens()
        && !DUMMY.equals(value = st.nextToken())) { // version 05 onwards
      // Discount
      o.put(JsonTagsZ.DISCOUNT, value);
    }
    if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION06) && st.hasMoreTokens()
        && !DUMMY.equals(value = st.nextToken())) { // version 06 onwards
      // Kiosk ID
      o.put(JsonTagsZ.KIOSK_ID, value);
    }
    o.put(JsonTagsZ.MATERIALS, getMaterialsVector(st, null));
    return o;
  }

  public static String encode(String msg) {
    return msg.replace(' ', '%');
  }

  public static String decode(String msg) {
    return msg.replace('%', ' ');
  }

  // Convert tokens to string
  public static String tokensToString(StringTokenizer st) {
    return tokensToString(st, -1);
  }

  // Convert n tokens to string
  public static String tokensToString(StringTokenizer st, int n) {
    String msg = "";
    int i = 0;
    while (st.hasMoreTokens() && (n < 0 || i < n)) {
      msg += st.nextToken();
      if (st.hasMoreTokens() && (n < 0 || i != (n - 1))) {
        msg += " ";
      }
      i++;
    }
    return msg;
  }

  // Create a message Id at random
  public static String createMessageId() {
    Random r = new Random();
    r.setSeed(System.currentTimeMillis());
    float f = r.nextFloat();
    return String.valueOf((int) ((f * 100.0f) % 100));
  }

  // Add message headers - msgNo numMsgs sessionId
  private static Vector addHeaders(Vector v, String msgId) {
    Vector v1 = new Vector();
    int size = v.size();
    Enumeration en = v.elements();
    int i = 1;
    while (en.hasMoreElements()) {
      String header = i + " " + size + " " + msgId;
      v1.addElement(header + " " + (String) en.nextElement());
      i++;
    }
    return v1;
  }

  // Hex decoding
  public static String decodeHex(String hexString, String charset) {
    if (hexString == null) {
      return null;
    }
    if (charset == null) {
      charset = CHARSET_DEFAULT;
    }
    String str = null;
    try {
      str = new String(Hex.decodeHex(hexString.toCharArray()), charset);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return str;
  }

  // Hex encoding
  public static String encodeHex(String normalString, String charset) {
    if (normalString == null) {
      return null;
    }
    if (charset == null) {
      charset = CHARSET_DEFAULT;
    }
    String hexString = null;
    try {
      hexString = new String(Hex.encodeHex(normalString.getBytes(charset)));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return hexString;
  }
}
