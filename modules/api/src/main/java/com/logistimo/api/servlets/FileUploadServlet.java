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
import com.logistimo.services.blobstore.BlobstoreService;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.logistimo.logger.XLog;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileUploadServlet extends HttpServlet {
  private static final XLog _logger = XLog.getLog(FileUploadServlet.class);

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
    // Check that we have a file upload request
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    if (!isMultipart) {
      return;
    }
    try {
      MultipartHttpServletRequest
          multiRequest =
          new CommonsMultipartResolver().resolveMultipart(request);
      MultiValueMap<String, MultipartFile> fileMap = multiRequest.getMultiFileMap();
      BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
      Map<String, String> names = new HashMap<>(1);
      for (String fieldName : fileMap.keySet()) {
        MultipartFile file = fileMap.getFirst(fieldName);
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long sizeInBytes = file.getSize();
        String
            blobKey =
            blobstoreService.store(fileName, contentType, sizeInBytes, file.getInputStream());
        names.put(fieldName, blobKey);

      }
      request.getSession().setAttribute("blobs", names);
      RequestDispatcher
          dispatcher =
          getServletContext().getRequestDispatcher(request.getParameter("ru"));
      dispatcher.forward(multiRequest, response);
    } catch (Exception ex) {
      _logger.severe("Upload failed", ex);
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
    throw new ServletException(
        "GET method used with " + getClass().getName() + ": POST method required.");
  }
}