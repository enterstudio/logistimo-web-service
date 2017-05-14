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

import com.logistimo.AppFactory;
import com.logistimo.services.blobstore.BlobstoreRecordKey;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.mapred.IMapredService;

import com.logistimo.logger.XLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by charan on 29/09/14.
 */
public class BlobStoreInputFormat extends InputFormat<BlobstoreRecordKey, byte[]> {

  private static XLog xLogger = XLog.getLog(BlobStoreInputFormat.class);

  private BufferedReader in;
  private long offset = 0;

  @Override
  protected void init() {
    BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
    InputStream
        stream =
        blobstoreService.getInputStream(getConfiguration().get(IMapredService.PARAM_BLOBKEY));
    in = new BufferedReader(new InputStreamReader(stream));
  }

  @Override
  public KeyVal<BlobstoreRecordKey, byte[]> readNext() {
    KeyVal<BlobstoreRecordKey, byte[]> retVal = null;
    try {
      String line = in.readLine();
      if (line != null) {
        byte bytes[] = line.getBytes();
        retVal = new KeyVal<BlobstoreRecordKey, byte[]>(new BlobstoreRecordKey(offset), bytes);
        offset += bytes.length;
      }
    } catch (IOException e) {
      xLogger.severe("Failed to read from Blobstore for key {0}",
          getConfiguration().get(IMapredService.PARAM_BLOBKEY), e);
      try {
        in.close();
      } catch (IOException e1) {
        xLogger.warn("Exception while closing input stream", e1);
      }
    }
    return retVal;
  }

  @Override
  public void close() {
    try {
      in.close();
    } catch (IOException e1) {
      xLogger.warn("Exception while closing input stream", e1);
    }
  }
}
