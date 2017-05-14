package com.logistimo.api.builders;

import org.apache.commons.lang.StringUtils;

import com.logistimo.entities.models.UserEntitiesModel;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.api.util.SessionMgr;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.api.models.CurrentUserModel;
import com.logistimo.api.util.SecurityUtils;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Mohan Raja on 19/03/15.
 */
public class CurrentUserBuilder {
  public CurrentUserModel buildCurrentUserModel(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
      DomainsService ds = Services.getService(DomainsServiceImpl.class, locale);
      String domainName = ds.getDomain(domainId).getName();
      UserEntitiesModel userEntitiesModel = as.getUserWithKiosks(userId);
      IUserAccount user = userEntitiesModel.getUserAccount();
      String loggedUserLng = user.getLanguage();
      CurrentUserModel model = new CurrentUserModel();
      if (StringUtils.isNotEmpty(user.getFullName())) {
        model.ufn = user.getFullName();
      } else {
        model.ufn = user.getFirstName();
      }
      model.unm = userId;
      model.dnm = domainName;
      model.lng = loggedUserLng;
      model.em = user.getEmail();
      model.eid = user.getPrimaryKiosk();
      if (model.eid == null) {
        List<IKiosk> kiosks;
        if (SecurityConstants.ROLE_SERVICEMANAGER.equals(sUser.getRole())) {
          kiosks = userEntitiesModel.getKiosks();
          if (kiosks != null && kiosks.size() == 1) {
            model.eid = kiosks.get(0).getKioskId();
          }
        }
      }
      return model;
    } catch (ServiceException | ObjectNotFoundException e) {
      throw new InvalidServiceException(backendMessages.getString("current.user.fetch.error"));
    }
  }
}
