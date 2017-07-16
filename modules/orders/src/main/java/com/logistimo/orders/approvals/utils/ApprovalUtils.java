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

package com.logistimo.orders.approvals.utils;

import com.logistimo.approvals.client.models.Approver;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.config.models.ApprovalsConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.entities.entity.IApprovers;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.ValidationException;
import com.logistimo.orders.approvals.ApprovalType;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by charan on 22/06/17.
 */
public class ApprovalUtils {

  private ApprovalUtils() {
  }

  public static List<Approver> getApproversForOrderType(IOrder order, ApprovalType approvalType)
      throws ServiceException, ValidationException {

    ApprovalsConfig.OrderConfig
        orderConfig = null;
    int expiry;
    List<Approver> approvers = new ArrayList<>();
    List<String> primaryApprovers;
    List<String> secondaryApprovers;

    if (ApprovalType.TRANSFERS.equals(approvalType)) {
      orderConfig = DomainConfig.getInstance(order.getDomainId()).getApprovalsConfig().getOrderConfig();
      expiry = orderConfig.getExpiry(order.getOrderType());
      primaryApprovers = orderConfig.getPrimaryApprovers();
      secondaryApprovers = orderConfig.getSecondaryApprovers();
    } else {
      String
          type;
      Long kioskId;
      EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);

      if (ApprovalType.PURCHASE_ORDER.equals(approvalType)) {
        type = IApprovers.PURCHASE_ORDER;
        kioskId = order.getKioskId();
        IKiosk kiosk;
        try {
          kiosk = entitiesService.getKioskIfPresent(kioskId, false);
        } catch (ObjectNotFoundException e) {
          throw new ValidationException("OA016", kioskId);
        }
        orderConfig = DomainConfig.getInstance(kiosk.getDomainId()).getApprovalsConfig().getOrderConfig();
        expiry = orderConfig.getExpiry(order.getOrderType());
        if (!orderConfig.isPurchaseApprovalEnabled(kiosk.getTags())) {
          throw new ValidationException("OA013", SecurityUtils.getLocale(), kiosk.getName());
        }
      } else {
        type = IApprovers.SALES_ORDER;
        kioskId = order.getServicingKiosk();
        IKiosk kiosk;
        try {
          kiosk = entitiesService.getKioskIfPresent(kioskId, false);
        } catch (ObjectNotFoundException e) {
          throw new ValidationException("OA016", kioskId);
        }
        orderConfig = DomainConfig.getInstance(kiosk.getDomainId()).getApprovalsConfig().getOrderConfig();
        expiry = orderConfig.getExpiry(order.getOrderType());
        if (!orderConfig.isSaleApprovalEnabled(kiosk.getTags())) {
          throw new ValidationException("OA014", SecurityUtils.getLocale(), kiosk.getName());
        }
      }

      primaryApprovers =
          entitiesService.getApprovers(kioskId, IApprovers.PRIMARY_APPROVER, type)
              .stream()
              .map(IApprovers::getUserId)
              .collect(Collectors.toList());
      secondaryApprovers =
          entitiesService.getApprovers(kioskId, IApprovers.SECONDARY_APPROVER, type)
              .stream()
              .map(IApprovers::getUserId)
              .collect(Collectors.toList());
    }

    setApprovers(expiry, approvers, primaryApprovers, Approver.PRIMARY);
    setApprovers(expiry, approvers, secondaryApprovers, Approver.SECONDARY);
    return approvers;
  }

  private static void setApprovers(int expiry, List<Approver> approvers,
      List<String> primaryApprovers, String primary) {
    if (!primaryApprovers.isEmpty()) {
      Approver approver = new Approver();
      approver.setExpiry(expiry);
      approver.setUserIds(primaryApprovers);
      approver.setType(primary);
      approvers.add(approver);
    }
  }

  public static String getApprovalType(Integer approvalType) {
    if (IOrder.PURCHASE_ORDER == approvalType) {
      return "purchase";
    }
    if (IOrder.TRANSFER_ORDER == approvalType) {
      return "transfer";
    }
    if (IOrder.SALES_ORDER == approvalType) {
      return "sales";
    }
    return null;
  }

}
