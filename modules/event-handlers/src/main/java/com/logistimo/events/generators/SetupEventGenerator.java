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
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventSpec.ParamComparator;
import com.logistimo.config.models.EventSpec.ParamSpec;
import com.logistimo.config.models.EventSpec.Subscriber;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IPoolGroup;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.events.models.ObjectData;
import com.logistimo.logger.XLog;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;


/**
 * @author Arun
 */
public class SetupEventGenerator extends EventGenerator {

  // Params.
  public static final String PARAM_IPADDRESS = "ipaddress";
  private static final XLog xLogger = XLog.getLog(SetupEventGenerator.class);

  public SetupEventGenerator(Long domainId, String objectType) {
    super(domainId, objectType);
  }

  @Override
  public ParamSpec match(int eventId, Map<String, Object> params) {
    switch (eventId) {
      case IEvent.IP_ADDRESS_MATCHED: {
        DomainConfig dc = DomainConfig.getInstance(domainId);
        EventsConfig ec = dc.getEventsConfig();
        return ec.matchEvent(objectType, eventId, params, new ParamComparator() {
          @Override
          public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
            String
                t1 =
                (String) params1.get(PARAM_IPADDRESS); // from generate call of event-generator
            String t2 = (String) params2.get(PARAM_IPADDRESS); // from spec.
            if (t1 != null && t2 != null && !t2.isEmpty()) {
              // t2 is a CSV of IP-address patterns; tokenize it
              String[] ipPatterns = t2.split(",");
              for (int i = 0; i < ipPatterns.length; i++) {
                if (t1.startsWith(ipPatterns[i])) {
                  return true;
                }
              }
              return false; // no match
            } else {
              return false;
            }
          }
        });
      }
      default:
        return super.match(eventId, params);
    }
  }

  @Override
  public List<String> getSubscriberIds(Subscriber subscriber, Object o, Long domainId) {
    xLogger.fine("Entered getSubscriberIds");
    if (subscriber == null) {
      return null;
    }
    if (EventSpec.Subscriber.CUSTOMERS.equals(subscriber.type) && o != null) {
      if (o instanceof IUserAccount && ((IUserAccount) o).isEnabled()) {
        List<String> userIds = new ArrayList<String>();
        userIds.add(((IUserAccount) o).getUserId());
        return userIds;
      } else if (o instanceof IKiosk) {
        return EventHandler.getSubsribers(subscriber.type, ((IKiosk) o).getKioskId());
      } else if (o instanceof IAssetRelation) {
        try {
          IAssetRelation assetRelation = (IAssetRelation) o;
          AssetManagementService ams = Services.getService(AssetManagementServiceImpl.class);
          IAsset asset = ams.getAsset(assetRelation.getAssetId());
          if(asset.getKioskId() != null) {
            return EventHandler.getSubsribers(subscriber.type, asset.getKioskId());
          }
        } catch (Exception e) {
          xLogger.warn("Error while fetching asset details for asset: {0}",
              ((IAssetRelation) o).getAssetId());
        }
        return null;
      } else if(o instanceof IAsset) {
        return EventHandler.getSubsribers(subscriber.type, ((IAsset)o).getKioskId());
      }
      else {
        xLogger.warn(
            "Invalid object type {0} for subscriber type {1} in SetupEventGenerator.getSubscriberIds",
            o.getClass().getName(), subscriber.type);
        return null;
      }
    } else if (EventSpec.Subscriber.ADMINISTRATORS.equals(subscriber.type) && o != null
        && !(o instanceof Long)) {
      if (domainId == null) {
        if (o instanceof IUserAccount && ((IUserAccount) o).isEnabled()) {
          domainId = ((IUserAccount) o).getDomainId();
        } else if (o instanceof IKiosk) {
          domainId = ((IKiosk) o).getDomainId();
        } else if (o instanceof IPoolGroup) {
          domainId = ((IPoolGroup) o).getDomainId();
        } else if (o instanceof IAsset) {
          domainId = ((IAsset) o).getDomainId();
        } else if (o instanceof IAssetRelation) {
          domainId = ((IAssetRelation) o).getDomainId();
        } else {
          xLogger.warn(
              "Invalid object type {0} for subscriber type {1} in SetupEventGenerator.getSubscriberIds",
              o.getClass().getName(), subscriber.type);
          return null;
        }
      }
      return EventHandler.getSubsribers(subscriber.type, domainId);
    } else if (EventSpec.Subscriber.ASSET_USERS.equals(subscriber.type) && o != null) {
      IAsset asset;
      IAssetRelation assetRelation = null;
      List<String> assetUsers = new ArrayList<>(1);
      if (o instanceof IAsset) {
        asset = (IAsset) o;
        addOwnersAndMaintainers(asset, assetUsers);
      } else if(o instanceof IAssetRelation){
        try {
          assetRelation = (IAssetRelation) o;
          AssetManagementService ams = Services.getService(AssetManagementServiceImpl.class);
          asset = ams.getAsset(assetRelation.getRelatedAssetId());
          addOwnersAndMaintainers(asset, assetUsers);
          asset = ams.getAsset(assetRelation.getAssetId());
          addOwnersAndMaintainers(asset, assetUsers);
        } catch (Exception e) {
          xLogger.warn("{0} while getting asset {1} in domain {2}", e.getMessage(),
              assetRelation.getAssetId(), assetRelation.getDomainId(), e);
        }
      }
      return assetUsers;
    } else {
      return super.getSubscriberIds(subscriber, o, domainId);
    }
  }

  private void addOwnersAndMaintainers(IAsset asset, List<String> assetUsers) {
    if (!CollectionUtils.isEmpty(asset.getOwners())) {
      assetUsers.addAll(asset.getOwners());
    }
    if (!CollectionUtils.isEmpty(asset.getMaintainers())) {
      assetUsers.addAll(asset.getMaintainers());
    }
  }

  // Get common metadata associated with a given object
  @Override
  public ObjectData getObjectData(Object o, PersistenceManager pm) {
    ObjectData od = new ObjectData();
    if (o instanceof IKiosk) {
      od.kioskId = ((IKiosk) o).getKioskId();
      if (od.kioskId != null) {
        updateObjectLocation(od, pm);
      }
      od.oid = od.kioskId;
      return od;
    } else if (o instanceof IUserAccount) {
      IUserAccount u = (IUserAccount) o;
      od.city = u.getCity();
      od.district = u.getDistrict();
      od.state = u.getState();
      od.oid = u.getUserId();
      od.userId = (String) od.oid;
      return od;
    } else {
      return super.getObjectData(o, pm);
    }
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
    if (JDOUtils.getImplClass(IUserAccount.class).getName().equals(objectType)) {
      try {
        switch (eventId) {
          case IEvent.NO_ACTIVITY: {
            Date start = getInactiveDurationStart(event);
            if (start != null) {
              return EventHandler.hasNoActivity(domainId, objectType, start, pm);
            }
          }
          case IEvent.EXPIRED: {
            try {
              IUserAccount
                  u =
                  JDOUtils.getObjectById(IUserAccount.class,
                      AppFactory.get().getDaoUtil().createKeyFromString(event.getObjectId()), pm);
              Date start = getInactiveDurationStart(event);
              if (start != null) {
                Date loginDate = u.getLastLogin();
                return u.isEnabled() && (loginDate == null || loginDate.compareTo(start) == -1);
              }
            } catch (JDOObjectNotFoundException e1) {
              return false;
            }
          }
        }
      } catch (Exception e) {
        xLogger.warn("{0} when retrieving {1} with key {2} in domain {3} for event {4}: {5}",
            e.getClass().getName(), event.getObjectType(), event.getObjectId(), event.getDomainId(),
            eventId, e);
      }
    }
    return super.isEventValid(event, pm);
  }
}
