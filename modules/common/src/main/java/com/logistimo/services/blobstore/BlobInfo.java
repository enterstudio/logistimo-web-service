package com.logistimo.services.blobstore;

import java.util.Date;

public class BlobInfo {

  private String filename;
  private Date creation;
  private String contentType;
  private BlobKey blobKey;
  private long size;

  public BlobInfo(BlobKey blobKey, String contentType, Date creation, String filename, long size) {
    this.blobKey = blobKey;
    this.contentType = contentType;
    this.creation = creation;
    this.filename = filename;
    this.size = size;
  }


  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public Date getCreation() {
    return creation;
  }

  public void setCreation(Date creation) {
    this.creation = creation;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public BlobKey getBlobKey() {
    return blobKey;
  }

  public void setBlobKey(BlobKey blobKey) {
    this.blobKey = blobKey;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }
}
