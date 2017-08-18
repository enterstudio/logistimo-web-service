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


import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;

import com.logistimo.logger.XLog;
import com.logistimo.services.files.AppEngineFile;
import com.logistimo.services.files.FileService;
import com.logistimo.services.files.FileServiceFactory;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by charan on 01/10/14.
 */
@Component
public class HDFSBlobStoreService extends CommonBlobStoreService {

  public static final String
      rootPath =
      ConfigUtil.get("blobstore.upload.root.path", "/user/logistimoapp/uploads/");
  private static final XLog LOGGER = XLog.getLog(HDFSBlobStoreService.class);
  private Configuration configuration;
  private FileSystem fileSystem;

  public HDFSBlobStoreService() {
    configuration = new Configuration();
    init();
  }

  private synchronized void init() {
    if (fileSystem == null) {

      LOGGER.info("HDFS Root path {0}", rootPath);
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
      } catch (Exception e) {
        LOGGER.severe("Failed to initialize HDFS filesystem ", e);
        throw new BlobStoreServiceException(e);
      }
    }
  }

  public byte[] fetchFull(String blobKey) {
    InputStream inputStream = null;
    try {
      inputStream = getInputStream(blobKey);
      return IOUtils.toByteArray(inputStream);
    } catch (IOException e) {
      LOGGER.severe("Failed to read data for blobkey {0}", blobKey, e);
      throw new BlobStoreServiceException(e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          //ignored
        }
      }
    }
  }


  public byte[] fetchData(String blobKey, long startIndex, long l) {
    CountingInputStream inputStream = new CountingInputStream(getInputStream(blobKey));
    byte[] bytes = new byte[(int) l];
    try {
      int readSize = inputStream.read(bytes, (int) startIndex, (int) l);
      if (readSize < l) {
        bytes = ArrayUtils.subarray(bytes, 0, readSize - 1);
      }
    } catch (IOException e) {
      LOGGER.warn("Failed to read bytes", e);
    } finally {
      try {
        inputStream.close();
      } catch (IOException ignored) {
        LOGGER.warn("Exception while closing inputStream", ignored);
      }
    }
    return bytes;
  }

  public void delete(String blobKeyStr) {
    Path path = new Path(getFullPath(blobKeyStr));
    try {
      if (fileSystem.exists(path)) {
        fileSystem.delete(path, true);
        fileSystem.delete(new Path(getMetaPath(blobKeyStr)), true);
      }
    } catch (IOException e) {
      LOGGER.severe("Failed to delete blob with key {0}", blobKeyStr, e);
      throw new BlobStoreServiceException(e);
    }
  }


  @Override
  public InputStream getInputStream(String blobKey) {
    Path path = new Path(getFullPath(blobKey));
    InputStream inputStream = null;
    try {
      if (fileSystem.exists(path)) {
        inputStream = fileSystem.open(path);
      }
    } catch (IOException e) {
      LOGGER.severe("Failed to read file from FS with blob key {0}", blobKey, e);
      throw new BlobStoreServiceException(e);
    }
    return inputStream;
  }

  private String getFullPath(String blobKey) {
    return rootPath + blobKey;
  }

  private String getMetaPath(String blobKey) {
    return rootPath + blobKey + ".meta";
  }

  @Override
  public BlobInfo getBlobInfo(String blobKey) {
    BlobInfo blobInfo = null;
    Path path = new Path(getMetaPath(blobKey));
    FSDataInputStream inputStream = null;
    try {
      if (fileSystem.exists(path)) {
        inputStream = fileSystem.open(path);
        Properties properties = new Properties();
        properties.load(inputStream);
        blobInfo = new BlobInfo(new BlobKey(blobKey), properties.getProperty("contentType"),
            new Date(Long.valueOf(properties.getProperty("created"))),
            properties.getProperty("fileName"),
            Long.valueOf(properties.getProperty("size")));
      }
    } catch (IOException e) {
      LOGGER.severe("Failed to read blob info for key {0}", blobKey, e);
      throw new BlobStoreServiceException(e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          LOGGER.warn("Error while closing HDFS input stream", e);
        }
      }
    }

    return blobInfo;
  }

  @Override
  public String store(String fileName, String contentType, long sizeInBytes,
                      InputStream inputStream) {
    return store(fileName, contentType, sizeInBytes, inputStream, null);
  }

  @Override
  public String store(String fileName, String contentType, long sizeInBytes,
                      InputStream inputStream, String pathPrefix) {
    final String blobKey = DigestUtils.md5Hex((fileName + System.currentTimeMillis()).getBytes());
    Calendar calendar = Calendar.getInstance();

    String
        subPath =
        (StringUtils.isNotBlank(pathPrefix) ? pathPrefix : "") + "/" + calendar.get(Calendar.YEAR)
            + "/" + calendar.get(Calendar.MONTH) + "/";
    String filePath = rootPath + subPath;
    long size = 0;
    FSDataOutputStream stream = null;
    FSDataOutputStream metaStream = null;
    Path fsPath = null;
    try {
      Path path = new Path(filePath);
      if (!fileSystem.exists(path)) {
        fileSystem.mkdirs(path);
      }

      fsPath = new Path(path, blobKey);
      stream = fileSystem.create(fsPath);
      size = ByteStreams.copy(inputStream, stream);

      Path metaFile = new Path(path, blobKey + ".meta");
      Properties properties = new Properties();
      properties.setProperty("contentType", contentType);
      properties.setProperty("fileName", fileName);
      properties.setProperty("size", String.valueOf(size));
      properties.setProperty("created", String.valueOf(System.currentTimeMillis()));
      metaStream = fileSystem.create(metaFile);
      properties.store(metaStream, "File created on " + new Date());

    } catch (IOException e) {
      LOGGER.severe("Failed to store file {0}", fileName, e);
      throw new BlobStoreServiceException(e);
    } finally {
      try {
        if (stream != null) {
          stream.close();
        }
      } catch (IOException e) {
        LOGGER.severe("Failed to close stream for file {0}, hdfs file {1}", fileName, blobKey, e);
      }
      try {
        if (metaStream != null) {
          metaStream.close();
        }
      } catch (IOException e) {
        LOGGER.severe("Failed to close meta stream for file {0} , hdfs file {1}", fileName,
            blobKey + ".meta", e);
      }

    }
    return subPath + blobKey;
  }

  public byte[] read(String blobKey) {
    return fetchFull(blobKey);
  }

  // Write bytes into Blobstore with the specified filename and MIME type; returns the blob key as a String
  public BlobHandle write(byte[] data, String fileName, String mimeType, boolean finalize) {
    LOGGER.fine("Entering writeBytes");
    BlobHandle handle = new BlobHandle();
    // Get a file service
    FileService fileService = FileServiceFactory.getFileService();
    try {
      // Create a new Blob file with mime-type
      AppEngineFile file = fileService.createNewBlobFile(mimeType, fileName);
      // Open a channel to write to it
      boolean lock = finalize;
      FileChannel writeChannel = fileService.openWriteChannel(file, lock);
      DataOutputStream out = new DataOutputStream(Channels.newOutputStream(writeChannel));
      // This time we write to the channel using standard Java
      out.write(data);
      out.close();
      // Now read from the file using the Blobstore API
      handle.filePath = file.getFullPath();
      // Now finalize, if needed
      if (finalize) {
        writeChannel.close();
        // Get the blob key, which is available for finalized blobs
        BlobKey blobKey = fileService.getBlobKey(file);
        if (blobKey != null) {
          handle.blobKey = blobKey.getKeyString();
        }
      }
    } catch (Exception e) {
      LOGGER.severe(
          "Exception while trying to write to blobstore: file = {0}, mimeType = {1}: {2} : {3}",
          fileName, mimeType, e.getClass().getName(), e.getMessage());
    }
    LOGGER.fine("Exiting uploadToBlobStore");
    return handle;
  }

  // Remove a blob from the blob store
  public void remove(String blobKeyStr) {
    LOGGER.fine("Entering remove");
    //BlobKey blobKey = new BlobKey(blobKeyStr);
    try {
      delete(blobKeyStr);
    } catch (Exception e) {
      LOGGER.severe("Exception {0} when removing blob with key {1}: {2}", e.getClass().getName(),
          blobKeyStr, e.getMessage());
    }
    LOGGER.fine("Exiting remove");
  }

  // Serve a blob over a HTTP response channel
  public void serve(String blobKeyStr, HttpServletResponse resp) throws IOException {
    LOGGER.fine("Entering serve");
    BlobInfo bInfo = getBlobInfo(blobKeyStr);
    if (bInfo != null) {
      resp.setContentType(bInfo.getContentType());
      resp.setContentLength((int) bInfo.getSize());
      byte[] bytes = fetchFull(blobKeyStr);
      resp.getOutputStream().write(bytes);
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    LOGGER.fine("Exiting serve");
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
