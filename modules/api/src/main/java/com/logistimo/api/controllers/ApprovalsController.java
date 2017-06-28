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

package com.logistimo.api.controllers;

import com.logistimo.approvals.client.models.Approval;
import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.approvals.client.models.RestResponsePage;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.exception.ValidationException;
import com.logistimo.logger.XLog;
import com.logistimo.models.StatusModel;
import com.logistimo.orders.approvals.actions.CreateApprovalAction;
import com.logistimo.orders.approvals.actions.GetOrderApprovalAction;
import com.logistimo.orders.approvals.actions.GetOrderApprovalsAction;
import com.logistimo.orders.approvals.actions.UpdateApprovalStatusAction;
import com.logistimo.orders.approvals.builders.ApprovalsBuilder;
import com.logistimo.orders.approvals.models.ApprovalModel;
import com.logistimo.orders.approvals.models.ApprovalRequestModel;
import com.logistimo.orders.approvals.models.CreateApprovalResponseModel;
import com.logistimo.orders.approvals.models.OrderApprovalFilters;
import com.logistimo.orders.approvals.service.IApprovalService;
import com.logistimo.orders.approvals.service.impl.ApprovalServiceImpl;
import com.logistimo.pagination.PageParams;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/order-approvals")
public class ApprovalsController {

  private static final XLog LOGGER = XLog.getLog(ApprovalsController.class);

  private static final String QUEUED = "QUEUED";

  @Autowired
  private CreateApprovalAction createApprovalAction;

  @Autowired
  private GetOrderApprovalsAction getOrderApprovalsAction;

  @Autowired
  private GetOrderApprovalAction getOrderApprovalAction;

  @Autowired
  private UpdateApprovalStatusAction updateApprovalStatusAction;


  @Autowired
  private ApprovalsBuilder builder;

  @RequestMapping(value = "", method = RequestMethod.POST)
  public
  @ResponseBody
  CreateApprovalResponseModel createOrderApproval(
      @RequestBody ApprovalRequestModel approvalRequestModel)
      throws ServiceException, ValidationException, ObjectNotFoundException {

    CreateApprovalResponse
        createApprovalResponse =
        createApprovalAction.invoke(approvalRequestModel);
    return builder.buildApprovalRequestModel(createApprovalResponse);

  }


  @RequestMapping(value = "/{approvalId}/status", method = RequestMethod.PUT)
  public
  @ResponseBody
  void updateApprovalStatus(@PathVariable String approvalId,
                            @RequestBody StatusModel model)
      throws ValidationException, ServiceException, ObjectNotFoundException {
    updateApprovalStatusAction.invoke(model, approvalId);
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public
  @ResponseBody
  RestResponsePage<ApprovalModel> getApprovals(
      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
      @RequestParam(required = false, name = "entity_id") Long entityId,
      @RequestParam(required = false, name = "order_id") Long orderId,
      @RequestParam(required = false, name = "status") String status,
      @RequestParam(required = false, name = "expiring_in") Integer expiringIn,
      @RequestParam(required = false, name = "type") String type,
      @RequestParam(required = false, name = "requester_id") String requesterId,
      @RequestParam(required = false, name = "approver_id") String approverId,
      @RequestParam(required = false, value = "embed") String[] embed)
      throws ServiceException, ObjectNotFoundException {

    RestResponsePage<Approval> approvals = getOrderApprovalsAction.invoke(
        (OrderApprovalFilters) new OrderApprovalFilters()
            .setEntityId(entityId)
            .setOrderId(orderId)
            .setExpiringInMinutes(expiringIn)
            .setStatus(status)
            .setType(type)
            .setRequesterId(requesterId)
            .setApproverId(approverId)
            .setDomainId(SecurityUtils.getCurrentDomainId())
            .setOffset(offset)
            .setSize(size));

    return builder.buildApprovalsModel(approvals, embed);
  }

  @RequestMapping(value = "/{approvalId}", method = RequestMethod.GET)
  public
  @ResponseBody
  ApprovalModel getApproval(@PathVariable String approvalId,
                            @RequestParam(required = false) String[] embed)
      throws ServiceException, ObjectNotFoundException {
    return builder.buildApprovalModel(getOrderApprovalAction.invoke(approvalId), embed);
  }

  @RequestMapping(value = "/requesters", method = RequestMethod.GET)
  public
  @ResponseBody
  Collection<String> findRequesters(@RequestParam String q,
                                    HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    IApprovalService approvalService;
    UsersService usersService;
    Collection<String> requesters = null;
    if (StringUtils.isNotEmpty(q)) {
      try {
        approvalService = Services.getService(ApprovalServiceImpl.class, locale);
        usersService = Services.getService(UsersServiceImpl.class, locale);
        requesters = approvalService.getFilteredRequesters(q, domainId, usersService, 0);
      } catch (Exception e) {
        LOGGER.fine("Exception while fetching requesters for domain {0} and starting with {1}",
            domainId, q, e);
      }
    }
    return requesters;
  }

  @RequestMapping(value = "/approvers", method = RequestMethod.GET)
  public
  @ResponseBody
  Collection<String> findApprovers(@RequestParam String q,
                                   HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    IApprovalService approvalService;
    UsersService usersService;
    Collection<String> approvers = null;
    if (StringUtils.isNotEmpty(q)) {
      try {
        approvalService = Services.getService(ApprovalServiceImpl.class, locale);
        usersService = Services.getService(UsersServiceImpl.class, locale);
        approvers = approvalService.getFilteredRequesters(q, domainId, usersService, 1);
      } catch (ServiceException e) {
        e.printStackTrace();
      }
    }
    return approvers;
  }

}
