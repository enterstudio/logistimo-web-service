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

package com.logistimo.auth.service;

import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;

/**
 * Created by charan on 08/03/17.
 */
public interface AuthorizationService {

  /**
   * Authorise a user to perform transaction based on role based transaction type capabilities
   *
   * @param transType transaction type
   * @return true if authorised, false otherwise
   */
  boolean authoriseTransactionAccess(String transType, Long domainId, String userId)
      throws ServiceException, ObjectNotFoundException;

  /**
   * Authorise a user to perform add,edit,remove Entities or Users, checks capability "Allow creation of entities" of user
   * also restricts Read-only and Asset-only users to perform these operations
   *
   * @param userId-userId of the user trying to perform these actions
   * @return true if authorised, false otherwise
   * @throws ObjectNotFoundException if user not found
   */
  boolean authoriseUpdateKiosk(String userId, Long domainId)
      throws ServiceException, ObjectNotFoundException;

}
