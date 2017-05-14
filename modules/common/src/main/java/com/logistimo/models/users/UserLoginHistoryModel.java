package com.logistimo.models.users;

import java.util.Date;

/**
 * Created by mohansrinivas on 10/18/16.
 */
public class UserLoginHistoryModel {

  public String userId;
  public Integer lgSrc;
  public String usrAgnt;
  public String ipAddr;
  public Date loginTime;
  public String version;

  public UserLoginHistoryModel(String userId, Integer lgSrc, String usrAgnt, String ipAddr,
                               Date loginTime, String version) {
    this.userId = userId;
    this.lgSrc = lgSrc;
    this.usrAgnt = usrAgnt;
    this.ipAddr = ipAddr;
    this.loginTime = loginTime;
    this.version = version;
  }
}
