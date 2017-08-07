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

package com.logistimo.events.processor;

import com.logistimo.events.entity.IEvent;
import com.logistimo.events.exceptions.EventGenerationException;
import com.logistimo.events.models.CustomOptions;
import com.logistimo.events.models.EventData;

import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Locale;
import java.util.Map;

/**
 * Created by charan on 07/03/17.
 */
public class EventPublisher {

  private static ApplicationContext context;

  public static ApplicationContext getContext(){
    if(context == null) {
      context = new ClassPathXmlApplicationContext("camel-events-generator.xml");
    }
    return context;
  }

  public static void generate(Long domainId, int eventId, Map<String, Object> params, String objectType, String objectId, CustomOptions customOptions)
      throws EventGenerationException {
    generate(domainId, eventId, params, objectType, String.valueOf(objectId), customOptions, null);
  }

  public static void generate(Long domainId, int eventId, Map<String, Object> params, String objectType, String objectId,
                       CustomOptions customOptions, Object eventObject) throws EventGenerationException {
    EventData eventData = new EventData(domainId,eventId,params, objectType, objectId,
        customOptions, eventObject);
    try {
      ProducerTemplate
          camelTemplate =
          getContext()
              .getBean("camel-events-gen", ProducerTemplate.class);
      camelTemplate.sendBody("direct:events", ExchangePattern.InOnly, eventData);
    }catch (Exception e){
      throw new EventGenerationException(e);
    }
  }

  public static String getEventName(int eventId, Locale locale) {
    switch (eventId) {
      case IEvent.CREATED:
        return "created";
      case IEvent.MODIFIED:
        return "modified";
      case IEvent.DELETED:
        return "deleted";
      case IEvent.NOT_CREATED:
        return "not created";
      case IEvent.EXPIRED:
        return "expired";
      case IEvent.NO_ACTIVITY:
        return "no activity";
      case IEvent.FULFILLMENT_DUE:
        return "fulfillment due";
      case IEvent.STATUS_CHANGE:
      case IEvent.STATUS_CHANGE_TEMP:
        return "status changed";
      case IEvent.STOCKOUT:
        return "zero stock"; // "stockout";
      case IEvent.STOCK_REPLENISHED:
        return "stock replenished";
      case IEvent.UNDERSTOCK:
        return "< min.";
      case IEvent.OVERSTOCK:
        return "> max.";
      case IEvent.STOCKCOUNT_EXCEEDS_THRESHOLD:
        return "stockcount exceeds threshold";
      case IEvent.STOCK_COUNTED:
        return "stock counted";
      case IEvent.STOCK_ISSUED:
        return "issued";
      case IEvent.STOCK_RECEIVED:
        return "received";
      case IEvent.STOCK_WASTED:
        return "wasted";
      case IEvent.STOCK_TRANSFERRED:
        return "transferred";
      case IEvent.CREDIT_LIMIT_EXCEEDED:
        return "> credit-limit";
      case IEvent.PAYMENT_DUE:
        return "payment due";
      case IEvent.PAID:
        return "paid";
      case IEvent.HIGH_EXCURSION:
        return "high excursion";
      case IEvent.LOW_EXCURSION:
        return "low excursion";
      case IEvent.INCURSION:
        return "incursion";
      case IEvent.NO_DATA_FROM_DEVICE:
        return "no data from device";
      case IEvent.HIGH_WARNING:
        return "high warning";
      case IEvent.LOW_WARNING:
        return "low warning";
      case IEvent.HIGH_ALARM:
        return "high alarm";
      case IEvent.LOW_ALARM:
        return "low alarm";
      case IEvent.SENSOR_DISCONNECTED:
        return "sensor disconnected";
      case IEvent.SENSOR_CONNECTION_NORMAL:
        return "sensor connected";
      case IEvent.BATTERY_ALARM:
        return "battery alarm";
      case IEvent.BATTERY_LOW:
        return "battery low";
      case IEvent.BATTERY_NORMAL:
        return "battery normal";
      case IEvent.ASSET_INACTIVE:
        return "asset inactive";
      case IEvent.ASSET_ACTIVE:
        return "asset active";
      case IEvent.POWER_OUTAGE:
        return "power outage";
      case IEvent.POWER_NORMAL:
        return "power available";
      case IEvent.DEVICE_DISCONNECTED:
        return "device disconnected";
      case IEvent.DEVICE_CONNECTION_NORMAL:
        return "device connected";
      case IEvent.COMMENTED:
        return "comment";
      default:
        return "Unknown";
    }
  }

}
