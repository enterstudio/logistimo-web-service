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

package com.logistimo.services.blobstore;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface BlobstoreService {

  int MAX_BLOB_FETCH_SIZE = 1024;

  Map<String, List<String>> getUploads(HttpServletRequest req);

  Map<String, String> getUploadedBlobs(HttpServletRequest req);

  void serve(String blobKeyStr, HttpServletResponse resp) throws IOException;

  String createUploadUrl(String url);

  InputStream getInputStream(String blobKey);

  String store(String fileName, String contentType, long sizeInBytes, InputStream inputStream);

  String store(String fileName, String contentType, long sizeInBytes, InputStream inputStream,
               String pathPrefix);

  byte[] read(String blobKey);

  // Write bytes into Blobstore with the specified filename and MIME type; returns the blob key as a String
  BlobHandle write(byte[] data, String fileName, String mimeType, boolean finalize);

  // Remove a blob from the blob store
  void remove(String blobKeyStr);

  // Get the blob info.
  BlobInfo getBlobInfo(String blobKey);

}
