package com.logistimo.approvals.client.command;

import com.logistimo.approvals.client.config.Constants;
import com.logistimo.approvals.client.exceptions.ApprovalBadRequestException;
import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.approvals.client.models.ErrorResponse;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by charan on 23/06/17.
 */
public class GetApprovalCommand extends HystrixCommand<CreateApprovalResponse> {

  private final String path;
  private final String approvalId;
  private final RestTemplate restTemplate;

  public GetApprovalCommand(RestTemplate restTemplate, String path, String approvalId) {
    super(HystrixCommandGroupKey.Factory.asKey(Constants.APPROVALS_CLIENT),
        Constants.TIMEOUT_IN_MILLISECONDS);
    this.restTemplate = restTemplate;
    this.path = path + "/{approvalId}";
    this.approvalId = approvalId;
  }

  @Override
  protected CreateApprovalResponse run() throws Exception {
    try {
      return restTemplate.getForEntity(path, CreateApprovalResponse.class, approvalId).getBody();
    } catch (HttpClientErrorException exception) {
      throw new HystrixBadRequestException(exception.getMessage(),
          new ApprovalBadRequestException(ErrorResponse.getErrorResponse(exception), exception));
    }

  }
}
