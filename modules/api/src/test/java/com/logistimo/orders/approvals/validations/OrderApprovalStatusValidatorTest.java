package com.logistimo.orders.approvals.validations;

import com.logistimo.exception.ValidationException;
import com.logistimo.orders.approvals.ApprovalType;
import com.logistimo.orders.approvals.models.ApprovalRequestModel;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.Order;

import junit.framework.TestCase;

import java.util.Locale;

/**
 * Created by charan on 22/06/17.
 */
public class OrderApprovalStatusValidatorTest extends TestCase {

  public void testValidate() throws Exception {
    Order order = new Order(1l);
    order.setStatus(IOrder.COMPLETED);
    ApprovalRequestModel approvalRequestModel = new ApprovalRequestModel();
    approvalRequestModel.setApprovalType(ApprovalType.PURCHASE_ORDER);
    try {
      OrderApprovalStatusValidator validator = new OrderApprovalStatusValidator(approvalRequestModel, order, Locale.ENGLISH);
      validator.validate();
    } catch (ValidationException e) {
      assertTrue("Expected validation exception OA001", e.getCode().equals("OA001"));
    }
  }

  //TODO: Naveen to add more tests to cover this validator
}