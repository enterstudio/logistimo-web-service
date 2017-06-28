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

package com.logistimo.orders.approvals.validations;

import com.logistimo.entities.auth.EntityAuthoriser;
import com.logistimo.exception.SystemException;
import com.logistimo.exception.ValidationException;
import com.logistimo.models.StatusModel;
import com.logistimo.orders.approvals.constants.ApprovalConstants;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.validations.Validator;

/**
 * Created by naveensnair on 23/06/17.
 */
public class ApprovalStatusUpdateRequesterValidator implements Validator{

  private final SecureUserDetails userDetails;
  private final IOrderApprovalMapping orderApprovalMapping;
  private final StatusModel statusModel;

  public ApprovalStatusUpdateRequesterValidator(IOrderApprovalMapping orderApprovalMapping,
                                                SecureUserDetails secureUserDetails,
                                                StatusModel statusModel) {
    this.userDetails = secureUserDetails;
    this.orderApprovalMapping = orderApprovalMapping;
    this.statusModel = statusModel;
  }

  /**
   * checks whether user is admin, user has access to respective kiosk
   * @throws ValidationException
   */
  @Override
  public void validate() throws ValidationException {
    if(statusModel.getStatus().equals(ApprovalConstants.CANCELLED)) {
        Long kioskId = orderApprovalMapping.getKioskId();
      try {
        if(!EntityAuthoriser.authoriseEntity(userDetails, kioskId)){
          throw new ValidationException("OA015", userDetails.getLocale(), kioskId);
        }
      } catch (ServiceException e) {
        throw new SystemException(e);
      }
    }
  }
}
