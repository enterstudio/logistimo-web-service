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

package com.logistimo.api.servlets;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.proto.JsonBean;
import com.logistimo.proto.ProtocolException;
import com.logistimo.utils.ParamChecker;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public abstract class JsonRestServlet extends SgServlet {
  protected static final String UTF8_CHARSET = "; charset=\"UTF-8\"";
  protected static final String JSON_UTF8 = "application/json" + UTF8_CHARSET;

  protected static final String XML_UTF8 = "application/xml" + UTF8_CHARSET;

  protected static final String TEXT_UTF8 = "text/plain" + UTF8_CHARSET;
  protected RestResource[] resources;

  protected JsonRestServlet(RestResource... resources) {
    this.resources = resources;
  }

  protected JsonRestServlet() {

  }

  protected final void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException,
      IOException {
    try {
      //validateRestUrl(request.getMethod(), getResourceName(request), request.getParameterMap());
      super.service(request, response);
    } catch (XServletException se) {
      sendErrorResponse(response, se.getHttpStatusCode(), se.getErrorCode().toString(),
          se.getMessage());
    }
  }

  protected String getResourceName(HttpServletRequest request) {
    String requestPath = request.getPathInfo();
    if (requestPath != null) {
      while (requestPath.startsWith("/")) {
        requestPath = requestPath.substring(1);
      }
      requestPath = requestPath.trim();
    } else {
      requestPath = "";
    }
    return requestPath;
  }

  protected void sendJsonResponse(HttpServletResponse response, int statusCode, JsonBean bean)
      throws IOException, ProtocolException {
    sendJsonResponse(response, statusCode, bean.toJSONString());
  }

  protected void sendJsonResponse(HttpServletResponse response, int statusCode, String jsonString)
      throws IOException {
    response.setStatus(statusCode);
    response.setContentType(JSON_UTF8);
    PrintWriter pw = response.getWriter();
    pw.write(jsonString);
    pw.close();
  }

  protected void sendJsonResponse(HttpServletResponse response, int statusCode, JSONObject jsonObj)
      throws IOException, JSONException {
    response.setStatus(statusCode);
    response.setContentType(JSON_UTF8);
    //jsonObj.writeJSONString(response.getWriter()); // used earlier with json.simple
    jsonObj.write(response.getWriter());
  }

  protected void sendJsonListResponse(HttpServletResponse response, int statusCode,
                                      List<JSONObject> jsonObjList)
      throws IOException, JSONException {
    response.setStatus(statusCode);
    response.setContentType(JSON_UTF8);
    Iterator<JSONObject> it = jsonObjList.iterator();
    while (it.hasNext()) {
      JSONObject jsonObj = it.next();
      //jsonObj.writeJSONString(response.getWriter()); // used earlier with json.simple
      jsonObj.write(response.getWriter());
    }
  }

  protected void sendErrorResponse(HttpServletResponse response, int statusCode, String errorCode,
                                   String errorMsg)
      throws IOException {
    response.setStatus(statusCode);
    response.setHeader("ws.error.code", errorCode);
    response.setHeader("ws.error.message", errorMsg);
  }

  // Override parents method on sending response - typically, error response
  @Override
  protected void sendResponse(HttpServletRequest request, HttpServletResponse response, String msg)
      throws IOException {
    if (msg == null) {
      return;
    }
    sendJsonResponse(response, 200, msg);
  }

  // Get JSON input
  protected String getJsonInput(HttpServletRequest req) throws IOException {
    return IOUtils.toString(req.getInputStream());
  }

  public static class RestParameter {
    private String name;
    @SuppressWarnings({"rawtypes", "unused"})
    private Class type;
    @SuppressWarnings("unused")
    private List<String> methods;
    @SuppressWarnings("unused")
    private boolean mandatory;

    @SuppressWarnings("rawtypes")
    public RestParameter(String name, Class type, boolean isMandatory, List<String> methods) {
      this.name = ParamChecker.notEmpty(name, "name");
      if (type != Integer.class && type != Float.class && type != String.class) {
        throw new IllegalArgumentException("Only integer, float and String params are supported");
      }
      this.type = ParamChecker.notNull(type, "type");
      this.mandatory = isMandatory;
      this.methods = ParamChecker.notNullElements(methods, "methods");
    }
  }

  public static class RestResource {
    @SuppressWarnings("unused")
    private String name;
    private Map<String, RestParameter> paramMap;
    @SuppressWarnings("unused")
    private List<String> methods;
    @SuppressWarnings("unused")
    private boolean isWildCard;

    public RestResource(String name, List<String> methods, List<RestParameter> params) {
      this.name = ParamChecker.notEmpty(name, "name");
      isWildCard = name.equals("*");
      ParamChecker.notNullElements(methods, "methods");
      if (methods.size() == 0) {
        throw new IllegalArgumentException(
            "Atleast one HTTP method must be provided for the resource to be accessible.");
      }
      this.methods = methods;

      ParamChecker.notNullElements(params, "params");
      paramMap = new HashMap<String, RestParameter>();
      for (RestParameter param : params) {
        paramMap.put(param.name, param);
      }
    }

  }
}
