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
