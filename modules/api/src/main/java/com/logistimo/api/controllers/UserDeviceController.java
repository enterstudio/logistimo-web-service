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

package com.logistimo.api.controllers;

import com.logistimo.api.models.UserDeviceModel;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.logger.XLog;
import com.logistimo.services.ServiceException;
import com.logistimo.users.service.UsersService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by yuvaraj on 23/05/17.
 */
@Controller
@RequestMapping("/user-device")
public class UserDeviceController {

  private static final XLog xLogger = XLog.getLog(UserDeviceController.class);

  @Autowired
  UsersService usersService;

  @RequestMapping(value = "/", method = RequestMethod.POST)
  public
  @ResponseBody
  void addEditUserDevice(@RequestBody UserDeviceModel udModel)
      throws ServiceException {

    validate(udModel, SecurityUtils.getUserDetails().getUsername());
    usersService.addEditUserDevice(udModel);
    xLogger.info("USER DEVICE for user: {0} STORED SUCCESSFULLY", udModel);
  }

  private void validate(UserDeviceModel udModel, String userId) {
    if (udModel == null ||
        (udModel.userid == null || udModel.appname == null || udModel.token == null) ||
        !userId.equals(udModel.userid)) {
      throw new InvalidDataException("validation error");
    }
  }

}
