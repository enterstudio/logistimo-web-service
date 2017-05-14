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

import java.util.Date;
import java.util.List;

/**
 * Created by charan on 27/11/15.
 */
public interface IAssetStatus {
  Integer TYPE_DEVICE = 0;
  Integer TYPE_TEMPERATURE = 1;
  Integer TYPE_BATTERY = 2;
  Integer TYPE_ACTIVITY = 3;
  Integer TYPE_XSENSOR = 4;
  Integer TYPE_DEVCONN = 5;
  Integer TYPE_POWER = 6;
  Integer TYPE_STATE = 7;

  /**
   * @return asset status id
   */
  Long getId();

  /**
   * Set Id for this record.
   */
  void setId(Long id);

  /**
   * Get Asset Id
   *
   * @return assetId
   */
  Long getAssetId();

  /**
   * Set asset Id.
   */
  void setAssetId(Long assetId);

  /**
   * @return monitoring point id
   */
  Integer getMpId();

  /**
   * Set monitoring point id for this status update.
   */
  void setMpId(Integer mpId);

  /**
   * Get status of this event type.
   *
   * @return status
   */
  Integer getStatus();

  /**
   * Set status code for this status update.
   */
  void setStatus(Integer status);

  /**
   * Get associated temperature at the time of this update.
   *
   * @return temperature
   */
  Float getTmp();

  /**
   * Set temperature
   */
  void setTmp(Float temperature);

  /**
   * Get event timestamp.
   *
   * @return timestamp
   */
  Date getTs();

  /**
   * Set event timestamp
   */
  void setTs(Date ts);

  /**
   * Init asset status.
   */
  void init(Long assetId, int mpId, String sId, int type, int st, int abnSt, double tmp, int time,
            List<IAssetAttribute> attrs, List<String> tags);

  /**
   * Adds new attribute to this record.
   */
  void addAttribute(IAssetAttribute assetAttribute);

  /**
   * @return asset attributes
   */
  List<? extends IAssetAttribute> getAttributes();

  /**
   * Set asset attributes like min/max
   */
  void setAttributes(List<? extends IAssetAttribute> attributes);

  /**
   * Event type
   *
   * @return type
   */
  Integer getType();

  /**
   * Set event type
   */
  void setType(Integer type);

  /**
   * @return sensor Id
   */
  String getsId();

  /**
   * set sensor id.
   */
  void setsId(String sId);

  Integer getAbnStatus();

  void setAbnStatus(Integer abnStatus);

  List<String> getTags();

  void setTags(List<String> tags);
}
