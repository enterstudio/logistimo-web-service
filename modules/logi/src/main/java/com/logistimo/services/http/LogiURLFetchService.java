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

package com.logistimo.services.http;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.net.URL;

/**
 * Created by charan on 16/03/15.
 */
public class LogiURLFetchService implements URLFetchService {

  @Override
  public HttpResponse post(URL urlObj, byte[] payload, String userName, String password,
                           int timeout) {
    HttpClient client = new HttpClient();
    PostMethod method = new PostMethod(urlObj.toString());
    method.setRequestEntity(new ByteArrayRequestEntity(payload));
    method.setRequestHeader("Content-type", "application/json");
    client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
        new DefaultHttpMethodRetryHandler());
    client.getParams().setSoTimeout(1000 * timeout);
    client.getParams().setConnectionManagerTimeout(1000 * timeout);
    if (userName != null && password != null) {
      setBasicAuthorization(method, userName, password);
    }
    try {
      int response = client.executeMethod(method);
      return new HttpResponse(response, method.getResponseBody());
    } catch (IOException e) {
      throw new RuntimeException("Failed to process post request URL: " + urlObj, e);
    } finally {
      method.releaseConnection();
    }

  }

  @Override
  public HttpResponse get(URL urlObj, String userName, String password, int timeout) {
    HttpClient client = new HttpClient();
    HttpMethod method = new GetMethod(urlObj.toString());

    client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
        new DefaultHttpMethodRetryHandler());
    client.getParams().setSoTimeout(1000 * timeout);
    client.getParams().setConnectionManagerTimeout(1000 * timeout);
    if (userName != null && password != null) {
      setBasicAuthorization(method, userName, password);
    }
    try {
      int response = client.executeMethod(method);
      return new HttpResponse(response, method.getResponseBody());
    } catch (IOException e) {
      throw new RuntimeException("Failed to get " + urlObj.toString(), e);
    } finally {
      method.releaseConnection();
    }
  }

  private void setBasicAuthorization(HttpMethod method, String userName, String password) {
    String userpass = userName + ":" + password;
    String
        basicAuth =
        "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
    method.addRequestHeader(new Header("Authorization", basicAuth));
  }
}
