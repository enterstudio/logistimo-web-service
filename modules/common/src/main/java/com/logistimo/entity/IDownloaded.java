package com.logistimo.entity;

import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface IDownloaded {
  Long getKey();

  void setKey(Long key);

  String getUserId();

  void setUserId(String userId);

  Date getTime();

  void setTime(Date time);

  String getVersion();

  void setVersion(String version);

  String getLocale();

  void setLocale(String locale);

  String getDeviceDetails();

  void setDeviceDetails(String deviceDetails);
}
