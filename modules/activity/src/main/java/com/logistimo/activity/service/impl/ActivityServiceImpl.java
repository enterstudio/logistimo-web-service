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

package com.logistimo.activity.service.impl;

import com.logistimo.activity.dao.impl.ActivityDao;
import com.logistimo.activity.entity.IActivity;
import com.logistimo.activity.service.ActivityService;
import com.logistimo.activity.dao.IActivityDao;
import com.logistimo.dao.JDOUtils;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.logger.XLog;

import java.util.Date;

import javax.jdo.PersistenceManager;


/**
 * Created by kumargaurav on 07/10/16.
 */
public class ActivityServiceImpl extends ServiceImpl implements ActivityService {

  private static final XLog xLogger = XLog.getLog(ActivityServiceImpl.class);

  private IActivityDao activityDao = new ActivityDao();

  @Override
  public void init(Services services) throws ServiceException {

  }

  @Override
  public void destroy() throws ServiceException {

  }

  @Override
  public Class<? extends Service> getInterface() {
    return ActivityService.class;
  }

  public IActivity createActivity(IActivity activity) throws ServiceException {
    return activityDao.createActivity(activity);
  }

  public Results getActivity(String objectId, String objectType, Date fromDate, Date toDate,
      String userId, String tag, PageParams pageParams)
      throws ServiceException {
    return activityDao.getActivity(objectId, objectType, fromDate, toDate, userId, tag, pageParams);
  }

  public IActivity getLatestActivityWithStatus(String objectType, String objectId,
      String newValue) throws ServiceException {
    return activityDao.getLatestActivityByStatus(objectType, objectId, newValue);
  }

  @Override
  public IActivity createActivity(String objectType, String objectId, String field,
      String prevValue, String newValue,
      String updatingUserId, Long domainId, String messageId,
      String tag,
      PersistenceManager pm) {
    return createActivity(objectType, objectId, field, prevValue, newValue, updatingUserId,
        domainId, messageId, tag, null, pm);
  }


  @Override
  public IActivity createActivity(String objectType, String objectId, String field,
      String prevValue, String newValue,
      String updatingUserId, Long domainId, String messageId,
      String tag, Date date,
      PersistenceManager pm) {
    IActivity activity = JDOUtils.createInstance(IActivity.class);
    activity.setObjectType(objectType);
    activity.setObjectId(objectId);
    activity.setField(field);
    activity.setNewValue(newValue);
    activity.setPrevValue(prevValue);
    Date createDate = date;
    if (null == date) {
      createDate = new Date();
    }
    activity.setCreateDate(createDate);
    activity.setTag(tag);
    activity.setUserId(updatingUserId);
    activity.setDomainId(domainId);
    activity.setMessageId(messageId);
    PersistenceManager localPM = pm;
    boolean useLocalPM = localPM == null;
    try {
      if (useLocalPM) {
        localPM = PMF.get().getPersistenceManager();
      }
      localPM.makePersistent(activity);
      if (useLocalPM) {
        activity = localPM.detachCopy(activity);
      }
    } finally {
      if (useLocalPM && localPM != null) {
        localPM.close();
      }
    }
    return activity;
  }
}
