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

package com.logistimo.materials.entity;

import com.logistimo.domains.ISubDomain;
import com.logistimo.domains.ISuperDomain;
import com.logistimo.events.IEvents;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * @author Mohan Raja
 */
public interface IHandlingUnit extends ISubDomain, ISuperDomain {

  String NAME = "name";
  String QUANTITY = "quantity";
  String HUID = "huid";

  Long getId();

  void setId(Long id);

  Long getDomainId();

  void setDomainId(Long sdId);

  String getName();

  void setName(String name);

  String getNormalisedName();

  Set<? extends IHandlingUnitContent> getContents();

  void setContents(Set<? extends IHandlingUnitContent> items);

  String getDescription();

  void setDescription(String description);

  BigDecimal getQuantity();

  void setQuantity(BigDecimal quantity);

  BigDecimal getVolume();

  void setVolume(BigDecimal volume);

  BigDecimal getWeight();

  void setWeight(BigDecimal weight);

  Date getTimeStamp();

  void setTimeStamp(Date timeStamp);

  Date getLastUpdated();

  void setLastUpdated(Date lastUpdated);

  String getCreatedBy();

  void setCreatedBy(String cb);

  String getUpdatedBy();

  void setUpdatedBy(String ub);
}
