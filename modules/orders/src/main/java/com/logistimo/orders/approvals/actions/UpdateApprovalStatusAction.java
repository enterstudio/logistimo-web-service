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

package com.logistimo.orders.approvals.actions;

import com.logistimo.approvals.client.IApprovalsClient;
import com.logistimo.approvals.client.models.UpdateApprovalRequest;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.exception.ValidationException;
import com.logistimo.models.StatusModel;
import com.logistimo.orders.approvals.builders.ApprovalsBuilder;
import com.logistimo.orders.approvals.constants.ApprovalConstants;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.validations.ApprovalStatusUpdateRequesterValidator;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by naveensnair on 23/06/17.
 */
@Component
public class UpdateApprovalStatusAction {

  @Autowired
  private ApprovalsBuilder builder;

  @Autowired
  private IApprovalsClient approvalsClient;

  @Autowired
  private IApprovalsDao approvalDao;

  @Autowired
  private OrderManagementService oms;

  public void invoke(StatusModel statusModel, String approvalId)
      throws ObjectNotFoundException, ValidationException {
    IOrderApprovalMapping orderApprovalMapping = approvalDao.getOrderApprovalMapping(approvalId,
        ApprovalConstants.PENDING);
    SecureUserDetails secureUserDetails = SecurityUtils.getUserDetails();
    validateReqeusterStatus(orderApprovalMapping, secureUserDetails, statusModel);
    UpdateApprovalRequest updateApprovalRequest = builder.buildUpdateApprovalRequest(statusModel);
    updateApprovalStatus(updateApprovalRequest, approvalId);
    approvalDao.updateOrderApprovalStatus(approvalId, statusModel, secureUserDetails.getUsername());
    oms.updateOrderVisibility(orderApprovalMapping.getOrderId());
  }

  private void validateReqeusterStatus(IOrderApprovalMapping orderApprovalMapping,
                                       SecureUserDetails secureUserDetails,
                                       StatusModel statusModel)
      throws ValidationException {
    new ApprovalStatusUpdateRequesterValidator(orderApprovalMapping,
        secureUserDetails, statusModel).validate();
  }

  protected void updateApprovalStatus(UpdateApprovalRequest request, String approvalId) {
    approvalsClient.updateApprovalRequest(request, approvalId);
  }
}
