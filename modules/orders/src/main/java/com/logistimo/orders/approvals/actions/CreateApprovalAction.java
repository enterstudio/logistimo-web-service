package com.logistimo.orders.approvals.actions;

import com.logistimo.approvals.client.IApprovalsClient;
import com.logistimo.approvals.client.models.Approver;
import com.logistimo.approvals.client.models.CreateApprovalRequest;
import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.exception.ValidationException;
import com.logistimo.logger.XLog;
import com.logistimo.orders.approvals.ApprovalType;
import com.logistimo.orders.approvals.builders.ApprovalsBuilder;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.models.ApprovalRequestModel;
import com.logistimo.orders.approvals.service.impl.ApprovalServiceImpl;
import com.logistimo.orders.approvals.utils.ApprovalUtils;
import com.logistimo.orders.approvals.validations.ApprovalRequesterValidator;
import com.logistimo.orders.approvals.validations.OrderApprovalStatusValidator;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Created by charan on 22/06/17.
 */
@Component
public class CreateApprovalAction {

  private static final XLog LOGGER = XLog.getLog(ApprovalServiceImpl.class);

  @Autowired
  private ApprovalsBuilder builder;

  @Autowired
  private IApprovalsClient approvalsClient;

  @Autowired
  private IApprovalsDao approvalDao;

  @Autowired
  private OrderManagementService oms;

  public CreateApprovalResponse invoke(ApprovalRequestModel approvalRequestModel)
      throws ServiceException, ObjectNotFoundException, ValidationException {

    Locale locale = SecurityUtils.getLocale();

    IOrder order = oms.getOrder(approvalRequestModel.getOrderId());

    validateApprovalRequest(approvalRequestModel, order, locale);

    List<Approver>
        approvers =
        ApprovalUtils.getApproversForOrderType(order, approvalRequestModel.getApprovalType());

    CreateApprovalRequest approvalRequest = builder.buildApprovalRequest(order,
        approvalRequestModel.getMessage(), approvalRequestModel.getRequesterId(), approvers);

    return createApproval(approvalRequest, approvalRequestModel.getApprovalType());

  }

  private void validateApprovalRequest(ApprovalRequestModel approvalRequest, IOrder order,
                                       Locale locale) throws ValidationException {
    new ApprovalRequesterValidator(approvalRequest, order,
        SecurityUtils.getUserDetails().getUsername(), locale).validate();
    new OrderApprovalStatusValidator(approvalRequest, order, locale).validate();
  }

  private CreateApprovalResponse createApproval(CreateApprovalRequest approvalRequest,
                                                ApprovalType approvalType)
      throws ServiceException {
    CreateApprovalResponse approvalResponse = null;
    try {
      approvalResponse = approvalsClient.createApproval(approvalRequest);
      approvalDao.updateOrderApprovalMapping(approvalResponse, approvalType.getValue());
    } catch (Exception e) {
      LOGGER.severe(
          "Error while propagating approval response {0} to order approval mapping for order type {1}",
          approvalResponse, approvalType, e);
      throw new ServiceException(
          "Error while creating approval for order : ", approvalRequest.getTypeId(), e);
    }
    return approvalResponse;
  }

  // approveRequest - Update approvals service
  // , Update order table, make visible to customer/vendor based on order type/approval type if necessary
  // , Update order_approval_mapping table
  // , Authorise whether user can approve this request ( Approvals service should make sure requester id is in approvers list)
  // , Enforce approval work flows in approvals_service ( once rejected, cannot be approved, etc )
  // , Fire a notification request ( Should this be based on approvals service trigger ), this could happen before comment.

  // cancelRequest
  // , Comments mandatory

  // rejectRequest
  // , Comments mandatory

  // commentOnApprovalRequest or sendCommentToConversation -- requires
  // - broadcast sms to all parties except for commenter

  // process expiry notification
  // - broadcast sms to all approvers for whom this is expired.

  protected CreateApprovalResponse createApproval(CreateApprovalRequest approvalRequest) {
    return approvalsClient.createApproval(approvalRequest);
  }

}
