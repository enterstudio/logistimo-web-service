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

package com.logistimo.entities.utils;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.MsgUtil;

import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.CharacterConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The {@code EntityMoveHelper} class is a helper class for {@link EntityMover}.
 *
 * @author Mohan Raja
 * @see EntityMover
 */
@SuppressWarnings("unchecked")
public class EntityMoveHelper {

  /**
   * Validates whether any user {@code userIds} is associated with any entity other than {@code kIds},
   * which is selected to move to another domain.
   *
   * @param userIds set of user ids selected to move to another domain.
   * @param kIds    set of entity ids selected to move to another domain.
   * @return list of string with user name and associated entity names of user if that entity is
   * selected to move to another domain.
   * @throws ServiceException when there is error in fetching object.
   */
  public static List<String> validateUsers(Set<String> userIds, List<Long> kIds)
      throws ServiceException {
    List<String> userErrors = new ArrayList<String>();
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, null);
    StringBuilder missedKiosks = new StringBuilder();
    for (String userId : userIds) {
      List<Long> userKIds = as.getKioskIdsForUser(userId, null, null).getResults();
      if (userKIds!=null && !kIds.containsAll(userKIds)) {
        missedKiosks.append(MsgUtil.newLine()).append(MsgUtil.newLine());
        missedKiosks.append("User id: ").append(userId);
        missedKiosks.append(MsgUtil.newLine()).append("Missed entity:");
        for (Long userKId : userKIds) {
          if (!kIds.contains(userKId)) {
            IKiosk kiosk = as.getKiosk(userKId, false);
            missedKiosks.append(kiosk.getName()).append(CharacterConstants.COMMA);
          }
        }
        missedKiosks.setLength(missedKiosks.length() - 1);
        userErrors.add(missedKiosks.toString());
        missedKiosks.setLength(0);
      }
    }
    return userErrors;
  }

  /**
   * Extracts and returns set of unique user ids from list of {@code kiosks}
   *
   * @param kiosks list of kiosk object from which user id need to be extracted
   * @return set of unique user ids from list of {@code kiosks}
   */
  public static Set<String> extractUserIds(List<IKiosk> kiosks) {
    Set<String> userIds = new HashSet<String>();
    for (IKiosk kiosk : kiosks) {
      for (IUserAccount s : kiosk.getUsers()) {
        userIds.add(s.getUserId());
      }
    }
    return userIds;
  }
}
