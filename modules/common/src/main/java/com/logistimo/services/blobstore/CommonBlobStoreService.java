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
