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

package com.logistimo.approvals.client.models;


import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by naveensnair on 19/06/17.
 */
public class ApprovalDomainMapping {

    private Long id;

  @SerializedName("approval_id")
  private String approvalId;

  @SerializedName("domain_id")
  private Long domainId;

  @SerializedName("created_at")
  private Date createdAt;

  @SerializedName("updated_at")
  private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

  public String getApprovalId() {
    return approvalId;
    }

  public void setApprovalId(String approvalId) {
    this.approvalId = approvalId;
    }

  public Long getDomainId() {
    return domainId;
    }

  public void setDomainId(Long domainId) {
    this.domainId = domainId;
    }

  public Date getCreatedAt() {
    return createdAt;
    }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
    }

  public Date getUpdatedAt() {
    return updatedAt;
    }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
    }
}
