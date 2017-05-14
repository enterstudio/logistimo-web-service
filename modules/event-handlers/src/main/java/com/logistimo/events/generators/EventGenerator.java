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
package com.logistimo.events.generators;

import com.logistimo.accounting.entity.IAccount;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventSpec.ParamComparator;
import com.logistimo.config.models.EventSpec.ParamSpec;
import com.logistimo.config.models.EventSpec.Subscriber;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.events.EventConstants;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.exceptions.EventGenerationException;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.events.handlers.EventHandlingException;
import com.logistimo.events.models.CustomOptions;
import com.logistimo.events.models.ObjectData;
import com.logistimo.events.templates.ITemplate;
import com.logistimo.events.templates.TemplateUtils;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.CommonUtils;
import com.logistimo.utils.LocalDateUtil;

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

import javax.jdo.PersistenceManager;


/**
 * @author Arun
 */
public class EventGenerator {

  // Logger
  protected static final XLog xLogger = XLog.getLog(EventGenerator.class);
  protected Long domainId = null;
  protected String objectType = null; // class name of object on which the event occurs



  public EventGenerator(Long domainId, String objectType) {
    this.domainId = domainId;
    this.objectType = objectType;
  }

  // Generate no activity event
  public static void generateNoActivityEvent(Long domainId, String objectType, ParamSpec paramSpec)
      throws EventGenerationException {
    xLogger.fine("Entered generateNoActivityEvent");
    CustomOptions customOptions = new CustomOptions();
    customOptions.message =
        paramSpec
            .getMessageTemplate(); // use the message template as is; no variable replacement possible here, given no objects
    // Check if options are specified for Administrators or Users only
    Map<Integer, List<Subscriber>> subscriberMap = paramSpec.getSubcribers();
    // Get the domain owners
    if (subscriberMap == null || subscriberMap.isEmpty()) {
      return;
    }
    EventGenerator eg = EventGeneratorFactory.getEventGenerator(domainId, objectType);
    customOptions.userIds = new HashMap<Integer, List<String>>();
    Iterator<Entry<Integer, List<Subscriber>>> it = subscriberMap.entrySet().iterator();
    while (it.hasNext()) {
      Entry<Integer, List<Subscriber>> entry = it.next();
      Integer freq = entry.getKey();
      // Get users Id list from custom options
      List<String> customUserIds = customOptions.userIds.get(freq);
      if (customUserIds == null) {
        customUserIds = new ArrayList<String>();
        customOptions.userIds.put(freq, customUserIds);
      }
      List<Subscriber> subscribers = entry.getValue();
      if (subscribers == null || subscribers.isEmpty()) {
        continue;
      }
      Iterator<Subscriber> subsIt = subscribers.iterator();
      while (subsIt.hasNext()) {
        Subscriber s = subsIt.next();
        List<String> userIds = null;
        if (Subscriber.ADMINISTRATORS.equals(s.type)) {
          userIds = eg.getSubscriberIds(s, domainId);
        } else if (Subscriber.USERS.equals(s.type)) {
          userIds = s.ids;
        }
        // Add unique user Ids to custom user Ids
        if (userIds == null || userIds.isEmpty()) {
          continue;
        }
        Iterator<String> userIdsIt = userIds.iterator();
        while (userIdsIt.hasNext()) {
          String userId = userIdsIt.next();
          if (!customUserIds.contains(userId)) {
            customUserIds.add(userId);
          }
        }
      }
    }
    // Generate event
    if (!customOptions.userIds.isEmpty()) {
      Map<String, Object> params = paramSpec.getParams();
      if (params == null) {
        params = new HashMap<>(1);
      }
      params.put(EventConstants.EVENT_TIME,
          LocalDateUtil.getZeroTime(DomainConfig.getInstance(domainId).getTimezone()).getTime());
      eg.generate(IEvent.NO_ACTIVITY, params, null, customOptions);
    }
    xLogger.fine("Exiting generateNoActivityEvent");
  }

  // Get the location of a given object
  public static void updateObjectLocation(ObjectData od, PersistenceManager pm) {
    if (od == null || od.kioskId == null) {
      return;
    }
    try {
      IKiosk k = JDOUtils.getObjectById(IKiosk.class, od.kioskId, pm);
      od.city = k.getCity();
      od.state = k.getState();
      od.district = k.getDistrict();
    } catch (Exception e) {
      xLogger.warn("{0} when getting kiosk {1} to update object data location: {2}",
          e.getClass().getName(), od.kioskId, e);
    }
  }

  // Get inactive duration start date from event params
  protected static Date getInactiveDurationStart(IEvent event) {
    int inactiveDurationDays = getInactiveDuration((Map<String, Object>) event.getParams());
    if (inactiveDurationDays > 0) {
      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(event.getTimestamp());
      cal.add(Calendar.DATE, -1 * inactiveDurationDays);
      return cal.getTime();
    }
    return null;
  }

  // Get inactive duration from event params
  protected static int getInactiveDuration(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return 0;
    }
    String inactiveDurationStr = (String) params.get(EventConstants.PARAM_INACTIVEDURATION);
    if (inactiveDurationStr == null || inactiveDurationStr.isEmpty()) {
      return 0;
    }
    try {
      return Integer.parseInt(inactiveDurationStr);
    } catch (NumberFormatException e) {
      xLogger.warn("Invalid number for inactive duration: {0}", inactiveDurationStr);
    }
    return 0;
  }

  // Exclude disabled user Ids
  private static List<String> getEnabledUserIds(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return userIds;
    }
    List<String> eUids;
    try {
      UsersService as = Services.getService(UsersServiceImpl.class);
      eUids = as.getEnabledUserIds(userIds);
    } catch (ServiceException e) {
      eUids = null;
    }
    return eUids;
  }

  // Exclude disabled user Ids
  private static List<String> getEnabledUserIdsWithTags(List<String> userTags, Long domainId) {
    if (userTags == null || userTags.isEmpty()) {
      return userTags;
    }
    List<String> eUids;
    try {
      UsersService as = Services.getService(UsersServiceImpl.class);
      eUids = as.getEnabledUserIdsWithTags(userTags, domainId);
    } catch (ServiceException e) {
      eUids = null;
    }
    return eUids;
  }

  public static boolean tagParamsNotMatched(Map<String, Object> params1,
                                            Map<String, Object> params2, String tagType) {
    List<String> t1 = null, t2 = null;
    if (params1 != null && !params1.isEmpty()) {
      if (TagUtil.TYPE_ENTITY.equals(tagType)) {
        t1 = (List<String>) params1.get(EventConstants.PARAM_ENTITYTAGSTOEXCLUDE);
      } else if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
        t1 = (List<String>) params1.get(EventConstants.PARAM_MATERIALTAGSTOEXCLUDE);
      } else if (TagUtil.TYPE_ORDER.equals(tagType)) {
        t1 = (List<String>) params1.get(EventConstants.PARAM_ORDERTAGSTOEXCLUDE);
      }
    }
    if (params2 != null && !params2.isEmpty()) {
      if (TagUtil.TYPE_ENTITY.equals(tagType)) {
        t2 = (List<String>) params2.get(EventConstants.PARAM_ENTITYTAGSTOEXCLUDE);
      } else if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
        t2 = (List<String>) params2.get(EventConstants.PARAM_MATERIALTAGSTOEXCLUDE);
      } else if (TagUtil.TYPE_ORDER.equals(tagType)) {
        t2 = (List<String>) params2.get(EventConstants.PARAM_ORDERTAGSTOEXCLUDE);
      }
    }

    return !CommonUtils.listContains(t1, t2);
  }

  public static boolean paramMatched(Map<String, Object> params1, Map<String, Object> params2,
                                     String paramName) {
    String paramValue1 = null, paramValue2 = null;
    boolean hasParamValue1 = false, hasParamValue2 = false;
    if (params1 != null && !params1.isEmpty()) {
      paramValue1 = (String) params1.get(paramName);
    }
    if (params2 != null && !params2.isEmpty()) {
      paramValue2 = (String) params2.get(paramName);
    }
    hasParamValue1 = paramValue1 != null && !paramValue1.isEmpty();
    hasParamValue2 = paramValue2 != null && !paramValue2.isEmpty();
    boolean paramMatched = false;
    if (!hasParamValue1 && !hasParamValue2) {
      paramMatched = true;
    } else if (!hasParamValue1 && hasParamValue2 || hasParamValue1 && !hasParamValue2) {
      paramMatched = false;
    } else {
      if (paramName != null && !paramName.isEmpty()) {
        if (paramName.equalsIgnoreCase(EventConstants.PARAM_INACTIVEDURATION)) {
          if (Integer.parseInt(paramValue1) == Integer.parseInt(paramValue2)) {
            paramMatched = true;
          } else {
            paramMatched = false;
          }
        } else if (paramName.equalsIgnoreCase(EventConstants.PARAM_REMINDMINSAFTER)) {
          if (Integer.parseInt(paramValue1) == Integer.parseInt(paramValue2)) {
            paramMatched = true;
          } else {
            paramMatched = false;
          }
        } else if (paramName
            .equalsIgnoreCase(EventConstants.PARAM_STOCKCOUNTTHRESHOLD)) {
          if (Float.parseFloat(paramValue1) >= Float.parseFloat(paramValue2)) {
            paramMatched = true;
          } else {
            paramMatched = false;
          }
        } else {
          paramMatched = paramValue1.equalsIgnoreCase(paramValue2);
        }
      }

    }
    return paramMatched;
  }

  public String getObjectType() {
    return objectType;
  }

  // Check if an event is configured and matches the specified parameter conditions, if any
  // The matched paramSpec is returned, otherwise null
  public ParamSpec match(int eventId, Map<String, Object> params) {
    DomainConfig dc = DomainConfig.getInstance(domainId);
    EventsConfig ec = dc.getEventsConfig();
    ParamComparator paramComparator = null;
    if (eventId == IEvent.NO_ACTIVITY) { // no activity event comparator
      paramComparator = new ParamComparator() {
        @Override
        public boolean compare(Map<String, Object> params1,
                               Map<String, Object> params2) { // params1 is from actual event; params2 is from event spec.
          String t1 = (String) params1.get(EventConstants.PARAM_INACTIVEDURATION);
          String t2 = (String) params2.get(EventConstants.PARAM_INACTIVEDURATION);
          if (t1 != null && t2 != null) {
            return Float.parseFloat(t1) >= Float.parseFloat(t2);
          } else {
            return false;
          }
        }
      };
    }
    if (JDOUtils.getImplClass(IKiosk.class).getName().equals(objectType)) {
      if (eventId == IEvent.CREATED || eventId == IEvent.MODIFIED || eventId == IEvent.DELETED) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            return tagParamsNotMatched(params1, params2, TagUtil.TYPE_ENTITY);
          }
        };
      }
    } else if (JDOUtils.getImplClass(IMaterial.class).getName().equals(objectType)) {
      if (eventId == IEvent.CREATED || eventId == IEvent.MODIFIED || eventId == IEvent.DELETED) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            return tagParamsNotMatched(params1, params2, TagUtil.TYPE_MATERIAL);
          }
        };
      }

    } else if (JDOUtils.getImplClass(IInvntry.class).getName().equals(objectType)) {
      if (eventId == IEvent.CREATED || eventId == IEvent.MODIFIED || eventId == IEvent.DELETED
          || eventId == IEvent.STOCKOUT
          || eventId == IEvent.UNDERSTOCK || eventId == IEvent.OVERSTOCK
          || eventId == IEvent.STOCK_REPLENISHED) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            return tagParamsNotMatched(params1, params2, TagUtil.TYPE_ENTITY)
                && tagParamsNotMatched(params1, params2, TagUtil.TYPE_MATERIAL);
          }
        };
      }
    } else if (JDOUtils.getImplClass(ITransaction.class).getName().equals(objectType)) {
      if (eventId == IEvent.STOCK_COUNTED || eventId == IEvent.STOCK_ISSUED
          || eventId == IEvent.STOCK_RECEIVED || eventId == IEvent.STOCK_TRANSFERRED
          || eventId == IEvent.STOCK_WASTED) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            boolean
                tagParamsNotMatched =
                tagParamsNotMatched(params1, params2, TagUtil.TYPE_MATERIAL) && tagParamsNotMatched(
                    params1, params2, TagUtil.TYPE_ENTITY);
            boolean
                paramMatched =
                paramMatched(params1, params2, EventConstants.PARAM_STATUS)
                    && paramMatched(params1, params2, EventConstants.PARAM_REASON);
            return paramMatched && tagParamsNotMatched;
          }
        };
      }
    } else if (JDOUtils.getImplClass(IOrder.class).getName().equals(objectType)) {
      if (eventId == IEvent.CREATED || eventId == IEvent.MODIFIED || eventId == IEvent.PAID
          || eventId == IEvent.FULFILLMENT_DUE) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            return tagParamsNotMatched(params1, params2, TagUtil.TYPE_ORDER) && tagParamsNotMatched(
                params1, params2, TagUtil.TYPE_ENTITY);

          }
        };
      } else if (eventId == IEvent.STATUS_CHANGE) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            boolean
                tagParamsNotMatched =
                tagParamsNotMatched(params1, params2, TagUtil.TYPE_ORDER) && tagParamsNotMatched(
                    params1, params2, TagUtil.TYPE_ENTITY);
            boolean
                paramMatched =
                paramMatched(params1, params2, OrdersEventGenerator.PARAM_STATUS);
            return paramMatched && tagParamsNotMatched;

          }
        };
      } else if (eventId == IEvent.EXPIRED) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            boolean
                tagParamsNotMatched =
                tagParamsNotMatched(params1, params2, TagUtil.TYPE_ORDER) && tagParamsNotMatched(
                    params1, params2, TagUtil.TYPE_ENTITY);
            boolean
                paramMatched =
                paramMatched(params1, params2, EventConstants.PARAM_INACTIVEDURATION);
            return paramMatched && tagParamsNotMatched;

          }
        };
      }
    } else if (JDOUtils.getImplClass(IShipment.class).getName().equals(objectType)) {
      if (eventId == IEvent.CREATED || eventId == IEvent.MODIFIED) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            return tagParamsNotMatched(params1, params2, TagUtil.TYPE_ORDER) && tagParamsNotMatched(
                    params1, params2, TagUtil.TYPE_ENTITY);

          }
        };
      } else if (eventId == IEvent.STATUS_CHANGE) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            boolean
                    tagParamsNotMatched =
                    tagParamsNotMatched(params1, params2, TagUtil.TYPE_ORDER) && tagParamsNotMatched(
                            params1, params2, TagUtil.TYPE_ENTITY);
            boolean
                    paramMatched =
                    paramMatched(params1, params2, OrdersEventGenerator.PARAM_STATUS);
            return paramMatched && tagParamsNotMatched;

          }
        };
      }
    } else if (JDOUtils.getImplClass(IInvntryBatch.class).getName().equals(objectType)) {
      if (eventId == IEvent.EXPIRED) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            boolean
                tagParamsNotMatched =
                tagParamsNotMatched(params1, params2, TagUtil.TYPE_MATERIAL) && tagParamsNotMatched(
                    params1, params2, TagUtil.TYPE_ENTITY);
            boolean
                paramMatched =
                paramMatched(params1, params2, EventConstants.PARAM_EXPIRESINDAYS);
            return paramMatched && tagParamsNotMatched;
          }
        };
      }
    } else if (JDOUtils.getImplClass(IAccount.class).getName().equals(objectType)) {
      if (eventId == IEvent.CREDIT_LIMIT_EXCEEDED) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            return tagParamsNotMatched(params1, params2, TagUtil.TYPE_ENTITY);
          }
        };
      }
    } else if (JDOUtils.getImplClass(IAssetStatus.class).getName().equals(objectType)) {
      if (eventId == IEvent.HIGH_EXCURSION || eventId == IEvent.LOW_EXCURSION
          || eventId == IEvent.HIGH_WARNING || eventId == IEvent.LOW_WARNING
          || eventId == IEvent.HIGH_ALARM || eventId == IEvent.LOW_ALARM || IEvent.ASSET_ALARM_GROUP
          .contains(eventId)) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            boolean
                tagParamsNotMatched =
                tagParamsNotMatched(params1, params2, TagUtil.TYPE_ENTITY);
            boolean
                paramMatched =
                paramMatched(params1, params2, EventConstants.PARAM_REMINDMINSAFTER);
            return paramMatched && tagParamsNotMatched;
          }
        };
      } else if (eventId == IEvent.NO_ACTIVITY) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            boolean
                tagParamsNotMatched =
                tagParamsNotMatched(params1, params2, TagUtil.TYPE_ENTITY);
            boolean
                paramMatched =
                paramMatched(params1, params2, EventConstants.PARAM_INACTIVEDURATION);
            return paramMatched && tagParamsNotMatched;
          }
        };
      } else if (eventId == IEvent.INCURSION || IEvent.ASSET_ALARM_NORMAL_GROUP.contains(eventId)) {
        paramComparator = new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            return tagParamsNotMatched(params1, params2, TagUtil.TYPE_ENTITY);
          }
        };
      }
    }
    return ec.matchEvent(objectType, eventId, params, paramComparator);
  }

  // Generate events and persist, after first matching
  // (an optional message, if passed, is sent instead of generating message from specification; if userIds are passed, the specified users will be notified for this event)
  public void generate(int eventId, Map<String, Object> params, String objectId,
                       CustomOptions customOptions) throws EventGenerationException {
    try {
      Map<String, Object> tagParams = EventHandler.getTagParams(objectType, objectId);
      if(params == null){
        params = tagParams;
      }else {
        if (tagParams != null) {
          params.putAll(tagParams);
        }
      }
      ParamSpec paramSpec = match(eventId, params);
      if (paramSpec != null) {
        doGenerate(eventId, params, objectId, paramSpec.isRealTime(), paramSpec.getEtaMillis(),
            customOptions);
      }
    } catch (Exception e) {
      xLogger.warn("Error in generating event", e);
      throw new EventGenerationException(e);
    }
  }

  public void generateDeleteEvent(EventGenerator eg, Object o, Object key, Locale locale,
                                         String timezone, Map<String, Object> params)
      throws EventHandlingException, EventGenerationException {
    ParamSpec paramSpec = eg.match(IEvent.DELETED, params);
    if (paramSpec != null) {
      // Get the message
      String
          message =
          eg.getDisplayString(o, paramSpec, locale, timezone, null, new Date(), false);
      if (message != null && !message.isEmpty()) {
        CustomOptions customOptions = new CustomOptions();
        customOptions.message = message;
        // Get the subscribers associated with different frequencies
        Map<Integer, List<Subscriber>> subscriberMap = paramSpec.getSubcribers();
        // Get the list of user Ids per frequency
        if (subscriberMap != null && !subscriberMap.isEmpty()) {
          customOptions.userIds = new HashMap<Integer, List<String>>();
          Iterator<Entry<Integer, List<Subscriber>>> it = subscriberMap.entrySet().iterator();
          while (it.hasNext()) {
            Entry<Integer, List<Subscriber>> entry = it.next();
            Integer freq = entry.getKey();
            List<Subscriber> subscribers = entry.getValue();
            if (subscribers == null || subscribers.isEmpty()) {
              continue;
            }
            List<String> userIds = customOptions.userIds.get(freq);
            if (userIds == null) {
              userIds = new ArrayList<String>();
              customOptions.userIds.put(freq, userIds);
            }
            Iterator<Subscriber> it2 = subscribers.iterator();
            while (it2.hasNext()) {
              Subscriber s = it2.next();
              List<String> uids = eg.getSubscriberIds(s, o);
              if (uids != null && !uids.isEmpty()) {
                Iterator<String> it3 = uids.iterator();
                while (it3.hasNext()) {
                  String uId = it3.next();
                  if (!userIds.contains(uId)) {
                    userIds.add(uId);
                  }
                }
              }
            }
          }
        }
        // Get a delete event with custom notification options
        eg.generate(IEvent.DELETED, params, String.valueOf(key),
            customOptions);
      }
    }
  }

  // Get the (HTML) display string associated with a given event (a series of HTML table rows are returned)
  // NOTE: All events are expected to have the same event-spec. key (i.e. eventId:objectType:paramsKey)
  public String getDisplayString(Object o, ParamSpec paramSpec, Locale locale, String timezone,
                                 List<String> excludeVars, Date updationTime, boolean htmlize)
      throws EventGenerationException {
    if (o == null || paramSpec == null) {
      return "";
    }
    String msg = "";
    try {
      msg =
          EventsConfig.replaceTemplateVariables(paramSpec.getMessageTemplate(),
              getTemplateValues(o, locale, timezone, excludeVars, updationTime), htmlize);
    } catch (Exception e) {
      xLogger
          .warn("{0} when getting message for object {1} of type {2}: {3}", e.getClass().getName(),
              o, o.getClass().getName(), e);
    }
    return msg;
  }

  public List<String> getSubscriberIds(Subscriber subscriber, Object o) {
    return getSubscriberIds(subscriber, o, null);
  }

  // Get the list of user Ids for a given subscriber type and object
  // USERS types handled here; other types should be handled by sub-classes respectively
  public List<String> getSubscriberIds(Subscriber subscriber, Object o, Long domainId) {
    if (subscriber == null) {
      return null;
    }
    if (EventSpec.Subscriber.USERS.equals(subscriber.type)) {
      return getEnabledUserIds(subscriber.ids);
    } else if (Subscriber.USER_TAGS.equals(subscriber.type)) {
      return getEnabledUserIdsWithTags(subscriber.ids, domainId);
    } else if (EventSpec.Subscriber.ADMINISTRATORS.equals(subscriber.type) && o != null
        && o instanceof Long) {
      return EventHandler.getSubsribers(subscriber.type, o);
    } else {
      return null;
    }
  }

  // Is this event still valid?
  // NOTE: This should be overridden
  public boolean isEventValid(IEvent event, PersistenceManager pm) {
    return true;
  }

  // Generate the event to be logged (an optional message can be passed to be notified)
  protected void doGenerate(int eventId, Map<String, Object> params, String objectId,
                            boolean isRealTime, long etaMillis, CustomOptions customOptions)
      throws EventGenerationException {
    String message = null;
    Map<Integer, List<String>> userIds = null;
    if (customOptions != null) {
      message = customOptions.message;
      userIds = customOptions.userIds;
    }
    Date eventTime = new Date();
    if (params != null) {
      eventTime = (Date) params.get(EventConstants.EVENT_TIME);
    }
    EventHandler.log(JDOUtils.createInstance(IEvent.class)
        .init(domainId, eventId, params, objectType, objectId, isRealTime, message, userIds,
            etaMillis, eventTime));
  }

  // Get the template variables associated with this object
  private Map<String, String> getTemplateValues(Object eventObject, Locale locale, String timezone,
                                                List<String> excludeVars, Date updationTime) {
    ITemplate template = TemplateUtils.getTemplateType(eventObject);
    if(template != null) {
      return template.getTemplateValues(locale, timezone, excludeVars, updationTime);
    }
    return null;
  }

  // Get common metadata associated with a given object
  public ObjectData getObjectData(Object o, PersistenceManager pm) {
    return new ObjectData();
  }

  public Map<String, Object> getTagParams(Object o) {
    return null;
  }





}
