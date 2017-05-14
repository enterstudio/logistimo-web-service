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

package com.logistimo.dashboards.entity;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author Mohan Raja
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Widget implements IWidget {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long wId; // Widget ID
  @Persistent
  private Long dId;
  @Persistent
  private String name;
  @Persistent
  private String desc;
  @Persistent
  private String createdBy; // userId of owner of domain
  @Persistent
  private Date createdOn;
  @Persistent
  private String title;
  @Persistent
  private String subtitle;
  @Persistent
  private String type;
  @Persistent
  private String freq;
  @Persistent
  private int nop;
  @Persistent
  private String aggrTy;
  @Persistent
  private String aggr;
  @Persistent
  private String yLabel;
  @Persistent
  private Boolean expEnabled;
  @Persistent
  private Boolean showLeg;

  @Override
  public Long getwId() {
    return wId;
  }

  @Override
  public void setwId(Long wId) {
    this.wId = wId;
  }

  @Override
  public Long getdId() {
    return dId;
  }

  @Override
  public void setdId(Long dId) {
    this.dId = dId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDesc() {
    return desc;
  }

  @Override
  public void setDesc(String desc) {
    this.desc = desc;
  }

  @Override
  public String getCreatedBy() {
    return createdBy;
  }

  @Override
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public Date getCreatedOn() {
    return createdOn;
  }

  @Override
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getSubtitle() {
    return subtitle;
  }

  @Override
  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getFreq() {
    return freq;
  }

  @Override
  public void setFreq(String freq) {
    this.freq = freq;
  }

  @Override
  public int getNop() {
    return nop;
  }

  @Override
  public void setNop(int nop) {
    this.nop = nop;
  }

  @Override
  public String getAggrTy() {
    return aggrTy;
  }

  public void setAggrTy(String aggrTy) {
    this.aggrTy = aggrTy;
  }

  @Override
  public String getAggr() {
    return aggr;
  }

  public void setAggr(String aggr) {
    this.aggr = aggr;
  }

  @Override
  public String getyLabel() {
    return yLabel;
  }

  @Override
  public void setyLabel(String yLabel) {
    this.yLabel = yLabel;
  }

  @Override
  public Boolean getExpEnabled() {
    return expEnabled;
  }

  @Override
  public void setExpEnabled(Boolean expEnabled) {
    this.expEnabled = expEnabled;
  }

  @Override
  public Boolean getShowLeg() {
    return showLeg;
  }

  @Override
  public void setShowLeg(Boolean showLeg) {
    this.showLeg = showLeg;
  }
}
