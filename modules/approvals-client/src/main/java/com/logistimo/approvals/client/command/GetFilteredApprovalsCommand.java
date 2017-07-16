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

package com.logistimo.approvals.client.command;

import com.logistimo.approvals.client.config.Constants;
import com.logistimo.approvals.client.exceptions.BadRequestException;
import com.logistimo.approvals.client.models.Approval;
import com.logistimo.approvals.client.models.ErrorResponse;
import com.logistimo.approvals.client.models.RestResponsePage;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by naveensnair on 16/06/17.
 */
public class GetFilteredApprovalsCommand extends HystrixCommand<RestResponsePage<Approval>> {

  private final UriComponentsBuilder uriBuilder;
  private final RestTemplate restTemplate;

  public GetFilteredApprovalsCommand(RestTemplate restTemplate, String url) {
    super(HystrixCommandGroupKey.Factory.asKey(Constants.APPROVALS_CLIENT),
        Constants.TIMEOUT_IN_MILLISECONDS);
    this.restTemplate = restTemplate;
    this.uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
  }

  public GetFilteredApprovalsCommand withOffset(int offset) {
    uriBuilder.queryParam("offset", offset);
    return this;
  }

  public GetFilteredApprovalsCommand withSize(int size) {
    uriBuilder.queryParam("size", size);
    return this;
  }

  public GetFilteredApprovalsCommand withType(String type, String typeId) {
    if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(typeId)) {
      uriBuilder.queryParam("type", type);
      uriBuilder.queryParam("type_id", typeId);
    }
    return this;
  }

  public GetFilteredApprovalsCommand withStatus(String status) {
    if (StringUtils.isNotBlank(status)) {
      uriBuilder.queryParam("status", status);
    }
    return this;
  }

  public GetFilteredApprovalsCommand withAttribute(String name, String value) {
    if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
      uriBuilder.queryParam("attribute_key", name);
      uriBuilder.queryParam("attribute_value", value);
    }
    return this;
  }

  public GetFilteredApprovalsCommand withRequester(String requesterId) {
    if(StringUtils.isNotBlank(requesterId)) {
      uriBuilder.queryParam("requester_id", requesterId);
    }
    return this;
  }

  public GetFilteredApprovalsCommand withApprover(String approverId) {
    if (StringUtils.isNotBlank(approverId)) {
      uriBuilder.queryParam("approver_id", approverId);
    }
    return this;
  }

  public GetFilteredApprovalsCommand withExpiringIn(String expiringIn) {
    if (StringUtils.isNotBlank(expiringIn)) {
      uriBuilder.queryParam("expiring_in", expiringIn);
    }
    return this;
  }

  public GetFilteredApprovalsCommand withDomainId(Long domainId) {
    if (domainId != null) {
      uriBuilder.queryParam("domain_id", domainId);
    }
    return this;
  }

  public GetFilteredApprovalsCommand withOrderedBy(String orderedBy) {
    if (StringUtils.isNotBlank(orderedBy)) {
      uriBuilder.queryParam("ordered_by", orderedBy);
    }
    return this;
  }

  @Override
  protected RestResponsePage<Approval> run() throws Exception {
    try {
      ParameterizedTypeReference<RestResponsePage<Approval>>
          responsetype =
          new ParameterizedTypeReference<RestResponsePage<Approval>>() {
          };
      ResponseEntity<RestResponsePage<Approval>>
          result =
          restTemplate
              .exchange(uriBuilder.build().encode().toUri(), HttpMethod.GET, null, responsetype);
      return result.getBody();
    } catch (HttpClientErrorException exception) {
      throw new HystrixBadRequestException(exception.getMessage(),
          new BadRequestException(ErrorResponse.getErrorResponse(exception), exception));
    }
  }


}
