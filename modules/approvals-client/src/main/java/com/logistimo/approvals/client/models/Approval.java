
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

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

import java.util.Set;

/**
 * Created by naveensnair on 16/06/17.
 */


public class Approval {

    @SerializedName("id")
    private String id;

    @SerializedName("type")
    private String type;

    @SerializedName("type_id")
    private String typeId;

    @SerializedName("status")
    private String status;

    @SerializedName("requester_id")
    private String requesterId;

    @SerializedName("source_domain_id")
    private Long sourceDomainId;

    @SerializedName("conversation_id")
    private String conversationId;

    @SerializedName("updated_by")
    private String updatedBy;

    @SerializedName("expire_at")
    private Date expireAt;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("approvers")
    private Set<ApproverQueue> approvers;

    @JsonProperty("attributes")
    private Set<ApprovalAttributes> attributes;

    @SerializedName("domains")
    private Set<ApprovalDomainMapping> domains;

    @SerializedName("version")
    private Long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public Long getSourceDomainId() {
        return sourceDomainId;
    }

    public void setSourceDomainId(Long sourceDomainId) {
        this.sourceDomainId = sourceDomainId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Set<ApproverQueue> getApprovers() {
        return approvers;
    }

    public void setApprovers(Set<ApproverQueue> approvers) {
        this.approvers = approvers;
    }

    public Set<ApprovalAttributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<ApprovalAttributes> attributes) {
        this.attributes = attributes;
    }

    public Set<ApprovalDomainMapping> getDomains() {
        return domains;
    }

    public void setDomains(Set<ApprovalDomainMapping> domains) {
        this.domains = domains;
    }
}

