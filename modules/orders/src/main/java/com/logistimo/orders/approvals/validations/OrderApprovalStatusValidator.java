package com.logistimo.orders.approvals.validations;

import com.logistimo.exception.ValidationException;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.approvals.ApprovalType;
import com.logistimo.orders.approvals.models.ApprovalRequestModel;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.validations.Validator;

import java.util.Locale;

/**
 * Created by charan on 22/06/17.
 */
public class OrderApprovalStatusValidator implements Validator {

  private final ApprovalRequestModel approvalRequestModel;
  private final IOrder order;
  private final Locale locale;

  public OrderApprovalStatusValidator(ApprovalRequestModel approvalRequestModel, IOrder order,
                                      Locale locale) {
    this.approvalRequestModel = approvalRequestModel;
    this.order = order;
    this.locale = locale;
  }

  public void validate() throws ValidationException {
    ApprovalType approvalType = approvalRequestModel.getApprovalType();
    if (ApprovalType.PURCHASE_ORDER.equals(approvalType)) {
      if (!(order.getStatus().equals(IOrder.PENDING) || order.getStatus()
          .equals(IOrder.CONFIRMED))) {
        throw new ValidationException("OA001", order.getIdString(),
            OrderUtils.getStatusDisplay(order.getStatus(), locale));
      } else if (!(order.isVisibleToCustomer() && !order.isVisibleToVendor())) {
        throw new ValidationException("OA011", order.getIdString(),
            OrderUtils.getStatusDisplay(order.getStatus(), locale));
      }
    } else if (ApprovalType.SALES_ORDER.equals(approvalType)) {
      if (!(order.getStatus().equals(IOrder.PENDING) || order.getStatus()
          .equals(IOrder.CONFIRMED))) {
        throw new ValidationException("OA002", order.getIdString(),
            OrderUtils.getStatusDisplay(order.getStatus(), locale));
      } else if (!(order.isVisibleToCustomer() && order.isVisibleToVendor())) {
        throw new ValidationException("OA010", order.getIdString(),
            OrderUtils.getStatusDisplay(order.getStatus(), locale));
      }
    } else if (ApprovalType.TRANSFERS.equals(approvalType)) {
      if (!order.getStatus().equals(IOrder.PENDING)) {
        throw new ValidationException("OA003", order.getIdString(),
            OrderUtils.getStatusDisplay(order.getStatus(), locale));
      } else if (order.isVisibleToCustomer() || order.isVisibleToVendor()) {
        throw new ValidationException("OA012", order.getIdString(),
            OrderUtils.getStatusDisplay(order.getStatus(), locale));
      }
    } else {
      throw new ValidationException("OA004", approvalType);
    }

  }
}
