package com.logistimo.api.interceptor;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor to pre-extract Security domain details.
 *
 * @author charan
 */
public class APISecurityInterceptor extends HandlerInterceptorAdapter {

  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler) throws Exception {

    long startTime = System.currentTimeMillis();
    request.setAttribute("startTime", startTime);

    return true;
  }

}
