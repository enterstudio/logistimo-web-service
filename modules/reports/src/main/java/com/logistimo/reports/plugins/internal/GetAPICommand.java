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

package com.logistimo.reports.plugins.internal;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * @author Mohan Raja
 */
public class GetAPICommand extends HystrixCommand<Response> {

  private final String baseURL;
  private final Client client;
  public static final String EXTERNAL_SERVICE = "external-service";
  public static final String PATH = "/users/{id}";

  private String id;

  public GetAPICommand(String baseURL, Client client, String id) {
    super(HystrixCommandGroupKey.Factory.asKey(EXTERNAL_SERVICE), 30_000);
    this.baseURL = baseURL;
    this.client = client;
    this.id = id;
  }

  @Override
  public Response run() throws Exception {
    URI link = UriBuilder.fromUri(baseURL).path(PATH).build(id);
    return client.target(link).request(MediaType.APPLICATION_JSON_TYPE).get();
  }
}