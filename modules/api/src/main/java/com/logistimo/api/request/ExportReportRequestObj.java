package com.logistimo.api.request;

import java.util.Map;

/**
 * Created by naveensnair on 07/04/15.
 */
public class ExportReportRequestObj {
  public String type;
  public String startDate;
  public String endDate;
  public String frequency;
  public Map<String, String[]> filterMap;

}
