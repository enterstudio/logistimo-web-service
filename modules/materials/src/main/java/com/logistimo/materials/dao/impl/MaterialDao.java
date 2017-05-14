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

package com.logistimo.materials.dao.impl;

import com.logistimo.dao.DaoFactory;

import com.logistimo.materials.dao.IMaterialDao;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.entity.Material;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.impl.PMF;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.utils.QueryUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 13/02/15.
 */
public class MaterialDao implements IMaterialDao {

  private static final XLog xLogger = XLog.getLog(MaterialDao.class);

  private ITagDao tagDao = new TagDao();

  @Override
  public String getKeyString(Long materialId) {
    return String.valueOf(materialId);
  }

  @Override
  public List<IMaterial> getMaterialByName(Long domainId, String materialName) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(Material.class);
    query.setFilter("dId.contains(domainIdParam) && uName == unameParam");
    query.declareParameters("Long domainIdParam, String unameParam");
    List<IMaterial> results = null;
    try {
      results = (List<IMaterial>) query.execute(domainId, materialName);
      if (results != null) {
        results = (List<IMaterial>) pm.detachCopyAll(results);
      }
    } catch (Exception e) {
      xLogger.warn("Error while fetching materials with name {0}", materialName, e);
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return results;
  }

  @Override
  public boolean checkCustomIdExists(Long domainId, String customId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Check if another material by the same custom ID exists in the database
    Query query = pm.newQuery(Material.class);
    query.setFilter("dId.contains(domainIdParam) && cId == cidParam");
    query.declareParameters("Long domainIdParam, String cidParam");
    boolean customIdExists = false;
    try {
      @SuppressWarnings("unchecked")
      List<IMaterial> results = (List<IMaterial>) query.execute(domainId, customId);
      if (results != null && results.size() == 1) {
        // Material with this name already exists in the database!
        customIdExists = true;
      }
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    xLogger.fine("Exiting checkIfCustomIdExists");
    return customIdExists;
  }

  @Override
  public Results searchMaterialsNoHU(Long domainId, String q) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    try {
      query =
          pm.newQuery("javax.jdo.query.SQL",
              "SELECT * FROM MATERIAL M, MATERIAL_DOMAINS MD WHERE MD.MATERIALID_OID = M.MATERIALID AND MD.DOMAIN_ID="
                  + domainId +
                  " AND UNAME LIKE '" + q.toLowerCase()
                  + "%' AND NOT EXISTS(SELECT 1 FROM HANDLINGUNITCONTENT WHERE CNTID = MATERIALID AND TY=0) LIMIT 8");
      query.setClass(Material.class);
      List<Material> data = (List<Material>) query.execute();
      if (data != null) {
        data = (List<Material>) pm.detachCopyAll(data);
        return new Results(data, null);
      }
    } catch (Exception e) {
      xLogger.severe(
          "Error while fetching material with no handling unit from domain {0} and query {1}",
          domainId, q, e);
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
    return null;
  }

  @Override
  public Results getAllMaterials(Long domainId, String tag, PageParams pageParams) {
    xLogger.fine("Entering getAllMaterials");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IMaterial> materials = new ArrayList<IMaterial>();
    // Formulate query
    String filters = "dId.contains(domainIdParam)";
    String declaration = "Long domainIdParam";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("domainIdParam", domainId);
    if (tag != null) {
      filters += " && tgs.contains(tagsParam)";
      declaration += ", Long tagsParam";
      params.put("tagsParam", tagDao.getTagFilter(tag, ITag.MATERIAL_TAG));
    }
    Query query = pm.newQuery(Material.class);
    query.setFilter(filters);
    query.declareParameters(declaration);
    query.setOrdering("uName asc");
    if (pageParams != null) {
      QueryUtil.setPageParams(query, pageParams);
    }
    String cursor = null;
    try {
      materials = (List<IMaterial>) query.executeWithMap(params);
      materials
          .size(); // TODO - fix to avoid "object manager closed" exception; find some other method to do this without retrieving all entities
      cursor = QueryUtil.getCursor(materials);
      materials = (List<IMaterial>) pm.detachCopyAll(materials);
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }

    xLogger.fine("Exiting getAllMaterials");
    return new Results(materials, cursor);
  }

  @Override
  public List<Long> getAllMaterialsIds(Long domainId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<Long> materialIds = new ArrayList<>(1);
    try {
      query =
          pm.newQuery("javax.jdo.query.SQL",
              "SELECT MATERIALID FROM MATERIAL M, MATERIAL_DOMAINS MD WHERE " +
                  "MD.MATERIALID_OID = M.MATERIALID AND MD.DOMAIN_ID=" + domainId);
      List matIds = (List) query.execute();
      for (Object matId : matIds) {
        Long matIdL = (Long) matId;
        if (matIdL != null) {
          materialIds.add(matIdL);
        }
      }
      return materialIds;
    } catch (Exception e) {
      xLogger.severe("Error while fetching material ids from domain {0} and query {1}", domainId,
          query, e);
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
    return null;

  }

  @Override
  public boolean checkMaterialExists(Long domainId, String materialName) {
    List<IMaterial> results = getMaterialByName(domainId, materialName);
    if (results != null && results.size() > 0) {
      return true;
    }
    return false;
  }

}

