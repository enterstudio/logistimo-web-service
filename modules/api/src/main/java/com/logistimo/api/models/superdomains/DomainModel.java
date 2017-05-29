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

package com.logistimo.api.models.superdomains;

import com.logistimo.models.superdomains.DomainSuggestionModel;

import java.util.List;

/**
 * Created by naveensnair on 30/06/15.
 */
public class DomainModel {
  public String dn; //Current domain name
  public Long dId; //Current domain id
  public List<DomainSuggestionModel> ldl; //linked domain list
  public DomainPermissionModel dp; //domain permission list
  public String name; // domain name
  public String createdOn;
  public String description;
  public String lastUpdatedBy;
  public String lastUpdatedByn;
  public String lastUpdatedOn;
  public String ownerId;
  public String ownerName;
  public boolean isActive;
  public boolean hasChild;

}
