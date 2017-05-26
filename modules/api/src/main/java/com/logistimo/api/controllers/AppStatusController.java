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

import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.logger.XLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;

/**
 * In order to automate deployments, automatically put server in maintenance mode for deployments.
 * Check the existence of a file for a location mentioned in "status.file.path" path variable under "samaanguru" properties file .
 * If file exists, return response code 200.
 * If file not exists, return response code 503 i.e., Service unavailable.
 */


@Controller
@RequestMapping("/app")
public class AppStatusController {
  public static XLog xLog = XLog.getLog(AppStatusController.class);

  @RequestMapping(value = "/status")
  public ResponseEntity getStatus() {
    try {
      if (new File(ConfigUtil.get("status.file.path")).exists()) {
        return new ResponseEntity(HttpStatus.OK);
      }
    } catch (Exception e) {
      xLog.warn("Error in checking the status of application:", e);
    }
    return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
  }
}
