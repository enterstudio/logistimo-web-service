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

package com.logistimo.services.storage;

import com.logistimo.services.blobstore.BlobKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.transaction.NotSupportedException;

/**
 * Created by charan on 02/10/14.
 */
public interface StorageUtil {

  byte[] read(String blobKey);

  /**
   * Read the blob as a BlobInputStream given the blob key
   **/
  InputStream readAsBlobstoreInputStream(BlobKey blobKey) throws IOException;

  OutputStream getOutputStream(String bucketName, String filename, boolean append)
      throws IOException;

  OutputStream getOutputStream(String bucketName, String filename) throws IOException;

  InputStream getInputStream(String bucketName, String filename)
      throws IOException, ClassNotFoundException;

  long getFileSizeInBytes(String bucketName, String filename)
      throws IOException, ClassNotFoundException, NotSupportedException;

  void removeFile(String bucketName, String fileName) throws IOException;

  // Get the application name - e.g. logistimo-web, logistimo-dev
  String getApplicationName();

  // Get the fully qualified bucket name
  String getQualifiedBucketName(String simpleBucketName);

  // Writes data to a Google Cloud Storage file in the specified bucket.
  void write(String bucketName, String filename, String data) throws IOException;

  // Writes data to a Google Cloud Storage file in the specified bucket.
  void write(String bucketName, String filename, String data, String mimeType) throws IOException;

  // Appends data to an existing Google Cloud Storage file in the specified bucket.
  void append(String bucketName, String filename, String data) throws IOException;

  // Writes data to a Google Cloud Storage file in the specified bucket.
  void write(String bucketName, String filename, byte[] data) throws IOException;

  void write(String bucketName, String filename, byte[] data, String mimeType) throws IOException;

  // Appends data to an existing Google Cloud Storage file in the specified bucket.
  void append(String bucketName, String filename, byte[] data) throws IOException;

  byte[] readFile(String bucketName, String fileName) throws IOException, ClassNotFoundException;

  String getExternalUrl(String bucketName, String fileName);
}
