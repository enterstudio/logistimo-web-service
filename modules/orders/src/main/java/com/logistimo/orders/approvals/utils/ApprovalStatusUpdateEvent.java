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

import java.util.Date;
import java.util.List;

/**
 * Created by nitisha.khandelwal on 02/06/17.
 */

public class ApprovalStatusUpdateEvent {

  private String approvalId;

  private String type;

  private String typeId;

  private String requesterId;

  private List<String> approverIds;

  private String status;

  private String updatedBy;

  private Date updatedAt;

  public String getApprovalId() {
    return approvalId;
  }

  public String getType() {
    return type;
  }

  public String getTypeId() {
    return typeId;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public List<String> getApproverIds() {
    return approverIds;
  }

  public String getStatus() {
    return status;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }
}
