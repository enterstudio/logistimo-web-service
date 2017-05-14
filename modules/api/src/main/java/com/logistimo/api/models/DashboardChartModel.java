package com.logistimo.api.models;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public class DashboardChartModel implements Serializable {

  public Long value;
  public Double per; //Percentage
  public Long num;
  public Long den;
  public Long kid;
  public Long mid;
  public Map<String, DashboardChartModel> materials;

  public DashboardChartModel() {
  }

  public DashboardChartModel(Long value) {
    this.value = value;
  }

  public DashboardChartModel(Long value, Long kid) {
    this.value = value;
    this.kid = kid;
  }

  public DashboardChartModel(Long value, Double per, Long num, Long den) {
    this.value = value;
    this.per = per;
    this.num = num;
    this.den = den;
  }
}
