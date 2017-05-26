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

import com.logistimo.domains.IMultiDomain;

import java.util.Date;
import java.util.List;

/**
 * Created by charan on 20/05/15.
 */
public interface IBBoard extends IMultiDomain {
  String TYPE_POST = "pst"; // manual text message post

  Long getKey();

  Long getDomainId();

  void setDomainId(Long domainId);

  String getType();

  void setType(String type);

  Long getEventKey();

  void setEventKey(Long eky);

  Integer getEventId();

  void setEventId(Integer eventId);

  String getMessage();

  void setMessage(String message);

  String getMediaUrl();

  void setMediaUrl(String mediaUrl);

  Long getKioskId();

  void setKioskId(Long kioskId);

  String getCity();

  void setCity(String city);

  String getDistrict();

  void setDistrict(String district);

  String getState();

  void setState(String state);

  List<String> getTags();

  void setTags(List<String> tags);

  String getUserId();

  void setUserId(String userId);

  Date getTimestamp();

  void setTimestamp(Date timestamp);
}
