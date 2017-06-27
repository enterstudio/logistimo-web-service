package com.logistimo.auth;

import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by charan on 22/06/17.
 */
public class GenericAuthoriser {

  public static final Integer NO_ACCESS = 0;
  public static final Integer VIEW_ACCESS = 1;
  public static final Integer MANAGE_MASTER_DATA = 2;

  private GenericAuthoriser() {
  }

  public static boolean authoriseUser(HttpServletRequest request, String userId)
      throws ServiceException {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    String rUserId = sUser.getUsername();
    String role = sUser.getRole();
    Long domainId = SecurityUtils.getDomainId(request);
    UsersService as = Services.getService(UsersServiceImpl.class, locale);
    return role.equals(SecurityConstants.ROLE_SUPERUSER) || as
        .hasAccessToUser(userId, rUserId, domainId, role);
  }

  public static boolean authoriseAdmin(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    String role = sUser.getRole();
    return SecurityUtil.compareRoles(role, SecurityConstants.ROLE_DOMAINOWNER) >= 0;
  }

  public static boolean authoriseSMS(String mobileNumber, String userMobileNumber, String userId,
                                     String tokenSuffix) throws ServiceException {
    boolean isAuthorised;

    //If token is present validate token else validate mobile number
    AuthenticationService as = Services.getService(AuthenticationServiceImpl.class);
    String token = as.getUserToken(userId);
    if (token != null) {
      isAuthorised = token.endsWith(tokenSuffix);
    } else {
      String tmpUserMobileNumber = userMobileNumber.replaceAll("[+ ]", "");
      isAuthorised = tmpUserMobileNumber.equals(mobileNumber);
    }

    return isAuthorised;
  }
}
