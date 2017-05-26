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

import com.logistimo.constants.CharacterConstants;
import com.logistimo.logger.XLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;

/**
 * Created by smriti on 2/6/17.
 */

@Controller
@RequestMapping("/migrator")
public class MigrationController {
  private static final XLog XLOGGER = XLog.getLog(MigrationController.class);

  @RequestMapping(value = "/materialstatus/")
  public
  @ResponseBody
  void updateMaterialStatusConfig(@RequestParam(required = false) String key, @RequestParam(required = false) String force) {
    boolean isSuccess;
    if(key == null) {
      isSuccess = MSConfigMigrator.update(force);
    } else if (key.contains(CharacterConstants.COMMA)) {
      isSuccess = MSConfigMigrator.update(Arrays.asList(key.split(CharacterConstants.COMMA)), force);
    } else {
      isSuccess = MSConfigMigrator.update(key, force);
    }
    if(isSuccess) {
      XLOGGER.info("Migrating material status configuration completed successfully.");
    } else {
      XLOGGER.info("Error in migrating material status configuration");
    }
  }
}
