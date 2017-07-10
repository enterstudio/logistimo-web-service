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
