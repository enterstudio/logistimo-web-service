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

import com.logistimo.domains.IOverlappedDomain;

import java.util.Date;
import java.util.List;

/**
 * Created by kaniyarasu on 02/11/15.
 */
public interface IAsset extends IOverlappedDomain {

  int STATUS_NORMAL = 0;
  int STATUS_EXCURSION = 1; // out of range
  int STATUS_WARNING = 2; // out of range
  int STATUS_ALARM = 3; // out of range
  int ABNORMAL_TYPE_LOW = 1;
  int ABNORMAL_TYPE_HIGH = 2;
  int STATUS_INACTIVE = 1;
  int STATUS_ACTIVE = 0;

  int TEMP_DEVICE = 1;
  int TEMP_SENSOR = 4;
  int FRIDGE = 2;
  int DEEP_FREEZER = 2;

  int MONITORING_ASSET = 1;
  int MONITORED_ASSET = 2;
  int NON_MONITORED_ASSET = 3;

  int STATUS_BATTERY_LOW = 1;
  int STATUS_BATTERY_ALARM = 2;
  int STATUS_SENSOR_DISCONNECTED = 1;
  int STATUS_ASSET_INACTIVE = 1;
  int STATUS_DEVICE_DISCONNECTED = 1;
  int STATUS_POWER_OUTAGE = 1;

  String getModel();

  void setModel(String model);

  /**
   * @return normalised serial id
   */
  String getNsId();

  /**
   * Set Normalized serial id of the asset
   */
  void setNsId(String nsId);

  /**
   * @return Monitoring point of the parent asset
   */
  String getLocationId();

  /**
   * Set the monitoring point of the parent asset
   */
  void setLocationId(String lId);

  Long getId();

  void setId(Long id);

  /**
   * Serial number of the asset
   */
  String getSerialId();

  void setSerialId(String sId);

  String getVendorId();

  void setVendorId(String vId);

  Long getKioskId();

  void setKioskId(Long kId);

  List<String> getOwners();

  void setOwners(List<String> ons);

  List<String> getMaintainers();

  void setMaintainers(List<String> mns);

  Date getCreatedOn();

  void setCreatedOn(Date cOn);

  String getUpdatedBy();

  void setUpdatedBy(String ub);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);

  String getCreatedBy();

  void setCreatedBy(String cb);

  Date getUpdatedOn();

  void setUpdatedOn(Date uOn);

  Integer getType();

  void setType(Integer type);

  Integer getYom();

  void setYom(Integer yom);
}
