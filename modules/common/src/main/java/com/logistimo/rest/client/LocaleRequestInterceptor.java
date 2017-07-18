package com.logistimo.rest.client;

import com.logistimo.security.SecureUserDetails;
import com.logistimo.utils.ThreadLocalUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created by charan on 14/07/17.
 */
public class LocaleRequestInterceptor implements ClientHttpRequestInterceptor {


  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                      ClientHttpRequestExecution execution) throws
      IOException {
    SecureUserDetails
        userDetails =
        ThreadLocalUtil.get().getSecureUserDetails();
    if (userDetails != null) {
      request.getHeaders().set("Accept-Language", userDetails.getLocale().getLanguage());
    }
    return execution.execute(request, body);
  }
}
