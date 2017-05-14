package com.logistimo.services.blobstore;

import com.logistimo.logger.XLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by charan on 01/10/14.
 */
public abstract class CommonBlobStoreService implements BlobstoreService {


  private static final XLog LOGGER = XLog.getLog(CommonBlobStoreService.class);

  @Override
  public Map<String, List<String>> getUploads(HttpServletRequest req) {
    Map<String, List<String>> response = null;
    HttpSession session = req.getSession();
    Map<String, String> blobs = (Map<String, String>) session.getAttribute("blobs");
    for (String name : blobs.keySet()) {
      response = new HashMap<String, List<String>>(1);
      List<String> keys = new ArrayList<String>(1);
      keys.add(blobs.get(name));
      response.put(name, keys);
    }
    session.removeAttribute("blobs");
    return response;
  }

  @Override
  public Map<String, String> getUploadedBlobs(HttpServletRequest req) {
    Map<String, String> response = null;
    HttpSession session = req.getSession();
    Map<String, String> blobs = (Map<String, String>) session.getAttribute("blobs");
    for (String name : blobs.keySet()) {
      response = new HashMap<String, String>(1);
      response.put(name, blobs.get(name));
    }
    session.removeAttribute("blobs");
    return response;
  }


  @Override
  public String createUploadUrl(String url) {
    try {
      return "/s/fileupload?ru=" + URLEncoder.encode(url, "utf-8");
    } catch (UnsupportedEncodingException e) {
      LOGGER.warn("Failed to encode url " + url, e);
    }
    return null;
  }
}
