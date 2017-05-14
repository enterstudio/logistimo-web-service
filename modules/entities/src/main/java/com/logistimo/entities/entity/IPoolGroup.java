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

import java.util.Date;
import java.util.List;

/**
 * Created by charan on 20/05/15.
 */
public interface IPoolGroup {

  Long getGroupId();

  void setGroupId(Long groupId);

  Long getDomainId();

  void setDomainId(Long dId);

  String getName();

  void setName(String name);

  String getDescription();

  void setDescription(String description);

  String getOwnerId();

  void setOwnerId(String ownerId);

  Date getTimeStamp();

  void setTimeStamp(Date timeStamp);

  List<? extends IKiosk> getKiosks();

  void setKiosks(List<? extends IKiosk> kiosks);

  String getCity();

  void setCity(String city);

  String getStreet();

  void setStreet(String street);

  String getTaluk();

  void setTaluk(String taluk);

  String getDistrict();

  void setDistrict(String district);

  String getState();

  void setState(String state);

  String getCountry();

  void setCountry(String country);

  String getPinCode();

  void setPinCode(String pinCode);

  // Returns location in format: city, taluk, district, state
  String getLocation();

  Date getUpdatedOn();

  void setUpdatedOn(Date date);

  String getUpdatedBy();

  void setUpdatedBy(String username);

  String getCreatedBy();

  void setCreatedBy(String username);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);
}
