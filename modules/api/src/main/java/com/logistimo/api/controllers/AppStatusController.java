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
