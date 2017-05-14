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
}
