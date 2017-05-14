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
import com.logistimo.dao.IDaoUtil;
import com.logistimo.dao.JDOUtils;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventSpec.ParamComparator;
import com.logistimo.config.models.EventSpec.ParamSpec;
import com.logistimo.config.models.EventSpec.Subscriber;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.events.EventConstants;
import com.logistimo.events.entity.IEvent;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.events.models.ObjectData;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.tags.TagUtil;

import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;


/**
 * Processes events associated with Inventory & Inventory transactions
 *
 * @author Arun
 */
public class TransactionsEventGenerator extends EventGenerator {

  // Logger
  private static final XLog xLogger = XLog.getLog(TransactionsEventGenerator.class);

  private IInvntryDao invntryDao = new InvntryDao();

  private ITransDao transDao = new TransDao();

  private IDaoUtil daoUtil = AppFactory.get().getDaoUtil();

  public TransactionsEventGenerator(Long domainId, String objectType) {
    super(domainId, objectType);
  }

  @Override
  public List<String> getSubscriberIds(Subscriber subscriber, Object o, Long domainId) {
    if (subscriber == null) {
      return null;
    }
    if (EventSpec.Subscriber.CUSTOMERS.equals(subscriber.type) && o != null) {
      Long kioskId = null;
      if (o instanceof ITransaction) {
        String transType = ((ITransaction) o).getType();
        if (ITransaction.TYPE_ISSUE.equals(transType) || ITransaction.TYPE_TRANSFER
            .equals(transType)) {
          kioskId = ((ITransaction) o).getLinkedKioskId();
        } else {
          kioskId = ((ITransaction) o).getKioskId();
        }
      } else if (o instanceof IInvntry) {
        kioskId = ((IInvntry) o).getKioskId();
      } else if (o instanceof IInvntryBatch) {
        kioskId = ((IInvntryBatch) o).getKioskId();
      }
      return EventHandler.getSubsribers(subscriber.type, kioskId);
    } else if (EventSpec.Subscriber.VENDORS.equals(subscriber.type) && o != null) {
      Long kioskId = null;
      if (o instanceof ITransaction) {
        String transType = ((ITransaction) o).getType();
        if (ITransaction.TYPE_ISSUE.equals(transType) || ITransaction.TYPE_TRANSFER
            .equals(transType)) {
          kioskId = ((ITransaction) o).getKioskId();
        } else if (ITransaction.TYPE_RECEIPT.equals(transType)) {
          kioskId = ((ITransaction) o).getLinkedKioskId();
        }
      }
      return EventHandler.getSubsribers(subscriber.type, kioskId);
    } else if (EventSpec.Subscriber.ADMINISTRATORS.equals(subscriber.type) && o != null
        && !(o instanceof Long)) {
      if (domainId == null) {
        if (o instanceof ITransaction) {
          domainId = ((ITransaction) o).getDomainId();
        } else if (o instanceof IInvntry) {
          domainId = ((IInvntry) o).getDomainId();
        } else if (o instanceof IInvntryBatch) {
          domainId = ((IInvntryBatch) o).getDomainId();
        }
      }
      return EventHandler.getSubsribers(subscriber.type, domainId);
    } else {
      return super.getSubscriberIds(subscriber, o, domainId);
    }
  }

  @Override
  public ParamSpec match(int eventId, Map<String, Object> params) {
    switch (eventId) {
      case IEvent.STOCKCOUNT_EXCEEDS_THRESHOLD: {
        DomainConfig dc = DomainConfig.getInstance(domainId);
        EventsConfig ec = dc.getEventsConfig();
        return ec.matchEvent(objectType, eventId, params, new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            boolean
                tagParamsNotMatched =
                tagParamsNotMatched(params1, params2, TagUtil.TYPE_MATERIAL) && tagParamsNotMatched(
                    params1, params2, TagUtil.TYPE_ENTITY);
            boolean
                paramMatched =
                paramMatched(params1, params2,
                    EventConstants.PARAM_STOCKCOUNTTHRESHOLD);
            return paramMatched && tagParamsNotMatched;
          }
        });
      }

      default:
        return super.match(eventId, params);
    }
  }

  // Get common metadata associated with a given object
  @Override
  public ObjectData getObjectData(Object o, PersistenceManager pm) {
    ObjectData od = new ObjectData();
    if (o instanceof IInvntry) {
      IInvntry inv = ((IInvntry) o);
      od.tags = inv.getTags(TagUtil.TYPE_MATERIAL); // TODO: for entity tags?
      od.kioskId = inv.getKioskId();
      od.oid = inv.getKeyString();
      od.materialId = inv.getMaterialId();
    } else if (o instanceof ITransaction) {
      ITransaction trans = (ITransaction) o;
      try {
        IInvntry inv = invntryDao.findId(trans.getKioskId(), trans.getMaterialId());
        od.tags = inv.getTags(TagUtil.TYPE_MATERIAL); // TODO: for entity tags?
        od.kioskId = inv.getKioskId();
        od.oid = transDao.getKeyAsString(trans);
        od.materialId = trans.getMaterialId();
        if (ITransaction.TYPE_ISSUE.equals(trans.getType()) || ITransaction.TYPE_TRANSFER
            .equals(trans.getType())) {
          od.customerId = trans.getLinkedKioskId();
        } else {
          od.vendorId = trans.getLinkedKioskId();
        }
        od.userId = trans.getSourceUserId();
      } catch (Exception e) {
        xLogger.warn("{0} when getting tags for Inventory object for kiosk-material {1}-{2}: {3}",
            e.getClass().getName(), trans.getKioskId(), trans.getMaterialId(), e);
      }
    } else if (o instanceof IInvntryBatch) {
      IInvntryBatch invBatch = ((IInvntryBatch) o);
      od.tags = invBatch.getTags(TagUtil.TYPE_MATERIAL); // TODO: for entity tags?
      od.kioskId = invBatch.getKioskId();
      od.oid = invBatch.getKeyString();
      od.materialId = invBatch.getMaterialId();
    } else {
      return super.getObjectData(o, pm);
    }
    if (od.kioskId != null) {
      updateObjectLocation(od, pm);
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
    String objectType = event.getObjectType();
    try {
      switch (eventId) {
        case IEvent.NO_ACTIVITY: {
          Date start = getInactiveDurationStart(event);
          if (start != null) {
            return EventHandler.hasNoActivity(domainId, objectType, start, pm);
          }
        }
        case IEvent.STOCKOUT: {
          try {
            IInvntry
                inv =
                JDOUtils.getObjectById(IInvntry.class,
                    daoUtil.createKeyFromString(event.getObjectId()));
            return BigUtil.equalsZero(inv.getStock());
          } catch (JDOObjectNotFoundException e1) {
            return false;
          }
        }
        case IEvent.UNDERSTOCK: {
          try {
            IInvntry
                inv =
                JDOUtils.getObjectById(IInvntry.class,
                    daoUtil.createKeyFromString(event.getObjectId()));
            return inv.isStockUnsafe();
          } catch (JDOObjectNotFoundException e1) {
            return false;
          }
        }
        case IEvent.EXPIRED: { // batch expiry
          return isBatchExpiryValid(event, pm);
        }
      }
    } catch (Exception e) {
      xLogger.warn("{0} when retrieving {1} with key {2} in domain {3} for event {4}: {5}",
          e.getClass().getName(), event.getObjectType(), event.getObjectId(), event.getDomainId(),
          eventId, e);
    }
    return super.isEventValid(event, pm);
  }

  // Check if the batch expiry event is still valid (say, to be shown on bulletin-board)
  private boolean isBatchExpiryValid(IEvent event, PersistenceManager pm) {
    try {
      // Get inv. batch object
      IInvntryBatch
          invBatch =
          JDOUtils
              .getObjectById(IInvntryBatch.class, daoUtil.createKeyFromString(event.getObjectId()),
                  pm);
      if (BigUtil.equalsZero(
          invBatch.getQuantity())) // invalid if this batch has no stock, no longer relevant
      {
        return false;
      }
                        /*
                        // Get expiry duration for notification
			Map<String,Object> params = event.getParams();
			int expiresInDays = 0;
			if ( params != null && !params.isEmpty() ) {
				String expiresInDaysStr = (String) params.get( PARAM_EXPIRESINDAYS );
				if ( expiresInDaysStr != null && !expiresInDaysStr.isEmpty() )
					expiresInDays = Integer.parseInt( expiresInDaysStr );
			}
			*/
      // Let keep the event valid, up to X days after the actual expiry
      // Today, with duration offset
      DomainConfig dc = DomainConfig.getInstance(event.getDomainId());
      Calendar calToday = LocalDateUtil.getZeroTime(dc.getTimezone());
      // Batch expiry date
      Calendar calExpiry = LocalDateUtil.getZeroTime(dc.getTimezone());
      calExpiry.setTime(invBatch.getBatchExpiry());
      calExpiry.add(Calendar.DATE, 3); // push expiry by 3 days (the validity period for the event)
      return calToday.getTime().compareTo(calExpiry.getTime()) < 0;
    } catch (JDOObjectNotFoundException e) {
      return false;
    }
  }
}
