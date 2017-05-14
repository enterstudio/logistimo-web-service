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

import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.events.EventConstants;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.generators.OrdersEventGenerator;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.Results;
import com.logistimo.proto.JsonTagsZ;

import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Generate order events (esp. fulfillment due and expiry events)
 *
 * @author Arun
 */
public class OrderEventsCreationProcessor implements Processor {

  // Logger
  private static final XLog xLogger = XLog.getLog(OrderEventsCreationProcessor.class);

  // Get fulfillment reminder due date - Old
        /*private static IEvent getFulfillmentDueEvent( Long domainId, IOrder o, ParamSpec paramSpec, float daysBefore, Date now ) {
                xLogger.fine( "Entered getFulfillmentRemiderDate" );
		// Check if confirmed times exist
		Date d = o.getConfirmedFulfillmentTimeStart();
		if ( d == null )
			d = o.getConfirmedFulfillmentTimeEnd();
		// Check if expected times exist
		if ( d == null ) {
			try {
				List<Map<String,Date>> timeRanges = LocalDateUtil.parseTimeRanges( o.getExpectedFulfillmentTimeRangesCSV(), null, null );
				if ( timeRanges != null && !timeRanges.isEmpty() ) { // take the first time-range for calculation (TODO: determine the closest time)
					Map<String,Date> timeRange = timeRanges.get( 0 );
					d = timeRange.get( JsonTagsZ.TIME_START );
					if ( d == null )
						d = timeRange.get( JsonTagsZ.TIME_END );
				}
			} catch ( Exception e ) {
				xLogger.warn( "{0} when getting fulfillment-due event's reminder date for order {1}, with expiry duration = {2}: {3}", e.getClass().getName(), o.getOrderId(), daysBefore, e.getMessage() );
			}
		}
		if ( d != null ) {
			float daysDiff = ( d.getTime() - now.getTime() ) / 86400000F; // NOTE: d is a future date
			if ( daysDiff <= daysBefore ) {
				boolean tagParamsNotMatched = EventGenerator.tagParamsNotMatched( EventHandler.getTagParams( o ), paramSpec.getParams(), TagUtil.TYPE_ENTITY ) &&
						EventGenerator.tagParamsNotMatched( EventHandler.getTagParams( o ), paramSpec.getParams(), TagUtil.TYPE_ORDER );
				if ( tagParamsNotMatched ) {
					return JDOUtils.createInstance(IEvent.class).init(domainId, IEvent.FULFILLMENT_DUE, paramSpec.getParams(), JDOUtils.getImplClass(IOrder.class).getName(), o.getIdString(), paramSpec.isRealTime(), null, null, -1);
				}

			}
		}
		return null;
	}
	*/
  private static IEvent getFulfillmentDueEvent(Long domainId, IOrder o, EventSpec.ParamSpec paramSpec,
                                               float daysBefore, Date now) {
    xLogger.fine("Entered getFulfillmentDueEvent");
    // Check if confirmed times exist
    Date d = o.getConfirmedFulfillmentTimeStart();
    if (d == null) {
      d = o.getConfirmedFulfillmentTimeEnd();
    }
    // Check if expected times exist
    if (d == null) {
      try {
        List<Map<String, Date>>
            timeRanges =
            LocalDateUtil.parseTimeRanges(o.getExpectedFulfillmentTimeRangesCSV(), null, null);
        if (timeRanges != null && !timeRanges
            .isEmpty()) { // take the first time-range for calculation (TODO: determine the closest time)
          Map<String, Date> timeRange = timeRanges.get(0);
          d = timeRange.get(JsonTagsZ.TIME_START);
          if (d == null) {
            d = timeRange.get(JsonTagsZ.TIME_END);
          }
        }
      } catch (Exception e) {
        xLogger.warn(
            "{0} when getting fulfillment-due event's reminder date for order {1}, with expiry duration = {2}: {3}",
            e.getClass().getName(), o.getOrderId(), daysBefore, e.getMessage());
      }
    }
    if (d != null) {
      float daysDiff = (d.getTime() - now.getTime()) / 86400000F; // NOTE: d is a future date
      if (daysDiff <= daysBefore) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(EventConstants.PARAM_REMINDDAYSBEFORE, String.valueOf(daysBefore));
        Map<String, Object> tagParams = EventHandler.getTagParams(o);
        if (tagParams != null && !tagParams.isEmpty()) {
          params.putAll(EventHandler.getTagParams(o));
        }

        return JDOUtils.createInstance(IEvent.class).init(domainId, IEvent.FULFILLMENT_DUE, params,
            JDOUtils.getImplClass(IOrder.class).getName(), o.getIdString(), paramSpec.isRealTime(),
            null, null, -1);
      }
    }
    return null;
  }

  private static IEvent getInactiveEvent(Long domainId, IOrder o, EventSpec.ParamSpec paramSpec,
                                         float inactiveDuration, Date now) {
    xLogger.fine("Entered getInactiveEvent");
    IEvent e = null;
    if (OrdersEventGenerator.isOrderInactive(o, now, Math.round(inactiveDuration))) {
      xLogger.fine("order is inactive for " + Math.round(inactiveDuration) + " days");
      Map<String, Object> params = new HashMap<String, Object>();
      params
          .put(EventConstants.PARAM_INACTIVEDURATION, String.valueOf(Math.round(inactiveDuration)));
      Map<String, Object> tagParams = EventHandler.getTagParams(o);
      if (tagParams != null && !tagParams.isEmpty()) {
        params.putAll(EventHandler.getTagParams(o));
      }

      e =
          JDOUtils.createInstance(IEvent.class)
              .init(domainId, IEvent.EXPIRED, params, JDOUtils.getImplClass(IOrder.class).getName(),
                  o.getIdString(), paramSpec.isRealTime(), null, null, -1,
                  LocalDateUtil.getZeroTime(DomainConfig.getInstance(domainId).getTimezone()).getTime());
    }
    return e;
  }

  // Generate events for orders - fulfillment due and expiry
  @SuppressWarnings("unchecked")
  @Override
  public String process(Long domainId, Results results, String prevOutput, PersistenceManager pm)
      throws ProcessingException {
    xLogger.fine("Entered OrderEventsGenerationProcessor.process");
    if (results == null) {
      return null;
    }
    List<IOrder> orders = results.getResults();
    if (orders == null) {
      return null;
    }

    //Check if list is empty without using size. To avoid eager fetching.
    try {
      orders.get(0);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
    // Get the events config.
    EventsConfig ec = DomainConfig.getInstance(domainId).getEventsConfig();
    // Get the fulfillment reminder date
    EventSpec
        espec =
        ec.getEventSpec(IEvent.FULFILLMENT_DUE, JDOUtils.getImplClass(IOrder.class).getName());
    float daysBefore = -1;
    EventSpec.ParamSpec paramSpecFulfillmentDue = null;
    if (espec != null) {
      paramSpecFulfillmentDue = espec.getFirstParamSpec();
      if (paramSpecFulfillmentDue != null) {
        Map<String, Object> params = paramSpecFulfillmentDue.getParams();
        String daysBeforeStr = null;
        if (params != null && !params.isEmpty()) {
          daysBeforeStr = (String) params.get(EventConstants.PARAM_REMINDDAYSBEFORE);
        }
        if (daysBeforeStr != null && !daysBeforeStr.isEmpty()) {
          try {
            daysBefore = Float.parseFloat(daysBeforeStr);
          } catch (NumberFormatException e) {
            xLogger.warn("Number format exception in daysBefore {0}: {1}", daysBeforeStr,
                e.getMessage());
          }
        }
      }
    }
    // Get the order expiry event details
    float inactiveDuration = 0;
    EventSpec.ParamSpec paramSpecExpiry = null;
    espec = ec.getEventSpec(IEvent.EXPIRED, JDOUtils.getImplClass(IOrder.class).getName());
    if (espec != null) {
      paramSpecExpiry = espec.getFirstParamSpec();
      if (paramSpecExpiry != null) {
        Map<String, Object> params = paramSpecExpiry.getParams();
        String inactiveDurationStr = (String) params.get(EventConstants.PARAM_INACTIVEDURATION);
        if (inactiveDurationStr != null && !inactiveDurationStr.isEmpty()) {
          try {
            inactiveDuration = Float.parseFloat(inactiveDurationStr);
          } catch (NumberFormatException e) {
            xLogger
                .warn("Number format exception in inactiveDuration {0}: {1}", inactiveDurationStr,
                    e.getMessage());
          }
        }
      }
    }
    if (daysBefore < 0 && inactiveDuration == 0) {
      return null; // do nothing
    }
    // Now
    Date now = new Date();
    // Loop over orders and generate relevant events
    List<IEvent> events = new ArrayList<IEvent>();
    Iterator<IOrder> it = orders.iterator();
    while (it.hasNext()) {
      IOrder o = it.next();
      // Check if this order is in the source domain, only then generate the event (superdomains)
      if (!o.getDomainId().equals(domainId)) {
        continue;
      }
      // Generate fulfillment due event if needed
      String status = o.getStatus();
      if (daysBefore >= 0 && !IOrder.CANCELLED.equals(status) && !IOrder.FULFILLED.equals(status)) {
        IEvent e = getFulfillmentDueEvent(domainId, o, paramSpecFulfillmentDue, daysBefore, now);
        if (e != null) {
          events.add(e);
        }
      }
      // Generate order exiry event if needed
      if (inactiveDuration >= 1) {
        IEvent e = getInactiveEvent(domainId, o, paramSpecExpiry, inactiveDuration, now);
        if (e != null) {
          events.add(e);
        }
      }
    }
    if (!events.isEmpty()) {
      EventHandler.log(events);
    }

    xLogger.fine("Exiting OrderEventsGenerationProcessor.process");
    return null;
  }
        /*
        // Get order expiry/inactive event - Old
	private static IEvent getInactiveEvent( Long domainId, IOrder o, ParamSpec paramSpec, float inactiveDuration, Date now ) {
		xLogger.fine( "Entered getInactiveEvent" );
		IEvent e = null;
		if ( EventHandler.isOrderInactive( o, now, Math.round( inactiveDuration ) ) ) {
			boolean tagParamsNotMatched = EventGenerator.tagParamsNotMatched( EventHandler.getTagParams( o ), paramSpec.getParams(), TagUtil.TYPE_ENTITY ) &&
					EventGenerator.tagParamsNotMatched( EventHandler.getTagParams( o ), paramSpec.getParams(), TagUtil.TYPE_ORDER );
			if ( tagParamsNotMatched ) {
				e = JDOUtils.createInstance(IEvent.class).init(domainId, IEvent.NO_ACTIVITY, paramSpec.getParams(), JDOUtils.getImplClass(IOrder.class).getName(), o.getIdString(), paramSpec.isRealTime(), null, null, -1);
			}
		}
		return e;
	}
	*/

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_MESSAGE;
  }
}
