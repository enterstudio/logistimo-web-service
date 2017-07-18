package com.logistimo.locations.config;

import com.logistimo.services.utils.ConfigUtil;
import org.springframework.stereotype.Component;

/**
 * Created by kumargaurav on 13/07/17.
 */
@Component
public class LocationClientProperties {
    private String url =
            ConfigUtil.get("location.service.url=", "http://localhost:9090");

    private String path =
            ConfigUtil.get("location.service.path", "/locations/city");

    private String fallbackEndpoint =  ConfigUtil.get("location.service.fallback","activemq:queue:location");

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return url.concat(path);
    }

    public String getFallbackEndpoint() {
        return fallbackEndpoint;
    }
}
