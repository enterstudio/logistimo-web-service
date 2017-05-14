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

import com.logistimo.AppFactory;
import com.logistimo.services.blobstore.BlobKey;
import com.logistimo.services.blobstore.BlobStoreServiceException;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by charan on 02/10/14.
 */
public class HDFSStorageUtil implements StorageUtil {

  public static final String
      rootPath =
      ConfigUtil.get("blobstore.root.path", "/user/logistimoapp/");
  private static final XLog LOGGER = XLog.getLog(HDFSStorageUtil.class);
  Configuration configuration = null;
  BlobstoreService blobstoreService = null;
  private FileSystem fileSystem;

  public HDFSStorageUtil() {
    this.blobstoreService = AppFactory.get().getBlobstoreService();
    configuration = new Configuration();
    try {
      fileSystem = FileSystem.get(configuration);
      if (!fileSystem.exists(new Path(rootPath))) {
        fileSystem.mkdirs(new Path(rootPath));
      }
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          try {
            fileSystem.close();
          } catch (IOException e) {
            LOGGER.warn("Exception while closing fileSystem", e);
          }
        }
      });
    } catch (IOException e) {
      LOGGER.severe("Failed to instantiate fileSystem", e);
      throw new BlobStoreServiceException(e);
    }
  }

  @Override
  public byte[] read(String blobKey) {
    return blobstoreService.read(blobKey);
  }

  @Override
  public InputStream readAsBlobstoreInputStream(BlobKey blobKey) throws IOException {
    return blobstoreService.getInputStream(blobKey.getKeyString());
  }

  @Override
  public OutputStream getOutputStream(String bucketName, String filename, boolean append)
      throws IOException {
    String finalPathStr = rootPath + "/" + bucketName + "/";
    FSDataOutputStream stream = null;
    try {
      Path parent = new Path(finalPathStr);
      if (!fileSystem.exists(parent)) {
        fileSystem.mkdirs(parent);
      }
      Path finalPath = new Path(parent, filename);
      if (!fileSystem.exists(finalPath)) {
        stream = fileSystem.create(finalPath);
      } else {
        if (append) {
          stream = fileSystem.append(finalPath);
        } else {
          removeFile(bucketName, filename);
          stream = fileSystem.create(finalPath);
        }
      }
    } catch (Exception e) {
      LOGGER.severe("Failed to create output stream for bucket: {0} and file: {1}", bucketName,
          filename, e);
      throw new BlobStoreServiceException(e);
    }
    return stream;
  }

  @Override
  public OutputStream getOutputStream(String bucketName, String filename) throws IOException {
    return getOutputStream(bucketName, filename, true);
  }

  @Override
  public InputStream getInputStream(String bucketName, String filename)
      throws IOException, ClassNotFoundException {
    Path finalPath = new Path(rootPath + "/" + bucketName + "/" + filename);
    InputStream inputStream = null;
    try {
      if (fileSystem.exists(finalPath)) {
        inputStream = fileSystem.open(finalPath);
      } else {
        throw new IOException("File not found in bucket " + bucketName + " fileName " + filename);
      }
    } catch (Exception e) {
      LOGGER
          .severe("Failed to get input stream for bucket: {0} and file: {1}", bucketName, filename,
              e);
      throw new BlobStoreServiceException(e);
    }
    return inputStream;
  }

  @Override
  public long getFileSizeInBytes(String bucketName, String filename)
      throws IOException, ClassNotFoundException {
    Path finalPath = new Path(rootPath + "/" + bucketName + "/" + filename);
    try {
      if (fileSystem.exists(finalPath)) {
        return fileSystem.getFileStatus(finalPath).getLen();
      } else {
        throw new IOException("File not found in bucket " + bucketName + " fileName " + filename);
      }
    } catch (Exception e) {
      LOGGER
          .severe("Failed to get input stream for bucket: {0} and file: {1}", bucketName, filename,
              e);
      throw new BlobStoreServiceException(e);
    }
  }

  @Override
  public void removeFile(String bucketName, String fileName) throws IOException {
    Path finalPath = new Path(rootPath + "/" + bucketName + "/" + fileName);
    try {
      if (fileSystem.exists(finalPath)) {
        fileSystem.delete(finalPath, true);
      } else {
        LOGGER.warn("File requested for delete doesn't exist bucket {0} and file {1}", bucketName,
            fileName);
      }
    } catch (Exception e) {
      LOGGER.severe("Failed to delete bucket {0} and file {1}", bucketName, fileName, e);
      throw new IOException(e);
    }
  }

  // Get the application name - e.g. logistimo-web, logistimo-dev
  @Override
  public String getApplicationName() {
    return System.getProperty("name", "logistimo-dev");
  }

  @Override
  public String getQualifiedBucketName(String simpleBucketName) {
    return simpleBucketName;
  }

  @Override
  public void write(String bucketName, String filename, byte[] data, String mimeType)
      throws IOException {
    write(bucketName, filename, data, mimeType, false);
  }


  public void write(String bucketName, String filename, byte[] data, String mimeType,
                    boolean append) throws IOException {
    OutputStream outputStream = null;
    try {
      outputStream = getOutputStream(bucketName, filename, append);
      outputStream.write(data);
    } catch (Exception e) {
      LOGGER
          .severe("Failed to write content to HDFS bucket: {0} fileName: {1} data: {2}", bucketName,
              filename, data, e);
      throw new IOException(e);
    } finally {
      if (outputStream != null) {
        outputStream.close();
      }
    }
  }

  @Override
  public void write(String bucketName, String filename, byte[] data) throws IOException {
    write(bucketName, filename, data, null);
  }

  @Override
  public void append(String bucketName, String filename, byte[] data) throws IOException {
    // Append is same as write, since getOutputStream fetch append /new write stream accordingly.
    write(bucketName, filename, data, null, true);
  }

  @Override
  public byte[] readFile(String bucketName, String fileName)
      throws IOException, ClassNotFoundException {
    InputStream is = null;
    try {
      is = getInputStream(bucketName, fileName);
      if (is != null) {
        return IOUtils.toByteArray(is);
      }
    } catch (IOException e) {
      LOGGER.severe("Failed to read data for blobkey {0}", fileName, e);
      throw new BlobStoreServiceException(e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          LOGGER.warn("Error while closing HDFS input stream", e);
        }
      }
    }
    LOGGER.warn("Store file in bucket {0} with name {1} not found", bucketName, fileName);
    return null;
  }

  @Override
  public void write(String bucketName, String filename, String data, String mimeType)
      throws IOException {
    try {
      write(bucketName, filename, data.getBytes("UTF-8"), mimeType);
    } catch (UnsupportedEncodingException e) {
      LOGGER.severe("{0} while getting bytes from data. Message: {1}", e.getClass().getName(),
          e.getMessage());
    }
  }

  @Override
  public void write(String bucketName, String filename, String data) throws IOException {
    write(bucketName, filename, data, null);
  }

  @Override
  public void append(String bucketName, String filename, String data) throws IOException {
    try {
      append(bucketName, filename, data.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      LOGGER.severe("{0} while getting bytes from data. Message: {1}", e.getClass().getName(),
          e.getMessage());
    }
  }


  public void close() {
    if (fileSystem != null) {
      try {
        fileSystem.close();
      } catch (IOException e) {
        LOGGER.warn("Exception while closing fileSystem", e);
      }
    }
  }

}
