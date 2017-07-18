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

package com.logistimo.orders.approvals.builders;

import com.logistimo.approvals.builders.RestResponsePageBuilder;
import com.logistimo.approvals.client.models.Approval;
import com.logistimo.approvals.client.models.Approver;
import com.logistimo.approvals.client.models.ApproverQueue;
import com.logistimo.approvals.client.models.ApproverResponse;
import com.logistimo.approvals.client.models.CreateApprovalRequest;
import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.approvals.client.models.RestResponsePage;
import com.logistimo.approvals.client.models.UpdateApprovalRequest;
import com.logistimo.constants.EmbedConstants;
import com.logistimo.logger.XLog;
import com.logistimo.models.StatusModel;
import com.logistimo.orders.approvals.ApprovalType;
import com.logistimo.orders.approvals.dao.impl.ApprovalsDao;
import com.logistimo.orders.approvals.models.ApprovalModel;
import com.logistimo.orders.approvals.models.ApproverModel;
import com.logistimo.orders.approvals.models.CreateApprovalResponseModel;
import com.logistimo.orders.builders.OrderBuilder;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.orders.entity.approvals.OrderApprovalMapping;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.users.builders.UserBuilder;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.models.UserContactModel;
import com.logistimo.users.service.UsersService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by naveensnair on 13/06/17.
 */
@Component
public class ApprovalsBuilder {

  public static final String
      ORDER_0_NOT_FOUND_BUILDING_APPROVAL_1 =
      "Order {0} not found, building approval {1}";
  private static final XLog LOGGER = XLog.getLog(ApprovalsBuilder.class);
  @Autowired
  private UserBuilder userBuilder;

  @Autowired
  private OrderBuilder orderBuilder;

  @Autowired
  private UsersService usersService;

  @Autowired
  private ApprovalsDao approvalsDao;

  public IOrderApprovalMapping buildOrderApprovalMapping(CreateApprovalResponse approvalResponse,
                                                         Integer approvalType, Long kioskId) {
    IOrderApprovalMapping orderApprovalMapping = null;
    if (approvalResponse != null) {
      orderApprovalMapping = new OrderApprovalMapping();
      orderApprovalMapping.setApprovalId(approvalResponse.getApprovalId());
      orderApprovalMapping.setOrderId(Long.parseLong(approvalResponse.getTypeId()));
      orderApprovalMapping.setApprovalType(approvalType);
      orderApprovalMapping.setCreatedBy(approvalResponse.getRequesterId());
      orderApprovalMapping.setCreatedAt(approvalResponse.getCreatedAt());
      orderApprovalMapping.setStatus(approvalResponse.getStatus());
      orderApprovalMapping.setUpdatedAt(approvalResponse.getUpdatedAt());
      orderApprovalMapping.setUpdatedBy(approvalResponse.getRequesterId());
      orderApprovalMapping.setKioskId(kioskId);
    }
    return orderApprovalMapping;
  }

  public CreateApprovalRequest buildApprovalRequest(IOrder order, String msg, String userId,
                                                    List<Approver> approverList) {
    CreateApprovalRequest request = new CreateApprovalRequest();
    request.setType("order");
    request.setTypeId(order.getIdString());
    request.setSourceDomainId(order.getDomainId());
    request.setDomains(order.getDomainIds());
    Map<String, String> attributes = new HashMap<>(1);
    attributes.put("kioskid", String.valueOf(order.getKioskId()));
    request.setAttributes(attributes);
    request.setMessage(msg);
    request.setRequesterId(userId);
    request.setApprovers(approverList);
    return request;
  }

  public RestResponsePage<ApprovalModel> buildApprovalsModel(RestResponsePage<Approval> response,
                                                             String[] embed)
      throws ServiceException {

    List<ApprovalModel> approvalModels = new ArrayList<>(1);
    for (Approval approval : response.getContent()) {
      approvalModels.add(buildApprovalListingModel(approval, embed));
    }

    return new RestResponsePageBuilder<ApprovalModel>()
        .withRestResponsePage(response)
        .withContent(approvalModels)
        .build();

  }


  public ApprovalModel buildApprovalModel(CreateApprovalResponse approvalResponse, String[] embed)
      throws ServiceException, ObjectNotFoundException {
    ApprovalModel model = new ApprovalModel();
    model.setId(approvalResponse.getApprovalId());
    model.setOrderId(Long.parseLong(approvalResponse.getTypeId()));
    model.setCreatedAt(approvalResponse.getCreatedAt());
    model.setExpiresAt(approvalResponse.getExpireAt());
    StatusModel statusModel = new StatusModel();
    statusModel.setStatus(approvalResponse.getStatus());
    statusModel.setUpdatedBy(approvalResponse.getUpdatedBy());
    statusModel.setUpdatedAt(approvalResponse.getUpdatedAt());
    model.setStatus(statusModel);
    model.setActiveApproverType(approvalResponse.getActiveApproverType());
    model.setApprovalType(ApprovalType.get(
        approvalsDao.getApprovalType(Long.valueOf(approvalResponse.getTypeId()),
            model.getId())));
    model.setApprovers(buildApproversModel(approvalResponse, usersService));
    model.setConversationId(approvalResponse.getConversationId());
    model.setStatusUpdatedBy(buildRequestorModel(approvalResponse.getUpdatedBy(), approvalResponse.getApprovalId()));
    model.setRequester(buildRequestorModel(approvalResponse.getRequesterId(), approvalResponse.getApprovalId()));
    if (embed != null) {
      for (String s : embed) {
        if (EmbedConstants.ORDER.equals(s)) {
          try {
            model.setOrder(orderBuilder.build(Long.valueOf(approvalResponse.getTypeId())));
          } catch (ObjectNotFoundException e) {
            LOGGER.info(ORDER_0_NOT_FOUND_BUILDING_APPROVAL_1, approvalResponse.getTypeId(),
                approvalResponse.getApprovalId());
          }
        } else if (EmbedConstants.ORDER_META.equals(s)) {
          try {
            model.setOrder(orderBuilder.buildMeta(Long.valueOf(approvalResponse.getTypeId())));
          } catch (ObjectNotFoundException e) {
            LOGGER.info(ORDER_0_NOT_FOUND_BUILDING_APPROVAL_1, approvalResponse.getTypeId(),
                approvalResponse.getApprovalId());
          }
        }
      }
    }
    return model;
  }

  public ApprovalModel buildApprovalListingModel(Approval approval, String[] embed)
      throws ServiceException {
      ApprovalModel model = new ApprovalModel();
    model.setId(approval.getId());
    model.setOrderId(Long.parseLong(approval.getTypeId()));
    model.setCreatedAt(approval.getCreatedAt());
    model.setExpiresAt(approval.getExpireAt());
    model.setApprovers(buildApprovers(approval));
    model.setConversationId(approval.getConversationId());
    StatusModel statusModel = new StatusModel();
    statusModel.setStatus(approval.getStatus());
    statusModel.setUpdatedAt(approval.getUpdatedAt());
    statusModel.setUpdatedBy(approval.getUpdatedBy());
    model.setStatus(statusModel);
    model.setRequester(buildRequestorModel(approval.getRequesterId(), approval.getId()));

    if (embed != null) {
      for (String s : embed) {
        if (EmbedConstants.ORDER.equals(s)) {
          try {
            model.setOrder(orderBuilder.build(Long.valueOf(approval.getTypeId())));
          } catch (ObjectNotFoundException e) {
            LOGGER.info(ORDER_0_NOT_FOUND_BUILDING_APPROVAL_1, approval.getTypeId(),
                approval.getId());
          }
        } else if (EmbedConstants.ORDER_META.equals(s)) {
          try {
            model.setOrder(orderBuilder.buildMeta(Long.valueOf(approval.getTypeId())));
          } catch (ObjectNotFoundException e) {
            LOGGER.info(ORDER_0_NOT_FOUND_BUILDING_APPROVAL_1, approval.getTypeId(),
                approval.getId());
          }
        }
      }
    }
    return model;
  }

  private UserContactModel buildRequestorModel(String requesterId, String approvalId) {
    UserContactModel contactModel = new UserContactModel();
    try {
      contactModel.setUserId(requesterId);
      userBuilder.buildUserContactModel(requesterId, contactModel);
    } catch (ObjectNotFoundException e) {
      LOGGER.info("Requester {0} not found , for approval {1}", requesterId,
          approvalId);
    }
    return contactModel;
  }

  public List<ApproverModel> buildApprovers(Approval approval) {
    List<ApproverModel> approverModels = new ArrayList<>(1);
    Set<ApproverQueue> approverQueueSet = approval.getApprovers();
    for (ApproverQueue queue : approverQueueSet) {

      ApproverModel model = new ApproverModel();
      model.setApproverType(queue.getType());
      try {
        userBuilder.buildUserContactModel(queue.getUserId(), model);
      } catch (ObjectNotFoundException e) {
        LOGGER.info("Unable to find approver {0} defined for approval {1}", queue.getUserId(),
            queue.getApprovalId());
      }
      approverModels.add(model);
    }
    return approverModels;
  }

  public CreateApprovalResponseModel buildApprovalRequestModel(CreateApprovalResponse response)
      throws ServiceException, ObjectNotFoundException {
    CreateApprovalResponseModel model = new CreateApprovalResponseModel();
    model.setId(response.getApprovalId());
    model.setOrderId(Long.parseLong(response.getTypeId()));
    model.setCreatedAt(response.getCreatedAt());
    model.setExpiresAt(response.getExpireAt());
    model.setRequester(buildRequestorModel(response.getRequesterId(), response.getApprovalId()));
    model.setStatus(
        buildStatusModel(response.getRequesterId(), response.getStatus(), response.getUpdatedAt()));
    model.setConversationId(response.getConversationId());
    List<ApproverModel> approverModelList = buildApproversModel(response, usersService);
    model.setApprovers(approverModelList);
    return model;
  }

  public List<ApproverModel> buildApproversModel(CreateApprovalResponse response,
                                                 UsersService usersService)
      throws ServiceException, ObjectNotFoundException {
    List<ApproverModel> models = new ArrayList<>();
    List<ApproverResponse> approverResponses = response.getApprovers();
    if(response.getApprovers() != null && !response.getApprovers().isEmpty()) {
      for (ApproverResponse approverResponse : approverResponses) {
        ApproverModel model = new ApproverModel();
        if (StringUtils.isNotEmpty(approverResponse.getUserId())) {
          IUserAccount userAccount = usersService.getUserAccount(approverResponse.getUserId());
          model.setApproverType(approverResponse.getType());
          model.setEmail(userAccount.getEmail());
          model.setName(userAccount.getFullName());
          model.setPhone(userAccount.getMobilePhoneNumber());
          model.setUserId(userAccount.getUserId());
          models.add(model);
        }
      }
    }

    return models;
  }

  public StatusModel buildStatusModel(String updatedBy, String status, Date updatedAt) {
    StatusModel statusModel = new StatusModel();
    statusModel.setUpdatedBy(updatedBy);
    statusModel.setStatus(status);
    statusModel.setUpdatedAt(updatedAt);
    return statusModel;
  }

  public UpdateApprovalRequest buildUpdateApprovalRequest(StatusModel model) {
    UpdateApprovalRequest request = new UpdateApprovalRequest();
    request.setMessage(model.getMessage());
    request.setStatus(model.getStatus());
    request.setUpdatedBy(model.getUpdatedBy());
    return request;
  }
}
