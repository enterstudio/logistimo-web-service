package com.logistimo.orders.approvals.validations;

import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.entities.auth.EntityAuthoriser;
import com.logistimo.exception.ValidationException;
import com.logistimo.orders.approvals.ApprovalType;
import com.logistimo.orders.approvals.models.ApprovalRequestModel;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.ServiceException;
import com.logistimo.validations.Validator;

import java.util.Locale;

/**
 * Created by charan on 22/06/17.
 */
public class ApprovalRequesterValidator implements Validator {

  private final ApprovalRequestModel approvalRequestModel;
  private final String userId;
  private final Locale locale;
  private final IOrder order;

  public ApprovalRequesterValidator(ApprovalRequestModel approvalRequestModel, IOrder order,
                                    String userId, Locale locale) {
    this.approvalRequestModel = approvalRequestModel;
    this.userId = userId;
    this.locale = locale;
    this.order = order;
  }

  public void validate() throws ValidationException {

    if (!approvalRequestModel.getRequesterId().equals(userId)) {
      throw new ValidationException("OA005", locale, approvalRequestModel.getRequesterId(), userId);
    }

    try {
      boolean
          hasAccessToCustomer =
          EntityAuthoriser.authoriseEntityPerm(SecurityUtils.getUserDetails(), order.getKioskId())
              > 0;
      boolean hasAccessToVendor = EntityAuthoriser.authoriseEntityPerm(
          SecurityUtils.getUserDetails(), order.getServicingKiosk()) > 0;
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
