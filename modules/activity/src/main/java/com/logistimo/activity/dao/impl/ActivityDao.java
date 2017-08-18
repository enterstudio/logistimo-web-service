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

package com.logistimo.activity.dao.impl;

import com.logistimo.activity.builders.ActivityBuilder;
import com.logistimo.activity.dao.IActivityDao;
import com.logistimo.activity.entity.Activity;
import com.logistimo.activity.entity.IActivity;
import com.logistimo.activity.models.ActivityModel;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.QueryConstants;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by kumargaurav on 11/10/16.
 */
public class ActivityDao implements IActivityDao {

  private static final XLog xLogger = XLog.getLog(ActivityDao.class);
  private static final String AND_OBJECT_TYPE = " AND objectType = ";

  private ActivityBuilder activityBuilder = new ActivityBuilder();

  public IActivity createActivity(IActivity activity) throws ServiceException {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      pm.makePersistent(activity);
      return pm.detachCopy(activity);
    } catch (Exception e) {
      xLogger.severe("{0} while creating activity {1}", e.getMessage(), activity, e);
      throw new ServiceException(e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  public IActivity getLatestActivityByStatus(String objectType, String objectId, String newValue)
      throws ServiceException {

    PersistenceManager pm = null;
    Query query = null;
    List<String> parameters = new ArrayList<>(3);
    List<IActivity> activities;
    IActivity activity = null;

    try {
      pm = PMF.get().getPersistenceManager();
      StringBuilder builder = new StringBuilder("SELECT * FROM ACTIVITY ");
      if (null != objectId) {
        builder.append(" WHERE objectId = ").append(CharacterConstants.QUESTION);
        parameters.add(objectId);
        if (null != objectType) {
          builder.append(AND_OBJECT_TYPE).append(CharacterConstants.QUESTION);
          parameters.add(objectType);
        }
        if (null != newValue) {
          builder.append(" AND newValue = ").append(CharacterConstants.QUESTION);
          parameters.add(newValue);
        }
      }

      builder.append(" ORDER BY CREATEDATE DESC LIMIT 1");

      query = pm.newQuery(Constants.JAVAX_JDO_QUERY_SQL, builder.toString());
      query.setClass(Activity.class);
      activities = (List<IActivity>) query.executeWithArray(parameters.toArray());
      activities = (List<IActivity>) pm.detachCopyAll(activities);
      if (!activities.isEmpty()) {
        activity = activities.get(0);
      }
    } catch (Exception e) {
      xLogger.severe("{0} while getting activity {1}", e.getMessage(), objectId, true, e);
      throw new ServiceException(e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }

      if (pm != null) {
        pm.close();
      }
    }
    return activity;
  }

  public Results getActivity(String objectId, String objectType, Date fromDate, Date toDate,
      String userId, String tag, PageParams pageParams) throws ServiceException {
    PersistenceManager pm = null;
    Query query = null;
    Query cntQuery = null;
    List<String> parameters = new ArrayList<>();
    Results res = null;
    try {
      if (pageParams == null) {
        pageParams = new PageParams(0, 50);
      }
      pm = PMF.get().getPersistenceManager();
      StringBuilder builder = new StringBuilder("SELECT * FROM ACTIVITY ");
      //as per discussion we expect objectId nd objectType combo or tag
      if (null != objectId) {
        builder.append(" WHERE objectId = ").append(CharacterConstants.QUESTION);
        parameters.add(objectId);
        if (null != objectType) {
          builder.append(AND_OBJECT_TYPE).append(CharacterConstants.QUESTION);
          parameters.add(objectType);
        }
        if (null != tag) {
          builder.append(" AND tag = ").append(CharacterConstants.QUESTION);
          parameters.add(tag);
        }
      } else if (null != tag) {
        builder.append(" WHERE tag = ").append(CharacterConstants.QUESTION);
        parameters.add(tag);
        if (null != objectType) {
          builder.append(AND_OBJECT_TYPE).append(CharacterConstants.QUESTION);
          parameters.add(objectType);
        }
      }

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      if (null != fromDate) {
        builder.append(" AND createDate >= TIMESTAMP(").append(CharacterConstants.QUESTION)
            .append(")");
        parameters.add(sdf.format(fromDate));
      }
      if (null != toDate) {
        builder.append(" AND createDate <= TIMESTAMP(").append(CharacterConstants.QUESTION)
            .append(")");
        parameters.add(sdf.format(toDate));
      }
      if (null != userId) {
        builder.append(" AND userId = ").append(CharacterConstants.QUESTION);
        parameters.add(userId);
      }

      final String orderBy = " ORDER BY CREATEDATE DESC";
      builder.append(orderBy);
      String
          limitStr =
          " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams.getSize();
      builder.append(limitStr);

      query = pm.newQuery(Constants.JAVAX_JDO_QUERY_SQL, builder.toString());
      query.setClass(Activity.class);
      List<IActivity> activities = (List<IActivity>) query.executeWithArray(parameters.toArray());
      activities = (List<IActivity>) pm.detachCopyAll(activities);

      String
          cntQueryStr =
          builder.toString().replace("*", QueryConstants.ROW_COUNT)
              .replace(orderBy, CharacterConstants.EMPTY);
      cntQueryStr = cntQueryStr.replace(limitStr, CharacterConstants.EMPTY);

      cntQuery = pm.newQuery(Constants.JAVAX_JDO_QUERY_SQL, cntQueryStr);
      int
          count =
          ((Long) ((List) cntQuery.executeWithArray(parameters.toArray())).iterator().next())
              .intValue();

      List<ActivityModel> models = null;
      if (null != activities) {
        models = activityBuilder.buildModelList(activities);
      }
      res = new Results(models, null, count, pageParams.getOffset());

    } catch (Exception e) {
      xLogger.severe("{0} while getting activity {1}", e.getMessage(), objectId, true, e);
      throw new ServiceException(e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (cntQuery != null) {
        try {
          cntQuery.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (pm != null) {
        pm.close();
      }
    }
    return res;
  }

}
