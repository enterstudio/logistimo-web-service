package com.logistimo.api.auth;

import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

/**
 * Created by charan on 09/03/17.
 */
public class AuthenticationUtil {

  public static IUserAccount authenticateToken(String authtoken, Integer actionInitiator)
      throws ServiceException, ObjectNotFoundException {
    AuthenticationService aus = Services.getService(AuthenticationServiceImpl.class, null);
    String userId = aus.authenticateToken(authtoken, actionInitiator);
    UsersService usersService = Services.getService(UsersServiceImpl.class);
    return usersService.getUserAccount(userId);
  }
}
