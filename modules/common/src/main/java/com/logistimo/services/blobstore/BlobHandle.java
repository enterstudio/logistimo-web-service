package com.logistimo.services.blobstore;

/**
 * Created by charan on 03/03/15.
 */
public class BlobHandle {


  public String filePath = null;
  public String blobKey = null;

  public BlobHandle() {
  }

  public BlobHandle(String handleString) {
    if (handleString == null || handleString.isEmpty()) {
      return;
    }
    int index = handleString.indexOf(",");
    if (index == 0) {
      return;
    }
    if (index == -1) {
      filePath = handleString;
    }
    filePath = handleString.substring(0, index);
    blobKey = handleString.substring(index + 1);
  }

  public String toString() {
    return (filePath != null ? filePath : "") + "," + (blobKey != null ? blobKey : "");
  }

}
