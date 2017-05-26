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
public class SecurityConstants {
  public static final String ROLE_DOMAINOWNER = "ROLE_do";
  // User roles
  // NOTE: ROLE_ is a prefix to each role value, given Spring Security expects such a prefix for role values
  public static final String ROLE_KIOSKOWNER = "ROLE_ko";
  public static final String ROLE_SERVICEMANAGER = "ROLE_sm";
  public static final String ROLE_SUPERUSER = "ROLE_su";
  // All roles
  public static final String[] ROLES = {ROLE_KIOSKOWNER, ROLE_SERVICEMANAGER, ROLE_DOMAINOWNER,
      ROLE_SUPERUSER};
}
