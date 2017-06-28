package com.logistimo.orders.approvals.actions;

import com.logistimo.approvals.client.IApprovalsClient;
import com.logistimo.approvals.client.models.Approval;
import com.logistimo.approvals.client.models.RestResponsePage;
import com.logistimo.orders.approvals.models.OrderApprovalFilters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charan on 22/06/17.
 */
@Component
public class GetOrderApprovalsAction {

  @Autowired
  IApprovalsClient approvalsClient;

  public RestResponsePage<Approval> invoke(OrderApprovalFilters orderApprovalFilters) {
    return approvalsClient.fetchApprovals(orderApprovalFilters);
  }
}
