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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by charan on 30/11/15.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")

public class AssetAttribute implements IAssetAttribute {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private long id;

  /**
   * Attribute name
   */
  @Persistent
  private String att;

  /**
   * Attribute value
   */
  @Persistent
  private String val;

  @Persistent
  @Column(name = "ASSET_STATUS_ID")
  private AssetStatus assetStatusId;


  @Override
  public String getVal() {
    return val;
  }

  @Override
  public void setVal(String val) {
    this.val = val;
  }

  @Override
  public String getAtt() {
    return att;
  }

  @Override
  public void setAtt(String att) {
    this.att = att;
  }

  @Override
  public IAssetAttribute init(String key, String value) {
    this.att = key;
    this.val = value;
    return this;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public AssetStatus getAssetStatusId() {
    return assetStatusId;
  }

  public void setAssetStatusId(AssetStatus assetStatusId) {
    this.assetStatusId = assetStatusId;
  }
}
