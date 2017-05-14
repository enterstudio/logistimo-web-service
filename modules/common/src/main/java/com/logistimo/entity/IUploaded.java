package com.logistimo.entity;

import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface IUploaded {
  String APP_FILENAME = "logistimo";
  String SEPARATOR = "_";
  // Files / file types
  String JAD = "jad";
  String JAR = "jar";
  String JADFILE = APP_FILENAME + "." + JAD;
  String JARFILE = APP_FILENAME + "." + JAR;
  // Upload types
  String EXPORT = "exprt";
  String BULKUPLOAD = "blkupld";
  String CUSTOMREPORT_TEMPLATE = "customreporttemplate";
  // Statuses
  int STATUS_PENDING = 0;
  int STATUS_DONE = 1;

  String getId();

  void setId(String key);

  String getFileName();

  void setFileName(String fileName);

  String getVersion();

  void setVersion(String version);

  String getLocale();

  void setLocale(String locale);

  String getDescription();

  void setDescription(String description);

  String getUserId();

  void setUserId(String userId);

  Date getTimeStamp();

  void setTimeStamp(Date timeStamp);

  String getBlobKey();

  void setBlobKey(String blobKey);

  Long getDomainId();

  void setDomainId(Long dId);

  String getType();

  void setType(String ty);

  String getJobId();

  void setJobId(String jid);

  int getJobStatus();

  void setJobStatus(int status);

}
