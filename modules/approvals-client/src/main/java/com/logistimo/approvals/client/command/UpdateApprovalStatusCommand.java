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
import com.logistimo.approvals.client.models.ErrorResponse;
import com.logistimo.approvals.client.models.UpdateApprovalRequest;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by nitisha.khandelwal on 31/05/17.
 */

public class UpdateApprovalStatusCommand extends HystrixCommand<String> {

  private final String path;
  private final String approvalId;
  private final UpdateApprovalRequest request;
  private final RestTemplate restTemplate;

  public UpdateApprovalStatusCommand(RestTemplate restTemplate, String path, String approvalId,
                                     UpdateApprovalRequest request) {
    super(HystrixCommandGroupKey.Factory.asKey(Constants.APPROVALS_CLIENT),
        Constants.TIMEOUT_IN_MILLISECONDS);
    this.restTemplate = restTemplate;
    this.approvalId = approvalId;
    this.path = path + "/{approvalId}/status";
    this.request = request;
  }

  @Override
  protected String run() throws Exception {
    try {
      restTemplate.put(path, request, approvalId);
      return approvalId;
    } catch (HttpClientErrorException exception) {
      throw new HystrixBadRequestException(exception.getMessage(),
          new BadRequestException(ErrorResponse.getErrorResponse(exception), exception));
    }
  }
}
