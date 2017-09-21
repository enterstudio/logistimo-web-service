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

package com.logistimo.events.generators;


import com.logistimo.assets.entity.IAsset;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.dao.JDOUtils;

import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.events.EventConstants;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.events.models.ObjectData;
import com.logistimo.tags.TagUtil;

import com.logistimo.events.entity.IEvent;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Created by kaniyarasu on 12/12/14.
 */
public class AssetEventGenerator extends EventGenerator {
  protected static final XLog xLogger = XLog.getLog(AssetEventGenerator.class);

  public AssetEventGenerator(Long domainId, String objectType) {
    super(domainId, objectType);
  }

  @Override
  public ObjectData getObjectData(Object o, PersistenceManager pm) {
    IAssetStatus assetStatus = (IAssetStatus) o;
    ObjectData od = new ObjectData();
    od.oid = assetStatus.getAssetId();
    IAsset asset = JDOUtils.getObjectById(IAsset.class, assetStatus.getAssetId());
    if (asset != null) {
      od.kioskId = asset.getKioskId();
    }
    return od;
  }

  @Override
  public List<String> getSubscriberIds(EventSpec.Subscriber subscriber, Object o, Long domainId) {
    if (subscriber == null) {
      return null;
    }
    IAsset asset = null;
    if (o != null) {
      IAssetStatus assetStatus = (IAssetStatus) o;
      try {
        asset = JDOUtils.getObjectById(IAsset.class, assetStatus.getAssetId());
      } catch (Exception e) {
        xLogger.warn("Error while finding the asset Id", e);
      }
    }
    if (EventSpec.Subscriber.CUSTOMERS.equals(subscriber.type) && asset != null) {
      return EventHandler.getSubsribers(subscriber.type, asset.getKioskId());
    } else if (EventSpec.Subscriber.ADMINISTRATORS.equals(subscriber.type) && asset != null) {
      return EventHandler
          .getSubsribers(subscriber.type, domainId != null ? domainId : asset.getDomainId());
    } else if (EventSpec.Subscriber.ASSET_USERS.equals(subscriber.type) && asset != null) {
      List<String> assetUsers = new ArrayList<>(1), assetOwners = asset.getOwners(),
          assetMaintainers =
              asset.getMaintainers();
      if (!assetOwners.isEmpty()) {
        assetUsers.addAll(assetOwners);
      }
      if (!assetMaintainers.isEmpty()) {
        assetUsers.addAll(assetMaintainers);
      }
      return assetUsers;
    } else {
      return super.getSubscriberIds(subscriber, o, domainId);
    }
  }

  @Override
  public boolean isEventValid(IEvent event, PersistenceManager pm) {
    // Get the object of the event
    String objectId = event.getObjectId();
    IAssetStatus assetStatus;
    boolean isEventValid = false;
    try {
      if (objectId != null) {
        assetStatus = JDOUtils.getObjectById(IAssetStatus.class, objectId);

        switch (event.getId()) {
          case IEvent.HIGH_EXCURSION:
            if (IAsset.STATUS_EXCURSION == assetStatus.getStatus()
                && IAsset.ABNORMAL_TYPE_HIGH == assetStatus.getAbnStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.HIGH_WARNING:
            if (IAsset.STATUS_WARNING == assetStatus.getStatus()
                && IAsset.ABNORMAL_TYPE_HIGH == assetStatus.getAbnStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.HIGH_ALARM:
            if (IAsset.STATUS_ALARM == assetStatus.getStatus()
                && IAsset.ABNORMAL_TYPE_HIGH == assetStatus.getAbnStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.LOW_EXCURSION:
            if (IAsset.STATUS_EXCURSION == assetStatus.getStatus()
                && IAsset.ABNORMAL_TYPE_LOW == assetStatus.getAbnStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.LOW_WARNING:
            if (IAsset.STATUS_WARNING == assetStatus.getStatus()
                && IAsset.ABNORMAL_TYPE_LOW == assetStatus.getAbnStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.LOW_ALARM:
            if (IAsset.STATUS_ALARM == assetStatus.getStatus()
                && IAsset.ABNORMAL_TYPE_LOW == assetStatus.getAbnStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.INCURSION:
            if (IAsset.STATUS_NORMAL == assetStatus.getStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.NO_ACTIVITY:
            if (IAsset.STATUS_INACTIVE == assetStatus.getStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.BATTERY_LOW:
            if (IAsset.STATUS_BATTERY_LOW == assetStatus.getStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.BATTERY_ALARM:
            if (IAsset.STATUS_BATTERY_ALARM == assetStatus.getStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.SENSOR_DISCONNECTED:
            if (IAsset.STATUS_SENSOR_DISCONNECTED == assetStatus.getStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.ASSET_INACTIVE:
            if (IAsset.STATUS_ASSET_INACTIVE == assetStatus.getStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.POWER_OUTAGE:
            if (IAsset.STATUS_POWER_OUTAGE == assetStatus.getStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.DEVICE_DISCONNECTED:
            if (IAsset.STATUS_DEVICE_DISCONNECTED == assetStatus.getStatus()) {
              isEventValid = true;
            }
            break;
          case IEvent.STATUS_CHANGE:
          case IEvent.STATUS_CHANGE_TEMP:
            isEventValid = true;
            break;
          default:
            if (IEvent.ASSET_ALARM_NORMAL_GROUP.contains(event.getId())
                && IAsset.STATUS_NORMAL == assetStatus.getStatus()) {
              isEventValid = true;
            }
        }
        if ((assetStatus.getTs().compareTo(event.getTimestamp()) != 0)) {
          isEventValid = false;
        }

      }
    } catch (Exception e1) {
      xLogger
          .warn("Object {0} ({1}) not found when notify event in domain {2}", objectId, objectType,
              domainId);
    }
    return isEventValid;
  }

  public Map<String, Object> getTagParams(Object eventObject) {
    Map<String, Object> params = new HashMap<String, Object>(1);
    List<String> mTags = null, eTags = null, oTags = null;
    if (eventObject instanceof IAssetStatus) {
      try {
        IAsset
            asset =
            JDOUtils.getObjectById(IAsset.class, ((IAssetStatus) eventObject).getAssetId());

        IKiosk kiosk = JDOUtils.getObjectById(IKiosk.class, asset.getKioskId());
        eTags = kiosk.getTags();
      } catch (Exception e) {
        xLogger.warn("Failed to get tags for Kiosk related to asset st:{0} and id:{1}",
            ((IAssetStatus) eventObject).getId(), ((IAssetStatus) eventObject).getAssetId(), e);
      }
    }
    params.put(EventConstants.PARAM_ENTITYTAGSTOEXCLUDE, eTags);
    if (eTags != null && !eTags.isEmpty()) {
      params.put(EventConstants.PARAM_ENTITYTAGSTOEXCLUDE, eTags);
    }
    return params.isEmpty() ? null : params;
  }

  public EventSpec.ParamSpec match(int eventId, Map<String, Object> params) {
    DomainConfig dc = DomainConfig.getInstance(domainId);
    EventsConfig ec = dc.getEventsConfig();
    EventSpec.ParamComparator paramComparator = null;
    if (eventId == IEvent.NO_ACTIVITY) { // no activity event comparator
      paramComparator = new EventSpec.ParamComparator() {
        @Override
        public boolean compare(Map<String, Object> params1,
                               Map<String, Object> params2) { // params1 is from actual event; params2 is from event spec.
          String t1 = (String) params1.get(EventConstants.PARAM_INACTIVEDURATION);
          String t2 = (String) params2.get(EventConstants.PARAM_INACTIVEDURATION);
          return t1 != null && t2 != null && Float.parseFloat(t1) >= Float.parseFloat(t2);
        }
      };
    }

    if (eventId == IEvent.HIGH_EXCURSION || eventId == IEvent.LOW_EXCURSION
        || eventId == IEvent.HIGH_WARNING || eventId == IEvent.LOW_WARNING
        || eventId == IEvent.HIGH_ALARM || eventId == IEvent.LOW_ALARM || IEvent.ASSET_ALARM_GROUP
        .contains(eventId)) {
      paramComparator = new EventSpec.ParamComparator() {
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
      paramComparator = new EventSpec.ParamComparator() {
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
      paramComparator = new EventSpec.ParamComparator() {
        @Override
        public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
          return tagParamsNotMatched(params1, params2, TagUtil.TYPE_ENTITY);
        }
      };
    } else if (eventId == IEvent.STATUS_CHANGE || eventId == IEvent.STATUS_CHANGE_TEMP) {
      paramComparator = new EventSpec.ParamComparator() {
        @Override
        public boolean compare(Map<String, Object> params1, Map<String, Object> params2) {
          boolean
              tagParamsNotMatched =
              tagParamsNotMatched(params1, params2, TagUtil.TYPE_ENTITY);
          boolean
              paramMatched =
              paramMatched(params1, params2, EventConstants.PARAM_STATUS);
          return paramMatched && tagParamsNotMatched;
        }
      };
    }
    return ec.matchEvent(objectType, eventId, params, paramComparator);
  }
}
