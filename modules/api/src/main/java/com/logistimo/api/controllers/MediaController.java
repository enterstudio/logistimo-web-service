package com.logistimo.api.controllers;

import com.logistimo.AppFactory;
import com.logistimo.dao.JDOUtils;
import com.logistimo.media.SupportedMediaTypes;
import com.logistimo.media.endpoints.IMediaEndPoint;
import com.logistimo.media.entity.IMedia;
import com.logistimo.services.blobstore.BlobKey;
import com.logistimo.services.blobstore.BlobstoreService;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.logger.XLog;
import com.logistimo.api.builders.MediaBuilder;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.api.models.MediaModel;
import com.logistimo.api.models.MediaModels;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Mohan Raja
 */

@Controller
@RequestMapping("/mediaendpoint")
public class MediaController {
  private static final XLog xLogger = XLog.getLog(MediaController.class);

  MediaBuilder builder = new MediaBuilder();

  @RequestMapping(value = "/v1/mediaforDomain/{domainKey:.+}", method = RequestMethod.GET)
  @ResponseBody
  public MediaModels getMedia(@PathVariable String domainKey) {
    IMediaEndPoint endPoint = JDOUtils.createInstance(IMediaEndPoint.class);
    List<IMedia> mediaList = endPoint.getMedias(domainKey);
    return new MediaModels(builder.constructMediaModelList(mediaList));
  }

  @RequestMapping(value = "/v1/media/{id:.+}", method = RequestMethod.DELETE)
  @ResponseBody
  public void deleteMedia(@PathVariable Long id) {
    IMediaEndPoint endPoint = JDOUtils.createInstance(IMediaEndPoint.class);
    try {
      endPoint.removeMedia(id);
    } catch (ServiceException e) {
      xLogger.warn("Error while deleting image.", e);
      throw new InvalidServiceException("Error while deleting image.");
    }
  }

  @RequestMapping(value = "/v1/media/", method = RequestMethod.POST)
  @ResponseBody
  public MediaModels uploadMedia(@RequestBody MediaModel model) {
    IMedia media = builder.constructMedia(model);
    IMediaEndPoint endPoint = JDOUtils.createInstance(IMediaEndPoint.class);
    IMedia m = endPoint.insertMedia(media);
    List<MediaModel> modelList = new ArrayList<>(1);
    MediaModel mm = builder.constructMediaModel(m);
    if (mm != null) {
      modelList.add(mm);
    }
    return new MediaModels(modelList);
  }

  @RequestMapping(value = "/v2/media/{domainId:.+}", method = RequestMethod.POST)
  @ResponseBody
  public MediaModels uploadFileMedia(@PathVariable String domainId, HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    MediaModels mediaResponse = new MediaModels(new ArrayList<MediaModel>(1));
    if (!isMultipart) {
      throw new InvalidDataException("Media upload has to be a multi part file upload request");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      MultipartHttpServletRequest
          multiRequest =
          new CommonsMultipartResolver().resolveMultipart(request);
      MultiValueMap<String, MultipartFile> fileMap = multiRequest.getMultiFileMap();
      BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
      for (String fieldName : fileMap.keySet()) {
        MultipartFile file = fileMap.getFirst(fieldName);
        String fileName = file.getOriginalFilename();
        try {
          String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
          String contentType = file.getContentType();
          long sizeInBytes = file.getSize();
          String
              blobKey =
              blobstoreService
                  .store(domainId, contentType, sizeInBytes, file.getInputStream(), "/media");
          IMedia media = JDOUtils.createInstance(IMedia.class);
          media.setBlobKey(new BlobKey(blobKey));
          media.setServingUrl(blobKey);
          media.setUploadTime(new Date());
          media.setMediaType(SupportedMediaTypes.valueOf(fileExt));
          media.setDomainKey(domainId);
          pm.makePersistent(media);
          mediaResponse.items.add(builder.constructMediaModel(media, fieldName));
        } catch (IllegalArgumentException e) {
          throw new InvalidDataException(
              "File type should be jpg/jpeg/png/giff but was " + fileName);
        }
      }
    } catch (Exception e) {
      xLogger.severe("Error occurred while uploading image", e);
      throw e;
    } finally {
      pm.close();
    }
    return mediaResponse;
  }

  @RequestMapping(value = "/media", method = RequestMethod.GET)
  @ResponseBody
  public void serveImageURL(@RequestParam String url, HttpServletResponse response) {
    try {
      AppFactory.get().getBlobstoreService().serve(url, response);
    } catch (IOException e) {
      xLogger.warn("Error in serving image with key:" + url, e);
    }
  }
}
