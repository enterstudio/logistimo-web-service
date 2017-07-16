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

package com.logistimo.api.servlets.mobile.builders;

import com.logistimo.proto.MobileUserModel;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vani on 28/06/17.
 */
public class MobileUserBuilder {
  /**
   * Builds a list of user models as required by the mobile from a list of user account objects
   */
  public List<MobileUserModel> buildMobileUserModels(List<IUserAccount> users) {
    if (users == null || users.isEmpty()) {
      return null;
    }
    List<MobileUserModel> userModels = new ArrayList<>(users.size());
    for (IUserAccount user : users) {
      MobileUserModel userModel = buildMobileUserModel(user);
      userModels.add(userModel);
    }
    return userModels;
  }

  private MobileUserModel buildMobileUserModel(IUserAccount user) {
    MobileUserModel mobileUserModel = new MobileUserModel();
    mobileUserModel.uid = user.getUserId();
    mobileUserModel.mob = user.getMobilePhoneNumber();
    mobileUserModel.eml = user.getEmail();
    mobileUserModel.n = user.getFullName();
    return mobileUserModel;
  }

  /**
   * Constructs a list of UserAccount objects from a list of user ids
   */
  public List<IUserAccount> constructUserAccount(UsersService as, List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return null;
    }
    List<IUserAccount> list = new ArrayList<>(userIds.size());
    for (String userId : userIds) {
      try {
        list.add(as.getUserAccount(userId));
      } catch (Exception ignored) {
        // do nothing
      }
    }
    return list;
  }
}
