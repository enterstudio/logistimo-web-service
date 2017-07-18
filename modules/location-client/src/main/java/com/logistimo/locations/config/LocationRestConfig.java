package com.logistimo.locations.config;

import com.logistimo.rest.client.RestConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by kumargaurav on 13/07/17.
 */
@Configuration
public class LocationRestConfig {

    @Bean(name = "locationsRestTemplate")
    public RestTemplate locationsRestTemplate() {
        return RestConfig.restTemplate();
    }

}
