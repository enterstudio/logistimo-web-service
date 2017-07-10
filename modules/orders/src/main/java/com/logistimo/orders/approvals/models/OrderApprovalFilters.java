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

package com.logistimo.orders.approvals.models;

import com.logistimo.approvals.client.models.ApprovalFilters;
import com.logistimo.constants.Constants;

import java.util.Objects;

/**
 * Created by charan on 22/06/17.
 */
public class OrderApprovalFilters extends ApprovalFilters {


  private Long entityId;
  private Long orderId;

  public Long getEntityId() {
    return entityId;
  }

  public OrderApprovalFilters setEntityId(Long entityId) {
    this.entityId = entityId;
    setAttributeKey("entity");
    setAttributeValue(Objects.toString(entityId, Constants.EMPTY));
    return this;
  }

  public Long getOrderId() {
    return orderId;
  }

  public OrderApprovalFilters setOrderId(Long orderId) {
    this.orderId = orderId;
    setType("ORDER");
    setTypeId(Objects.toString(orderId, Constants.EMPTY));
    return this;
  }

  public OrderApprovalFilters setExpiringInMinutes(Integer expiringIn) {
    setExpiringInMinutes(Objects.toString(expiringIn, Constants.EMPTY));
    return this;
  }
}
