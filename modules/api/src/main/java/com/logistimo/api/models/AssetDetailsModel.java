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

package com.logistimo.api.models;

import com.google.gson.JsonElement;

import com.logistimo.assets.models.AssetModels;
import com.logistimo.assets.models.AssetRelationModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by kaniyarasu on 13/11/15.
 */
public class AssetDetailsModel {
  public int sno;
  public Long id;
  public String dId;
  public String vId;
  public EntityModel entity;
  public Long sdid;
  public String sdname;
  public Date con;
  public List<UserModel> ons;
  public List<UserModel> mts;
  public List<String> tags;
  public String lId;
  public JsonElement meta;
  public String cb;
  public String ub;
  public List<AssetModels.TemperatureSensorRequest> sns;
  public List<AssetModels.TempDeviceAlarmModel> alrm;
  public List<AssetModels.AssetStatus> tmp;
  public AssetModels.AssetStatus cfg;
  public AssetModels.AssetStatus ws;
  public Integer typ;
  public String mdl;
  public AssetModels.DeviceReadyModel drdy;
  public String rus;
  public String lub;
  public String ts;
  public String lts;
  public Map<Integer, AssetRelationModel.Relation> rel;
  public Boolean iDa = false;
  public Long iCon;
  public String lubn; // Last updated by name
  public String rusn; // Registered by name
}
