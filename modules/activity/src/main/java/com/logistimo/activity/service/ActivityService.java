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

package com.logistimo.activity.service;

import com.logistimo.activity.entity.IActivity;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.util.Date;

import javax.jdo.PersistenceManager;

/**
 * Created by kumargaurav on 07/10/16.
 */
public interface ActivityService extends Service {

  IActivity createActivity(IActivity activity) throws ServiceException;

  Results getActivity(String objectId, String objectType, Date fromDate, Date toDate, String userId,
      String tag, PageParams pageParams) throws ServiceException;

  IActivity getLatestActivityWithStatus(String objectType, String objectId, String newValue)
      throws ServiceException;

  IActivity createActivity(String objectType, String objectId, String field, String oldStatus,
      String newStatus, String updatingUserId, Long domainId, String messageId, String tag,
      PersistenceManager pm);

  IActivity createActivity(String objectType, String objectId, String field, String prevValue,
      String newValue, String updatingUserId, Long domainId, String messageId, String tag,
      Date date, PersistenceManager pm);
}
