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

package com.logistimo.shipments.entity;

import com.logistimo.config.models.EventsConfig;
import com.logistimo.conversations.entity.IMessage;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.conversations.service.impl.ConversationServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.OrderUtils;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.service.impl.ShipmentService;
import com.logistimo.users.entity.UserAccount;

import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.*;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by Mohan Raja on 28/09/16
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Shipment implements IShipment {

  static XLog xLogger = XLog.getLog(Shipment.class);

  @PrimaryKey
  @Persistent
  private String id;

  @Persistent
  private Long orderId;

  @Persistent
  private Integer noi;

  @Persistent(table = "SHIPMENT_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;

  @Persistent
  private Long sdId;

  @Persistent
  private Long kId;

  @Persistent
  private Long skId;

  @Persistent
  private String status = ShipmentStatus.OPEN.toString();

  @Persistent
  private Double lt;

  @Persistent
  private Double ln;

  @Persistent
  private Double geoacc;

  @Persistent
  private String geoerr;

  /**
   * Expected time of arrival
   */
  @Persistent
  private Date ead;

  /**
   * Actual date of fulfilment
   */
  @Persistent
  private Date afd;

  @Persistent
  private String transporter;

  @Persistent
  private String trackingId;

  @Persistent
  private String rsn;

  @Persistent
  private Date cOn;

  @Persistent
  private String cBy;

  @Persistent
  private Date uOn;

  @Persistent
  private String uBy;

  @Persistent
  private String cdrsn;

  @Persistent
  private String ps;

  @NotPersistent
  private List<ShipmentItem> items;

  @Override
  public String getShipmentId() {
    return id;
  }

  @Override
  public void setShipmentId(String id) {
    this.id = id;
  }

  @Override
  public Long getOrderId() {
    return orderId;
  }

  @Override
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  @Override
  public int getNumberOfItems() {
    return noi;
  }

  @Override
  public void setNumberOfItems(int noi) {
    this.noi = noi;
  }

  @Override
  public Long getDomainId() {
    return sdId;
  }

  @Override
  public void setDomainId(Long dId) {
    this.sdId = dId;
  }

  @Override
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kId) {
    this.kId = kId;
  }

  @Override
  public ShipmentStatus getStatus() {
    return ShipmentStatus.getStatus(status);
  }

  @Override
  public void setStatus(ShipmentStatus status) {
    this.status = status.toString();
  }

  @Override
  public Long getServicingKiosk() {
    return skId;
  }

  @Override
  public void setServicingKiosk(Long skId) {
    this.skId = skId;
  }

  @Override
  public Double getLatitude() {
    return lt;
  }

  @Override
  public void setLatitude(Double latitude) {
    this.lt = latitude;
  }

  @Override
  public Double getLongitude() {
    return ln;
  }

  @Override
  public void setLongitude(Double longitude) {
    this.ln = longitude;
  }

  @Override
  public Double getGeoAccuracy() {
    return geoacc;
  }

  @Override
  public void setGeoAccuracy(Double geoAccuracyMeters) {
    this.geoacc = geoAccuracyMeters;
  }

  @Override
  public String getGeoErrorCode() {
    return geoerr;
  }

  @Override
  public void setGeoErrorCode(String errorCode) {
    this.geoerr = errorCode;
  }

  @Override
  public Date getExpectedArrivalDate() {
    return ead;
  }

  @Override
  public void setExpectedArrivalDate(Date date) {
    this.ead = date;
  }

  @Override
  public Date getActualFulfilmentDate() {
    return afd;
  }

  @Override
  public void setActualFulfilmentDate(Date dof) {
    this.afd = dof;
  }

  @Override
  public List<? extends IShipmentItem> getShipmentItems() {
    return items;
  }

  @Override
  public void setShipmentItems(List<? extends IShipmentItem> items) {
    this.items = (List<ShipmentItem>) items;
  }

  @Override
  public String getTransporter() {
    return transporter;
  }

  @Override
  public void setTransporter(String transporter) {
    this.transporter = transporter;
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
  public String getTrackingId() {
    return trackingId;
  }

  @Override
  public void setTrackingId(String trackingId) {
    this.trackingId = trackingId;
  }

  @Override
  public Date getCreatedOn() {
    return cOn;
  }

  @Override
  public void setCreatedOn(Date cOn) {
    this.cOn = cOn;
  }

  @Override
  public String getCreatedBy() {
    return cBy;
  }

  @Override
  public void setCreatedBy(String cBy) {
    this.cBy = cBy;
  }

  @Override
  public Date getUpdatedOn() {
    return uOn;
  }

  @Override
  public void setUpdatedOn(Date uOn) {
    this.uOn = uOn;
  }

  @Override
  public String getUpdatedBy() {
    return uBy;
  }

  @Override
  public void setUpdatedBy(String uBy) {
    this.uBy = uBy;
  }

  @Override
  public List<Long> getDomainIds() {
    return dId;
  }

  @Override
  public void setDomainIds(List<Long> domainIds) {
    this.dId.clear();
    this.dId.addAll(domainIds);
  }

  @Override
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

  @Override
  public void removeDomainId(Long domainId) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.remove(domainId);
    }
  }

  @Override
  public void removeDomainIds(List<Long> domainIds) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.removeAll(domainIds);
    }
  }

  @Override
  public Long getLinkedDomainId() {
    try {
      if (skId != null) {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        return as.getKiosk(skId, false).getDomainId();
      }
    } catch (Exception e) {
      xLogger
          .warn("Error when trying to get domain id of linked kiosk {0}", getServicingKiosk(), e);
    }
    return null;
  }

  @Override
  public Long getKioskDomainId() {
    try {
      if (kId != null) {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        return as.getKiosk(kId, false).getDomainId();
      }
    } catch (Exception e) {
      xLogger.warn("Error when trying to get domain id of kiosk {0}", getKioskId(), e);
    }
    return null;
  }

  @Override
  public String getCancelledDiscrepancyReasons() {
    return cdrsn;
  }

  public void setCancelledDiscrepancyReasons(String cdrsn) {
    this.cdrsn = cdrsn;
  }

  @Override
  public String getPackageSize() {
    return ps;
  }

  public void setPackageSize(String ps) {
    this.ps = ps;
  }
}