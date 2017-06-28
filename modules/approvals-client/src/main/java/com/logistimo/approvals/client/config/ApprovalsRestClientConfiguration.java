package com.logistimo.approvals.client.config;

import com.logistimo.rest.client.RestConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by charan on 23/06/17.
 */
@Configuration
public class ApprovalsRestClientConfiguration {

  @Bean(name = "approvalsRestTemplate")
  public RestTemplate approvalsRestTemplate() {
    return RestConfig.restTemplate();
  }

}
