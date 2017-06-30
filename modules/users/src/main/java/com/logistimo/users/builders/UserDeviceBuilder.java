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

package com.logistimo.users.builders;

import com.logistimo.api.models.UserDeviceModel;
import com.logistimo.dao.JDOUtils;
import com.logistimo.users.entity.IUserDevice;

import java.util.Date;

public class UserDeviceBuilder {

  public IUserDevice buildUserDevice(IUserDevice userDevice, UserDeviceModel model) {

    Date currentDate = new Date();
    if (userDevice == null) {
      userDevice = JDOUtils.createInstance(IUserDevice.class);
      userDevice.setCreatedOn(currentDate);
    } else {
      userDevice.setUpdatedOn(currentDate);
    }
    userDevice.setUserId(model.userid);
    userDevice.setAppname(model.appname);
    userDevice.setToken(model.token);
    return userDevice;
  }
}
