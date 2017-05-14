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

package com.logistimo.auth.services.impl;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.service.AuthorizationService;
import com.logistimo.config.models.CapabilityConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by charan on 08/03/17.
 */
public class AuthorizationServiceImpl implements AuthorizationService {

  private static Map<String, String> trnscMap;

  static {
    trnscMap = new HashMap<>();
    trnscMap.put("i", "es");
    trnscMap.put("r", "er");
    trnscMap.put("p", "sc");
    trnscMap.put("w", "wa");
    trnscMap.put("t", "ts");
  }

  /**
   * Authorise a user to perform add,edit,remove Entities or Users, checks capability "Allow creation of entities" of user
   * also restricts Read-only and Asset-only users to perform these operations
   *
   * @param userId-userId of the user trying to perform these actions
   * @return true if authorised, false otherwise
   * @throws ObjectNotFoundException if user not found
   */
  public boolean authoriseUpdateKiosk(String userId, Long domainId)
      throws ServiceException, ObjectNotFoundException {
    UsersService as = Services.getService(UsersServiceImpl.class);
    String uRole = as.getUserAccount(userId).getRole();
    String permission = as.getUserAccount(userId).getPermission();
    if (!permission.equals(IUserAccount.PERMISSION_DEFAULT)) {
      return false;
    }
    DomainConfig dc = DomainConfig.getInstance(domainId);
    CapabilityConfig cConf = dc.getCapabilityByRole(uRole);
    List<String> creatableEnts;
    if (uRole.equals(SecurityConstants.ROLE_KIOSKOWNER) || uRole
        .equals(SecurityConstants.ROLE_SERVICEMANAGER)) {
      creatableEnts =
          (cConf != null) ? cConf.getCreatableEntityTypes() : dc.getCreatableEntityTypes();
      if (creatableEnts == null || !creatableEnts.contains(CapabilityConfig.TYPE_MANAGEDENTITY)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Authorise a user to perform transaction based on role based transaction type capabilities
   *
   * @param transType transaction type
   * @return true if authorised, false otherwise
   */
  public boolean authoriseTransactionAccess(String transType, Long domainId, String userId)
      throws ServiceException, ObjectNotFoundException {
    UsersService as = Services.getService(UsersServiceImpl.class);
    String uRole = as.getUserAccount(userId).getRole();

    DomainConfig dc = DomainConfig.getInstance(domainId);
    CapabilityConfig cConfig = dc.getCapabilityByRole(uRole);
    List<String> capabs;
    if (cConfig != null) {
      capabs = cConfig.getCapabilities();
    } else {
      capabs = dc.getTransactionMenus();
    }
    if (capabs == null) {
      return true;
    }

    String trnsc = trnscMap.get(transType);
    if (trnsc != null) {
      if (capabs.contains(trnsc)) {
        return false;
      }
    }
    return true;
  }

}
