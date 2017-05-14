package com.logistimo.entities.entity;

import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface IUserToKiosk {
  int DEFAULT_ROUTE_INDEX = 2147483647;

  Long getUserToKioskId();

  String getUserId();

  void setUserId(String userId);

  void setKioskId(Long kioskId, String kioskName);

  void setNormalizedKioskName(String kioskName);

  Long getKioskId();

  int getRouteIndex();

  void setRouteIndex(int ri);

  String getTag();

  void setTag(String tag);

  Long getDomainId();

  void setDomainId(Long domainId);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);
}
