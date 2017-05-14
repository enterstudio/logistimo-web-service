package com.logistimo.entity;

import java.util.Date;
import java.util.Map;

/**
 * Created by vani on 25/10/15.
 */
public interface IJobStatus {
  int INPROGRESS = 0;
  int COMPLETED = 1;
  int FAILED = 2;
  int INQUEUE = 3;

  String TYPE_EXPORT = "export";
  String TYPE_CUSTOMREPORT = "customreport";

  Long getJobId();

  void setJobId(Long jobId);


  String getDescription();

  void setDescription(String desc);

  Long getDomainId();

  void setDomainId(Long domainId);

  String getType();

  void setType(String jobType);

  String getSubType();

  void setSubType(String subType);

  int getStatus();

  void setStatus(int status);

  String getReason();

  void setReason(String reason);

  Date getStartTime();

  void setStartTime(Date startTime);

  Date getUpdatedTime();

  void setUpdatedTime(Date updatedTime);

  int getNumberOfRecordsCompleted();

  void setNumberOfRecordsCompleted(int numberOfRecordsCompleted);

  String getOutputName();

  void setOutputName(String outputName);

  String getOutputFileLocation();

  void setOutputFileLocation(String outputFileLocation);

  String getCreatedBy();

  void setCreatedBy(String createdBy);

  String getMetadata();

  void setMetadata(String metadata);

  Map<String, String> getMetadataMap();

}
