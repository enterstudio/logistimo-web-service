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