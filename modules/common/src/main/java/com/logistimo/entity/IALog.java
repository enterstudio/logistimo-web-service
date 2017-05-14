package com.logistimo.entity;

import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface IALog {
  // Types
  String TYPE_BBOARD = "bb";

  Long getKey();

  Long getDomainId();

  void setDomainId(Long domainId);

  String getType();

  void setType(String type);

  Date getTimestamp();

  void setTimestamp(Date timestamp);

  String getIPAddress();

  void setIPAddress(String ipAddress);

  String getUserAgent();

  void setUserAgent(String userAgent);

  IALog init(Long domainId, String typeBboard, String remoteAddr, String header);
}
