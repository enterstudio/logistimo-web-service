package com.logistimo.orders.approvals.validations;

import com.logistimo.exception.ValidationException;
import com.logistimo.orders.approvals.constants.ApprovalConstants;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.models.ApprovalRequestModel;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charan on 18/07/17.
 */
@Component
public class CreateApprovalValidator {

  @Autowired
  private IApprovalsDao approvalDao;

  public void validate(ApprovalRequestModel approvalRequestModel) throws ValidationException {
    IOrderApprovalMapping orderApprovalMapping = approvalDao
        .getOrderApprovalMapping(approvalRequestModel.getOrderId());

    if (orderApprovalMapping != null && (orderApprovalMapping.getApprovalType()
        .compareTo(approvalRequestModel.getApprovalType().getValue()) == 0) &&
        (orderApprovalMapping.getStatus().equalsIgnoreCase(ApprovalConstants.PENDING) ||
            orderApprovalMapping.getStatus().equalsIgnoreCase(ApprovalConstants.APPROVED))) {
      throw new ValidationException("OA018", approvalRequestModel.getApprovalType(),
          orderApprovalMapping.getStatus());
    }
  }
}
