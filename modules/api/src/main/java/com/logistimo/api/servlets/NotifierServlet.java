/**
 *
 */
package com.logistimo.api.servlets;

import com.logistimo.AppFactory;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.events.handlers.BBHandler;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.storage.StorageUtil;
import com.logistimo.users.entity.IUserAccount;

import com.logistimo.communications.service.EmailService;
import com.logistimo.communications.service.MessageService;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventSpec.NotifyOptions;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.processor.EventNotificationProcessor.EventNotificationData;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Arun
 */
public class NotifierServlet extends SgServlet {

  private static final long serialVersionUID = 1L;

  // Logger
  private static final XLog xLogger = XLog.getLog(NotifierServlet.class);
  // Actions
  private static final String ACTION_NOTIFY = "notify";
  private static final String ACTION_BATCHNOTIFY = "batchnotify";
  private static final String ACTION_TIGGERBATCHNOTIFY = "triggerbatchnotify";
  private static BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
  private static StorageUtil storageUtil = AppFactory.get().getStorageUtil();

  // Notify an event as a SMS / email message
  private static void notifyEvent(HttpServletRequest request, HttpServletResponse response,
                                  ResourceBundle backendMessages, ResourceBundle messages) {
    xLogger.fine("Entered notifyEvent");
    // Get the event key
    String eventKeyStr = request.getParameter("eventkey");
    String frequencyStr = request.getParameter("frequency");
    String method = request.getParameter("method");
    String eventType = null;
    if (method == null || method.isEmpty()) {
      method = MessageService.SMS; // default to SMS
    }
    Integer frequency = null;
    if (frequencyStr == null || frequencyStr.isEmpty()) {
      frequency = NotifyOptions.IMMEDIATE; // default to sending immediately
    } else {
      frequency = Integer.valueOf(frequencyStr);
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Get event
      IEvent event = JDOUtils.getObjectById(IEvent.class, Long.valueOf(eventKeyStr), pm);
      Long domainId = event.getDomainId();
      // Get the domain conifg.
      DomainConfig dc = DomainConfig.getInstance(domainId);

      boolean isEventValid = EventHandler.isEventValid(event, dc, pm);
      if (!isEventValid) {
        xLogger.warn(
            "Invalid event when notify event {0}  on object of type {1}, id {2} in domain {3};message in event = {4}",
            event.getId(), event.getObjectType(), event.getObjectId(), event.getDomainId(),
            event.getMessage());
        return;
      }

      // Get the message and the list of user Ids to notify
      EventHandler.MessageSubscribers
          ms =
          EventHandler.getMessageSubcribers(event, frequency, false, dc, pm, domainId);
      if (ms == null || !ms.isValid()) {
        xLogger.warn(
            "No valid message or subscribers or bb-post to notify for event {0} on object of type {1}, id {2} in domain {3}; message in event = {4}",
            event.getId(), event.getObjectType(), event.getObjectId(), event.getDomainId(),
            event.getMessage());
        return;
      }
      // Send SMS notification, if needed
      if (ms.message != null && !ms.message.isEmpty()) {
        // Get the user accounts
        List<IUserAccount> subscribers = new ArrayList<IUserAccount>();
        if (ms.userIds != null) {
          Iterator<String> it = ms.userIds.iterator();
          while (it.hasNext()) {
            String userId = it.next();
            try {
              IUserAccount u = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
              if (!subscribers.contains(u)) {
                subscribers.add(u);
              }
            } catch (Exception e) {
              xLogger.warn(
                  "{0} when getting user {1} for event notification for event {2}:{3} in domain {3}: {4}",
                  e.getClass().getName(), userId, event.getObjectType(), event.getId(),
                  e.getMessage());
              continue;
            }
          }
        }

        if (ms.paramSpec.getName() != null && !ms.paramSpec.getName().isEmpty()) {
          eventType = ms.paramSpec.getName();
        }
        // Notify to subscribers
        if (!subscribers.isEmpty()) {
          String countryCode = dc.getCountry();
          if (countryCode == null || countryCode.isEmpty()) {
            countryCode = subscribers.get(0).getCountry();
          }
          MessageService
              msgService =
              MessageService.getInstance(method, countryCode, true, domainId);
          int msgType = MessageService.getMessageType(ms.message);
          msgService
              .send(subscribers, ms.message, msgType, "[Logistimo] Notification", null, null, 1,
                  eventType);
          xLogger
              .info("Event ({0}) on object {1} notified to {2} subscribers in domain {3} via {4}",
                  eventKeyStr, event.getObjectId() + ":" + event.getObjectType(),
                  subscribers.size(), event.getDomainId(), method);
        }
      }
      // Post on bulletin board, if needed
      if (ms.bbMessage != null && !ms.bbMessage.isEmpty() && dc.isBBoardEnabled()) {
        BBHandler.add(ms.bbMessage, event, ms.objectData);
      }
    } catch (Exception e) {
      xLogger.severe("{0} when notifying for event {1}: {2}", e.getClass().getName(), eventKeyStr,
          e.getMessage(), e);
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting notifyEvent");
  }

  // Notify in batch
  private static void batchNotify(HttpServletRequest request, HttpServletResponse response,
                                  ResourceBundle backendMessages, ResourceBundle messages) {
    xLogger.fine("Entered batchNotify");
    // Get the request parameters
    String domainIdStr = request.getParameter("domainid");
    String eventDataJson = request.getParameter("output");
    xLogger.fine("eventDataJson: {0}", eventDataJson);
    if (domainIdStr == null || domainIdStr.isEmpty()) {
      xLogger.severe("No domain ID given for batch notification of events");
      return;
    }
    if (eventDataJson == null || eventDataJson.isEmpty()) {
      return; // nothing to notify on
    }
    Long domainId = null;
    try {
      domainId = Long.valueOf(domainIdStr);
      // Get the domain config.
      DomainConfig dc = DomainConfig.getInstance(domainId);
      EventNotificationData eventData = new EventNotificationData(eventDataJson);
      // Read the data
      if (eventData.blobKey == null) {
        xLogger.warn("Event data blob key is null in domain {0}", domainId);
        return; // nothing to notify on
      }
      // Read the message map data
      byte[] bytes = storageUtil.readFile(Constants.GCS_BUCKET_DEFAULT, eventData.blobKey);
      if (bytes == null || bytes.length == 0) {
        xLogger.warn("No bytes of event data message map found in domain {0} for blobKey {1}",
            domainId, eventData.blobKey);
        return;
      }
      eventData.loadMessageMap(new String(bytes, "UTF-8"));
      if (eventData.messageMap == null || eventData.messageMap.isEmpty()) {
        return; // nothing to notify on
      }
      // Get the final message to be sent to each user
      Map<String, List<String>> messageToUsers = new HashMap<String, List<String>>();
      Iterator<Entry<String, Map<String, List<String>>>>
          it =
          eventData.messageMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<String, Map<String, List<String>>> entry = it.next();
        String userId = entry.getKey();
        String message = getMessage(eventData.frequency, entry.getValue(), dc);
        if (message == null || message.isEmpty()) {
          continue;
        }
        List<String> userIds = messageToUsers.get(message);
        if (userIds == null) {
          userIds = new ArrayList<String>();
          messageToUsers.put(message, userIds);
        }
        if (!userIds.contains(userId)) {
          userIds.add(userId);
        }
      }
      // Send the messages to designated users
      sendMessage(domainId, messageToUsers, getSubject(domainId, eventData.frequency, dc),
          eventData.frequency);
      // Clean up
      blobstoreService.remove(eventData.blobKey);
    } catch (Exception e) {
      xLogger
          .severe("{0} when batch notifying in domain {1}: {2}", e.getClass().getName(), domainId,
              e.getMessage(), e);
    }

    xLogger.fine("Exiting batchNotify");
  }

  // Trigger batch notification events
  private static void triggerBatchNotify(HttpServletRequest request, HttpServletResponse response,
                                         ResourceBundle backendMessages, ResourceBundle messages) {
    xLogger.fine("Entered triggerBatchNotify");
    String domainIdStr = request.getParameter("domainid");
    String frequencyStr = request.getParameter("frequency");
    String startDateStr = request.getParameter("startdate"); // dd/MM/yyyy hh:mm:ss
    String endDateStr = request.getParameter("enddate"); // dd/MM/yyyy hh:mm:ss
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    if (domainId == null) {
      xLogger.severe("No domain ID given");
      return;
    }
    EventHandler.CustomDuration customDuration = null;
    if (frequencyStr != null && !frequencyStr.isEmpty()) {
      customDuration = new EventHandler.CustomDuration();
      customDuration.frequency = Integer.parseInt(frequencyStr);
    }
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_FORMAT);
    if (startDateStr != null && !startDateStr.isEmpty()) {
      if (customDuration == null) {
        customDuration = new EventHandler.CustomDuration();
      }
      customDuration.duration = new EventHandler.Duration();
      try {
        customDuration.duration.start = sdf.parse(startDateStr);
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    if (endDateStr != null && !endDateStr.isEmpty()) {
      try {
        if (customDuration == null) {
          customDuration = new EventHandler.CustomDuration();
        }
        if (customDuration.duration == null) {
          customDuration.duration = new EventHandler.Duration();
        }
        customDuration.duration.end = sdf.parse(endDateStr);
      } catch (ParseException e) {
        e.printStackTrace();
        return;
      }
    }
    // Start the notification
    EventHandler.batchNotify(domainId, customDuration);
    xLogger.fine("Exiting triggerBatchNotify");
  }

  // Send messages to users
  private static void sendMessage(Long domainId, Map<String, List<String>> messageToUsers,
                                  String subject, int frequency) {
    xLogger.fine("Entered sendMessage");
    if (messageToUsers == null || messageToUsers.isEmpty()) {
      return;
    }
    EmailService svc = EmailService.getInstance();
    Iterator<Entry<String, List<String>>> it = messageToUsers.entrySet().iterator();
    try {
      int numUsers = 0;
      while (it.hasNext()) {
        Entry<String, List<String>> entry = it.next();
        String message = entry.getKey();
        List<String> userIds = entry.getValue();
        if (userIds == null || userIds.isEmpty()) {
          continue;
        }
        numUsers += userIds.size();
        // Get the sending addresses
        List<String> addresses = getEmailAddresses(userIds, domainId);
        // Get message service and send
        if (addresses != null && !addresses.isEmpty()) {
          svc.sendHTML(domainId, addresses, subject, message, Constants.SYSTEM_ID);
        }
      }
      xLogger
          .info("Notified (in batch) {0} messages across {1} users in domain {2} for frequency {3}",
              messageToUsers.size(), numUsers, domainId, frequency);
    } catch (Exception e) {
      xLogger
          .severe("{0} when sending notification messages {1} in domain {2} with subject {3}: {4}",
              e.getClass().getName(), messageToUsers, domainId, subject, e.getMessage());
    }
    xLogger.fine("Exiting sendMessage");
  }

  // Get email addresses of users
  private static List<String> getEmailAddresses(List<String> userIds, Long domainId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    if (userIds == null || userIds.isEmpty()) {
      return null;
    }
    List<String> addresses = new ArrayList<String>();
    try {
      Iterator<String> it = userIds.iterator();
      while (it.hasNext()) {
        String userId = it.next();
        try {
          IUserAccount u = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
          if (u.isEnabled() && (u.getDomainId().equals(domainId) || SecurityConstants.ROLE_SUPERUSER
              .equals(u.getRole()))) {
            String email = u.getEmail();
            if (email != null && !email.isEmpty()) {
              addresses.add(email);
            }
          } else {
            xLogger
                .info("Skipping email notification for user {0} in domain {1}", userId, domainId);
          }
        } catch (Exception e) {
          xLogger.warn("{0} when getting email address for notification for user {1}: {2}",
              e.getClass().getName(), userId, e.getMessage());
        }
      }
    } finally {
      pm.close();
    }
    return addresses;
  }

  // Get the HTML message for a given set of event specifications
  private static String getMessage(int frequency, Map<String, List<String>> eventNameToMessages,
                                   DomainConfig dc) {
    String html = getHTMLHeader();
    if (eventNameToMessages == null || eventNameToMessages.isEmpty()) {
      return null;
    }
    Iterator<Entry<String, List<String>>> it = eventNameToMessages.entrySet().iterator();
    html += "<body>";
    html += getMessageTitle(frequency, new Date(), dc);
    while (it.hasNext()) {
      Entry<String, List<String>> entry = it.next();
      String eventName = entry.getKey();
      List<String> messages = entry.getValue();
      if (messages == null || messages.isEmpty()) {
        continue; // ignore, if no messages
      }
      html += "<div id=\"section\">";
      html += "<p><b>" + eventName + "</b></p>";
      html += "<table>";
      Iterator<String> messagesIt = messages.iterator();
      while (messagesIt.hasNext()) {
        html += "<tr><td>" + messagesIt.next() + "</td></tr>";
      }
      html += "</table></div>";
    }
    html += "</body></html>";
    return html;
  }

  // Get the HTML header with the necessary styles
  private static String getHTMLHeader() {
    String header = "<html><head>";
    header += "<style type=\"text/css\">\n";
    header += "#section { font-family:Arial; }\n";
    header += "#section table {font-size:11px;}\n";
    header +=
        "#section th {background-color:#9AC8CF;font-weight:bold;padding:3px; border:1px solid #9AC8CF;}\n";
    header += "#section td {padding:3px;border:1px solid #CDCDCD;}\n";
    header += "</style></head>";
    return header;
  }

  // Get the title of the message
  private static String getMessageTitle(int frequency, Date date, DomainConfig dc) {
    String title = "<h2>Events";
    Locale locale = dc.getLocale();
    if (locale == null) {
      locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    }
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, -1); // previous day
    String dateStr = LocalDateUtil.format(cal.getTime(), locale, dc.getTimezone());
    // Get only the date part
    String[] dateParts = dateStr.split(" ");
    if (dateParts.length > 0) {
      dateStr = dateParts[0];
    }
    if (frequency == NotifyOptions.DAILY) {
      title += " on " + dateStr;
    } else if (frequency == NotifyOptions.WEEKLY) {
      title += " in the week ending on " + dateStr;
    }
    title += "</h2>";
    return title;
  }

  // Get the message subject line
  private static String getSubject(Long domainId, int frequency, DomainConfig dc) {
    Locale locale = dc.getLocale();
    if (locale == null) {
      locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    }
    // Get domain name
    String domainNameSuffix = "";
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IDomain domain = JDOUtils.getObjectById(IDomain.class, domainId, pm);
      domainNameSuffix = " for " + domain.getName();
    } catch (Exception e) {
      // ignore
    } finally {
      pm.close();
    }
    return "[Logistimo] " + EventSpec.NotifyOptions.getFrequencyDisplay(frequency, locale)
        + " notifications" + domainNameSuffix;
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
                             ResourceBundle backendMessages,
                             ResourceBundle messages) throws ServletException, IOException,
      ServiceException {
    xLogger.fine("Entered processPost");
    // Get the action
    String action = request.getParameter("action");
    if (ACTION_NOTIFY.equals(action)) {
      notifyEvent(request, response, backendMessages, messages);
    } else if (ACTION_BATCHNOTIFY.equals(action)) {
      batchNotify(request, response, backendMessages, messages);
    } else if (ACTION_TIGGERBATCHNOTIFY.equals(action)) {
      triggerBatchNotify(request, response, backendMessages,
          messages); // for manual triggering of notification events
    } else {
      xLogger.severe("Invalid action: {0}", action);
    }
    xLogger.fine("Exiting processPost");
  }
}