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
package com.logistimo.events.handlers;

import com.logistimo.AppFactory;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.events.EventConstants;
import com.logistimo.events.dao.IEventDao;
import com.logistimo.events.dao.impl.EventDao;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.exceptions.EventGenerationException;
import com.logistimo.events.generators.EventGenerator;
import com.logistimo.events.generators.EventGeneratorFactory;
import com.logistimo.events.models.EventData;
import com.logistimo.events.models.ObjectData;
import com.logistimo.events.processor.AssetEventsCreationProcessor;
import com.logistimo.events.processor.EventNotificationProcessor;
import com.logistimo.events.processor.EventNotificationProcessor.EventNotificationData;
import com.logistimo.events.processor.OrderEventsCreationProcessor;
import com.logistimo.events.processor.UserEventsCreationProcessor;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.pagination.processor.BatchExpiryEventsCreationProcessor;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.Executor;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.QueryUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Manages events in datastore (Event) and enables event retrieval
 *
 * @author Arun
 */
public class EventHandler {

  // Event notification finalizer URL
  private static final String EVENT_NOTIFICATION_FINALIZER_URL = "/task/notifier";
  // Logger
  private static final XLog xLogger = XLog.getLog(EventHandler.class);

  private static IEventDao eventDao = new EventDao();

  /**
   * Add an event to the data store. Its key is returned.
   */
  public static Long log(IEvent event) {
    return eventDao.store(event);
  }

  /**
   * Add events to the datastore in batch
   */
  public static void log(List<IEvent> events) {
    eventDao.store(events);
  }

  /**
   * Get an event, given its key
   */
  public static IEvent getEvent(Long eventKey) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IEvent event = JDOUtils.getObjectById(IEvent.class, eventKey, pm);
      event = pm.detachCopy(event);
      return event;
    } catch (JDOObjectNotFoundException e) {
      return null;
    } finally {
      pm.close();
    }
  }

  /**
   * Get all users of a given type, given either a kioskId
   */
  @SuppressWarnings("unchecked")
  public static List<String> getSubsribers(String subscriberType, Object oid) {
    xLogger.fine("Entered EventHandler.getSubscribers: subscriber type = {0}, oid = {1}",
        subscriberType, oid);
    if (oid == null) {
      return null;
    }
    try {
      List<IUserAccount> users = null;
      if (EventSpec.Subscriber.CUSTOMERS.equals(subscriberType) || EventSpec.Subscriber.VENDORS
          .equals(subscriberType)) {
        Long kioskId = (Long) oid;
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        users = (List<IUserAccount>) as.getKiosk(kioskId).getUsers();
      } else if (EventSpec.Subscriber.ADMINISTRATORS.equals(subscriberType)) {
        Long domainId = (Long) oid;
        UsersService as = Services.getService(UsersServiceImpl.class);
        users =
            (List<IUserAccount>) as
                .getUsers(domainId, SecurityConstants.ROLE_DOMAINOWNER, true, null, null).getResults();
      } else if (EventSpec.Subscriber.CREATOR.equals(subscriberType)) {
        String createrId = (String) oid;
        UsersService as = Services.getService(UsersServiceImpl.class);
        users = new ArrayList<>(1);
        users.add(as.getUserAccount(createrId));
      } else {
        xLogger.severe("Invalid subscriber type {0} for oid {1}", subscriberType, oid);
        return null;
      }
      List<String> userIds = null;
      if (users != null && !users.isEmpty()) {
        userIds = new ArrayList<String>();
        Iterator<IUserAccount> it = users.iterator();
        while (it.hasNext()) {
          IUserAccount u = it.next();
          if (u.isEnabled()) // add a user only if he/she is enabled
          {
            userIds.add(u.getUserId());
          }
        }
      }
      return userIds;
    } catch (Exception e) {
      xLogger.severe("{0} getting subscribers of type {1} for oid {2}: {3}", e.getClass().getName(),
          subscriberType, oid, e.getMessage());
    }
    return null;
  }

  // Batch notify on all events generated in a given domain, for frequencies such as Daily, Weekly or Monthly
  public static void batchNotify(Long domainId, CustomDuration customDuration) {
    xLogger.fine("Entered EventNotifier.notify");
    try {
      // Get durations for notification
      Map<Integer, Duration> durations = getDurations(customDuration, domainId);
      xLogger.fine("Durations: {0}", durations);
      if (durations.isEmpty()) {
        xLogger.severe("No durations to notify for domain {0}", domainId);
        return;
      }
      Iterator<Entry<Integer, Duration>> it = durations.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, Duration> d = it.next();
        Duration duration = d.getValue();
        String queryStr = "SELECT FROM " + JDOUtils.getImplClass(IEvent.class).getName() +
            " WHERE dId.contains(dIdParam) && t > startParam";
        String paramStr = " PARAMETERS Long dIdParam, Date startParam";
        if (duration.end != null) {
          queryStr += " && t < endParam";
          paramStr += ", Date endParam";
        }
        queryStr += paramStr + " import java.util.Date; ORDER BY t DESC";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dIdParam", domainId);
        params.put("startParam",
            LocalDateUtil.getOffsetDate(duration.start, -1, Calendar.MILLISECOND));
        if (duration.end != null) {
          params.put("endParam", duration.end);
        }
        xLogger.fine("Query: {0}", queryStr);
        QueryParams qp = new QueryParams(queryStr, params);
        PagedExec.Finalizer finalizer = new PagedExec.Finalizer();
        finalizer.url =
            EVENT_NOTIFICATION_FINALIZER_URL + "?action=batchnotify&domainid=" + domainId;
        finalizer.queue = ITaskService.QUEUE_MESSAGE;
        xLogger.fine("Calling exec...");
        // Execute
        Executor.exec(domainId, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
            EventNotificationProcessor.class.getName(),
            new EventNotificationData(d.getKey().intValue()).toJSONString(), finalizer);
      }
    } catch (Exception e) {
      xLogger.severe("{0} in domain {1} when trying to notify events: {2}", e.getClass().getName(),
          domainId, e.getMessage());
    }
    xLogger.fine("Exiting EventNotifier.notify");
  }

  // Get the time-ranges for generating event data - daily, weekly, monthy
  public static Map<Integer, Duration> getDurations(CustomDuration customDuration, Long domainId) {
    // Get the durations
    Map<Integer, Duration> durations = new HashMap<Integer, Duration>();
    if (customDuration != null) {
      if (customDuration.duration != null) {
        durations.put(new Integer(customDuration.frequency), customDuration.duration);
      }
      return durations;
    }
    String timezone = DomainConfig.getInstance(domainId).getTimezone();
    // Get a day's time range (12am to 12am)
    Calendar cal = LocalDateUtil.getZeroTime(timezone);
    Duration daily = new Duration();
    cal.add(Calendar.DATE, -1);
    daily.start = cal.getTime();
    durations.put(new Integer(EventSpec.NotifyOptions.DAILY), daily); // add daily
    // Reset time to now
    cal = LocalDateUtil.getZeroTime(timezone);
    if (ConfigUtil.getBoolean("notifications.weekly", false)
        && cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
      Duration weekly = new Duration();
      cal.add(Calendar.DATE, -7);
      weekly.start = cal.getTime();
      durations.put(new Integer(EventSpec.NotifyOptions.WEEKLY), weekly);
      // Reset time to now
      cal = LocalDateUtil.getZeroTime(timezone);
    }

    if (ConfigUtil.getBoolean("notifications.monthly", false)
        && cal.get(Calendar.DAY_OF_MONTH) == 1) {
      Duration monthly = new Duration();
      // Move calendar to last day of previous month
      cal.add(Calendar.MONTH, -1);
      monthly.start = cal.getTime();
      durations.put(new Integer(EventSpec.NotifyOptions.MONTHLY), monthly);
    }
    return durations;
  }

  public static boolean isEventValid(IEvent event, DomainConfig domainConfig,
                                     PersistenceManager persistenceManager) {
    String objectId = null;
    try {
      String objectType = event.getObjectType();
      objectId = event.getObjectId();
      Long domainId = event.getDomainId();
      EventGenerator eventGenerator = EventGeneratorFactory.getEventGenerator(domainId, objectType);
      return eventGenerator.isEventValid(event, persistenceManager);
    } catch (Exception e) {
      xLogger.severe(
          "{0} when getting message and subscribers for event {1}:{2} in domain {3}, object Id = {4}: {5}",
          e.getClass().getName(), event.getId(), event.getObjectType(), event.getDomainId(),
          objectId, e.getMessage());
    }
    return true;
  }

  public static MessageSubscribers getMessageSubcribers(IEvent event, int frequency,
                                                        boolean isBatch, DomainConfig dc,
                                                        PersistenceManager pm, Long domainId) {
    MessageSubscribers ms = new MessageSubscribers();
    String objectId = null;
    try {
      String objectType = event.getObjectType();
      objectId = event.getObjectId();
      Long eventDomainId = event.getDomainId();
      // Get the event's custom notification options - message and userIds
      String message = event.getMessage();
      Map<Integer, List<String>> userIdsMap = event.getUserIds();
      boolean hasMessage = message != null && !message.isEmpty();
      boolean hasUsers = userIdsMap != null && !userIdsMap.isEmpty();
      // Get the event generator
      EventGenerator eg = EventGeneratorFactory.getEventGenerator(domainId, objectType);
      EventSpec.ParamSpec
          paramSpec = eg.match(event.getId(), event.getParams());
      if (paramSpec == null) {
        if (eventDomainId.equals(domainId)) {
          xLogger.warn(
              "No param spec. found for Object {0} ({1}) not found when notifying event in domain {2}",
              objectId, objectType, eventDomainId);
        }
        return null;
      }
      ms.paramSpec = paramSpec;

      Locale locale = dc.getLocale();
      if (locale == null) {
        locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
      }
      // Get the object of the event, if no custom message is present
      Object o = null;
      try {
        if (objectId != null) {
          o =
              pm.getObjectById(Class.forName(objectType),
                  AppFactory.get().getDaoUtil().createKeyFromString(objectId));
        }
      } catch (JDOObjectNotFoundException e1) {
        xLogger.warn("Object {0} ({1}) not found when notify event in domain {2}", objectId,
            objectType, eventDomainId);
      }
      // Get the unique subscribers for the given frequencies
      List<String> userIds = null;
      // Check if custom user Ids exist in the event
      if (eventDomainId.equals(domainId) && hasUsers) {
        userIds = userIdsMap.get(frequency);
      }
      if ((userIds == null || userIds.isEmpty())
          && o != null) { // get userIds from the param. spec.
        List<EventSpec.Subscriber> subscriberTypes = paramSpec.getSubscribers(frequency);
        if (subscriberTypes != null && !subscriberTypes.isEmpty()) {
          userIds = new ArrayList<>();
          Set<String> userIdsSet = new HashSet<>();
          for (EventSpec.Subscriber subscriberType : subscriberTypes) {
            List<String> uIds = eg.getSubscriberIds(subscriberType, o, domainId);
            if (uIds != null) {
              userIdsSet.addAll(uIds);
            }
          }
          userIds.addAll(userIdsSet);
        }
      }
      // Get message for SMS notification
      hasUsers = userIds != null && !userIds.isEmpty();
      if (hasMessage) { // prefix date, if batch
        if (isBatch) {
          message =
              LocalDateUtil.format(event.getTimestamp(), locale, dc.getTimezone()) + ": " + message;
        }
      } else if (hasUsers && o != null) {
        message =
            eg.getDisplayString(o, paramSpec, locale, dc.getTimezone(), null,
                event.getTimestamp(), isBatch);
        if (isBatch) {
          message =
              LocalDateUtil.format(event.getTimestamp(), locale, dc.getTimezone()) + ": "
                  + message; // prefix the message with the date, in case of batch/email notifications
        }
      } // Get message for bulletin-board notification, if needed
      if (!isBatch) {
        EventSpec.BBOptions bbOptions = paramSpec.getBBOptions();
        if (bbOptions != null && bbOptions.post) {
          boolean allowBBPost = true;
          // Check if the tag allows posting to BB
          if (bbOptions.tags != null && !bbOptions.tags.isEmpty() && o != null) {
            ObjectData od = eg.getObjectData(o, pm);
            if (od != null) {
              allowBBPost = listContains(od.tags, bbOptions.tags);
            }
          }
          if (allowBBPost) {
            ms.bbMessage =
                eg.getDisplayString(o, paramSpec, locale, dc.getTimezone(), null,
                    event.getTimestamp(), true);
          }
        }
      }
      // Update result with message and subscribers, if any
      ms.message = message;
      ms.userIds = userIds;
    } catch (Exception e) {
      xLogger.severe(
          "{0} when getting message and subscribers for event {1}:{2} in domain {3}, object Id = {4}: {5}",
          e.getClass().getName(), event.getId(), event.getObjectType(), event.getDomainId(),
          objectId, e.getMessage(), e);
    }
    return ms;
  }

  // Generate deferred events for a given domain and a given day - no order activity, order expired, fulfillment due, no inventory activity
  // NOTE: Run this method with sufficient time (mostly as a background task, rather than via a real-time request)
  public static void createDailyEvents(Long domainId) {
    xLogger.fine("Entered createDailyEvents");
    try {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      EventsConfig ec = dc.getEventsConfig();
      // Fulfillment due check
      EventSpec
          fdEventSpec =
          ec.getEventSpec(IEvent.FULFILLMENT_DUE,
              JDOUtils.getImplClass(IOrder.class).getName());
      // Order expired
      EventSpec
          oeEventSpec =
          ec.getEventSpec(IEvent.EXPIRED,
              JDOUtils.getImplClass(IOrder.class).getName());
      // Batch epxpired
      EventSpec
          beEventSpec =
          ec.getEventSpec(IEvent.EXPIRED,
              JDOUtils.getImplClass(IInvntryBatch.class).getName());
      // No order activity
      EventSpec
          noaEventSpec =
          ec.getEventSpec(IEvent.NO_ACTIVITY,
              JDOUtils.getImplClass(IOrder.class).getName());
      // No inventory activity
      EventSpec
          niaEventSpec =
          ec.getEventSpec(IEvent.NO_ACTIVITY,
              JDOUtils.getImplClass(ITransaction.class).getName());
      // No user login
      EventSpec
          nulEventSpec =
          ec.getEventSpec(IEvent.NO_ACTIVITY,
              JDOUtils.getImplClass(IUserAccount.class).getName());
      // User inactive/dormant
      EventSpec
          udiEventSpec =
          ec.getEventSpec(IEvent.EXPIRED,
              JDOUtils.getImplClass(IUserAccount.class).getName());
      //No Data from temperature device
      EventSpec
          ndaEventSpec =
          ec.getEventSpec(IEvent.NO_ACTIVITY,
              JDOUtils.getImplClass(IAssetStatus.class).getName());

      if (noaEventSpec != null || niaEventSpec != null || nulEventSpec != null
          || ndaEventSpec != null) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
          // Check no activity event first (given its efficient to do so)
          if (noaEventSpec != null) // no order check/generation
          {
            generateNoActivityEvent(domainId, JDOUtils.getImplClass(IOrder.class).getName(),
                noaEventSpec, pm);
          }
          if (niaEventSpec != null) // no inventory activty check/generation
          {
            generateNoActivityEvent(domainId, JDOUtils.getImplClass(ITransaction.class).getName(),
                niaEventSpec, pm);
          }
          if (nulEventSpec != null) // no user login activity
          {
            generateNoActivityEvent(domainId, JDOUtils.getImplClass(IUserAccount.class).getName(),
                nulEventSpec, pm);
          }
          if (ndaEventSpec != null) //no data from temperature device
          {
            generateNoActivityEventForAssets(domainId, ndaEventSpec);
          }
        } finally {
          pm.close();
        }
      }

      // Generate Order events, if needed
      if (fdEventSpec != null || oeEventSpec != null) {
        // Get start date (1 day before)
        Calendar cal = LocalDateUtil.getZeroTime(dc.getTimezone());
        cal.add(Calendar.DATE, -1); // previous day
        Date start = LocalDateUtil.getOffsetDate(cal.getTime(), -1, Calendar.MILLISECOND);
        // Get the orders for the previous day
        String
            queryStr =
            "SELECT FROM " + JDOUtils.getImplClass(IOrder.class).getName()
                + " WHERE dId.contains(dIdParam) && cOn > startParam PARAMETERS Long dIdParam, Date startParam import java.util.Date; ORDER BY cOn desc";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dIdParam", domainId);
        params.put("startParam", start);
        QueryParams qp = new QueryParams(queryStr, params);
        // Start paged execution for order event generation
        Executor.exec(domainId, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
            OrderEventsCreationProcessor.class.getName(), null, null);
      }

      // User dormant/inactive event
      if (udiEventSpec != null) {
        // Get inactive duration
        int inactiveDuration = 0;
        String
            inactiveDurationStr =
            udiEventSpec.getParamValue(EventConstants.PARAM_INACTIVEDURATION);
        if (inactiveDurationStr != null && !inactiveDurationStr.isEmpty()) {
          try {
            inactiveDuration = Integer.parseInt(inactiveDurationStr);
          } catch (NumberFormatException e) {
            xLogger.warn("Invalid number {0} for inactive duration: {1}", inactiveDurationStr,
                e.getMessage());
          }
        }
        if (inactiveDuration > 0) {
          String
              queryStr =
              "SELECT FROM " + JDOUtils.getImplClass(IUserAccount.class).getName()
                  + " WHERE sdId == dIdParam && (lastLogin == null || lastLogin < endParam) && (lre == null || lre < endParam) PARAMETERS Long dIdParam, Date endParam import java.util.Date; ORDER BY lastLogin desc";
          Map<String, Object> params = new HashMap<String, Object>();
          params.put("dIdParam", domainId);
          params.put("endParam", LocalDateUtil.getOffsetDate(new Date(), -1 * inactiveDuration));
          QueryParams qp = new QueryParams(queryStr, params);
          // Start paged execution for user inactivity event generation
          Executor.exec(domainId, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
              UserEventsCreationProcessor.class.getName(), null, null);
        }
      }

      // Batch expiry events, if needed
      if (beEventSpec != null) {
        generateBatchExpiryEvents(domainId, beEventSpec);
      }
    } catch (Exception e) {
      xLogger.severe("{0} when creating daily events in domain {1}: {2}", e.getClass().getName(),
          domainId, e.getMessage());
    }
    xLogger.fine("Exiting createDailyEvents");
  }

  // Check and generate no activity events
  private static void generateNoActivityEvent(Long domainId, String objectType, EventSpec es,
                                              PersistenceManager pm) {
    xLogger.fine("Entered generateNoActivityEvents");
    // Get the number of inactive days
    Map<String, EventSpec.ParamSpec> paramSpecs = es.getParamSpecs();
    if (paramSpecs == null || paramSpecs.isEmpty()) {
      return;
    }
    DomainConfig dc = DomainConfig.getInstance(domainId);
    // Iterate on paramSpecs
    Iterator<EventSpec.ParamSpec> it = paramSpecs.values().iterator();
    while (it.hasNext()) {
      EventSpec.ParamSpec paramSpec = it.next();
      // Get inactive duration offset
      String inactiveDurationStr = null;
      int inactiveDuration = 0;
      Map<String, Object> eventParams = paramSpec.getParams();
      if (eventParams != null && !eventParams.isEmpty()) {
        inactiveDurationStr = (String) eventParams.get(EventConstants.PARAM_INACTIVEDURATION);
        if (inactiveDurationStr != null && !inactiveDurationStr.isEmpty()) {
          try {
            inactiveDuration = Integer.parseInt(inactiveDurationStr);
          } catch (NumberFormatException e) {
            xLogger.warn(
                "Invalid number {0} for inactiveDuration while generating no activity event for object-type {1} in domain {2}. Message: {3} ",
                inactiveDurationStr, objectType, domainId, e.getMessage());
          }
        }
        if (inactiveDuration < 0) {
          xLogger.warn(
              "Invalid duration when generating no activity events in domain {0} for object-type {1}: {2}",
              domainId, objectType, inactiveDuration);
          return;
        }
        // Get start date
        Calendar cal = LocalDateUtil.getZeroTime(dc.getTimezone());
        cal.add(Calendar.DATE, -1 * inactiveDuration);
        Date start = LocalDateUtil.getOffsetDate(cal.getTime(), -1, Calendar.MILLISECOND);
        boolean generateEvent = hasNoActivity(domainId, objectType, start, pm);
        // Generate no activity event, if necessary
        if (generateEvent) {
          try {
            EventGenerator.generateNoActivityEvent(domainId, objectType, paramSpec);
            xLogger
                .info("No activity event generated for objectType {0} in domain {1} ", objectType,
                    domainId);
          } catch (EventGenerationException e) {
            xLogger.severe(
                "EventHandlingException when generating no activity event for {0} in domain {1}",
                objectType, domainId, e);
          }
        }
      }
    }
    xLogger.fine("Exiting generateNoActivityEvents");
  }

  // Private method that generates no activity event for temperature device.
  private static void generateNoActivityEventForAssets(Long domainId, EventSpec eventSpec)
      throws TaskSchedulingException, ProcessingException {
    // Get the param specs from eventSpec. It could be more than one. Iterate through each paramspec
    Map<String, EventSpec.ParamSpec> paramSpecs = eventSpec.getParamSpecs();
    if (paramSpecs == null || paramSpecs.isEmpty()) {
      return;
    }

    Map<String, Object> params = new HashMap<>(1);

    // Iterate on paramSpecs
    Iterator<EventSpec.ParamSpec> it = paramSpecs.values().iterator();
    while (it.hasNext()) {
      EventSpec.ParamSpec paramSpec = it.next();
      // Get inactive duration offset
      String inactiveDurationStr;
      int inactiveDuration = 0;
      Map<String, Object> eventParams = paramSpec.getParams();
      if (eventParams != null && !eventParams.isEmpty()) {
        inactiveDurationStr = (String) eventParams.get(EventConstants.PARAM_INACTIVEDURATION);
        if (inactiveDurationStr != null && !inactiveDurationStr.isEmpty()) {
          try {
            inactiveDuration = Integer.parseInt(inactiveDurationStr);
          } catch (NumberFormatException e) {
            xLogger.warn(
                "Invalid number {0} for inactiveDuration while generating temperature event in domain {1}. Message: {2} ",
                inactiveDurationStr, domainId, e.getMessage());
          }
        }
        // Get the offset date using inactiveDuration. ( Offset date = today - inactiveDuration number of days )
        if (inactiveDuration > 0) {
          AssetSystemConfig config = null;
          String csv = null;
          try {
            config = AssetSystemConfig.getInstance();
            Map<Integer, AssetSystemConfig.Asset>
                monitoredAssets =
                config.getAssetsByType(IAsset.MONITORED_ASSET);
            csv = monitoredAssets.keySet().toString();
            csv = csv.substring(1, csv.length() - 1);
          } catch (ConfigurationException e) {
            //do nothing
            xLogger.warn("Exception while getting asset configuration", e);
          }

          StringBuilder
              query =
              new StringBuilder(
                  "select ID,A.* from ASSETSTATUS A where A.ASSETID in ( select AD.ID_OID from ASSET_DOMAINS AD where EXISTS(SELECT 1 FROM ASSETRELATION AR WHERE AD.ID_OID = AR.ASSETID AND R.TYPE=2) AND AD.DOMAIN_ID = ")
                  .append(domainId)
                  .append(" ) AND A.TYPE = 3 AND A.STATUS = 1");
          if (csv != null) {
            query =
                new StringBuilder(
                    "select ID,A.* from ASSETSTATUS A where A.ASSETID in ( select ID from ASSET where ID in (select AD.ID_OID from ASSET_DOMAINS AD where EXISTS(SELECT 1 FROM ASSETRELATION AR WHERE AD.ID_OID = AR.ASSETID AND AR.TYPE=2) AND AD.DOMAIN_ID = ")
                    .append(domainId)
                    .append(") and TYPE in (")
                    .append(csv)
                    .append(")) AND A.TYPE = 3 AND A.STATUS = 1");
          }

          Date startDate = LocalDateUtil.getOffsetDate(new Date(), -1 * inactiveDuration);
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          query.append(" AND TS < TIMESTAMP('").append(sdf.format(startDate)).append("')");
          QueryParams
              qp =
              new QueryParams(query.toString(), params, QueryParams.QTYPE.SQL, IAssetStatus.class);
          // Start paged execution for device no activity event generation
          Executor.exec(domainId, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
              AssetEventsCreationProcessor.class.getName(), inactiveDurationStr, null);
        }
      }
    }
  }

  // Check whether any activity exists for the given objectType since the start date
  @SuppressWarnings({"rawtypes"})
  public static boolean hasNoActivity(Long domainId, String objectType, Date start,
                                      PersistenceManager pm) {
    xLogger.fine("Entered hasNoActivityEvent");
    boolean hasNoActivity = false;
    PageParams
        pageParams =
        new PageParams(null,
            1); // just get 1 result, since we are checking if any results are present at all
    // Check order activity
    if (JDOUtils.getImplClass(IOrder.class).getName().equals(objectType)) {
      // Orders created?
      boolean noOrdersCreated = true;
      String
          queryStr =
          "SELECT id FROM " + objectType
              + " WHERE dId.contains(dIdParam) && cOn > startParam PARAMETERS Long dIdParam, Date startParam import java.util.Date; ORDER BY cOn desc"; // keys only query for efficiency
      Query q = pm.newQuery(queryStr);
      QueryUtil.setPageParams(q, pageParams);
      try {
        List results = (List) q.execute(domainId, start);
        noOrdersCreated = results == null || results.isEmpty();
      } finally {
        q.closeAll();
      }
      // Orders status changed?
      boolean noOrderStatusChanges = true;
      queryStr =
          "SELECT id FROM " + objectType
              + " WHERE dId.contains(dIdParam) && stOn > startParam PARAMETERS Long dIdParam, Date startParam import java.util.Date; ORDER BY stOn desc"; // keys only query for efficiency
      q = pm.newQuery(queryStr);
      QueryUtil.setPageParams(q, pageParams);
      try {
        List results = (List) q.execute(domainId, start);
        noOrderStatusChanges = results == null || results.isEmpty();
      } finally {
        q.closeAll();
      }
      // Generate no order activity event, if needed
      hasNoActivity = noOrdersCreated && noOrderStatusChanges;
    } else if (JDOUtils.getImplClass(ITransaction.class).getName().equals(objectType)) {
      String
          queryStr =
          "SELECT key FROM " + objectType
              + " WHERE dId.contains(dIdParam) && t > startParam PARAMETERS Long dIdParam, Date startParam import java.util.Date; ORDER BY t desc";
      Query q = pm.newQuery(queryStr);
      QueryUtil.setPageParams(q, pageParams);
      try {
        List results = (List) q.execute(domainId, start);
        hasNoActivity = results == null || results.isEmpty();
      } finally {
        q.closeAll();
      }
    } else if (JDOUtils.getImplClass(IUserAccount.class).getName()
        .equals(objectType)) { // no user login activity
      String
          queryStr =
          "SELECT userId FROM " + objectType
              + " WHERE sdId == dIdParam && (lastLogin > startParam || lre > startParam) PARAMETERS Long dIdParam, Date startParam import java.util.Date; ORDER BY lastLogin desc";
      Query q = pm.newQuery(queryStr);
      QueryUtil.setPageParams(q, pageParams);
      try {
        List results = (List) q.execute(domainId, start);
        hasNoActivity = results == null || results.isEmpty();
      } finally {
        q.closeAll();
      }
    }
    xLogger.fine("Exiting generateNoActivityEvents");
    return hasNoActivity;
  }



  // Generate batch expiry events, if any
  private static void generateBatchExpiryEvents(Long domainId, EventSpec eventSpec)
      throws TaskSchedulingException, ProcessingException {
    xLogger.fine("Entered generateBatchExpiryEvents");
    Map<String, EventSpec.ParamSpec> paramSpecs = eventSpec.getParamSpecs();
    if (paramSpecs == null || paramSpecs.isEmpty()) {
      return;
    }
    // Form query (with partial params)
    String
        queryStr =
        "SELECT FROM " + JDOUtils.getImplClass(IInvntryBatch.class).getName()
            + " WHERE dId.contains(dIdParam) && vld == vldParam && bexp == bexpParam PARAMETERS Long dIdParam, Boolean vldParam, Date bexpParam import java.util.Date; ORDER BY bexp ASC";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("dIdParam", domainId);
    params.put("vldParam", Boolean.TRUE);
    // Iterate on params.
    Iterator<EventSpec.ParamSpec> it = paramSpecs.values().iterator();
    while (it.hasNext()) {
      EventSpec.ParamSpec paramSpec = it.next();
      // Get expiry offset
      String expiresInDaysStr = null;
      int expiresInDays = 0;
      Map<String, Object> eventParams = paramSpec.getParams();
      if (eventParams != null && !eventParams.isEmpty()) {
        expiresInDaysStr = (String) eventParams.get(EventConstants.PARAM_EXPIRESINDAYS);
        if (expiresInDaysStr != null && !expiresInDaysStr.isEmpty()) {
          try {
            expiresInDays = Integer.parseInt(expiresInDaysStr);
          } catch (NumberFormatException e) {
            xLogger.warn("Invaild number {0} for expiresInDays: {1}", expiresInDaysStr,
                e.getMessage());
          }
        }
      }
      // Today, with duration offset
      Calendar calExpiry = LocalDateUtil
          .getZeroTime(DomainConfig.getInstance(domainId).getTimezone());
      if (expiresInDays != 0) {
        calExpiry.add(Calendar.DATE, expiresInDays); // push expiry by X days
      }
      //Convert to server's date with zero time
      Calendar localExpiry = Calendar.getInstance();
      localExpiry.setTime(calExpiry.getTime());
      localExpiry = LocalDateUtil.resetTimeFields(localExpiry);
      // Update params
      params.put("bexpParam", localExpiry.getTime());
      QueryParams qp = new QueryParams(queryStr, params);
      PageParams pageParams = new PageParams(null, PageParams.DEFAULT_SIZE);
      // Execute
      Executor.exec(domainId, qp, pageParams, BatchExpiryEventsCreationProcessor.class.getName(),
          expiresInDaysStr, null);
    } // end while
    xLogger.fine("Exiting generateBatchExpiryEvents");
  }

  // Check if any item in list A is present in list B
  public static boolean listContains(List<String> a, List<String> b) {
    boolean hasA = a != null && !a.isEmpty();
    boolean hasB = b != null && !b.isEmpty();
    if (!hasA && !hasB) {
      return true;
    }
    if ((!hasA && hasB) || (hasA && !hasB)) {
      return false;
    }
    Iterator<String> itA = a.iterator();
    while (itA.hasNext()) {
      if (b.contains(itA.next())) {
        return true;
      }
    }
    return false;
  }

  public static Map<String, Object> getTagParams(String objectType, String objectId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Object o;
    try {
      if (objectId != null) {
        o =
            pm.getObjectById(Class.forName(objectType),
                AppFactory.get().getDaoUtil().createKeyFromString(objectId));
        return getTagParams(o);
      }
    } catch (Exception e) {
      xLogger.warn("Object {0} ({1}) not found when notify event ", objectId,
          objectType);
    } finally {
      pm.close();
    }
    return null;
  }

  // Get Tag Params
  public static Map<String, Object> getTagParams(Object eventObject) {
    Map<String, Object> params = new HashMap<String, Object>(1);
    List<String> mTags = null, eTags = null, oTags = null;

    if (eventObject instanceof ITransaction) {
      mTags = ((ITransaction) eventObject).getTags(TagUtil.TYPE_MATERIAL);
      eTags = ((ITransaction) eventObject).getTags(TagUtil.TYPE_ENTITY);
    } else if (eventObject instanceof IInvntry) {
      mTags = ((IInvntry) eventObject).getTags(TagUtil.TYPE_MATERIAL);
      eTags = ((IInvntry) eventObject).getTags(TagUtil.TYPE_ENTITY);
    } else if (eventObject instanceof IMaterial) {
      mTags = ((IMaterial) eventObject).getTags();
    } else if (eventObject instanceof IKiosk) {
      eTags = ((IKiosk) eventObject).getTags();
    } else if (eventObject instanceof IOrder) {
      eTags = ((IOrder) eventObject).getTags(TagUtil.TYPE_ENTITY);
      oTags = ((IOrder) eventObject).getTags(TagUtil.TYPE_ORDER);
    } else if (eventObject instanceof IShipment) {
        try {
            OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
            eTags = oms.getOrder(((IShipment) eventObject).getOrderId()).getTags(TagUtil.TYPE_ENTITY);
            oTags = oms.getOrder(((IShipment) eventObject).getOrderId()).getTags(TagUtil.TYPE_ORDER);
        } catch (ObjectNotFoundException e) {
            xLogger.warn("Exception while getting shipment tags", e);
        } catch (ServiceException e) {
            xLogger.warn("ServiceException while getting order of shipment in getTagParams", e);
        }
    } else if (eventObject instanceof IInvntryBatch) {
      mTags = ((IInvntryBatch) eventObject).getTags(TagUtil.TYPE_MATERIAL);
      eTags = ((IInvntryBatch) eventObject).getTags(TagUtil.TYPE_ENTITY);
    } else if (eventObject instanceof IAssetStatus) {
      try {
        IAsset
            asset =
            JDOUtils.getObjectById(IAsset.class, ((IAssetStatus) eventObject).getAssetId());
        if(asset.getKioskId() != null) {
          IKiosk kiosk = JDOUtils.getObjectById(IKiosk.class, asset.getKioskId());
          eTags = kiosk.getTags();
        }
      } catch (Exception e) {
        xLogger.warn("Failed to get tags for Kiosk related to asset st:{0} and id:{1}",
            ((IAssetStatus) eventObject).getId(), ((IAssetStatus) eventObject).getAssetId(), e);
      }
    }

    if (mTags != null && !mTags.isEmpty()) {
      params.put(EventConstants.PARAM_MATERIALTAGSTOEXCLUDE, mTags);
    }
    if (eTags != null && !eTags.isEmpty()) {
      params.put(EventConstants.PARAM_ENTITYTAGSTOEXCLUDE, eTags);
    }
    if (oTags != null && !oTags.isEmpty()) {
      params.put(EventConstants.PARAM_ORDERTAGSTOEXCLUDE, oTags);
    }
    return params.isEmpty() ? null : params;
  }

  public static void generateEvent(EventData eventData) {

  }


  public static class CustomDuration {
    public Duration duration = null;
    public int frequency = EventSpec.NotifyOptions.DAILY;
  }

	/*
        // Check if any item in list A is present in list B
	public static boolean contains(List<String> a, List<String> b) {
		if ( a == null || a.isEmpty() || b == null || b.isEmpty() )
			return false;
		for( String str : a ) {
			if ( b.contains( str ) ) {
				return true;
			}
		}
		return false;
	}*/

  public static class Duration {
    public Date start = null;
    public Date end = null;
  }

  // Get the message and a list of subscribers, given an event and its associated object
  public static class MessageSubscribers {
    public String message = null;
    public List<String> userIds = null;
    public String bbMessage = null; // message to post to bulletin-board, if any
    public ObjectData objectData = null;
    public EventSpec.ParamSpec paramSpec;

    public boolean isValid() {
      return (message != null && !message.isEmpty() && userIds != null && !userIds.isEmpty()) || (
          bbMessage != null && !bbMessage.isEmpty());
    }
  }


}
