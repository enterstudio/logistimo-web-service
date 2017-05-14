package com.logistimo.api.request;

import com.logistimo.config.models.CustomReportsConfig;
import com.logistimo.api.models.configuration.CustomReportsConfigModel;

/**
 * Created by naveensnair on 04/02/15.
 */
public class AddCustomReportRequestObj {
  public CustomReportsConfigModel customReport;
  public CustomReportsConfig.Config config;
}
