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

import com.logistimo.shipments.ShipmentStatus;

import com.logistimo.domains.ICrossDomain;
import com.logistimo.events.IEvents;

import java.util.Date;
import java.util.List;

/**
 * Created by Mohan Raja on 28/09/16
 */
public interface IShipment extends ICrossDomain {

  String getShipmentId();

  void setShipmentId(String id);

  Long getOrderId();

  void setOrderId(Long orderId);

  int getNumberOfItems();

  void setNumberOfItems(int noi);

  List<? extends IShipmentItem> getShipmentItems();

  void setShipmentItems(List<? extends IShipmentItem> items);

  String getTransporter();

  void setTransporter(String transporter);

  String getReason();

  void setReason(String reason);

  String getTrackingId();

  void setTrackingId(String trackingId);

  Long getKioskId();

  void setKioskId(Long kId);

  ShipmentStatus getStatus();

  void setStatus(ShipmentStatus status);

  Long getServicingKiosk();

  void setServicingKiosk(Long skId);

  Date getCreatedOn();

  void setCreatedOn(Date cOn);

  String getCreatedBy();

  void setCreatedBy(String cBy);

  Date getUpdatedOn();

  void setUpdatedOn(Date uOn);

  String getUpdatedBy();

  void setUpdatedBy(String userId);

  Double getLatitude();

  void setLatitude(Double latitude);

  Double getLongitude();

  void setLongitude(Double longitude);

  Double getGeoAccuracy();

  void setGeoAccuracy(Double geoAccuracyMeters);

  String getGeoErrorCode();

  void setGeoErrorCode(String errorCode);

  Date getExpectedArrivalDate();

  void setExpectedArrivalDate(Date date);

  Date getActualFulfilmentDate();

  void setActualFulfilmentDate(Date date);


  String getCancelledDiscrepancyReasons();

  void setCancelledDiscrepancyReasons(String cdrsn);

  String getPackageSize();

  void setPackageSize(String ps);

}
