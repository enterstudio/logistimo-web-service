package com.logistimo.api.filters;

import org.apache.commons.lang.StringUtils;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Mohan Raja on 02/04/15
 */
public class MediaSecurityFilter implements Filter {


  public static final String MEDIA_ENDPOINT_URL = "/_ah/api/mediaendpoint";

  private static final XLog xLogger = XLog.getLog(APISecurityFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    String servletPath = req.getServletPath() + req.getPathInfo();
    xLogger.fine("Servlet path: ", servletPath);
    if (req.getCharacterEncoding() == null) {
      request.setCharacterEncoding(Constants.UTF8);
    }

    if (!(StringUtils.isNotBlank(servletPath) && servletPath.startsWith(MEDIA_ENDPOINT_URL))) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    if (filterChain != null) {
      filterChain.doFilter(request, response);
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void destroy() {

  }
}
