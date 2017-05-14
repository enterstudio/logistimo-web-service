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

package com.logistimo.assets.entity;

import com.logistimo.dao.JDOUtils;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by charan on 27/11/15.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class AssetStatus implements IAssetStatus {

  /**
   * Hash based on Asset Id, Status Type, Monitoing point Id, Sensor Id.
   */
  @PrimaryKey
  @Persistent
  private Long id;

  /**
   * Asset Id
   */
  @Persistent
  private Long assetId;

  /**
   * Monitoring point Id
   */
  @Persistent
  private Integer mpId;

  /**
   * Sensor Id . Sent for device alarms, Sensor device.
   */
  @Persistent
  private String sId;

  /**
   * Alarm type. The following alarm types.
   * - Device status
   * - Temperature status
   * - Battery status
   * - Activity status
   * - External Sensor status
   * - Connection status , Transmitter not connected.
   */
  @Persistent
  private Integer type;

  @Persistent
  private Integer status;

  @Persistent
  private Integer abnStatus;

  @Persistent
  private Float tmp;

  @Persistent
  private Date ts;

  @Persistent(defaultFetchGroup = "true", mappedBy = "assetStatusId", dependentElement = "true")
  private List<AssetAttribute> attributes;

  @NotPersistent
  private List<String> tags;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public Long getAssetId() {
    return assetId;
  }

  @Override
  public void setAssetId(Long assetId) {
    this.assetId = assetId;
  }

  @Override
  public Integer getMpId() {
    return mpId;
  }

  @Override
  public void setMpId(Integer mpId) {
    this.mpId = mpId;
  }

  @Override
  public Integer getStatus() {
    return status;
  }

  @Override
  public void setStatus(Integer status) {
    this.status = status;
  }

  @Override
  public Integer getAbnStatus() {
    return abnStatus;
  }

  @Override
  public void setAbnStatus(Integer abnStatus) {
    this.abnStatus = abnStatus;
  }

  @Override
  public List<String> getTags() {
    return this.tags;
  }

  @Override
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  @Override
  public Float getTmp() {
    return tmp;
  }

  @Override
  public void setTmp(Float tmp) {
    this.tmp = tmp;
  }

  @Override
  public Date getTs() {
    return ts;
  }

  @Override
  public void setTs(Date ts) {
    this.ts = ts;
  }

  @Override
  public void init(Long assetId, int mpId, String sId, int type, int st, int aSt, double tmp,
                   int time, List<IAssetAttribute> attrs, List<String> tags) {
    this.assetId = assetId;
    this.mpId = mpId;
    this.sId = sId;
    this.type = type;
    this.status = st;
    this.abnStatus = aSt;
    this.tmp = new Float(tmp);
    this.ts = new Date();
    this.ts.setTime(time * 1000l);
    if (this.attributes == null) {
      this.attributes = new ArrayList<>(1);
    }
    if (attrs != null) {
      for (IAssetAttribute iAssetAttribute : attrs) {
        this.attributes.add((AssetAttribute) iAssetAttribute);
      }
    }
    this.tags = tags;
    this.id = JDOUtils.createAssetStatusKey(assetId, mpId, sId, type);
  }

  @Override
  public List<AssetAttribute> getAttributes() {
    return attributes;
  }

  @Override
  public void setAttributes(List<? extends IAssetAttribute> attributes) {
    if (this.attributes == null) {
      this.attributes = new ArrayList<>(1);
    } else {
      this.attributes.clear();
    }
    for (IAssetAttribute iAssetAttribute : attributes) {
      this.attributes.add((AssetAttribute) iAssetAttribute);
    }

  }

  @Override
  public void addAttribute(IAssetAttribute assetAttribute) {
    if (attributes == null) {
      attributes = new ArrayList<>(1);
    }
    attributes.add((AssetAttribute) assetAttribute);
  }

  @Override
  public Integer getType() {
    return type;
  }

  @Override
  public void setType(Integer type) {
    this.type = type;
  }

  @Override
  public String getsId() {
    return sId;
  }

  @Override
  public void setsId(String sId) {
    this.sId = sId;
  }

  @Override
  public String toString() {
    return "AssetStatus{" +
        "assetId=" + assetId +
        ", mpId=" + mpId +
        ", sId='" + sId + '\'' +
        ", type=" + type +
        ", status=" + status +
        ", abnStatus=" + abnStatus +
        ", tmp=" + tmp +
        ", ts=" + ts +
        '}';
  }
}
