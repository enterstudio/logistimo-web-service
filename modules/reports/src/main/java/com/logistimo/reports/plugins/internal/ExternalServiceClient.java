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

import com.logistimo.reports.plugins.IExteranalServiceClient;
import com.logistimo.reports.plugins.config.ExternalServiceClientConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

/**
 * @author Mohan Raja
 */
public class ExternalServiceClient implements IExteranalServiceClient {
  ExternalServiceClientConfiguration configuration;
  Client client;

  public static ExternalServiceClient getNewInstance() {
    return new ExternalServiceClient(new ExternalServiceClientConfiguration(), ClientBuilder.newClient());
  }

  public ExternalServiceClient(ExternalServiceClientConfiguration configuration, Client client) {
    this.configuration = configuration;
    this.client = client;
  }

  @Override
  public Response getResponse(String id) {
    GetAPICommand command = new GetAPICommand(configuration.getUrl(), client, id);
    return command.execute();
  }

  @Override
  public Response postRequest(QueryRequestModel request) {
    PostAPICommand command = new PostAPICommand(configuration.getUrl(), client, request);
    return command.execute();
  }

  public ExternalServiceClientConfiguration getConfiguration() {
    return configuration;
  }

  public Client getClient() {
    return client;
  }
}
