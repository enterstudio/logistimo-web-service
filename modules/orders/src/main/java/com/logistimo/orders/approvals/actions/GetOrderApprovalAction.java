package com.logistimo.orders.approvals.actions;

import com.logistimo.approvals.client.IApprovalsClient;
import com.logistimo.approvals.client.models.CreateApprovalResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charan on 22/06/17.
 */
@Component
public class GetOrderApprovalAction {

  @Autowired
  IApprovalsClient approvalsClient;

  public CreateApprovalResponse invoke(String approvalId) {
    return approvalsClient.getApproval(approvalId);
  }
}
