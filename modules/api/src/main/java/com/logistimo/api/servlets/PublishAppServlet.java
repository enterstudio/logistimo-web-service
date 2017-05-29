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

package com.logistimo.api.servlets;

import com.logistimo.AppFactory;
import com.logistimo.dao.JDOUtils;
import com.logistimo.services.blobstore.BlobstoreService;

import com.logistimo.entity.IUploaded;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.UploadService;
import com.logistimo.services.impl.UploadServiceImpl;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class PublishAppServlet extends SgServlet {
  private static final XLog xLogger = XLog.getLog(PublishAppServlet.class);

  private BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();

  @Override
  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    processPost(req, resp, backendMessages, messages);
  }

  @Override
  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entered processPost");
    String version = req.getParameter("version");
    String locale = req.getParameter("locale");
    String description = req.getParameter("description");
    Date timeStamp = new Date();
    String userId = req.getParameter("userid");

    resp.setContentType("text/plain");

    // Retrieves the blob key for the jad and jar files that were uploaded via addpublish.jsp.
    // If it fails, then redirects the user to an error page.
    Map<String, String> blobs = blobstoreService.getUploadedBlobs(req);
    Iterator<Entry<String, String>> entries = blobs.entrySet().iterator();
    List<IUploaded> uploads = new Vector<IUploaded>();

    while (entries.hasNext()) {
      Entry<String, String> thisEntry = (Entry<String, String>) entries.next();
      String fileName = (String) thisEntry.getKey();
      String blobKey = (String) thisEntry.getValue();
      if (blobKey == null) {
        xLogger.severe("BlobKey is null for file {0} ", fileName);
        throw new ServiceException("Blobkey is null for file " + fileName);
      }
      xLogger.info("fileName = {0} : blobKey = {1} ", fileName, blobKey);
      String blobKeyStr = blobKey;

      // Create an Uploaded object and set it's attributes
      IUploaded u = JDOUtils.createInstance(IUploaded.class);

      u.setFileName(fileName);
      u.setDescription(description);
      u.setVersion(version);
      u.setLocale(locale);
      u.setUserId(userId);
      u.setTimeStamp(timeStamp);
      u.setBlobKey(blobKeyStr);
      String key = JDOUtils.createUploadedKey(fileName, version, locale);
      u.setId(key);

      // Append it to the uploads vector
      uploads.add(u);

    }

    writeToDataStore(uploads);

    xLogger.fine("Exiting processPost");
    sendResponse(req, resp, "Successfully published the jad and jar files");

  }

  private void writeToDataStore(List<IUploaded> uploads) {
    xLogger.fine("Entered writeToDataStore");

    try {
      UploadService ums = null;
      // Create the UploadMgmtServiceImpl object
      ums = Services.getService(UploadServiceImpl.class);
      ums.addNewUpload(uploads);
    } catch (ServiceException se) {
      xLogger.severe("ServiceException: {0}", se.getMessage());
    }

    xLogger.fine("Exiting writeToDataStore");
  }

  @Override
  protected void sendResponse(HttpServletRequest request, HttpServletResponse response, String msg)
      throws IOException {
    response.sendRedirect("/pub/message.jsp?msg=" + msg);
  }


}