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

package com.logistimo.approvals.client.internal;

import com.logistimo.approvals.client.IApprovalsClient;
import com.logistimo.approvals.client.command.CreateApprovalCommand;
import com.logistimo.approvals.client.command.GetApprovalCommand;
import com.logistimo.approvals.client.command.GetFilteredApprovalsCommand;
import com.logistimo.approvals.client.command.UpdateApprovalStatusCommand;
import com.logistimo.approvals.client.config.ApprovalsClientConfiguration;
import com.logistimo.approvals.client.models.Approval;
import com.logistimo.approvals.client.models.ApprovalFilters;
import com.logistimo.approvals.client.models.CreateApprovalRequest;
import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.approvals.client.models.RestResponsePage;
import com.logistimo.approvals.client.models.UpdateApprovalRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by nitisha.khandelwal on 31/05/17.
 */
@Service
public class ApprovalsClient implements IApprovalsClient {

  @Autowired
  private ApprovalsClientConfiguration configuration;

  @Autowired
  @Qualifier("approvalsRestTemplate")
  private RestTemplate restTemplate;

  @Override
  public CreateApprovalResponse createApproval(CreateApprovalRequest request) {
    CreateApprovalCommand command = new CreateApprovalCommand(restTemplate, configuration.getPath(),
        request);
    return command.execute();
  }

  @Override
  public RestResponsePage<Approval> fetchApprovals(ApprovalFilters approvalFilters) {
    GetFilteredApprovalsCommand
        command =
        new GetFilteredApprovalsCommand(restTemplate, configuration.getPath())
            .withApprover(approvalFilters.getApproverId())
            .withRequester(approvalFilters.getRequesterId())
            .withAttribute(approvalFilters.getAttributeKey(), approvalFilters.getAttributeValue())
            .withDomainId(approvalFilters.getDomainId())
            .withExpiringIn(approvalFilters.getExpiringInMinutes())
            .withOffset(approvalFilters.getOffset())
            .withSize(approvalFilters.getSize())
            .withStatus(approvalFilters.getStatus())
            .withType(approvalFilters.getType(), approvalFilters.getTypeId())
            .withOrderedBy(approvalFilters.getOrderedBy());
    return command.execute();
  }

  @Override
  public CreateApprovalResponse getApproval(String approvalId) {
    GetApprovalCommand
        command =
        new GetApprovalCommand(restTemplate, configuration.getPath(), approvalId);
    return command.execute();
  }

  public void updateApprovalRequest(UpdateApprovalRequest request, String approvalId) {
    new UpdateApprovalStatusCommand(restTemplate, configuration.getPath(), approvalId, request)
        .execute();
  }
}
