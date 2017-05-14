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
