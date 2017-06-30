package com.logistimo.entity;

import java.util.Date;

/**
 * Created by yuvaraj on 22/03/17.
 */
public interface ILocationFailedJob
{
  String getId();

  void setId(String id);

  void setType(String type);

  String getType();

  void setPayLoad(String payload);

  String getPayLoad();

  void setCreateDate(Date createDate);

  Date getCreateDate();

  void setProcessFlag(boolean processFlag);

  boolean getProcessFlag();
}
