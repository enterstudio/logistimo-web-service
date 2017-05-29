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

/**
 * Created by charan on 09/03/17.
 */
public class SecurityUtil {
  // Compare roles to determine which has more privileges
  // Returns 0, if both roles have same privilege
  //		   1, if role1 has more privilege than role2
  //		  -1, if role1 has lesser privilege than role2
  public static int compareRoles(String role1, String role2) {
    if (role1.equals(role2)) {
      return 0;
    }
    if (SecurityConstants.ROLE_SUPERUSER.equals(role1)) {
      return 1;
    } else if (SecurityConstants.ROLE_DOMAINOWNER.equals(role1)) {
      if (SecurityConstants.ROLE_SUPERUSER.equals(role2)) {
        return -1;
      } else {
        return 1;
      }
    } else if (SecurityConstants.ROLE_SERVICEMANAGER.equals(role1)) {
      if (SecurityConstants.ROLE_KIOSKOWNER.equals(role2)) {
        return 1;
      } else {
        return -1;
      }
    } else if (SecurityConstants.ROLE_KIOSKOWNER.equals(role1)) {
      return -1;
    } else {
      return 0;
    }
  }
}
