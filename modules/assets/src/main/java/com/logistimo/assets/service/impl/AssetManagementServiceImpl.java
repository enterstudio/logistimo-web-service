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

package com.logistimo.assets.service.impl;

import com.google.common.base.Preconditions;

import com.logistimo.assets.AssetUtil;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.assets.models.AssetModel;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.events.EventConstants;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.utils.QueryUtil;
import com.sun.rowset.CachedRowSetImpl;

import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.sql.rowset.CachedRowSet;

/**
 * Created by kaniyarasu on 02/11/15.
 */
public class AssetManagementServiceImpl extends ServiceImpl implements AssetManagementService {
  private static final XLog xLogger = XLog.getLog(AssetManagementServiceImpl.class);

  @Override
  public void init(Services services) throws ServiceException {

  }

  @Override
  public void destroy() throws ServiceException {

  }

  @Override
  public Class<? extends Service> getInterface() {
    return AssetManagementService.class;
  }

  @Override
  public void createAsset(Long domainId, IAsset asset, final AssetModel assetModel)
      throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = null;
    try {
      tx = pm.currentTransaction();
      tx.begin();
      createAsset(domainId, asset, pm);
      AssetUtil.registerOrUpdateDevice(assetModel, domainId);
      tx.commit();
      asset = pm.detachCopy(asset);
      try {
        EventPublisher
            .generate(domainId, IEvent.CREATED, null, JDOUtils.getImplClassName(IAsset.class),
                String.valueOf(asset.getId()), null);
      } catch (Exception e) {
        xLogger.warn("Exception when generating event for creating asset {0} in domain {1}",
            asset.getSerialId(), domainId, e);
        throw new ServiceException(e);
      }

    } catch(IllegalArgumentException e) {
      throw e;
    } catch(Exception e) {
      throw new ServiceException(e);
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
  }

  @SuppressWarnings("unchecked")
  private Long createAsset(Long domainId, IAsset asset, PersistenceManager pm) throws ServiceException {
    if (domainId == null || asset == null
        || asset.getSerialId() == null || asset.getVendorId() == null) {
      throw new ServiceException("Invalid details for the asset");
    }
    try {
      validateAssets(asset);
      Query query = pm.newQuery(JDOUtils.getImplClass(IAsset.class));
      query.setFilter("vId == vendorIdParam && nsId == serialIdParam");
      query.declareParameters("String vendorIdParam, String serialIdParam");
      List<IAsset> assets = null;
      try {
        assets =
            (List<IAsset>) query.execute(asset.getVendorId(), asset.getSerialId().toLowerCase());
        assets = (List<IAsset>) pm.detachCopyAll(assets);
      } finally {
        query.closeAll();
      }

      if (assets == null || assets.size() == 0) {
        Date now = new Date();
        asset.setCreatedOn(now);
        asset.setUpdatedOn(now);
        asset.setDomainId(domainId);
        asset = (IAsset) DomainsUtil.addToDomain(asset, domainId, pm);
      } else {
        throw new ServiceException(
            asset.getSerialId() + "(" + asset.getVendorId() + ") " + backendMessages
                .getString("error.alreadyexists"));
      }
    } catch (ServiceException e) {
      xLogger.warn("{0} while creating asset {1}, {2} for the domain {4}", e.getMessage(),
          asset.getSerialId(), asset.getVendorId(), domainId, e);
      throw new ServiceException(e.getMessage());
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      xLogger.severe("{0} while creating asset {1}, {2} for the domain {4}", e.getMessage(),
          asset.getSerialId(), asset.getVendorId(), domainId, e);
      throw new ServiceException(e.getMessage());
    }

    return asset.getId();
  }

  @Override
  public IAsset getAsset(Long assetId) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    IAsset asset = null;
    String errMsg = null;
    try {
      asset = JDOUtils.getObjectById(IAsset.class, assetId, pm);
      asset = pm.detachCopy(asset);
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("getAsset: Asset {0} does not exist", assetId);
      errMsg =
          messages.getString("asset") + " " + assetId + " " + backendMessages
              .getString("error.notfound");
    } catch (Exception e) {
      errMsg = e.getMessage();
    } finally {
      pm.close();
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg);
    }
    return asset;
  }

  @Override
  public void updateAsset(Long domainId, IAsset asset, AssetModel assetModel)
      throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = null;
    try {
      tx = pm.currentTransaction();
      tx.begin();
      DomainsUtil.addToDomain(asset, domainId, pm);
      AssetUtil.registerOrUpdateDevice(assetModel, domainId);
      tx.commit();
      asset = pm.detachCopy(asset);
      try {
        if(assetModel.ws == null) {
          EventPublisher
              .generate(domainId, IEvent.MODIFIED, null, JDOUtils.getImplClassName(IAsset.class),
                  String.valueOf(asset.getId()), null);
        } else {
          Query query = pm.newQuery();
          query.setClass(JDOUtils.getImplClass(IAssetStatus.class));
          query.setFilter("assetId == assetIdParam && type == typeIdParam");
          query.declareParameters("Long assetIdParam, Integer typeIdParam");
          query.setUnique(true);
          IAssetStatus assetStatus = null;
          try {
            assetStatus = (IAssetStatus)query.execute(asset.getId(), IAssetStatus.TYPE_STATE);
            Map<String, Object> params = new HashMap<>();
            params.put(EventConstants.PARAM_STATUS, String.valueOf(assetModel.ws.st));
            Integer event = asset.getType() == IAsset.MONITORED_ASSET ? IEvent.STATUS_CHANGE : IEvent.STATUS_CHANGE_TEMP;
            EventPublisher
                .generate(domainId, event, params,
                    JDOUtils.getImplClassName(IAssetStatus.class),
                    String.valueOf(assetStatus.getId()), null);
          } catch (Exception e) {
            xLogger.warn("{0} while getting asset data for {1} in domain {2}", e.getMessage(), asset.getId(), domainId, e);
          }
        }
      } catch (Exception e) {
        xLogger.warn("Exception when generating event for updating asset {0} in domain {1}",
            asset.getSerialId(), domainId, e);
        throw new ServiceException(e);
      }
    } finally {
      if (tx != null) {
        if (tx.isActive()) {
          tx.rollback();
        }
      }
      pm.close();
    }
  }

  @Override
  public Results getAssetsByDomain(Long domainId, Integer assetType, PageParams pageParams)
      throws ServiceException {
    if (domainId == null) {
      throw new ServiceException("Domain id is not provided");
    }

    List<IAsset> assets = null;
    int numFound = 0;
    String cursor = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery(JDOUtils.getImplClass(IAsset.class));
      if (assetType != null && assetType != 0) {
        query.setFilter("dId.contains(domainIdParam) && type == assetTypeParam");
        query.declareParameters("Long domainIdParam, Integer assetTypeParam");
      } else {
        query.setFilter("dId.contains(domainIdParam)");
        query.declareParameters("Long domainIdParam");
      }
      query.setOrdering("sId asc");
      if (pageParams != null) {
        QueryUtil.setPageParams(query, pageParams);
      }

      try {
        if (assetType != null && assetType != 0) {
          assets = (List<IAsset>) query.execute(domainId, assetType);
        } else {
          assets = (List<IAsset>) query.execute(domainId);

        }
        cursor = QueryUtil.getCursor(assets);
        assets = (List<IAsset>) pm.detachCopyAll(assets);

        //Count query
        StringBuilder
            sqlQuery =
            new StringBuilder(
                "SELECT COUNT(1) FROM ASSET WHERE ID in (SELECT ID_OID from ASSET_DOMAINS where DOMAIN_ID = ");
        sqlQuery.append(domainId);
        sqlQuery.append(")");
        if (assetType != null && assetType != 0) {
          sqlQuery.append(" and TYPE = ").append(assetType);
        }

        Query cntQuery = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
        numFound = ((Long) ((List) cntQuery.execute()).iterator().next()).intValue();
        cntQuery.closeAll();
      } finally {
        query.closeAll();
      }
    } catch (Exception e) {
      xLogger.warn("{0} while getting assets for the domain {1}", e.getMessage(), domainId, e);
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }

    return new Results(assets, cursor, numFound, pageParams == null ? 0 : pageParams.getOffset());
  }

  @Override
  public List<IAsset> getAssetsByKiosk(Long kioskId) throws ServiceException {
    if (kioskId == null) {
      throw new ServiceException("");//TODO
    }

    List<IAsset> assets = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery(JDOUtils.getImplClass(IAsset.class));
      query.setFilter("kId == kioskIdParam");
      query.declareParameters("Long kioskIdParam");
      try {
        assets = (List<IAsset>) query.execute(kioskId);
        assets = (List<IAsset>) pm.detachCopyAll(assets);
      } finally {
        query.closeAll();
      }
    } catch (Exception e) {
      xLogger.severe("{0} while getting assets for the kiosk {1}", e.getMessage(), kioskId, e);
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }

    return assets;
  }

  @Override
  public List<IAsset> getAssetsByKiosk(Long kioskId, Integer assetType) throws ServiceException {
    if (kioskId == null || assetType == null) {
      throw new ServiceException(
          backendMessages.getString("kiosk") + " and AssetType are mandatory");
    }
    List<IAsset> assets = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery(JDOUtils.getImplClass(IAsset.class));
      query.setFilter("kId == kioskIdParam  && type == assetTypeParam");
      query.declareParameters("Long kioskIdParam, Integer assetTypeParam");
      try {
        assets = (List<IAsset>) query.execute(kioskId, assetType);
        assets = (List<IAsset>) pm.detachCopyAll(assets);
      } finally {
        query.closeAll();
      }
    } catch (Exception e) {
      xLogger.severe("{0} while getting assets for the kiosk {1}", e.getMessage(), kioskId, e);
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }

    return assets;
  }

  @Override
  public IAsset getAsset(String manufacturerId, String assetId) throws ServiceException {
    if (assetId == null || manufacturerId == null) {
      throw new ServiceException("");//TODO
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery(JDOUtils.getImplClass(IAsset.class));
      query.setFilter("vId == vendorIdParam && sId == serialIdParam");
      query.declareParameters("String vendorIdParam, String serialIdParam");
      List<IAsset> assets;
      try {
        assets = (List<IAsset>) query.execute(manufacturerId, assetId);
        assets = (List<IAsset>) pm.detachCopyAll(assets);

        if (assets != null && assets.size() == 1) {
          return assets.get(0);
        }
      } finally {
        query.closeAll();
      }
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("{0} while getting asset {1}, {2}", e.getMessage(), assetId, manufacturerId, e);
      throw new ServiceException(e.getMessage());
    } catch (Exception e) {
      xLogger
          .severe("{0} while getting asset {1}, {2}", e.getMessage(), assetId, manufacturerId, e);
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }

    return null;
  }


  private IAssetStatus getAssetStatus(Long assetId, Integer mpId, String sId, Integer type,
                                      PersistenceManager pm) throws ServiceException {
    Preconditions.checkArgument(!(assetId == null || (mpId == null && sId == null) || type == null),
        "Illegal argument expected assetId, mpId or sId and type");
    Long id = JDOUtils.createAssetStatusKey(assetId, mpId != null ? mpId : 0, sId, type);
    try {
      return JDOUtils.getObjectById(IAssetStatus.class, id, pm);
    } catch (Exception e) {
      //No such object
      return null;
    }
  }

  @Override
  public void updateAssetStatus(List<IAssetStatus> assetStatusModelList) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IAssetStatus> updated = new ArrayList<>();
    try {
      for (IAssetStatus assetStatusModel : assetStatusModelList) {
        try {
          IAssetStatus
              assetStatus =
              getAssetStatus(assetStatusModel.getAssetId(), assetStatusModel.getMpId(),
                  assetStatusModel.getsId(), assetStatusModel.getType(), pm);
          if (assetStatus == null) {
            pm.makePersistent(assetStatusModel);
            assetStatusModel = pm.detachCopy(assetStatusModel);
          } else {
            if (Objects.equals(assetStatusModel.getStatus(), assetStatus.getStatus())) {
              continue;
            }
            assetStatus.setStatus(assetStatusModel.getStatus());
            assetStatus.setTmp(assetStatusModel.getTmp());
            assetStatus.setTs(assetStatusModel.getTs());
            assetStatus.setAbnStatus(assetStatusModel.getAbnStatus());
            assetStatus.setAttributes(assetStatusModel.getAttributes());
          }
          updated.add(assetStatusModel);
        } catch (Exception e) {
          xLogger.severe("Error while persisting asset status for: {0}", assetStatusModel, e);
        }
      }
    } finally {
      pm.close();
    }
    try {
      AssetSystemConfig asc = AssetSystemConfig.getInstance();
      for (IAssetStatus assetStatus : updated) {
        IAsset asset = getAsset(assetStatus.getAssetId());
        AssetSystemConfig.Asset assetData = asc.assets.get(asset.getType());
        Integer assetType = assetData.type;
        int eventType = -1;
        if (assetType == IAsset.MONITORED_ASSET) {
          if (IAssetStatus.TYPE_TEMPERATURE.equals(assetStatus.getType())) {
            //Event notification
            if (IAsset.ABNORMAL_TYPE_HIGH == assetStatus.getAbnStatus()) {
              if (assetStatus.getStatus() == IAsset.STATUS_EXCURSION) {
                eventType = IEvent.HIGH_EXCURSION;
              } else if (assetStatus.getStatus() == IAsset.STATUS_WARNING) {
                eventType = IEvent.HIGH_WARNING;
              } else if (assetStatus.getStatus() == IAsset.STATUS_ALARM) {
                eventType = IEvent.HIGH_ALARM;
              }
            } else if (IAsset.ABNORMAL_TYPE_LOW == assetStatus.getAbnStatus()) {
              if (assetStatus.getStatus() == IAsset.STATUS_EXCURSION) {
                eventType = IEvent.LOW_EXCURSION;
              } else if (assetStatus.getStatus() == IAsset.STATUS_WARNING) {
                eventType = IEvent.LOW_WARNING;
              } else if (assetStatus.getStatus() == IAsset.STATUS_ALARM) {
                eventType = IEvent.LOW_ALARM;
              }
            } else if (assetStatus.getStatus() == IAsset.STATUS_NORMAL) {
              eventType = IEvent.INCURSION;
            }
          }
        } else if (assetType == IAsset.MONITORING_ASSET) {
          if (IAssetStatus.TYPE_BATTERY.equals(assetStatus.getType())) {
            if (IAsset.STATUS_BATTERY_LOW == assetStatus.getStatus()) {
              eventType = IEvent.BATTERY_LOW;
            } else if (IAsset.STATUS_BATTERY_ALARM == assetStatus.getStatus()) {
              eventType = IEvent.BATTERY_ALARM;
            } else if (IAsset.STATUS_NORMAL == assetStatus.getStatus()) {
              eventType = IEvent.BATTERY_NORMAL;
            }
          } else if (IAssetStatus.TYPE_ACTIVITY.equals(assetStatus.getType())) {
            if (IAsset.STATUS_ASSET_INACTIVE == assetStatus.getStatus()) {
              eventType = IEvent.ASSET_INACTIVE;
            } else if (IAsset.STATUS_NORMAL == assetStatus.getStatus()) {
              eventType = IEvent.ASSET_ACTIVE;
            }
          } else if (IAssetStatus.TYPE_DEVCONN.equals(assetStatus.getType())) {
            if (IAsset.STATUS_DEVICE_DISCONNECTED == assetStatus.getStatus()) {
              eventType = IEvent.DEVICE_DISCONNECTED;
            } else if (IAsset.STATUS_NORMAL == assetStatus.getStatus()) {
              eventType = IEvent.DEVICE_CONNECTION_NORMAL;
            }
          } else if (IAssetStatus.TYPE_XSENSOR.equals(assetStatus.getType())) {
            if (IAsset.STATUS_SENSOR_DISCONNECTED == assetStatus.getStatus()) {
              eventType = IEvent.SENSOR_DISCONNECTED;
            } else if (IAsset.STATUS_NORMAL == assetStatus.getStatus()) {
              eventType = IEvent.SENSOR_CONNECTION_NORMAL;
            }
          } else if (IAssetStatus.TYPE_POWER.equals(assetStatus.getType())) {
            if (IAsset.STATUS_POWER_OUTAGE == assetStatus.getStatus()) {
              eventType = IEvent.POWER_OUTAGE;
            } else if (IAsset.STATUS_NORMAL == assetStatus.getStatus()) {
              eventType = IEvent.POWER_NORMAL;
            }
          }
        }
        // NO_ACTIVITY event is handled during DailyEventsCreation
        // Log the high excursion, low excursion and incursion events. The NO_ACTIVITY event is handled by the DailyEventsGenerator
        if (eventType != -1) {
          EventsConfig ec = DomainConfig.getInstance(asset.getDomainId()).getEventsConfig();
          EventSpec
              tempEventSpec =
              ec.getEventSpec(eventType, JDOUtils.getImplClass(IAssetStatus.class).getName());
          // After getting tempEventSpec, call a private method to generate temperature events.
          if (tempEventSpec != null) {
            AssetUtil
                .generateAssetEvents(asset.getDomainId(), tempEventSpec, assetStatus, asset,
                    eventType);
          }
        }
      }
    } catch (Exception e) {
      xLogger.severe("Error while creating event for asset status", e);
    }
  }

  @Override
  public void deleteAsset(String manufacturerId, List<String> serialIds, Long domainId)
      throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = null;
    try {
      tx = pm.currentTransaction();
      tx.begin();
      Query query = pm.newQuery(JDOUtils.getImplClass(IAsset.class));
      query.setFilter("vId == vIdParam && assetIdParam.contains(sId)");
      query.declareParameters("String vIdParam, java.util.Collection assetIdParam");
      List<IAsset> assets;
      try {
        assets = (List<IAsset>) query.execute(manufacturerId, serialIds);
        for (IAsset asset : assets) {
          deleteAssetRelation(asset.getId(), domainId, asset, pm);
        }
        for (IAsset asset : assets) {
          EventPublisher
              .generate(domainId, IEvent.DELETED, null, JDOUtils.getImplClassName(IAsset.class),
                  String.valueOf(asset.getId()), null, asset);
        }
        pm.deletePersistentAll(assets);
        tx.commit();
      } finally {
        query.closeAll();
      }
    } catch (Exception e) {
      xLogger.severe("{0} while deleting assets: {1}, {2}", e.getMessage(), manufacturerId,
          serialIds.toString(), e);
      throw new ServiceException(e.getMessage());
    } finally {
      if(tx.isActive()){
        tx.rollback();
      }
      pm.close();
    }
  }

  @Override
  public IAssetRelation createOrUpdateAssetRelation(Long domainId, IAssetRelation assetRelation)
      throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery(JDOUtils.getImplClass(IAssetRelation.class));
      query.setFilter("assetId == assetIdParam");
      query.declareParameters("Long assetIdParam");
      List<IAssetRelation> assetRelations = null;
      try {
        assetRelations = (List<IAssetRelation>) query.execute(assetRelation.getAssetId());
        assetRelations = (List<IAssetRelation>) pm.detachCopyAll(assetRelations);
      } catch (Exception ignored) {
        //do nothing
      } finally {
        query.closeAll();
      }

      if (assetRelations != null && assetRelations.size() == 1) {
        IAssetRelation assetRelationTmp = assetRelations.get(0);
        assetRelationTmp.setRelatedAssetId(assetRelation.getRelatedAssetId());
        assetRelationTmp.setType(assetRelation.getType());
        pm.makePersistent(assetRelationTmp);
        return assetRelationTmp;
      } else {
        pm.makePersistent(assetRelation);
        EventPublisher.generate(domainId, IEvent.CREATED, null,
            JDOUtils.getImplClassName(IAssetRelation.class), String.valueOf(assetRelation.getId()), null);
        return assetRelation;
      }
    } catch (Exception e) {
      xLogger.warn("{0} while updating asset relationship for the asset {1}", e.getMessage(),
          assetRelation.getAssetId(), e);
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }
  }

  @Override
  public void deleteAssetRelation(Long assetId, Long domainId, IAsset asset)
      throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      deleteAssetRelation(assetId, domainId, asset, pm);
    } finally {
      pm.close();
    }
  }

  public void deleteAssetRelation(Long assetId, Long domainId, IAsset asset, PersistenceManager pm) throws ServiceException {
    try {
      Query query = pm.newQuery(JDOUtils.getImplClass(IAssetRelation.class));
      query.setFilter("assetId == assetIdParam");
      query.declareParameters("Long assetIdParam");
      List<IAssetRelation> assetRelations = null;
      try {
        assetRelations = (List<IAssetRelation>) query.execute(assetId);
        assetRelations = (List<IAssetRelation>) pm.detachCopyAll(assetRelations);
      } catch (Exception ignored) {
        //do nothing
      } finally {
        query.closeAll();
      }

      if (assetRelations != null && assetRelations.size() == 1) {
        IAssetRelation assetRelationTmp = assetRelations.get(0);
        for(IAssetRelation assetRelation: assetRelations){
          EventPublisher.generate(domainId, IEvent.DELETED, null,
              JDOUtils.getImplClassName(IAssetRelation.class), String.valueOf(assetRelation.getId()), null, assetRelation);
        }
        pm.deletePersistent(assetRelationTmp);
      }
    } catch (Exception e) {
      xLogger
          .warn("{0} while deleting asset relationship for the asset {1}", e.getMessage(), assetId,
              e);
      throw new ServiceException(e.getMessage());
    }
  }

  public void deleteAssetRelationByRelatedAsset(Long relatedAssetId) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery(JDOUtils.getImplClass(IAssetRelation.class));
      query.setFilter("relatedAssetId == relatedAssetIdParam");
      query.declareParameters("Long relatedAssetIdParam");
      List<IAssetRelation> assetRelations = null;
      try {
        assetRelations = (List<IAssetRelation>) query.execute(relatedAssetId);
        assetRelations = (List<IAssetRelation>) pm.detachCopyAll(assetRelations);
      } catch (Exception ignored) {
        //do nothing
      } finally {
        query.closeAll();
      }

      if (assetRelations != null && assetRelations.size() == 1) {
        IAssetRelation assetRelationTmp = assetRelations.get(0);
        pm.deletePersistent(assetRelationTmp);
      }
    } catch (Exception e) {
      xLogger.warn("{0} while deleting asset relationship for the asset {1}", e.getMessage(),
          relatedAssetId, e);
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }
  }

  @Override
  public IAssetRelation getAssetRelationByRelatedAsset(Long relatedAssetId)
      throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery(JDOUtils.getImplClass(IAssetRelation.class));
      query.setFilter("relatedAssetId == relatedAssetIdParam");
      query.declareParameters("Long relatedAssetIdParam");
      List<IAssetRelation> assetRelations = null;
      try {
        assetRelations = (List<IAssetRelation>) query.execute(relatedAssetId);
        assetRelations = (List<IAssetRelation>) pm.detachCopyAll(assetRelations);
      } catch (Exception ignored) {
        //do nothing
      } finally {
        query.closeAll();
      }

      if (assetRelations != null && assetRelations.size() == 1) {
        return assetRelations.get(0);
      }
    } catch (Exception e) {
      xLogger.warn("{0} while deleting asset relationship for the asset {1}", e.getMessage(),
          relatedAssetId, e);
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }

    return null;
  }

  @Override
  public IAssetRelation getAssetRelationByAsset(Long assetId) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery(JDOUtils.getImplClass(IAssetRelation.class));
      query.setFilter("assetId == assetIdParam");
      query.declareParameters("Long assetIdParam");
      List<IAssetRelation> assetRelations = null;
      try {
        assetRelations = (List<IAssetRelation>) query.execute(assetId);
        assetRelations = (List<IAssetRelation>) pm.detachCopyAll(assetRelations);
      } catch (Exception ignored) {
        //do nothing
      } finally {
        query.closeAll();
      }

      if (assetRelations != null && assetRelations.size() == 1) {
        return assetRelations.get(0);
      }
    } catch (Exception e) {
      xLogger
          .warn("{0} while getting asset relationship for the asset {1}", e.getMessage(), assetId,
              e);
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }

    return null;
  }

  public List<IAsset> getAssets(Long domainId, Long kId, String q, String assetType, Boolean all)
      throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IAsset> assets = new ArrayList<>();
    try {
      StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ASSET WHERE");

      if (all) {
        sqlQuery.append(" ID IN (SELECT ID_OID FROM ASSET_DOMAINS WHERE DOMAIN_ID = ")
            .append(domainId).append(")");
      } else {
        sqlQuery.append(" ID NOT IN (SELECT RELATEDASSETID FROM ASSETRELATION where RELATEDASSETID is not NULL) AND SDID = ")
            .append(domainId);
      }

      if (kId != null) {
        sqlQuery.append(" AND KID = ").append(kId);
      }

      if (!q.isEmpty()) {
        sqlQuery.append(" AND nsId like '").append(q).append("%'");
      }

      if (assetType != null) {
        sqlQuery.append(" AND type = ").append(assetType);
      }

      sqlQuery.append(" ORDER BY nsId asc LIMIT 0, 10");
      Query query = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
      query.setClass(JDOUtils.getImplClass(IAsset.class));
      try {
        assets = (List<IAsset>) query.execute();
        assets = (List<IAsset>) pm.detachCopyAll(assets);
      } finally {
        query.closeAll();
      }
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    } finally {
      pm.close();
    }

    return assets;
  }

  /**
   * Checks whether the serial/model number of monitored asset matches with the format provided in the configuration
   * @param asset
   * @throws ServiceException
   */
  private void validateAssets(IAsset asset) throws ServiceException, IllegalArgumentException {
    try {
      AssetSystemConfig asc = AssetSystemConfig.getInstance();
      AssetSystemConfig.Asset assetData = asc.assets.get(asset.getType());
      Integer assetType = assetData.type;
      if (assetType == IAsset.MONITORED_ASSET) {
        AssetSystemConfig.Manufacturer
            manc =
            assetData.getManufacturers().get(asset.getVendorId().toLowerCase());
        String regex = manc.serialFormat;
        if (StringUtils.isNotBlank(regex)) {
          if (!asset.getSerialId().matches(regex)) {
            String errorMessage = messages.getString("serialno.format").concat("\n").concat(
                manc.serialFormatDescription);
            throw new IllegalArgumentException(errorMessage);
          }
        }
        regex = manc.modelFormat;
        if (StringUtils.isNotBlank(regex)) {
          if (!asset.getModel().matches(regex)) {
            String errorMessage = messages.getString("modelno.format").concat("\n").concat(
                manc.modelFormatDescription);
            throw new IllegalArgumentException(errorMessage);
          }
        }
      }
    } catch (ConfigurationException e) {
      xLogger.warn("Error while getting asset system config", e);
    } catch (IllegalArgumentException e) {
      xLogger.warn(e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public Map<String, Integer> getTemperatureStatus(Long entityId) {
    PersistenceManager pm = null;
    JDOConnection conn = null;
    Statement statement = null;
    try {
      AssetSystemConfig config = AssetSystemConfig.getInstance();
      Map<Integer, AssetSystemConfig.Asset>
          monitoredAssets =
          config.getAssetsByType(IAsset.MONITORED_ASSET);
      String csv = monitoredAssets.keySet().toString();
      csv = csv.substring(1, csv.length() - 1);

      pm = PMF.get().getPersistenceManager();
      conn = pm.getDataStoreConnection();
      java.sql.Connection sqlConn = (java.sql.Connection) conn;
      statement = sqlConn.createStatement();
      CachedRowSet rowSet = new CachedRowSetImpl();
      String
          query =
          "SELECT STAT, COUNT(1) COUNT FROM (SELECT ID, IF(ASF.STAT = 'tu', 'tu',(SELECT IF(MAX(ABNSTATUS) = 2, 'th', "
              + "IF(MAX(ABNSTATUS) = 1, 'tl', 'tn')) FROM ASSETSTATUS AO WHERE AO.ASSETID = ASF.ASSETID AND AO.TYPE = 1 AND AO.STATUS = 3 "
              + ")) STAT FROM (SELECT A.ID FROM ASSET A WHERE TYPE IN (TOKEN_TYPE) AND KID = TOKEN_KID AND "
              + "EXISTS(SELECT 1 FROM ASSETRELATION R WHERE A.ID = R.ASSETID AND R.TYPE = 2) AND EXISTS"
              + "(SELECT 1 FROM ASSETSTATUS S WHERE S.ASSETID = A.ID AND S.TYPE = 7 AND S.STATUS = 0)"
              + ") A "
              + "LEFT JOIN (SELECT ASSETID, IF(MIN(STATUS) = 0, 'tk', 'tu') STAT FROM ASSETSTATUS ASI WHERE ASI.TYPE = 3 AND "
              + "ASI.ASSETID IN (SELECT ID FROM ASSET WHERE TYPE IN (TOKEN_TYPE) AND KID = TOKEN_KID) GROUP BY ASI.ASSETID) ASF "
              + "ON A.ID = ASF.ASSETID) T GROUP BY T.STAT";
      query = query.replace("TOKEN_TYPE", csv).replace("TOKEN_KID", String.valueOf(entityId));
      rowSet.populate(statement.executeQuery(query));
      Map<String, Integer> stats = new HashMap<>(4);
      while (rowSet.next()) {
        stats.put(rowSet.getString("STAT"), rowSet.getInt("COUNT"));
      }
      return stats;
    } catch (Exception e) {
      xLogger.severe("Error in fetching Temperature status for assets of entity {0}", entityId, e);
    } finally {
      try {
        if (statement != null) {
          statement.close();
        }
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing statement", statement);
      }

      try {
        if (conn != null) {
          conn.close();
        }
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing connection", ignored);
      }

      try {
        if (pm != null) {
          pm.close();
        }
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing pm", ignored);
      }
    }
    return null;
  }

  @Override
  public List<String> getModelSuggestion(Long domainId, String term) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    try {
      StringBuilder sqlQuery = new StringBuilder("SELECT DISTINCT MODEL FROM ASSET WHERE");
      sqlQuery.append(" ID IN (SELECT ID_OID FROM ASSET_DOMAINS WHERE DOMAIN_ID = ")
          .append(domainId).append(")");
      if (StringUtils.isNotEmpty(term)) {
        sqlQuery.append(" AND lower(MODEL) like '%").append(term.toLowerCase()).append("%'");
      }
      sqlQuery.append(" LIMIT 0, 10");
      query = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
      List modelList = (List) query.execute();
      List<String> models = new ArrayList<>(modelList.size());
      for (Object o : modelList) {
        models.add((String) o);
      }
      return models;
    } catch (Exception e) {
      xLogger.warn("Error while fetching suggestions for asset models", e);
    } finally {
      if(query != null) {
        query.closeAll();
      }
      pm.close();
    }
    return null;
  }

  @Override
  public String getMonitoredAssetIdsForReport(Map<String, String> filters) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Connection conn = (java.sql.Connection) pm.getDataStoreConnection();
    Statement statement = null;
    StringBuilder assetIds = new StringBuilder("");
    StringBuilder assetQuery = new StringBuilder("SELECT SID,VID FROM ASSET WHERE TYPE != 1 ");
    if (filters.containsKey("TOKEN_CN")
        || filters.containsKey("TOKEN_ST")
        || filters.containsKey("TOKEN_DIS")
        || filters.containsKey("TOKEN_TALUK")
        || filters.containsKey("TOKEN_KID")
        || filters.containsKey("TOKEN_KTAG")) {
      assetQuery.append("AND KID IN (");
      StringBuilder kioskQuery =
          new StringBuilder("SELECT KIOSKID FROM KIOSK K ,KIOSK_DOMAINS KD WHERE ");
        kioskQuery
            .append(" K.KIOSKID = KD.KIOSKID_OID AND KD.DOMAIN_ID = ")
            .append(filters.get("TOKEN_DID"));
      if (filters.containsKey("TOKEN_CN")) {
        kioskQuery.append(" AND K.COUNTRY = ").append(filters.get("TOKEN_CN"));
      }
      if (filters.containsKey("TOKEN_ST")) {
        kioskQuery.append(" AND K.STATE = ").append(filters.get("TOKEN_ST"));
      }
      if (filters.containsKey("TOKEN_DIS")) {
        kioskQuery.append(" AND K.DISTRICT = ").append(filters.get("TOKEN_DIS"));
      }
      if (filters.containsKey("TOKEN_TALUK")) {
        kioskQuery.append(" AND K.TALUK = ").append(filters.get("TOKEN_TALUK"));
      }
      if (filters.containsKey("TOKEN_KID")) {
        kioskQuery.append(" AND K.KIOSKID = ").append(filters.get("TOKEN_KID"));
      } else if (filters.containsKey("TOKEN_KTAG")) {
        kioskQuery.append(" AND K.KIOSKID IN (");
        StringBuilder ktagQuery =
            new StringBuilder(
                "SELECT KT.KIOSKID FROM "
                    + "KIOSK_TAGS KT,TAG T WHERE T.ID = KT.ID AND T.NAME = "
                    + filters.get("TOKEN_KTAG"));
        kioskQuery.append(ktagQuery).append(") ");
      }
      assetQuery.append(kioskQuery);
      assetQuery.append(")");
    } else {
      assetQuery.append("AND KID IN (");
      assetQuery
          .append("SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID = ")
          .append(filters.get("TOKEN_DID"))
          .append(")");
    }
    if (filters.containsKey("TOKEN_ATYPE")) {
      assetQuery.append(" AND ");
      assetQuery.append("type = ").append(filters.get("TOKEN_ATYPE"));
    }
    if (filters.containsKey("TOKEN_VID")) {
      assetQuery.append(" AND ");
      assetQuery.append(" VID = ").append(filters.get("TOKEN_VID"));
    }
    if (filters.containsKey("TOKEN_DMODEL")) {
      assetQuery.append(" AND ");
      assetQuery.append("model = ").append(filters.get("TOKEN_DMODEL"));
    }
    if (filters.containsKey("TOKEN_MYEAR")) {
      assetQuery.append(" AND ");
      assetQuery.append("yom = ").append(filters.get("TOKEN_MYEAR"));
    }
    if (filters.containsKey("TOKEN_SIZE") && filters.containsKey("TOKEN_OFFSET")) {
      assetQuery
          .append(" LIMIT ")
          .append(filters.get("TOKEN_OFFSET"))
          .append(CharacterConstants.COMMA)
          .append(filters.get("TOKEN_SIZE"));
    }
    try {
      statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(assetQuery.toString());
      while (rs.next()) {
        if (StringUtils.isNotEmpty(assetIds.toString())) {
          assetIds.append(CharacterConstants.COMMA);
        }
        assetIds.append(CharacterConstants.S_QUOTE).append(rs.getString("VID"))
            .append(CharacterConstants.UNDERSCORE).append(rs.getString("SID"))
            .append(CharacterConstants.S_QUOTE);
      }
      return assetIds.toString();
    } catch (Exception e) {
      xLogger.warn("Error while fetching asset ids for reports", e);
    } finally {
      try {
        pm.close();
        conn.close();
        if(statement != null){
          statement.close();
        }
      } catch (Exception e) {
        xLogger.warn("Exception while closing connection", e);
      }
    }
    return null;
  }

  @Override
  public String getVendorIdsForReports(String did) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query;
    StringBuilder vidQuery = new StringBuilder();
    StringBuilder vids =  new StringBuilder("");
    if (StringUtils.isNotEmpty(did)) {
      vidQuery
          .append("SELECT DISTINCT VID FROM ASSET WHERE KID IN (")
          .append("SELECT KIOSKID FROM KIOSK K ,KIOSK_DOMAINS KD WHERE ")
          .append("K.KIOSKID = KD.KIOSKID_OID AND KD.DOMAIN_ID = ").append(did)
          .append(CharacterConstants.C_BRACKET);
    }
    query = pm.newQuery("javax.jdo.query.SQL", vidQuery.toString());
    try{
    List<String> result = (List) query.execute();
    for(int i=0;i<result.size();i++){
        if(i!=0){
            vids.append(CharacterConstants.COMMA);
        }
        vids.append(CharacterConstants.S_QUOTE).append(result.get(i))
                .append(CharacterConstants.S_QUOTE);
    }
    return vids.toString();
    } catch (Exception e){
      xLogger.warn("Error while fetching vendor ids for reports", e);
    } finally {
        if(query != null) {
            query.closeAll();
        }
        pm.close();
    }
    return null;
  }

  @Override
  public String getAssetTypesForReports(String did,String exclude) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query;
    StringBuilder q = new StringBuilder();
    StringBuilder types = new StringBuilder("");
    if (StringUtils.isNotEmpty(did)) {
      q.append("SELECT DISTINCT TYPE FROM ASSET WHERE KID IN (")
          .append("SELECT KIOSKID FROM KIOSK K ,KIOSK_DOMAINS KD WHERE ")
          .append("K.KIOSKID = KD.KIOSKID_OID AND KD.DOMAIN_ID = ")
          .append(did)
          .append(CharacterConstants.C_BRACKET);
    }
    if(StringUtils.isNotEmpty(exclude)){
      q.append(" AND TYPE != ").append(exclude);
    }
    query = pm.newQuery("javax.jdo.query.SQL", q.toString());
    try {
      List<Integer> result = (List) query.execute();
      for (int i = 0; i < result.size(); i++) {
        if (i != 0) {
          types.append(CharacterConstants.COMMA);
        }
        types.append(CharacterConstants.S_QUOTE).append(result.get(i).toString())
                .append(CharacterConstants.S_QUOTE);
      }
      return types.toString();
    } catch (Exception e) {
      xLogger.warn("Error while fetching asset types for reports", e);
    } finally {
      if (query != null) {
        query.closeAll();
      }
      pm.close();
    }
    return null;
  }
}
