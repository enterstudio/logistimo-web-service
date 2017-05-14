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
package com.logistimo.communications.service;


import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.ServiceResponse;
import com.logistimo.logger.XLog;
import com.logistimo.services.utils.ConfigUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * @author Arun
 */
public class EmailService extends MessageService {

  // Mime types
  public static final String MIME_HTML = "text/html";
  public static final String MIME_CSV = "text/csv";
  // Logger
  private static final XLog xLogger = XLog.getLog(EmailService.class);
  // Properties parameter names
  private static final String FROMADDRESS = "email.fromaddress";
  private static final String FROMNAME = "email.fromname";
  // Provider ID
  private static final String GAE_PROVIDERID = "gae-email"; // can be anything
  private String fromaddress = null;
  private String fromname = null;
  private String mimeType = null;

  public EmailService(String mimeType) {
    // Get the from name and address from properties file
    fromaddress = ConfigUtil.get(FROMADDRESS,"service@logistimo.com");
    fromname = ConfigUtil.get(FROMNAME,"Logistimo Service");
    this.mimeType = mimeType;
  }

  public static EmailService getInstance() {
    return new EmailService(null);
  }

  public static EmailService getInstance(String mimeType) {
    return new EmailService(mimeType);
  }

  public String getStatusMessage(String statusCode, Locale locale) {
    return null; // nothing to return here
  }

  public String getProviderId() {
    return GAE_PROVIDERID;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  // Convenience method to send a HTML email, with logging
  public ServiceResponse sendHTML(Long domainId, List<String> addresses, String subject,
                                  String message, String sendingUserId)
      throws MessageHandlingException, IOException {
    mimeType = "text/html";
    int msgType = MessageService.getMessageType(message);
    return sendWithAttachment(addresses, message, msgType, subject, null, null, null);
  }

  // Send an email
  @Override
  protected ServiceResponse doSend(List<String> addresses, String message, int messageType,
                                   String subject, String port, Long domainId)
      throws IOException, MessageHandlingException {
    return sendWithAttachment(addresses, message, messageType, subject, null, null, null);
  }

  // Send with attachment
  public ServiceResponse sendWithAttachment(List<String> addresses, String message, int messageType,
                                            String subject, byte[] attachment,
                                            String attachmentMimeType, String attachmentFilename)
      throws IOException, MessageHandlingException {
    xLogger.fine("Entering sendWithAttachment");
    Properties props = new Properties();
    props.put("mail.smtp.host", ConfigUtil.get("mail.smtp.host","localhost"));
    props.put("mail.smtp.port", ConfigUtil.getInt("mail.smtp.port", 25));
    Session session = Session.getDefaultInstance(props, null);
    ServiceResponse resp;
    try {
      // Set message properties
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(fromaddress, fromname));
      // Get recepient addresses
      InternetAddress[] recepients = getInternetAddresses(addresses);
      msg.addRecipients(Message.RecipientType.TO, recepients);
      if (subject != null) {
        msg.setSubject(subject);
      }
      Multipart mp = null;
      if (mimeType == null || mimeType.equals("text/plain")) {
        msg.setText(message);
      } else if (mimeType.equals("text/html")) {
        msg.setContent(message, "text/html; charset=utf-8");
      } else {
        mp = getMultiPartMsg(message);
      }
      // Add attachment, if any
      if (attachment != null && attachment.length > 0) {
        if (mp == null) {
          mp = new MimeMultipart();
        }
        addAttachment(mp, attachment, attachmentMimeType, attachmentFilename);
      }
      // Add multipart to message, if present
      if (mp != null) {
        msg.setContent(mp);
      }
      // Save changes to message
      msg.saveChanges();
      // Send
      Transport.send(msg);

      // Form the response
      resp = new ServiceResponse(GAE_PROVIDERID);
      resp.setMethod(ServiceResponse.METHOD_STATUS);
      resp.addResponse("success", addresses);
    } catch (MessagingException e) {
      throw new MessageHandlingException(e);
    }
    xLogger.fine("Exiting sendWithAttachment");
    return resp;
  }

  // Send with attachment
  public ServiceResponse sendWithAttachmentStream(List<String> addresses, String message,
                                                  int messageType, String subject,
                                                  InputStream attachmentStream,
                                                  String attachmentMimeType,
                                                  String attachmentFilename)
      throws IOException, MessageHandlingException {
    xLogger.fine("Entering sendWithAttachmentStream");
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    ServiceResponse resp;
    props.put("mail.smtp.host", ConfigUtil.get("mail.smtp.host","localhost"));
    props.put("mail.smtp.port", ConfigUtil.getInt("mail.smtp.port",25));

    try {
      // Set message properties
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(fromaddress, fromname));
      // Get recepient addresses
      InternetAddress[] recepients = getInternetAddresses(addresses);
      msg.addRecipients(Message.RecipientType.TO, recepients);
      if (subject != null) {
        msg.setSubject(subject);
      }
      Multipart mp = null;
      if (mimeType == null || mimeType.equals("text/plain")) {
        msg.setText(message);
      } else if (mimeType.equals("text/html")) {
        msg.setContent(message, "text/html; charset=utf-8");
      } else {
        mp = getMultiPartMsg(message);
      }
      // Add attachment, if any
      if (attachmentStream != null) {
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html; charset=utf-8");
        if (mp == null) {
          mp = new MimeMultipart();
        }
        addAttachmentStream(mp, attachmentStream, attachmentMimeType, attachmentFilename,
            messageBodyPart);
      }
      // Add multipart to message, if present
      if (mp != null) {
        msg.setContent(mp);
      }
      // Save changes to message
      msg.saveChanges();
      // Send
      Transport.send(msg);

      // Form the response
      resp = new ServiceResponse(GAE_PROVIDERID);
      resp.setMethod(ServiceResponse.METHOD_STATUS);
      resp.addResponse("success", addresses);
    } catch (MessagingException e) {
      xLogger.severe("Error in sending mail with attachment stream: {0}", e.getMessage(), e);
      throw new MessageHandlingException(e);
    }
    xLogger.fine("Exiting sendWithAttachmentStream");
    return resp;
  }


  // Get the job Id, if any
  protected String getJobId(String respStr) {
    return null; // not used in this service
  }

  // Get the formatted address, if any
  public String getFormattedAddress(String address) {
    return address;
  }

  // Get an array of InternetAddresses, given a list of normal addresses
  protected InternetAddress[] getInternetAddresses(List<String> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      return null;
    }
    List<InternetAddress> iaddresses = new ArrayList<>(addresses.size());
    for (String address : addresses) {
      try {
        InternetAddress internetAddress = new InternetAddress(address, "");
        internetAddress.validate();
        iaddresses.add(internetAddress);
      } catch (UnsupportedEncodingException e) {
        xLogger.warn("Exception while creating InternetAddress for address {0}: {1}", address,
            e.getMessage());
      } catch (AddressException e) {
        xLogger.warn("Email address invalid ", address);
      }
    }
    return iaddresses.toArray(new InternetAddress[iaddresses.size()]);
  }

  // Get a multi-part message
  private Multipart getMultiPartMsg(String message) throws MessagingException {
    Multipart mp = new MimeMultipart();
    MimeBodyPart bodyPart = new MimeBodyPart();
    bodyPart.setHeader("Content-Type", mimeType);
    bodyPart.setContent(message, mimeType);
    mp.addBodyPart(bodyPart);
    return mp;
  }

  // Get a multi-part message
  private void addAttachment(Multipart mp, byte[] attachmentData, String mimeType, String filename)
      throws MessagingException {
    if (mp == null) {
      return;
    }
    ByteArrayDataSource dataSrc = new ByteArrayDataSource(attachmentData, mimeType);
    MimeBodyPart attachment = new MimeBodyPart();
    attachment.setFileName(filename);
    attachment.setDataHandler(new DataHandler(dataSrc));
    ///attachment.setContent( attachmentData, mimeType );
    mp.addBodyPart(attachment);
  }

  // Get a multi-part message
  private void addAttachmentStream(Multipart mp, InputStream attachmentStream, String mimeType,
                                   String filename, BodyPart message)
      throws MessagingException, IOException {
    if (mp == null) {
      return;
    }
    ByteArrayDataSource dataSrc = new ByteArrayDataSource(attachmentStream, mimeType);
    MimeBodyPart attachment = new MimeBodyPart();
    attachment.setFileName(filename);
    attachment.setDataHandler(new DataHandler(dataSrc));
    ///attachment.setContent( attachmentData, mimeType );
    mp.addBodyPart(message);
    mp.addBodyPart(attachment);
  }
}
