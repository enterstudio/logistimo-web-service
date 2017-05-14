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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.logistimo.entity.IJobStatus;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by vani on 25/10/15.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class JobStatus implements IJobStatus {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long jId; // Job ID, system generated
  @Persistent
  private String desc; // Job description
  @Persistent
  private Long dId; // Domain ID
  @Persistent
  private String ty; // Job Type
  @Persistent
  private String sty; // Job Sub type - type of data being exported in case of export
  @Persistent
  private int st; // Job Status
  @Persistent
  private String rsn; // Reason - specified only for failure
  @Persistent
  private Date stt; // Job start time
  @Persistent
  private Date ut; //Job updated or completed time
  @Persistent
  private int nr; // Number of records completed (exported in case job is export)
  @Persistent
  private String on; // Job Output name. Name of the exported file in case of export.
  @Persistent
  private String ofl; // Output file location.
  @Persistent
  private String crBy; // User ID of the user who started the job.
  @Persistent
  @Column(length = 2048)
  private String meta; // Metadata related to the job. Filter parameters in case of export

  public JobStatus() {
  }

  @Override
  public Long getJobId() {
    return jId;
  }

  @Override
  public void setJobId(Long id) {
    this.jId = id;
  }

  @Override
  public String getDescription() {
    return desc;
  }

  @Override
  public void setDescription(String desc) {
    this.desc = desc;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long domainId) {
    this.dId = domainId;
  }

  @Override
  public String getType() {
    return this.ty;
  }

  @Override
  public void setType(String type) {
    this.ty = type;
  }

  @Override
  public String getSubType() {
    return sty;
  }

  @Override
  public void setSubType(String subType) {
    this.sty = subType;
  }

  @Override
  public int getStatus() {
    return st;
  }

  @Override
  public void setStatus(int status) {
    this.st = status;
  }

  @Override
  public String getReason() {
    return rsn;
  }

  @Override
  public void setReason(String reason) {
    this.rsn = reason;
  }

  @Override
  public Date getStartTime() {
    return stt;
  }

  @Override
  public void setStartTime(Date startTime) {
    this.stt = startTime;
  }

  @Override
  public Date getUpdatedTime() {
    return ut;
  }

  @Override
  public void setUpdatedTime(Date updatedTime) {
    this.ut = updatedTime;
  }

  @Override
  public int getNumberOfRecordsCompleted() {
    return nr;
  }

  @Override
  public void setNumberOfRecordsCompleted(int numberOfRecordsCompleted) {
    this.nr = numberOfRecordsCompleted;
  }

  @Override
  public String getOutputName() {
    return on;
  }

  @Override
  public void setOutputName(String outputName) {
    this.on = outputName;
  }

  @Override
  public String getOutputFileLocation() {
    return ofl;
  }

  @Override
  public void setOutputFileLocation(String outputFileLocation) {
    this.ofl = outputFileLocation;
  }

  @Override
  public String getCreatedBy() {
    return crBy;
  }

  @Override
  public void setCreatedBy(String createdBy) {
    this.crBy = createdBy;
  }

  @Override
  public String getMetadata() {
    return meta;
  }

  @Override
  public void setMetadata(String metadata) {
    this.meta = metadata;
  }

  @Override
  public Map<String, String> getMetadataMap() {
    Map<String, String> metadataMap = null;
    if (meta != null) {
      Type type = new TypeToken<Map<String, String>>() {
      }.getType();
      metadataMap = new Gson().fromJson(this.meta, type);
    }
    return metadataMap;
  }
}
