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

package com.logistimo.api.controllers;

import com.logistimo.AppFactory;
import com.logistimo.api.builders.MaterialBuilder;
import com.logistimo.api.models.MaterialModel;
import com.logistimo.api.util.SearchUtil;
import com.logistimo.auth.GenericAuthoriser;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.models.ICounter;
import com.logistimo.pagination.Navigator;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.Counter;
import com.logistimo.utils.MsgUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/materials")
public class MaterialsController {
  private static final XLog xLogger = XLog.getLog(MaterialsController.class);
  private static final String CREATEENTITY_TASK_URL = "/task/createentity";
  MaterialBuilder mBuilder = new MaterialBuilder();

  @RequestMapping(value = "/delete", method = RequestMethod.POST)
  public
  @ResponseBody
  String delete(@RequestBody String materialIds, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    StringBuffer matNames = new StringBuffer();
    if (materialIds == null || materialIds.trim().equals("")) {
      throw new InvalidServiceException(backendMessages.getString("materials.delete.none"));
    }
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());

    String[] materialsArray = materialIds.split(",");
    Map<String, String> params = new HashMap<String, String>(5);
    params.put("action", "remove");
    params.put("type", "materials");
    params.put("domainid", String.valueOf(domainId));
    params.put("execute", "true"); // now add the "execute" indicator here
    for (String mat : materialsArray) {
      params.put("materialids", mat);
      try {
        MaterialCatalogService
            materialCatalogService =
            Services.getService(MaterialCatalogServiceImpl.class, locale);
        IMaterial m = materialCatalogService.getMaterial(Long.parseLong(mat));
        matNames.append(m.getName())
            .append(",");//we are getting material object only to log the material name
        AppFactory.get().getTaskService()
            .schedule(ITaskService.QUEUE_DEFAULT, CREATEENTITY_TASK_URL, params,
                ITaskService.METHOD_POST);
      } catch (Exception e) {
        xLogger.warn("{0} when scheduling task to delete material {1} in domain {2}: {3}",
            e.getClass().getName(), mat, domainId, e.getMessage());
      }
    }
    if (matNames.length() > 0) {
      matNames.setLength(matNames.length() - 1);//for auditlog remove last comma
    }
    xLogger.info("AUDITLOG\t{0}\t{1}\tMATERIAL\t " +
        "DELETE\t{2}\t{3}", domainId, sUser.getUsername(), materialIds, matNames.toString());
    return backendMessages.getString("schedule.task.remove.success") + " " + materialsArray.length
        + " " + backendMessages.getString("materials.lowercase") + ". " + backendMessages
        .getString("materials.removal.time");
  }

  @RequestMapping(value = "/material/{materialId}", method = RequestMethod.GET)
  public
  @ResponseBody
  MaterialModel getMaterialById(@PathVariable Long materialId, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    MaterialCatalogServiceImpl materialCatalogService;
    IUserAccount cb = null;
    IUserAccount ub = null;
    try {
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      materialCatalogService = Services.getService(MaterialCatalogServiceImpl.class, locale);
      IMaterial m = materialCatalogService.getMaterial(materialId);
      Map<Long, String> domainNames = new HashMap<>(1);
      if (m.getCreatedBy() != null) {
        try {
          cb = as.getUserAccount(m.getCreatedBy());
        } catch (Exception e) {
          //ignore
        }
      }
      if (m.getLastUpdatedBy() != null) {
        try {
          ub = as.getUserAccount(m.getLastUpdatedBy());
        } catch (Exception e) {
          //ignore
        }
      }
      return mBuilder.buildMaterialModel(m, cb, ub, sUser, 1, domainNames);
    } catch (ServiceException e) {
      xLogger.warn("Error Fetching Material details for " + materialId, e);
      throw new InvalidServiceException(
          backendMessages.getString("material.detail.fetch.error") + " " + materialId);
    }
  }

  @RequestMapping("/check/")
  public
  @ResponseBody
  boolean checkMaterialExist(@RequestParam String mnm, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    MaterialCatalogServiceImpl ims;
    try {
      ims = Services.getService(MaterialCatalogServiceImpl.class, locale);
      IMaterial m = ims.getMaterialByName(domainId, mnm);
      if (m != null) {
        return true;
      }
    } catch (ServiceException e) {
      xLogger.warn("Error Fetching Material details for name " + mnm, e);
      throw new InvalidServiceException(
          backendMessages.getString("material.detail.fetch.error") + " " + mnm);
    }
    return false;
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getDomainMaterials(
      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) boolean ihu,
      @RequestParam(required = false) String entityId,
      HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    try {
      MaterialCatalogService
          mc =
          Services.getService(MaterialCatalogServiceImpl.class, user.getLocale());
      Navigator
          navigator =
          new Navigator(request.getSession(), "MaterialsController.getDomainMaterials", offset,
              size, "dummy", 0);
      PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
      Results results;
      if (StringUtils.isNotBlank(q)) {
        if (ihu) {
          results = mc.searchMaterialsNoHU(domainId, q);
        } else {
          results = SearchUtil.findMaterials(domainId, q, pageParams);
        }
      } else {
        results = mc.getAllMaterials(domainId, tag, pageParams);
      }
      if (StringUtils.isBlank(q)) {
        ICounter counter = Counter.getMaterialCounter(domainId, tag);
        if (counter.getCount() > results.getSize()) {
          results.setNumFound(counter.getCount());
        } else {
          results.setNumFound(results.getSize());
        }
      } else {
        results.setNumFound(-1);
      }
      results.setOffset(offset);
      navigator.setResultParams(results);
      if (StringUtils.isNotBlank(entityId)) {
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class, user.getLocale());
        Results eResults = ims.getInventoryByKiosk(Long.valueOf(entityId), null);
        EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
        IKiosk k = as.getKiosk(Long.valueOf(entityId));
        return mBuilder.buildMaterialModelListWithEntity(results, user, eResults, domainId, k);
      } else {
        return mBuilder.buildMaterialModelList(results, user, domainId);
      }
    } catch (ServiceException e) {
      xLogger.warn("Error Fetching Material details for domain " + domainId, e);
      throw new InvalidServiceException(
          backendMessages.getString("material.detail.fetch.domain.error") + " " + domainId);
    }
  }

  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public
  @ResponseBody
  String create(@RequestBody MaterialModel materialModel, HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    MaterialCatalogServiceImpl materialCatalogService;
    IMaterial m = mBuilder.buildMaterial(materialModel);
    m.setCreatedBy(user.getUsername());
    m.setLastUpdatedBy(user.getUsername());
    try {
      materialCatalogService = Services.getService(MaterialCatalogServiceImpl.class, locale);
      if (m.getName() != null) {
        long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
        IMaterial temp = materialCatalogService.getMaterialByName(domainId, m.getName());
        if (temp != null) {
          throw new InvalidDataException(
              backendMessages.getString("material.uppercase") + " " + m.getName() + " "
                  + backendMessages.getString("error.alreadyexists"));
        } else {
          materialCatalogService.addMaterial(domainId, m);
          xLogger.info("AUDITLOG\t {0}\t {1}\t MATERIAL\t " +
              "CREATE \t {2} \t {3}", domainId, user.getUsername(), m.getMaterialId(), m.getName());
        }
      } else {
        throw new InvalidDataException(backendMessages.getString("material.no.name"));
      }
    } catch (ServiceException e) {
      xLogger.warn("Error creating material " + m.getMaterialId());
      throw new InvalidServiceException(
          backendMessages.getString("material.create.error") + " " + MsgUtil.bold(m.getName()) +
              MsgUtil.addErrorMsg(e.getMessage()));
    }
    return backendMessages.getString("material.uppercase") + " " + MsgUtil.bold(materialModel.mnm)
        + " " + backendMessages.getString("create.success");
  }

  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateMaterial(@RequestBody MaterialModel materialModel, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    MaterialCatalogServiceImpl materialCatalogService;
    InventoryManagementService ims;
    IMaterial m = mBuilder.buildMaterial(materialModel);
    m.setLastUpdatedBy(sUser.getUsername());
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    try {
      materialCatalogService = Services.getService(MaterialCatalogServiceImpl.class, locale);
      IMaterial mat = materialCatalogService.getMaterial(m.getMaterialId());
      if (mat.isBatchEnabled() != m.isBatchEnabled()) {
        ims = Services.getService(InventoryManagementServiceImpl.class);
        if(!ims.validateMaterialBatchManagementUpdate(m.getMaterialId())) {
          return null;
        }
      }
      if (m.getName() != null) {
        materialCatalogService.updateMaterial(m, domainId);
        xLogger.info("AUDITLOG\t{0}\t{1}\tMATERIAL\t " +
            "UPDATE\t{2}\t{3}", domainId, sUser.getUsername(), m.getMaterialId(), m.getName());
      } else {
        throw new InvalidDataException(backendMessages.getString("material.no.name"));
      }
    } catch (Exception e) {
      xLogger.warn("Error updating material" + m.getMaterialId(), e);
      throw new InvalidServiceException(
          backendMessages.getString("material.update.error") + " " + MsgUtil.bold(m.getName()) +
              MsgUtil.addErrorMsg(e.getMessage()));
    }
    return backendMessages.getString("material.uppercase") + " " + MsgUtil.bold(materialModel.mnm)
        + " " + backendMessages.getString("updated.successfully.lowercase");
  }
}
