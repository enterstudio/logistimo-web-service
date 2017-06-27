package com.logistimo.entities.entity;

import java.util.Date;
import java.util.List;

/**
 * Created by naveensnair on 19/05/17.
 */
public interface IApprovers {

  int PRIMARY_APPROVER = 0;
  int SECONDARY_APPROVER = 1;
  String PURCHASE_ORDER = "p";
  String SALES_ORDER = "s";

  Long getId();

  Long getKioskId();

  void setKioskId(Long kid);

  String getUserId();

  void setUserId(String uid);

  Integer getType();

  void setType(Integer type);

  String getOrderType();

  void setOrderType(String orderType);

  Date getCreatedOn();

  void setCreatedOn(Date con);

  String getCreatedBy();

  void setCreatedBy(String cby);

  Date getUpdatedOn();

  void setUpdatedOn(Date uon);

  String getUpdatedBy();

  void setUpdatedBy(String uby);

  List<String> getPrimaryApprovers();

  void setPrimaryApprovers(List<String> pa);

  List<String> getSecondaryApprovers();

  void setSecondaryApprovers(List<String> sa);

  Long getSourceDomainId();

  void setSourceDomainId(Long sdid);
}
