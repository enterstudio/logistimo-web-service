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

package com.logistimo.activity.builders;

import com.logistimo.activity.entity.IActivity;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.conversations.service.impl.ConversationServiceImpl;
import com.logistimo.dao.JDOUtils;

import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.activity.models.ActivityModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kumargaurav on 07/10/16.
 */
public class ActivityBuilder {

  public IActivity buildActivity(ActivityModel model) {

    IActivity activity = JDOUtils.createInstance(IActivity.class);
    activity.setObjectId(model.objectId);
    activity.setObjectType(model.objectType);
    activity.setAction(model.action);
    activity.setField(model.field);
    activity.setDomainId(model.domainId);
    activity.setMessageId(model.messageId);
    activity.setNewValue(model.newValue);
    activity.setPrevValue(model.prevValue);
    activity.setTag(model.tag);
    activity.setUserId(model.userId);
    activity.setCreateDate(new Date());
    return activity;
  }

  public List<ActivityModel> buildModelList(List<IActivity> activity) {
    List<ActivityModel> models = new ArrayList<>(activity.size());
    for (IActivity iActivity : activity) {
      models.add(buildModel(iActivity));
    }
    return models;
  }

  public ActivityModel buildModel(IActivity activity) {
    ActivityModel model = new ActivityModel();
    model.id = activity.getId();
    model.objectId = activity.getObjectId();
    model.objectType = activity.getObjectType();
    model.domainId = activity.getDomainId();
    model.messageId = activity.getMessageId();
    try {
      ConversationService cs = Services.getService(ConversationServiceImpl.class);
      model.message = cs.getMessageById(activity.getMessageId()).getMessage();
    } catch (Exception e) {
      // ignored
    }
    model.field = activity.getField();
    model.prevValue = activity.getPrevValue();
    model.newValue = activity.getNewValue();
    if (null != activity.getCreateDate()) {
      model.createDate =
          LocalDateUtil.formatCustom(activity.getCreateDate(), Constants.DATETIME_FORMAT, null);
    }
    model.action = activity.getAction();
    model.userId = activity.getUserId();
    try {
      UsersService as = Services.getService(UsersServiceImpl.class);
      model.userName = as.getUserAccount(activity.getUserId()).getFullName();
    } catch (Exception e) {
      // ignored2
    }
    model.tag = activity.getTag();
    return model;
  }
}
