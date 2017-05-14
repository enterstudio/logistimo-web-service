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

package com.logistimo.services.mapper;


import java.io.IOException;

public class GenericMapper<T1, T2, T3, T4> {

  private Context context;
  private Configuration configuration;

  public Context getContext(Context context) {
    return this.context;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public void taskSetup(Context context) throws IOException, InterruptedException {
    setContext(context);
  }

  public void taskCleanup(Context context) throws IOException, InterruptedException {

    try {
      context.close();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  public void map(T1 t1, T2 t2, Context context) {

  }

  public static class Context {

    private Configuration configuration;
    private DatastoreMutationPool mutationPool;

    public Context(Configuration configuration) {
      this.configuration = configuration;
    }

    public Configuration getConfiguration() {
      return configuration;
    }

    public synchronized DatastoreMutationPool getMutationPool() {
      if (mutationPool == null) {
        mutationPool = new DatastoreMutationPool(100);
      }
      return mutationPool;
    }

    public void close() throws Exception {
      if (mutationPool != null) {
        mutationPool.close();
      }
    }
  }


}
