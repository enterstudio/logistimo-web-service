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

package com.logistimo.materials.service.impl;

import com.logistimo.dao.JDOUtils;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.service.IHandlingUnitService;

import org.apache.commons.lang.StringUtils;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.utils.QueryUtil;
import com.logistimo.logger.XLog;
import com.logistimo.exception.InvalidDataException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author Mohan Raja
 */
public class HandlingUnitServiceImpl extends ServiceImpl implements IHandlingUnitService {

  private static final XLog xLogger = XLog.getLog(HandlingUnitServiceImpl.class);

  @Override
  public Long addHandlingUnit(Long domainId, IHandlingUnit handlingUnit) throws ServiceException {
    Date now = new Date();
    handlingUnit.setTimeStamp(now);
    handlingUnit.setLastUpdated(now);
    handlingUnit.setDomainId(domainId);
    IHandlingUnit handlingUnitByName = getHandlingUnitByName(domainId, handlingUnit.getName());
    if (handlingUnitByName != null) {
      xLogger.warn("add: Handling unit with name {0} already exists", handlingUnit.getName());
      throw new ServiceException(
          backendMessages.getString("error.cannotadd") + ". '" + handlingUnit.getName() + "' "
              + backendMessages.getString("error.alreadyexists") + ".");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      DomainsUtil.addToDomain(handlingUnit, domainId, pm);
    } finally {
      pm.close();
    }

        /*
        //todo: This code already modified to handle notification. Need to enable this when we enable notification for handing units.
        EventGenerator eg = EventGeneratorFactory.getEventGenerator(domainId, JDOUtils.getImplClass(IHandlingUnit.class).getName());
        try {
            eg.generate( IEvent.CREATED, null, String.valueOf(handlingUnit.getId()), null );
        } catch (EventHandlingException e) {
            xLogger.warn( "Exception when generating event for handling unit-creation for handling unit {0} in domain {1}", handlingUnit.getId(), domainId,e );
        }*/
    return handlingUnit.getId();
  }

  @Override
  public void updateHandlingUnit(IHandlingUnit handlingUnit, Long domainId)
      throws ServiceException {
    xLogger.fine("Entering update Handling unit");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = pm.currentTransaction();
    try {
      tx.begin();
      try {
        IHandlingUnit hu = JDOUtils.getObjectById(IHandlingUnit.class, handlingUnit.getId(), pm);
        if (!domainId.equals(hu.getDomainId())) {
          xLogger.warn("Updating of this handling unit {0} is not allowed in this domain.",
              handlingUnit.getName(), handlingUnit.getDomainId());
          throw new ServiceException("Handling unit does not exist");
        }
        List<IHandlingUnit> temp = getHandlingUnitListByName(domainId, handlingUnit.getName());
        for (IHandlingUnit iHu : temp) {
          if (!iHu.getId().equals(handlingUnit.getId())) {
            throw new InvalidDataException("Handling unit " + hu.getName() + " " + backendMessages
                .getString("error.alreadyexists"));
          }
        }
        hu.setName(handlingUnit.getName());
        hu.setDescription(handlingUnit.getDescription());
        hu.setContents(handlingUnit.getContents());
        hu.setLastUpdated(new Date());
        hu.setUpdatedBy(handlingUnit.getUpdatedBy());
        handlingUnit = pm.makePersistent(hu);
        handlingUnit = pm.detachCopy(handlingUnit);
      } catch (JDOObjectNotFoundException e) {
        xLogger.warn("update handling unit: FAILED!! Handling unit does not exist: {0}",
            handlingUnit.getId());
      }
      tx.commit();

            /*
            //todo: This code already modified to handle notification. Need to enable this when we enable notification for handing units.
            EventGenerator eg = EventGeneratorFactory.getEventGenerator(handlingUnit.getDomainId(), JDOUtils.getImplClass(IHandlingUnit.class).getName());
            try {
                eg.generate( IEvent.MODIFIED, null, String.valueOf(handlingUnit.getId()), null );
            } catch (EventHandlingException e) {
                xLogger.warn( "Exception when generating event for handling unit modification for hu {0} in domain {1}", handlingUnit.getId(), handlingUnit.getDomainId(), e);
            }*/
    } finally {
      if (tx.isActive()) {
        xLogger.warn("update handling unit: Rolling back transaction");
        tx.rollback();
      }
      xLogger.fine("Exiting update handling unit");
      pm.close();
    }
  }

  @Override
  public IHandlingUnit getHandlingUnit(Long handlingUnitId) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IHandlingUnit material = JDOUtils.getObjectById(IHandlingUnit.class, handlingUnitId, pm);
      material = pm.detachCopy(material);
      return material;
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("get handling unit: FAILED!!! Handling unit {0} does not exist in the database",
          handlingUnitId, e);
      throw new ServiceException(
          "Handling unit " + handlingUnitId + " " + backendMessages.getString("error.notfound"));
    } finally {
      pm.close();
    }
  }

  @Override
  public IHandlingUnit getHandlingUnitByName(Long domainId, String handlingUnitName)
      throws ServiceException {
    if (domainId == null || StringUtils.isEmpty(handlingUnitName)) {
      throw new ServiceException("Invalid parameters");
    }
    IHandlingUnit hu = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Form the query
      Query huQuery = pm.newQuery(JDOUtils.getImplClass(IHandlingUnit.class));
      huQuery.setFilter("dId.contains(dIdParam) && nName == nameParam");
      huQuery.declareParameters("Long dIdParam, String nameParam");
      try {
        List<IHandlingUnit>
            results =
            (List<IHandlingUnit>) huQuery.execute(domainId, handlingUnitName.toLowerCase());
        if (results != null && !results.isEmpty()) {
          hu = results.get(0);
          hu = pm.detachCopy(hu);
        }
      } finally {
        huQuery.closeAll();
      }
    } catch (Exception e) {
      xLogger.severe("Error while trying to get handling unit by name {0}", handlingUnitName, e);
    } finally {
      pm.close();
    }
    return hu;
  }

  @Override
  public List<IHandlingUnit> getHandlingUnitListByName(Long domainId, String handlingUnitName)
      throws ServiceException {
    if (domainId == null || StringUtils.isEmpty(handlingUnitName)) {
      throw new ServiceException("Invalid parameters");
    }
    List<IHandlingUnit> results = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Form the query
      Query huQuery = pm.newQuery(JDOUtils.getImplClass(IHandlingUnit.class));
      huQuery.setFilter("dId.contains(dIdParam) && nName == nameParam");
      huQuery.declareParameters("Long dIdParam, String nameParam");
      try {
        results = (List<IHandlingUnit>) huQuery.execute(domainId, handlingUnitName.toLowerCase());
        if (results != null && !results.isEmpty()) {
          results = (List<IHandlingUnit>) pm.detachCopyAll(results);
        }
      } finally {
        huQuery.closeAll();
      }
    } catch (Exception e) {
      xLogger
          .severe("Error while trying to get handling unit list by name {0}", handlingUnitName, e);
    } finally {
      pm.close();
    }
    return results;
  }

  @Override
  public void deleteHandlingUnit(Long domainId, List<Long> handlingUnitIds)
      throws ServiceException {

  }

  @Override
  public Results getAllHandlingUnits(Long domainId, PageParams pageParams) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      List<IHandlingUnit> hUnits = new ArrayList<>();
      String filters = "dId.contains(domainIdParam)";
      String declaration = "Long domainIdParam";
      Map<String, Object> params = new HashMap<>();
      params.put("domainIdParam", domainId);
      Query query = pm.newQuery(JDOUtils.getImplClass(IHandlingUnit.class));
      query.setFilter(filters);
      query.declareParameters(declaration);
      query.setOrdering("nName asc");
      String cursor = null;
      try {
        if (pageParams != null) {
          QueryUtil.setPageParams(query, pageParams);
        }
        hUnits = (List<IHandlingUnit>) query.executeWithMap(params);
        hUnits.size();
        cursor = QueryUtil.getCursor(hUnits);
        hUnits = (List<IHandlingUnit>) pm.detachCopyAll(hUnits);
      } finally {
        query.closeAll();
      }
      return new Results(hUnits, cursor);
    } finally {
      pm.close();
    }
  }

  @Override
  public Map<String, String> getHandlingUnitDataByMaterialId(Long materialId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    try {
      query =
          pm.newQuery("javax.jdo.query.SQL",
              "SELECT ID,(SELECT NAME FROM HANDLINGUNIT WHERE ID=HU_ID_OID) NAME, QUANTITY, HU_ID_OID FROM HANDLINGUNITCONTENT HUC WHERE HUC.TY = '0' AND HUC.CNTID = ?");
      List data = (List) query.executeWithArray(materialId);
      if (data == null || data.size() == 0) {
        return null;
      }
      Map<String, String> huMap = new HashMap<>(3);
      Object[] o = (Object[]) data.get(0);
      huMap.put(IHandlingUnit.NAME, String.valueOf(o[1]));
      huMap.put(IHandlingUnit.QUANTITY,
          new BigDecimal(String.valueOf(o[2])).stripTrailingZeros().toPlainString());
      huMap.put(IHandlingUnit.HUID, String.valueOf(o[3]));
      return huMap;
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
  }

  @Override
  public void init(Services services) throws ServiceException {

  }

  @Override
  public void destroy() throws ServiceException {

  }

  @Override
  public Class<? extends Service> getInterface() {
    return HandlingUnitServiceImpl.class;
  }

  @Override
  public Service clone() throws CloneNotSupportedException {
    return null;
  }
}
