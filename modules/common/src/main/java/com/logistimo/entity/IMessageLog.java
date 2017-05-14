package com.logistimo.entity;

import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface IMessageLog {
  String getKey();

  void setKey(String key);

  String getType();

  void setType(String ty);

  String getProviderId();

  void setProviderId(String pid);

  String getAddress();

  void setAddress(String adr);

  String getUserId();

  void setUserId(String uId);

  String getSenderId();

  void setSenderId(String suId);

  String getMessage();

  void setMessage(String message);

  String getStatus();

  void setStatus(String st);

  Date getTimestamp();

  void setTimestamp(Date t);

  Date getDoneTime();

  void setDoneTime(Date dt);

  Long getDomainId();

  void setDomainId(Long dId);

  Integer getNotif();

  void setNotif(Integer notif);

  String getEventType();

  void setEventType(String eventType);

}
