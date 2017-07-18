package com.logistimo.locations.command;

import com.logistimo.exception.ErrorResponse;
import com.logistimo.exception.HttpBadRequestException;
import com.logistimo.locations.client.impl.LocationServiceImpl;
import com.logistimo.locations.constants.LocationConstants;
import com.logistimo.locations.model.LocationRequestModel;
import com.logistimo.locations.model.LocationResponseModel;
import com.logistimo.logger.XLog;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * Created by kumargaurav on 13/07/17.
 */
public class GetLocationCommand extends HystrixCommand<LocationResponseModel> {

  private LocationRequestModel model;

  private RestTemplate restClient;

  private MultiValueMap headers;

  private static final XLog log = XLog.getLog(LocationServiceImpl.class);

  public GetLocationCommand(RestTemplate restClient, LocationRequestModel model,
                            MultiValueMap headers) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(LocationConstants.CLIENT_NAME))
        .andCommandPropertiesDefaults(
            HystrixCommandProperties.Setter().
                withExecutionTimeoutInMilliseconds(LocationConstants.TIMED_OUT)));
    this.model = model;
    this.restClient = restClient;
    this.headers = headers;
  }

  @Override
  protected LocationResponseModel run() throws Exception {
    URI url = JerseyUriBuilder.fromUri(LocationConstants.URL).build();
    HttpEntity<LocationRequestModel> request = new HttpEntity<LocationRequestModel>(model, headers);
    try {
      ResponseEntity<LocationResponseModel>
          entity =
          restClient.postForEntity(url, request, LocationResponseModel.class);
      return entity.getBody();
    } catch (HttpClientErrorException exception) {
      throw new HystrixBadRequestException(exception.getMessage(),
          new HttpBadRequestException(ErrorResponse.getErrorResponse(exception), exception));
    }
  }
}
