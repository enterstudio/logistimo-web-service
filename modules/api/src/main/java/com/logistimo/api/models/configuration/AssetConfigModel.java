package com.logistimo.api.models.configuration;

import com.logistimo.config.models.AssetConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by naveensnair on 14/11/14.
 */
public class AssetConfigModel {
  public int enable;
  public Map<Integer, Asset> assets;
  public String lastUpdated;
  public String createdBy;
  public String fn;
  public AssetConfig.Configuration config;
  public Map<Integer, WorkingStatus> wses;
  public String namespace;

  public AssetConfigModel() {
    assets = new HashMap<>(1);
    wses = new HashMap<>(1);
  }

  public static class Asset {
    public Integer at;
    public Integer id;
    public String an;
    public boolean iTs = true;
    public boolean iGe = true;
    public Integer dMp;
    public Map<String, Mancfacturer> mcs;
    public Map<Integer, MonitoringPosition> mps;

    public Asset() {
      mcs = new HashMap<>(1);
      mps = new HashMap<>(1);
    }
  }

  public static class Mancfacturer {
    public Boolean iC;
    public String id;
    public String name;
    public Map<String, Model> model = new HashMap<>(1);
    public String serialFormat;
    public String modelFormat;
    public String serialFormatDescription;
    public String modelFormatDescription;
  }

  public static class Model {
    public String name;
    public String type;
    public Boolean iC;
    public String dS;
    public Map<String, Sensor> sns = new HashMap<>(1);
    public Feature fts;
  }

  public static class Sensor {
    public String name;
    public Integer mpId;
    public String cd;
  }

  public static class MonitoringPosition {
    public Integer mpId;
    public String name;
    public String sId;
  }

  public static class WorkingStatus {
    public Integer status;
    public String dV;
  }

  public static class Feature {
    public Boolean pc;
    public Boolean ds;
    public Boolean ps;
    public Boolean dl;
    public Boolean ct;
  }
}
