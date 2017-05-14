package com.logistimo.api.auth;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;

import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.api.util.SecurityUtils;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Mohan Raja on 10/03/15
 */

public class Authoriser {
  public static final Integer NO_ACCESS = 0;
  public static final Integer VIEW_ACCESS = 1;
  public static final Integer MANAGE_MASTER_DATA = 2;
  private static Map<String, String> trnscMap;


  public static boolean authoriseEntity(HttpServletRequest request, Long entityId)
      throws ServiceException {
    return authoriseEntityPerm(request, entityId) > 0;
  }

  public static Integer authoriseEntityPerm(HttpServletRequest request, Long entityId)
      throws ServiceException {
    return authoriseEntityPerm(request, entityId, false);
  }

  public static boolean authoriseEntity(HttpServletRequest request, Long entityId,
                                        boolean isManagable) throws ServiceException {
    return authoriseEntityPerm(request, entityId, isManagable) > 0;
  }

  public static Integer authoriseEntityPerm(HttpServletRequest request, Long entityId,
                                            boolean isManagable) throws ServiceException {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    String role = sUser.getRole();
    if (SecurityConstants.ROLE_SUPERUSER.equals(role)) {
      return MANAGE_MASTER_DATA;
    }
    Locale locale = sUser.getLocale();
    String userId = sUser.getUsername();
    UsersService as = Services.getService(UsersServiceImpl.class, locale);
    try {
      IUserAccount account = as.getUserAccount(userId);
      List<Long> domainIds = account.getAccessibleDomainIds();
      if (domainIds != null && domainIds.size() > 0) {
        for (Long dId : domainIds) {
          Integer permission = authoriseEntityPerm(entityId, role, locale, userId, dId);
          if (permission > NO_ACCESS) {
            return permission;
          }
        }
      }
    } catch (ObjectNotFoundException e) {
      throw new ServiceException("User not found: " + userId);
    }

    return NO_ACCESS;
  }

  public static boolean authoriseEntity(Long entityId, String role, Locale locale, String userId,
                                        Long domainId) throws ServiceException {
    return authoriseEntityPerm(entityId, role, locale, userId, domainId) > 0;
  }

  public static Integer authoriseEntityPerm(Long entityId, String role, Locale locale,
                                            String userId, Long domainId) throws ServiceException {
    Integer permission = NO_ACCESS;
    if (SecurityConstants.ROLE_SUPERUSER.equals(role)) {
      return MANAGE_MASTER_DATA;
    }
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
        /*if(!as.getKiosk(entityId).getDomainIds().contains(domainId)) {
            return NO_ACCESS;
        }*/
    boolean hasPermission = as.hasAccessToKiosk(userId, entityId, domainId, role);
    if (hasPermission) {
      permission = MANAGE_MASTER_DATA;
    }
    // Todo: Need to authorize entity for manager by some other way like using query (If possible validate inside hasAccessTokiosk method itself)
    if (!hasPermission && (SecurityConstants.ROLE_SERVICEMANAGER.equals(role)
        || SecurityConstants.ROLE_KIOSKOWNER.equals(role))) {
      try {
        permission = as.hasAccessToKiosk(userId, entityId);

      } catch (Exception ignored) { // Proceed to return false
        // do nothing
      }
    }
    return permission;
  }

  public static boolean authoriseEntityDomain(HttpServletRequest request, Long entityId)
      throws ServiceException {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    String role = sUser.getRole();
    Long domainId = SecurityUtils.getDomainId(request);
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
    return role.equals(SecurityConstants.ROLE_SUPERUSER) || as.getKiosk(entityId).getDomainIds()
        .contains(domainId);
  }


  public static boolean authoriseUser(HttpServletRequest request, String userId)
      throws ServiceException {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    String rUserId = sUser.getUsername();
    String role = sUser.getRole();
    Long domainId = SecurityUtils.getDomainId(request);
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
    return role.equals(SecurityConstants.ROLE_SUPERUSER) || as
        .hasAccessToUser(userId, rUserId, domainId, role);
  }

  public static boolean authoriseAdmin(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    String role = sUser.getRole();
    return SecurityUtil.compareRoles(role, SecurityConstants.ROLE_DOMAINOWNER) >= 0;
  }

  public static boolean authoriseInventoryAccess(HttpServletRequest request, Long entityId)
      throws ServiceException {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    EntitiesService as = Services.getService(EntitiesServiceImpl.class);
    if (SecurityUtil.compareRoles(sUser.getRole(), SecurityConstants.ROLE_DOMAINOWNER) < 0 && !as
        .hasKiosk(sUser.getUsername(), entityId)) {
      Results results = as.getKioskIdsForUser(sUser.getUsername(), null, null);
      if (results != null && results.getSize() > 0) {
        List<Long> userEntities = results.getResults();
        for (Long userEntityId : userEntities) {
          if (as.hasKioskLink(userEntityId, IKioskLink.TYPE_VENDOR, entityId) || as
              .hasKioskLink(userEntityId, IKioskLink.TYPE_CUSTOMER, entityId)) {
            return true;
          }
        }
      }
      return false;
    }
    return true;
  }



  public static boolean authoriseSMS(String mobileNumber, String userMobileNumber, String userId,
                                     String tokenSuffix) throws Exception {
    userMobileNumber = userMobileNumber.replaceAll("[+ ]", "");
    boolean isAuthorised = userMobileNumber.equals(mobileNumber);
    if (!isAuthorised) {
      AuthenticationService as = Services.getService(AuthenticationServiceImpl.class);
      String token = as.getUserToken(userId);
      if (token != null) {
        isAuthorised = token.endsWith(tokenSuffix);
      }
    }
    return isAuthorised;
  }
}
