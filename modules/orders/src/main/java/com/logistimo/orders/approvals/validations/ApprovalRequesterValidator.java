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

import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.entities.auth.EntityAuthoriser;
import com.logistimo.exception.ValidationException;
import com.logistimo.orders.approvals.ApprovalType;
import com.logistimo.orders.approvals.models.ApprovalRequestModel;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.ServiceException;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Created by charan on 22/06/17.
 */
@Component
public class ApprovalRequesterValidator {

  public void validate(ApprovalRequestModel approvalRequestModel, IOrder order,
                       String userId, Locale locale) throws ValidationException {

    if (!approvalRequestModel.getRequesterId().equals(userId)) {
      throw new ValidationException("OA005", locale, approvalRequestModel.getRequesterId(), userId);
    }

    try {
      boolean hasAccessToCustomer =
          EntityAuthoriser.authoriseEntityPerm(SecurityUtils.getUserDetails(), order.getKioskId())
              > 0;
      boolean hasAccessToVendor = EntityAuthoriser.authoriseEntityPerm(
          SecurityUtils.getUserDetails(), order.getServicingKiosk()) > 0;
      if (approvalRequestModel.getApprovalType() == null) {
        throw new ValidationException("OA017", new Object[0]);
      }
      if (approvalRequestModel.getApprovalType().equals(ApprovalType.TRANSFERS) && !(
          hasAccessToCustomer && hasAccessToVendor)) {
        throw new ValidationException("OA006", locale, userId);
      } else if (approvalRequestModel.getApprovalType().equals(ApprovalType.SALES_ORDER)
          && !hasAccessToVendor) {
        throw new ValidationException("OA007", locale, userId);
      } else if (approvalRequestModel.getApprovalType().equals(ApprovalType.PURCHASE_ORDER)
          && !hasAccessToCustomer) {
        throw new ValidationException("OA008", locale, userId);
      }
    } catch (ServiceException e) {
      throw new ValidationException(e);
    }

  }
}
