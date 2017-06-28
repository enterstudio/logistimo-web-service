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
package com.logistimo.orders.entity.approvals;

import com.logistimo.logger.XLog;
import com.logistimo.orders.entity.Order;

import java.util.Date;

import javax.jdo.annotations.*;

/**
 * Created by naveensnair on 08/06/17.
 */
@PersistenceCapable(table="ORDER_APPROVAL_MAPPING", identityType = IdentityType.APPLICATION, detachable = "true")
public class OrderApprovalMapping implements IOrderApprovalMapping{

    // Logger
    static XLog xLogger = XLog.getLog(Order.class);
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Column(name = "ID")
    private Long id; // order approval mapping id
    @Persistent
    @Column(name = "ORDER_ID")
    private Long orderId; // order id
    @Persistent
    @Column(name = "APPROVAL_ID")
    private String approvalId; // approval id
    @Persistent
    @Column(name = "STATUS")
    private String status; //approval status
    @Persistent
    @Column(name = "APPROVAL_TYPE")
    private Integer approvalType; //order type
    @Column(name = "CREATED_AT")
    private Date createdAt;
    @Column(name = "UPDATED_AT")
    private Date updatedAt;
    @Column(name = "CREATED_BY")
    private String createdBy;
    @Column(name = "UPDATED_BY")
    private String updatedBy;
    @Column(name = "KIOSK_ID")
    private Long kioskId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getOrderId() {
        return orderId;
    }

    @Override
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String getApprovalId() {
        return approvalId;
    }

    @Override
    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(Integer approvalType) {
        this.approvalType = approvalType;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getKioskId() {
        return kioskId;
    }

    public void setKioskId(Long kioskId) {
        this.kioskId = kioskId;
    }
}
