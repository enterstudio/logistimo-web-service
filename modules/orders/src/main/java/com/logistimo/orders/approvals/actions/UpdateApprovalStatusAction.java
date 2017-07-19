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
import com.logistimo.dao.JDOUtils;
import com.logistimo.events.entity.IEvent;
import com.logistimo.exception.ValidationException;
import com.logistimo.models.StatusModel;
import com.logistimo.orders.actions.GenerateOrderEventsAction;
import com.logistimo.orders.approvals.builders.ApprovalsBuilder;
import com.logistimo.orders.approvals.constants.ApprovalConstants;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.utils.OrderVisibilityUtils;
import com.logistimo.orders.approvals.validations.ApprovalStatusUpdateRequesterValidator;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

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
  private GenerateOrderEventsAction generateOrderEventsAction;

  @Autowired
  private ApprovalStatusUpdateRequesterValidator approvalStatusUpdateRequesterValidator;

  public void invoke(StatusModel statusModel, String approvalId)
      throws ObjectNotFoundException, ValidationException, ServiceException {
    IOrderApprovalMapping orderApprovalMapping = approvalDao.getOrderApprovalMapping(approvalId,
        ApprovalConstants.PENDING);
    SecureUserDetails secureUserDetails = SecurityUtils.getUserDetails();
    validateReqeusterStatus(orderApprovalMapping, secureUserDetails, statusModel);
    UpdateApprovalRequest updateApprovalRequest = builder.buildUpdateApprovalRequest(statusModel);
    updateApprovalStatus(updateApprovalRequest, approvalId);
    approvalDao.updateOrderApprovalStatus(approvalId, statusModel, secureUserDetails.getUsername());
    if (statusModel.getStatus().equals(ApprovalConstants.APPROVED) && !orderApprovalMapping
        .getApprovalType().equals(IOrder.SALES_ORDER)) {
      setOrderVisibilityUponApproval(orderApprovalMapping.getOrderId());
      generateOrderEventsAction
          .invoke(null, IEvent.CREATED, orderApprovalMapping.getOrderId(), null, null, null);
    }
  }

  private void validateReqeusterStatus(IOrderApprovalMapping orderApprovalMapping,
                                       SecureUserDetails secureUserDetails,
                                       StatusModel statusModel)
      throws ValidationException {
    approvalStatusUpdateRequesterValidator.validate(orderApprovalMapping,
        secureUserDetails, statusModel);
  }

  private void setOrderVisibilityUponApproval(Long orderId)
      throws ObjectNotFoundException, ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    IOrder o;
    try {
      o = JDOUtils.getObjectById(IOrder.class, orderId, pm);
      OrderVisibilityUtils.setOrderVisibility(o, o.getDomainId(), true);
      pm.makePersistent(o);
    } catch (JDOObjectNotFoundException e) {
      throw new ObjectNotFoundException(e.getMessage());
    } finally {
      pm.close();
    }
  }

  protected void updateApprovalStatus(UpdateApprovalRequest request, String approvalId) {
    approvalsClient.updateApprovalRequest(request, approvalId);
  }
}
