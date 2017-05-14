package com.logistimo.api.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohansrinivas on 10/20/16.
 */
public class DomainStatisticsModel {
  public Long did;   //domainId
  public String dnm;  // Domain name
  public Long akc; // active kiosk count
  public Long lkc; // live kiosk count
  public Long kc; // kiosk count
  public Long uc; // user count
  public Long mlwa; // monitored live working asset
  public Long mwa;  // monitored working asset
  public Long mawa; // monitored active working asset
  public Long mac;  // monitored asset count
  public Long miac;  // monitoring  asset count
  public List<DomainStatisticsModel> child = new ArrayList<>();
  public boolean hc; // has child flag
  /**
   * Last run time
   */
  public String lrt;

}
