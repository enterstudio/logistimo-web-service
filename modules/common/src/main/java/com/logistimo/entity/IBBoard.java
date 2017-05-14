package com.logistimo.entity;

import com.logistimo.domains.IMultiDomain;

import java.util.Date;
import java.util.List;

/**
 * Created by charan on 20/05/15.
 */
public interface IBBoard extends IMultiDomain {
  String TYPE_POST = "pst"; // manual text message post

  Long getKey();

  Long getDomainId();

  void setDomainId(Long domainId);

  String getType();

  void setType(String type);

  Long getEventKey();

  void setEventKey(Long eky);

  Integer getEventId();

  void setEventId(Integer eventId);

  String getMessage();

  void setMessage(String message);

  String getMediaUrl();

  void setMediaUrl(String mediaUrl);

  Long getKioskId();

  void setKioskId(Long kioskId);

  String getCity();

  void setCity(String city);

  String getDistrict();

  void setDistrict(String district);

  String getState();

  void setState(String state);

  List<String> getTags();

  void setTags(List<String> tags);

  String getUserId();

  void setUserId(String userId);

  Date getTimestamp();

  void setTimestamp(Date timestamp);
}
