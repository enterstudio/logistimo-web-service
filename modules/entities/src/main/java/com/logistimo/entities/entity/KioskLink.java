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

package com.logistimo.entities.entity;

import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.services.Services;
import com.logistimo.utils.NumberUtil;
import org.apache.commons.lang.StringUtils;

import javax.jdo.annotations.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class KioskLink implements IKioskLink {

  private static final XLog xLogger = XLog.getLog(KioskLink.class);

  @PrimaryKey
  @Persistent
  private String key;
  @Persistent
  private Long kioskId;
  @Persistent
  private String linkType;
  @Persistent
  private Long linkedKioskId;
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String description;
  @Persistent
  private Date createdOn;
  @Persistent
  private String cBy; // created by user ID
  //@Persistent
  //private Text prs; // permissions JSON text
  @Persistent
  private BigDecimal
      crl;
  // credit limit, typically set by vendor for a given customer (NOTE: this is persistent in Account, whenever the KioskLink is saved/updated)
  @NotPersistent
  private String lknm; // linked kiosk name (normalized - all lowercase) - for sorting purposes

  @Persistent
  private Integer ri = DEFAULT_ROUTE_INDEX; // Route Index
  @Persistent
  private String rtg; // Route tag
  @Persistent(table = "KIOSKLINK_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain Id

  // Transient storage for primary kiosk name, if present
  @NotPersistent
  private String kioskName;

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;

  public static String createId(Long kioskId, String type, Long linkedKioskId) {
    return kioskId + "." + type + "." + linkedKioskId;
  }

  @Override
  public String getId() {
    return key;
  }

  @Override
  public void setId(String id) {
    this.key = id;
  }

  @Override
  public Long getKioskId() {
    return kioskId;
  }

  @Override
  public void setKioskId(Long kioskId) {
    this.kioskId = kioskId;
  }

  @Override
  public String getLinkType() {
    return linkType;
  }

  @Override
  public void setLinkType(String linkType) {
    this.linkType = linkType;
  }

  @Override
  public Long getLinkedKioskId() {
    return linkedKioskId;
  }

  @Override
  public void setLinkedKioskId(Long linkedKioskId) {
    this.linkedKioskId = linkedKioskId;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
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
  public String getCreatedBy() {
    return cBy;
  }

  @Override
  public void setCreatedBy(String createdBy) {
    this.cBy = createdBy;
  }

  @Override
  public BigDecimal getCreditLimit() {
    if (crl != null) {
      return crl;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void setCreditLimit(BigDecimal crl) {
    this.crl = crl;
  }

  @Override
  public String getKioskName() {
    return kioskName;
  }

  @Override
  public void setKioskName(String kioskName) {
    this.kioskName = kioskName;
  }

  @Override
  public String getLinkedKioskName() {
    return lknm;
  }

  @Override
  public void setLinkedKioskName(String linkedKioskName) {
    if(StringUtils.isNotEmpty(linkedKioskName)) {
      lknm = linkedKioskName.toLowerCase();
    }
  }

  @Override
  public boolean equals(Object o1) {
    return key != null && key.equals(((IKioskLink) o1).getId());
  }

  @Override
  public int getRouteIndex() {
    return NumberUtil.getIntegerValue(ri);
  }

  @Override
  public void setRouteIndex(int ri) {
    this.ri = new Integer(ri);
  }

  @Override
  public String getRouteTag() {
    return rtg;
  }

  @Override
  public void setRouteTag(String tag) {
    rtg = tag;
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

  // Get the linked domain Id
  public Long getLinkedDomainId() {
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      return as.getKiosk(getLinkedKioskId(), false).getDomainId();
    } catch (Exception e) {
      xLogger.warn("{0} when trying to get linked kiosk {1} for kiosk link {2}-{3}: {4}",
          e.getClass().getName(), getKioskId(), getLinkedKioskId(), e.getMessage());
    }
    return null;
  }

  // Get the kiosk domain Id
  public Long getKioskDomainId() {
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      return as.getKiosk(getKioskId(), false).getDomainId();
    } catch (Exception e) {
      xLogger.warn("{0} when trying to get linked kiosk {1} for kiosk link {2}-{3}: {4}",
          e.getClass().getName(), getKioskId(), getLinkedKioskId(), e.getMessage());
    }
    return null;
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
