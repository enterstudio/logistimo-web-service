package com.logistimo.services.blobstore;

public class BlobKey {

  private String blobKey;

  public BlobKey(String blobKey) {
    this.blobKey = blobKey;
  }

  public String getKeyString() {

    return blobKey;
  }


  public boolean isEmpty() {
    return blobKey == null || blobKey.isEmpty();
  }
}
