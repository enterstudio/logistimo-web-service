package com.logistimo.api.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public class SessionDashboardModel implements Serializable {
  public Map<String, List<DashboardChartModel>> domain;
  // [ State/District/Entity Name: [Date : 1,2,3] ]
  public Map<String, Map<String, List<DashboardChartModel>>> data;
  public String mTy;
  public String mTyNm;
  public String mLev;
  public String ut; // Updated time
}
