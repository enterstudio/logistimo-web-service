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
import com.logistimo.approvals.client.models.Approval;
import com.logistimo.approvals.client.models.ApprovalFilters;
import com.logistimo.approvals.client.models.RestResponsePage;
import com.logistimo.exception.ErrorResponse;
import com.logistimo.exception.HttpBadRequestException;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
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
  private ApprovalFilters approvalFilters;

  public GetFilteredApprovalsCommand(RestTemplate restTemplate, String url,
                                     ApprovalFilters approvalFilters) {
    super(HystrixCommandGroupKey.Factory.asKey(Constants.APPROVALS_CLIENT),
        Constants.TIMEOUT_IN_MILLISECONDS);
    this.restTemplate = restTemplate;
    this.uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
    this.approvalFilters = approvalFilters;
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
              .exchange(uriBuilder.build().encode().toUri(), HttpMethod.POST,
                  new HttpEntity<>(approvalFilters), responsetype);
      return result.getBody();
    } catch (HttpClientErrorException exception) {
      throw new HystrixBadRequestException(exception.getMessage(),
          new HttpBadRequestException(ErrorResponse.getErrorResponse(exception), exception));
    }
  }

}
