/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

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
