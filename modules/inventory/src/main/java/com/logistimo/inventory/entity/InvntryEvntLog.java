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

/**
 *
 */
package com.logistimo.inventory.entity;

import com.logistimo.tags.TagUtil;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;

import com.logistimo.utils.NumberUtil;

import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * Logs the duration of bad stock levels - esp. stock outs, unsafe stock or excess stock
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true", cacheable = "false")
public class InvntryEvntLog implements IInvntryEvntLog {

  private static final XLog xLogger = XLog.getLog(InvntryEvntLog.class);

  private static ITagDao tagDao = new TagDao();
  @Persistent
  Integer
      ty;
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key;
  // type - stock-out, unsafe stock, excess stock, etc. sames Event.STOCKOUT, Event.UNDERSTOCK, Event.OVERSTOCK, etc.
  @Persistent(table = "INVNTRYEVENTLOG_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain Id
  @Persistent
  private Long kId;
  @Persistent
  private Long mId;
  @Persistent
  private Long invId;
  @Persistent
  private Date sd;
  @Persistent
  private Date ed;
  @Persistent
  private Long dr = 0L; // duration in milliseconds
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Integer ast; // aggregation status

  @Persistent(table = "INVELOGS_MTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> mtgs;

  @NotPersistent
  private List<String>
      oldmtgs;
  // list of material mtgs, if present (used mainly for queries involving kioskId and mtgs)

  @Persistent(table = "INVELOGS_KTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> ktgs;

  @NotPersistent
  private List<String> oldktgs; // list of kiosk mtgs (for queries)

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;

  public InvntryEvntLog(int type, Invntry inv) {
    dId = inv.getDomainIds();
    sdId = inv.getDomainId();
    Long kioskId = inv.getKioskId();
    Long materialId = inv.getMaterialId();
    Date start = inv.getTimestamp();
    invId = inv.getKey();
    ty = type;
    kId = kioskId;
    mId = materialId;
    sd = start;
    ed = null;
    List<String> tags = inv.getTags(TagUtil.TYPE_MATERIAL);
    if (tags != null && !tags.isEmpty()) {
      setTgs(tagDao.getTagsByNames(tags, ITag.MATERIAL_TAG), TagUtil.TYPE_MATERIAL);
    }
    tags = inv.getTags(TagUtil.TYPE_ENTITY);
    if (tags != null && !tags.isEmpty()) {
      setTgs(tagDao.getTagsByNames(tags, ITag.KIOSK_TAG), TagUtil.TYPE_ENTITY);
    }
    computeDuration();
  }

  public InvntryEvntLog() {

  }

  @Override
  public Long getKey() {
    return key;
  }

  public void setKey(Long key) {
    this.key = key;
  }

  @Override
  public Long getInvId() {
    return invId;
  }

  @Override
  public void setInvId(Long invId) {
    this.invId = invId;
  }


  public Long getDomainId() {
    return sdId;
  }

  public void setDomainId(Long domainId) {
    sdId = domainId;
  }

  public List<Long> getDomainIds() {
    return dId;
  }

  public void setDomainIds(List<Long> domainIds) {
    this.dId.clear();
    this.dId.addAll(domainIds);
  }

  public void addDomainIds(List<Long> domainIds) {
    if (domainIds == null || domainIds.isEmpty()) {
      return;
    }
    if (this.dId == null) {
      this.dId = new ArrayList<>();
    }
    for (Long dId : domainIds) {
      if (!this.dId.contains(dId)) {
        this.dId.add(dId);
      }
    }
  }

  public void removeDomainId(Long domainId) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.remove(domainId);
    }
  }

  public void removeDomainIds(List<Long> domainIds) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.removeAll(domainIds);
    }
  }

  @Override
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kioskId) {
    this.kId = kioskId;
  }

  @Override
  public Long getMaterialId() {
    return mId;
  }

  @Override
  public void setMaterialId(Long materialId) {
    this.mId = materialId;
  }

  @Override
  public int getType() {
    return NumberUtil.getIntegerValue(ty);
  }

  @Override
  public void setType(int type) {
    ty = new Integer(type);
  }

  @Override
  public Date getStartDate() {
    return sd;
  }

  @Override
  public void setStartDate(Date start) {
    this.sd = start;
  }

  @Override
  public boolean isOpen() {
    return ed == null;
  }

  @Override
  public Date getEndDate() {
    return ed;
  }

  @Override
  public void setEndDate(Date end) {
    this.ed = end;
    computeDuration();
  }

  @Override
  public long computeDuration() {
    if (sd == null || ed == null) {
      return 0L;
    }
    dr = (ed.getTime() - sd.getTime());
    return dr;
  }

  @Override
  public long getDuration() {
    return dr;
  } // duration in milliseconds

  public boolean isAggregated(int aggregationCode) {
    return (ast != null && ast == aggregationCode);
  }

  public void setAggregated(int aggregationCode) {
    ast = new Integer(aggregationCode);
  }

  @Override
  public List<String> getTags(String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      if (oldmtgs == null) {
        oldmtgs = TagUtil.getList(mtgs);
      }
      return oldmtgs;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      if (oldktgs == null) {
        oldktgs = TagUtil.getList(ktgs);
      }
      return oldktgs;
    } else {
      return null;
    }
  }

  @Override
  public void setTags(List<String> tags, String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      oldmtgs = tags;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      oldktgs = tags;
    }
  }

  @Override
  public void setTgs(List<? extends ITag> tags, String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      if (this.mtgs != null) {
        this.mtgs.clear();
        if (tags != null) {
          this.mtgs.addAll((List<Tag>) tags);
        }
      } else {
        this.mtgs = (List<Tag>) tags;
      }
      this.oldmtgs = null;

    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      if (this.ktgs != null) {
        this.ktgs.clear();
        if (tags != null) {
          this.ktgs.addAll((List<Tag>) tags);
        }
      } else {
        this.ktgs = (List<Tag>) tags;
      }
      this.oldktgs = null;
    }
  }



  public Date getArchivedAt() {
    return arcAt;
  }

  public void setArchivedAt(Date archivedAt) {
    arcAt = archivedAt;
  }

  public String getArchivedBy() {
    return arcBy;
  }

  public void setArchivedBy(String archivedBy) {
    arcBy = archivedBy;
  }
}
