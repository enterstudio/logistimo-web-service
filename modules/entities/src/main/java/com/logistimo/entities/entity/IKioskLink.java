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

import com.logistimo.domains.ICrossDomain;
import com.logistimo.entities.entity.IUserToKiosk;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface IKioskLink extends ICrossDomain {
  int DEFAULT_ROUTE_INDEX = IUserToKiosk.DEFAULT_ROUTE_INDEX;
  // Type of links
  String TYPE_VENDOR = "v";
  String TYPE_CUSTOMER = "c";

  String getId();

  void setId(String id);

  Long getKioskId();

  void setKioskId(Long kioskId);

  String getLinkType();

  void setLinkType(String linkType);

  Long getLinkedKioskId();

  void setLinkedKioskId(Long linkedKioskId);

  String getDescription();

  void setDescription(String description);

  Date getCreatedOn();

  void setCreatedOn(Date createdOn);

  String getCreatedBy();

  void setCreatedBy(String createdBy);

  BigDecimal getCreditLimit();

  void setCreditLimit(BigDecimal crl);

  String getKioskName();

  void setKioskName(String kioskName);

  String getLinkedKioskName();

  void setLinkedKioskName(String linkedKioskName);

  int getRouteIndex();

  void setRouteIndex(int ri);

  String getRouteTag();

  void setRouteTag(String tag);

  Long getDomainId();

  void setDomainId(Long domainId);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);
}
