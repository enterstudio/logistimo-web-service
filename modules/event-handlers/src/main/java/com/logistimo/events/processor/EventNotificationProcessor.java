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
package com.logistimo.events.processor;

import com.logistimo.AppFactory;
import com.logistimo.config.models.EventSpec;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.services.storage.StorageUtil;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.utils.ConfigUtil;

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.constants.Constants;
import com.logistimo.utils.JsonUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.jdo.PersistenceManager;

/**
 * @author Arun
 */
public class EventNotificationProcessor implements Processor {

  // Looger
  private static final XLog xLogger = XLog.getLog(EventNotificationProcessor.class);
  // JSON Tags
  private static final String FREQUENCY = "frequency";
  private static final String BLOBKEY = "blobkey";

  private static StorageUtil storageUtil = AppFactory.get().getStorageUtil();

  @SuppressWarnings("unchecked")
  @Override
  public String process(Long domainId, Results results, String eventDataJson, PersistenceManager pm)
      throws ProcessingException {
    xLogger.fine("Entered EventNotificationProcessor.process");
    if (results == null || results.getResults() == null) {
      return eventDataJson;
    }
    if (eventDataJson == null || eventDataJson
        .isEmpty()) // NOTE: even in the initial call, a JSON with the frequency is expected
    {
      throw new ProcessingException("Empty eventDataJson in domain " + domainId);
    }
    List<IEvent> events = (List<IEvent>) results.getResults();
    try {
      events.get(0);
    } catch (IndexOutOfBoundsException e) {
      return eventDataJson;
    }
    int maxMessagesPerEventType = ConfigUtil.getInt("notifications.email.items.max.per.event", 100);
    // Get the events config.
    DomainConfig dc = DomainConfig.getInstance(domainId);
    Locale locale = dc.getLocale();
    if (locale == null) {
      locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    }
    try {
      // Init. event data
      EventNotificationData eventData = new EventNotificationData(eventDataJson);
      // Load event data from blobstore, if available
      if (eventData.blobKey != null) {
        byte[] bytes = storageUtil.readFile(Constants.GCS_BUCKET_DEFAULT, eventData.blobKey);
        if (bytes == null) {
          xLogger.severe("Got no bytes for blob-key {0} in domain {1} when notifying events",
              eventData.blobKey, domainId);
          return eventDataJson;
        }
        String eventDataStr = new String(bytes, "UTF-8");
        eventData.loadMessageMap(eventDataStr);
      }
      // Process events
      Iterator<IEvent> eventsIt = events.iterator();
      while (eventsIt.hasNext()) {
        // Get next event
        IEvent event = eventsIt.next();
        boolean isEventValid = EventHandler.isEventValid(event, dc, pm);
        if (!isEventValid) {
          xLogger.fine(
              "Invalid event when notify event {0}  on object of type {1}, id {2} in domain {3};message in event = {4}",
              event.getId(), event.getObjectType(), event.getObjectId(), event.getDomainId(),
              event.getMessage());
          continue;
        }
        // Get the message and user Ids to notify for the given frequency
        EventHandler.MessageSubscribers
            ms =
            EventHandler.getMessageSubcribers(event, eventData.frequency, true, dc, pm, domainId);
        if (ms == null || !ms.isValid()) {
          xLogger.fine("No valid message or subscribers to notify for event {0}:{1} in domain {2}",
              event.getId(), event.getObjectType(), event.getDomainId());
          continue;
        }
        String eventName = ms.paramSpec.getName();
        // Create the userId - event - messages map
        Iterator<String> uIdsIt = ms.userIds.iterator();
        while (uIdsIt.hasNext()) {
          String userId = uIdsIt.next();
          // Get the eventSpec to messages map
          Map<String, List<String>> eventToMessages = eventData.messageMap.get(userId);
          if (eventToMessages == null) {
            eventToMessages = new HashMap<String, List<String>>();
            eventData.messageMap.put(userId, eventToMessages);
          }
          // Get message list
          List<String> messages = eventToMessages.get(eventName);
          if (messages == null) {
            messages = new ArrayList<String>();
            eventToMessages.put(eventName, messages);
          }
          // Add message
          if (!messages.contains(ms.message) && messages.size() <= maxMessagesPerEventType) {
            messages.add(ms.message);
          }
        }
      } // end while
      // Store event data as a blob
      if (!eventData.messageMap.isEmpty()) {
        String messageMapStr = eventData.toMessageMapJSON();
        if (eventData.blobKey != null) {
          storageUtil
              .removeFile(Constants.GCS_BUCKET_DEFAULT, eventData.blobKey); // remove older file
        }
        // Write the new data to a new file, and remember the new blob key
        String fileName = "eventdata." + domainId + "." + System.currentTimeMillis();
        eventData.blobKey = fileName;
        storageUtil.write(Constants.GCS_BUCKET_DEFAULT, fileName, messageMapStr.getBytes("UTF-8"),
            "text/plain");
      }
      xLogger.fine("Exiting EventNotificationProcessor.process: eventData: {0}",
          eventData.toJSONString());
      return eventData.toJSONString();
    } catch (Exception e) {
      throw new ProcessingException(
          e.getClass().getName() + " when processing event in domain " + domainId + ": " + e
              .getMessage());
    }
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_MESSAGE;
  }

  public static class EventNotificationData {
    public int
        frequency =
        EventSpec.NotifyOptions.DAILY;
    // EventSpec.NotifyOptions.XXX (immediate, daily, weekly, monthly, etc.)
    public String
        blobKey =
        null;
    // eventData's messageMap can become large; so it is stored to blobstore and the blobKey is stored here
    public Map<String, Map<String, List<String>>>
        messageMap =
        new HashMap<String, Map<String, List<String>>>();
    // userId --> (eventName --> message-list)

    public EventNotificationData(int frequency) {
      this.frequency = frequency;
    }

    public EventNotificationData(String jsonString) throws JSONException {
      JSONObject json = new JSONObject(jsonString);
      frequency = json.getInt(FREQUENCY);
      try {
        blobKey = json.getString(BLOBKEY);
      } catch (Exception e) {
        // ignore
      }
    }

    public String toJSONString() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(FREQUENCY, frequency);
      if (blobKey != null) {
        json.put(BLOBKEY, blobKey);
      }
      return json.toString();
    }

    @SuppressWarnings("unchecked")
    public void loadMessageMap(String jsonString) throws JSONException {
      JSONObject messageMapJson = new JSONObject(jsonString);
      // Get subscriber event specs.
      if (messageMapJson != null) {
        Iterator<String> keys = messageMapJson.keys();
        while (keys.hasNext()) {
          String userId = keys.next();
          JSONObject eventToMessagesJson = messageMapJson.optJSONObject(userId);
          if (eventToMessagesJson == null) {
            continue;
          }
          // Get the eventId --> message-list mapping
          Map<String, List<String>> eventNameToMessages = new HashMap<String, List<String>>();
          Iterator<String> eventNames = eventToMessagesJson.keys();
          while (eventNames.hasNext()) {
            String eventName = eventNames.next();
            List<String> messages = JsonUtil.toList(eventToMessagesJson.optJSONArray(eventName));
            if (messages != null && !messages.isEmpty()) {
              eventNameToMessages.put(eventName, messages);
            }
          }
          messageMap.put(userId, eventNameToMessages);
        }
      }
    }

    public String toMessageMapJSON() throws JSONException {
      if (messageMap.isEmpty()) {
        return null;
      }
      // Form the message map
      JSONObject messageMapJson = new JSONObject();
      Iterator<Entry<String, Map<String, List<String>>>> it = messageMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<String, Map<String, List<String>>> entry = it.next();
        String userId = entry.getKey();
        Map<String, List<String>> eventNameToMessages = entry.getValue();
        if (eventNameToMessages == null || eventNameToMessages.isEmpty()) {
          continue;
        }
        JSONObject eventNameToMessagesJson = new JSONObject();
        Iterator<Entry<String, List<String>>> it2 = eventNameToMessages.entrySet().iterator();
        while (it2.hasNext()) {
          Entry<String, List<String>> entry2 = it2.next();
          String eventName = entry2.getKey();
          List<String> messages = entry2.getValue();
          if (messages != null && !messages.isEmpty()) {
            eventNameToMessagesJson.put(eventName, JsonUtil.toJSON(messages));
          }
        }
        // Update message map json
        messageMapJson.put(userId, eventNameToMessagesJson);
      }
      return messageMapJson.toString();
    }
  }
}
