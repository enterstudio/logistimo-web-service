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

package com.logistimo.dashboards.entity;

import java.util.Date;

/**
 * @author Mohan Raja
 */
public interface IWidget {

  String MONTHLY = "m";
  String DAILY = "d";

  Long getwId();

  void setwId(Long wId);

  Long getdId();

  void setdId(Long dId);

  String getName();

  void setName(String name);

  String getDesc();

  void setDesc(String desc);

  String getCreatedBy();

  void setCreatedBy(String createdBy);

  Date getCreatedOn();

  void setCreatedOn(Date createdOn);

  String getTitle();

  void setTitle(String title);

  String getSubtitle();

  void setSubtitle(String subtitle);

  String getType();

  void setType(String type);

  String getFreq();

  void setFreq(String freq);

  int getNop();

  void setNop(int nop);

  String getAggrTy();

  void setAggrTy(String aggrTy);

  String getAggr();

  void setAggr(String aggr);

  String getyLabel();

  void setyLabel(String yLabel);

  Boolean getExpEnabled();

  void setExpEnabled(Boolean expEnabled);

  Boolean getShowLeg();

  void setShowLeg(Boolean showLeg);
}
