package com.logistimo.api.controllers;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.dao.JDOUtils;

import org.apache.commons.lang.StringUtils;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.logger.XLog;
import com.logistimo.api.builders.PoolGroupBuilder;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IPoolGroup;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.api.models.EntityGroupModel;
import com.logistimo.utils.MsgUtil;
import com.logistimo.api.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * @author charan
 */
@Controller
@RequestMapping("/ent-grps")
public class EntityGroupsController {

  private static final XLog xLogger = XLog.getLog(EntityGroupsController.class);
  PoolGroupBuilder builder = new PoolGroupBuilder();

  @RequestMapping("/")
  public
  @ResponseBody
  Results getEntityGroups(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());

    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, sUser.getLocale());
      List<IPoolGroup> poolGroups = as.findAllPoolGroups(domainId, 1, 0);
      int count = 0;
      List<EntityGroupModel> models = new ArrayList<EntityGroupModel>(1);
      for (IPoolGroup pg : poolGroups) {
        if (SecurityUtil.compareRoles(sUser.getRole(), SecurityConstants.ROLE_DOMAINOWNER) < 0
            && !sUser.getUsername().equals(pg.getOwnerId())) {
          continue;
        }
        EntityGroupModel model = new EntityGroupModel();
        model.sno = ++count;
        model.id = pg.getGroupId();
        model.nm = pg.getName();
        model.ct = pg.getCity();
        model.dt = pg.getDistrict();
        model.st = pg.getState();
        model.cnt = pg.getCountry();
        model.uid = pg.getOwnerId();
        model.num = pg.getKiosks() != null ? pg.getKiosks().size() : 0;
        model.t = pg.getTimeStamp();
        model.updOn = LocalDateUtil.format(pg.getTimeStamp(), locale, sUser.getTimezone());
        models.add(model);
      }
      return new Results(models, null, count, 0);
    } catch (ServiceException e) {
      xLogger.severe("Error in fetching entity groups", e);
      throw new InvalidServiceException(backendMessages.getString("poolgroup.fetch.error"));
    }
  }

  @RequestMapping(value = "/action/{action}", method = RequestMethod.POST)
  public
  @ResponseBody
  String create(@RequestBody EntityGroupModel entityGroupModel, @PathVariable String action,
                HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (entityGroupModel == null) {
      throw new BadRequestException(backendMessages.getString("poolgroup.create.error"));
    }
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    if (domainId == null) {
      xLogger.severe("Error in creating entity group");
      throw new InvalidServiceException(backendMessages.getString("poolgroup.create.error"));
    }
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, sUser.getLocale());
      IPoolGroup poolGroup = JDOUtils.createInstance(IPoolGroup.class);
      if (StringUtils.isNotBlank(entityGroupModel.nm)) {
        poolGroup.setName(entityGroupModel.nm);
      }
      if (StringUtils.isNotBlank(entityGroupModel.dsc)) {
        poolGroup.setDescription(entityGroupModel.dsc);
      }
      if (StringUtils.isNotBlank(entityGroupModel.uid)) {
        poolGroup.setOwnerId(entityGroupModel.uid);
      }
      if (entityGroupModel.ent != null && entityGroupModel.ent.size() > 0) {
        String[] kiosksArr = new String[entityGroupModel.ent.size()];
        for (int i = 0; i < entityGroupModel.ent.size(); i++) {
          kiosksArr[i] = String.valueOf(entityGroupModel.ent.get(i).id);
        }
        List<IKiosk> Kiosks = new ArrayList<IKiosk>();
        for (String k : kiosksArr) {
          Kiosks.add(as.getKiosk(Long.parseLong(k.trim()), false));
        }
        poolGroup.setKiosks(Kiosks);
      }
      poolGroup.setUpdatedBy(sUser.getUsername());
      if (action.equalsIgnoreCase("add")) {
        poolGroup.setCreatedBy(sUser.getUsername());
        as.addPoolGroup(domainId, poolGroup);
      } else if (action.equalsIgnoreCase("update")) {
        if (StringUtils.isNotEmpty(entityGroupModel.id.toString())) {
          poolGroup.setGroupId(entityGroupModel.id);
        }
        poolGroup.setDomainId(domainId);
        as.updatePoolGroup(poolGroup);
        return backendMessages.getString("poolgroup") + ' ' + MsgUtil.bold(entityGroupModel.nm)
            + ' ' + backendMessages.getString("update.success") + '.';
      }
      return backendMessages.getString("poolgroup") + ' ' + MsgUtil.bold(entityGroupModel.nm) + ' '
          + backendMessages.getString("create.success") + '.';
    } catch (ServiceException e) {
      if (action.equalsIgnoreCase("add")) {
        xLogger.severe("Error in creating entity group", e);
        throw new InvalidServiceException(backendMessages.getString("poolgroup.create.error"));
      } else {
        xLogger.severe("Error in updating entity group", e);
        throw new InvalidServiceException(backendMessages.getString("poolgroup.update.error"));
      }
    }
  }

  @RequestMapping(value = "/groupId/{groupId}", method = RequestMethod.GET)
  public
  @ResponseBody
  EntityGroupModel getPoolGroup(@PathVariable Long groupId, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    if (domainId == null) {
      xLogger.severe("Error in fetching entity groups");
      throw new InvalidServiceException(backendMessages.getString("poolgroup.fetch.error"));
    }
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, sUser.getLocale());
      String timeZone = sUser.getTimezone();
      EntityGroupModel entityGroupModel = null;
      IPoolGroup pg = as.getPoolGroup(groupId);
      if (pg != null) {
        entityGroupModel = builder.buildPoolGroupModel(pg, locale, timeZone);
      }
      return entityGroupModel;
    } catch (ServiceException e) {
      xLogger.severe("Error in fetching entity groups", e);
      throw new InvalidServiceException(backendMessages.getString("poolgroup.fetch.error"));
    }
  }

  @RequestMapping(value = "/delete", method = RequestMethod.POST)
  public
  @ResponseBody
  String deletePoolGroups(@RequestBody String groupIds, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (groupIds == null) {
      throw new BadRequestException(backendMessages.getString("poolgroup.delete.error"));
    }
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    if (domainId == null) {
      xLogger.severe("Error in deleting entity group");
      throw new InvalidServiceException(backendMessages.getString("poolgroup.delete.error"));
    }
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, sUser.getLocale());
      String[] pgIds = groupIds.split(",");
      ArrayList<Long> poolGroupIDs = new ArrayList<Long>();
      for (String pgID : pgIds) {
        poolGroupIDs.add(Long.parseLong(pgID.trim()));
      }
      as.deletePoolGroups(domainId, poolGroupIDs);
      return backendMessages.getString("poolgroup.delete.success");
    } catch (ServiceException e) {
      xLogger.severe("Error in deleting entity groups", e);
      throw new InvalidServiceException(backendMessages.getString("poolgroup.delete.error"));
    }
  }
}
