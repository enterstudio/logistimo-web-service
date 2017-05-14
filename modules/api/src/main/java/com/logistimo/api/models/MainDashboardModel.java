package com.logistimo.api.models;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public class MainDashboardModel implements Serializable {
  public Map<String, Long> invDomain;
  public Map<String, Long> entDomain;
  public Map<String, Long> tempDomain;
  public Map<String, Map<String, DashboardChartModel>> inv;
  public Map<String, Map<String, DashboardChartModel>> ent;
  public Map<String, Map<String, DashboardChartModel>> temp;
  public String mTy;
  public String mTyNm;
  public String mLev;
  public String mPTy;
  public Map<String, InvDashboardModel> invByMat;
  public String ut; // Updated time
  public DashboardChartModel pred; // Predictive: Overall predictive data on overall dashboard
}
