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

import com.logistimo.AppFactory;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.dao.IDaoUtil;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.events.models.ObjectData;
import com.logistimo.events.templates.TemplateUtils;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.shipments.entity.IShipment;

import org.apache.commons.lang.StringUtils;
import com.logistimo.events.entity.IEvent;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.json.JSONArray;
import org.json.JSONObject;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;


/**
 * @author Arun
 */
public class OrdersEventGenerator extends EventGenerator {


  public static final String PARAM_STATUS = "status";

  // Logger
  private static final XLog xLogger = XLog.getLog(OrdersEventGenerator.class);

  private IDaoUtil daoUtil = AppFactory.get().getDaoUtil();

  public OrdersEventGenerator(Long domainId, String objectType) {
    super(domainId, objectType);
  }

  @Override
  public List<String> getSubscriberIds(EventSpec.Subscriber subscriber, Object o, Long domainId) {
    if (subscriber == null) {
      return null;
    }
    //OrdersEventGenerator is used for order and shipment objects
    if (EventSpec.Subscriber.CUSTOMERS.equals(subscriber.type) && o != null) {
      Long kid = (o instanceof IOrder) ? ((IOrder) o).getKioskId() : ((IShipment) o).getKioskId();
      return EventHandler.getSubsribers(subscriber.type, kid);
    } else if (EventSpec.Subscriber.VENDORS.equals(subscriber.type) && o != null) {

      Long skid = (o instanceof IOrder) ? ((IOrder) o).getServicingKiosk() : ((IShipment) o).getServicingKiosk();
      return EventHandler.getSubsribers(subscriber.type, skid);
    } else if (EventSpec.Subscriber.ADMINISTRATORS.equals(subscriber.type) && o != null
        && o instanceof IOrder) {
      Long did = (o instanceof IOrder) ? ((IOrder) o).getDomainId() : ((IShipment) o).getDomainId();
      return EventHandler
          .getSubsribers(subscriber.type, domainId != null ? domainId : did);
    } else if (EventSpec.Subscriber.CREATOR.equals(subscriber.type) && o != null && o instanceof IOrder) {
      return EventHandler.getSubsribers(subscriber.type, ((IOrder) o).getUserId());
    } else {
      return super.getSubscriberIds(subscriber, o, domainId);
    }
  }

  // Get common metadata associated with a given object
  @Override
  public ObjectData getObjectData(Object o, PersistenceManager pm) {
    ObjectData od = new ObjectData();
    if(o instanceof IOrder){
      IOrder order = (IOrder) o;
      Long orderId = order.getOrderId();
      od.kioskId = order.getKioskId();
      if (od.kioskId != null) {
        updateObjectLocation(od, pm);
      }
      od.oid = orderId;
      od.vendorId = order.getServicingKiosk();
    } else if( o instanceof IShipment){
      IShipment shipment = (IShipment) o;
      Long orderId = shipment.getOrderId();
      od.kioskId = shipment.getKioskId();
      if (od.kioskId != null) {
        updateObjectLocation(od, pm);
      }
      od.oid = orderId;
      od.vendorId = shipment.getServicingKiosk();
    }
    return od;
  }

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

  // Is an event still valid?
  @Override
  public boolean isEventValid(IEvent event, PersistenceManager pm) {
    int eventId = event.getId();
    try {
      switch (eventId) {
        case IEvent.FULFILLMENT_DUE: {
          // Get the order
          try {
            IOrder
                o =
                JDOUtils
                    .getObjectById(IOrder.class, daoUtil.createKeyFromString(event.getObjectId()),
                        pm);
            String status = o.getStatus();
            return !(IOrder.FULFILLED.equals(status) || IOrder.CANCELLED.equals(status));
          } catch (JDOObjectNotFoundException e1) {
            return false;
          }
        }
        case IEvent.NO_ACTIVITY: {
          Date start = getInactiveDurationStart(event);
          if (start != null) {
            return EventHandler
                .hasNoActivity(domainId, JDOUtils.getImplClass(IOrder.class).getName(), start, pm);
          }
        }
        case IEvent.EXPIRED: {
          int inactiveDuration = getInactiveDuration((Map<String, Object>) event.getParams());
          if (inactiveDuration > 0) {
            // Get the order
            try {
              IOrder
                  o =
                  JDOUtils
                      .getObjectById(IOrder.class, daoUtil.createKeyFromString(event.getObjectId()),
                          pm);
              return isOrderInactive(o, new Date(), inactiveDuration);
            } catch (JDOObjectNotFoundException e1) {
              return false;
            }
          }
        }
        case IEvent.STATUS_CHANGE: {
            Map<String, Object> params = (Map<String, Object>) event.getParams();
            if (params != null && !params.isEmpty()) {
              // Get the state at event time
              String eventStatus = (String) params.get(PARAM_STATUS);
              if (StringUtils.isNotEmpty(eventStatus)) {
                try {
                  if (!StringUtils.contains(event.getObjectId(),"-")) {
                      IOrder o =
                        JDOUtils.getObjectById(
                            IOrder.class, daoUtil.createKeyFromString(event.getObjectId()), pm);
                    return eventStatus.equals(o.getStatus());
                  }else{
                      IShipment s =
                              JDOUtils.getObjectById(
                                      IShipment.class, daoUtil.createKeyFromString(event.getObjectId()), pm);
                      return eventStatus.equals(s.getStatus().toString());
                  }
                } catch (JDOObjectNotFoundException e1) {
                  return false;
                }
              }
            }
        }
      }
    } catch (Exception e) {
      xLogger.warn("{0} when retrieving order with key {1} in domain {2} for event {3}: {4}",
          e.getClass().getName(), event.getObjectId(), event.getDomainId(), eventId, e);
    }
    return super.isEventValid(event, pm);
  }

  public static float getOrderInactiveDuration(IOrder o, Date now) {
    Date cOn = o.getCreatedOn();
    Date stOn = o.getStatusUpdatedOn();
    // Get the more recent date
    Date d = cOn;
    if (stOn != null && stOn.compareTo(d) == 1) {
      d = stOn;
    }
    // Get the difference between now and d
    float daysDiff = (now.getTime() - d.getTime()) / 86400000F;

    return daysDiff;
  }

  // Check if an order is inactive / expired, given a start date of inactive duration
  public static boolean isOrderInactive(IOrder o, Date now, int inactiveDuration) {
                /* Moved this code to a method getOrderInactivePeriod, that is also used in OrderEventsCreationProcessor.
                Date cOn = o.getCreatedOn();
		Date stOn = o.getStatusUpdatedOn();
		// Get the more recent date
		Date d = cOn;
		if ( stOn != null && stOn.compareTo( d ) == 1 )
			d = stOn;
		// Get the difference between now and d
		float daysDiff = ( now.getTime() - d.getTime() ) / 86400000F;*/
    float daysDiff = getOrderInactiveDuration(o, now);
    return daysDiff >= inactiveDuration;
  }

  // Convenience method to get an order's status change messages, as a JSON (status-value --> completed notification message) (used in vieworder.jsp)
  public String getOrderStatusJSON(IOrder o, Locale locale, String timezone,
                                   List<String> excludeVars) {
    Map<String, EventSpec.ParamSpec> statusParamSpecMap = new HashMap<String, EventSpec.ParamSpec>();
    EventsConfig ec = DomainConfig.getInstance(domainId).getEventsConfig();
    // Get the param. spec. for status change event with no params. (i.e. check if it matches any kind of status params)
    EventSpec.ParamSpec
        paramSpec =
        ec.getEventParamSpec(IEvent.STATUS_CHANGE, JDOUtils.getImplClass(IOrder.class).getName(),
            null);
    if (paramSpec != null) {
      statusParamSpecMap.put("", paramSpec);
    } else {
      // Get the param. spec. for order statuses
      List<String>
          statuses =
          Arrays.asList(IOrder.CANCELLED, IOrder.COMPLETED, IOrder.CONFIRMED, IOrder.FULFILLED);
      Iterator<String> it = statuses.iterator();
      while (it.hasNext()) {
        String status = it.next();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(OrdersEventGenerator.PARAM_STATUS, status);
        EventSpec.ParamSpec
            pSpec =
            ec.getEventParamSpec(IEvent.STATUS_CHANGE,
                JDOUtils.getImplClass(IOrder.class).getName(),
                params);
        if (pSpec != null) {
          statusParamSpecMap.put(status, pSpec);
        }
      }
    }
    if (statusParamSpecMap.isEmpty()) {
      return null;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Get the order name-value pairs
    Map<String, String>
        orderValuePairs =
        TemplateUtils.getTemplateType(o).getTemplateValues(locale, timezone, excludeVars, new Date());
    try {
      JSONObject json = new JSONObject();
      Iterator<Map.Entry<String, EventSpec.ParamSpec>> it1 = statusParamSpecMap.entrySet().iterator();
      while (it1.hasNext()) {
        Map.Entry<String, EventSpec.ParamSpec> entry = it1.next();
        String status = entry.getKey();
        EventSpec.ParamSpec pSpec = entry.getValue();
        // Status JSON
        JSONObject statusJson = new JSONObject();
        // Get the message template
        String msg = pSpec.getMessageTemplate();
        if (msg != null && !msg.isEmpty()) {
          msg = EventsConfig.replaceTemplateVariables(msg, orderValuePairs, false);
          msg =
              msg.replaceAll("\\'",
                  "\\\\'"); // escape single quote, so that the messages can be passed as part of JS functions (e.g. in vieworder.jsp)
          statusJson.put("message", msg);
        }
        // Get the subscribers
        EventGenerator eg = EventGeneratorFactory.getEventGenerator(o.getDomainId(), "Order");
        List<EventSpec.Subscriber> subscribers = pSpec.getSubscribers(EventSpec.NotifyOptions.IMMEDIATE);
        if (subscribers != null && !subscribers.isEmpty()) {
          JSONArray subsArray = new JSONArray();
          List<String> uniqueUserIds = new ArrayList<String>();
          Iterator<EventSpec.Subscriber> subsIt = subscribers.iterator();
          while (subsIt.hasNext()) {
            EventSpec.Subscriber s = subsIt.next();
            List<String> userIds = eg.getSubscriberIds(s, o);
            xLogger.fine("subsriber: {0}, userIds: {1}", s.type, userIds);
            if (userIds == null || userIds.isEmpty()) {
              continue;
            }
            Iterator<String> userIdsIter = userIds.iterator();
            UsersService as = Services.getService(UsersServiceImpl.class);
            while (userIdsIter.hasNext()) {
              String userId = userIdsIter.next();
              if (!uniqueUserIds.contains(userId)) {
                uniqueUserIds.add(userId);
                try {
                  IUserAccount u = as.getUserAccount(userId);
                  // Create the users JSON
                  JSONObject userJson = new JSONObject();
                  userJson.put("id", userId);
                  userJson.put("name", u.getFullName());
                  userJson.put("phone", u.getMobilePhoneNumber());
                  subsArray.put(userJson);
                } catch (Exception e) {
                  // ignore
                  xLogger.warn("{0} when getting user account {1} in domain {2}: {3}",
                      e.getClass().getName(), userId, o.getDomainId(), e.getMessage());
                  continue;
                }
              }
            }
          }
          statusJson.put("subscribers", subsArray);
        }
        // Update order status JSON
        json.put(status, statusJson);
      }
      return json.toString();
    } catch (Exception e) {
      xLogger.warn(
          "{0} when getting order status JSON with completed SMS status messages for varialbles {1}: {2}",
          e.getClass().getName(), orderValuePairs, e.getMessage());
      return null;
    } finally {
      pm.close();
    }
  }
}
