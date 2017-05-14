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
