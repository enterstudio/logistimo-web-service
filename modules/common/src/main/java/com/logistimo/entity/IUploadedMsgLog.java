package com.logistimo.entity;

import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface IUploadedMsgLog {

  Date getTimeStamp();

  void setTimeStamp(Date timeStamp);

  Long getDomainId();

  void setDomainId(Long dId);

  String getMessage();

  void setMessage(String msg);

  String getUploadedId();

  void setUploadedId(String uploadedId);
}
