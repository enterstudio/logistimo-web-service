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

package com.logistimo.inventory.entity;

import com.logistimo.tags.entity.ITag;

import com.logistimo.domains.IOverlappedDomain;

import java.util.Date;
import java.util.List;

/**
 * Created by charan on 20/05/15.
 */
public interface IInvntryEvntLog extends IOverlappedDomain {

  int SOURCE_MINMAXUPDATE = 0;
  int SOURCE_STOCKUPDATE = 1;

  Long getKey();

  Long getInvId();

  void setInvId(Long invId);

  Long getDomainId();

  void setDomainId(Long dId);

  Long getKioskId();

  void setKioskId(Long kioskId);

  Long getMaterialId();

  void setMaterialId(Long materialId);

  int getType();

  void setType(int type);

  Date getStartDate();

  void setStartDate(Date start);

  boolean isOpen();

  Date getEndDate();

  void setEndDate(Date end);

  long computeDuration();

  long getDuration() // duration in milliseconds
  ;

  List<String> getTags(String tagType);

  void setTags(List<String> tags, String tagType);

  void setTgs(List<? extends ITag> tags, String tagType);

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);
}
