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

package com.logistimo.users;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;

import com.logistimo.services.Resources;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.apache.commons.lang.StringUtils;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by charan on 09/03/17.
 */
public class UserUtils {

  private static final XLog xLogger = XLog.getLog(UserUtils.class);

  // Static display functions
  public static String getRoleDisplay(String role, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String displayStr = "";
    if (SecurityConstants.ROLE_KIOSKOWNER.equals(role)) {
      displayStr = messages.getString("role.kioskowner");
    } else if (SecurityConstants.ROLE_SERVICEMANAGER.equals(role)) {
      displayStr = messages.getString("role.servicemanager");
    } else if (SecurityConstants.ROLE_DOMAINOWNER.equals(role)) {
      displayStr = messages.getString("role.domainowner");
    } else if (SecurityConstants.ROLE_SUPERUSER.equals(role)) {
      displayStr = messages.getString("role.superuser");
    }

    return displayStr;
  }

  //Permission display
  public static String getPermissionDisplay(String permission, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String dispStr = "";
    if (IUserAccount.PERMISSION_VIEW.equals(permission)) {
      dispStr = messages.getString("user.permission.view");
    } else if (IUserAccount.PERMISSION_ASSET.equals(permission)) {
      dispStr = messages.getString("user.permission.asset");
    }
    return dispStr;
  }

  public static String getGenderDisplay(String constant, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String displayStr = "";
    if (IUserAccount.GENDER_MALE.equals(constant)) {
      displayStr = messages.getString("gender.male");
    } else if (IUserAccount.GENDER_FEMALE.equals(constant)) {
      displayStr = messages.getString("gender.female");
    }

    return displayStr;
  }

  // Get enabled userIds list from userIds list and userTags list
  public static List<String> getEnabledUniqueUserIds(Long domainId, String userIdsCSV,
                                                     String userTagsCSV) {
    List<String> userIds = null;
    List<String> uIds = null;
    List<String> uTIds = null;
    UsersService as;
    try {
      as = Services.getService(UsersServiceImpl.class, Locale.ENGLISH);
      if (StringUtils.isNotEmpty(userIdsCSV)) {
        uIds = as.getEnabledUserIds(Arrays.asList(userIdsCSV.split(",")));
      }
      if (StringUtils.isNotEmpty(userTagsCSV)) {
        uTIds = as.getEnabledUserIdsWithTags(Arrays.asList(userTagsCSV.split(",")), domainId);
      }

      Set<String> eUIds = new HashSet<>();
      if (uIds != null && uIds.size() > 0) {
        eUIds.addAll(uIds);
      }
      if (uTIds != null && uTIds.size() > 0) {
        eUIds.addAll(uTIds);
      }
      if (eUIds.size() > 0) {
        userIds = new ArrayList<>(eUIds);
      }

    } catch (ServiceException e) {
      xLogger.fine("Error while fetching userIds from userIds : {1} or userTags : {2}", userIdsCSV,
          userTagsCSV);
    }

    return userIds;
  }
}
