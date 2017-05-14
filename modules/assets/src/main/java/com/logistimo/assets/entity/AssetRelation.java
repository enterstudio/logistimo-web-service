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

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by kaniyarasu on 08/12/15.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class AssetRelation implements IAssetRelation {
  /**
   * Primary key
   */
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  /**
   * Asset Id
   */
  private Long assetId;

  /**
   * Relation asset Id
   */
  private Long relatedAssetId;

  /**
   * Asset relationship type
   */
  private Integer type;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public Long getAssetId() {
    return assetId;
  }

  @Override
  public void setAssetId(Long assetId) {
    this.assetId = assetId;
  }

  @Override
  public Long getRelatedAssetId() {
    return relatedAssetId;
  }

  @Override
  public void setRelatedAssetId(Long relatedAssetId) {
    this.relatedAssetId = relatedAssetId;
  }

  @Override
  public Integer getType() {
    return type;
  }

  @Override
  public void setType(Integer type) {
    this.type = type;
  }

  @Override
  public Long getDomainId() {
    return null;
  }
}
