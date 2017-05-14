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

package com.logistimo.media.endpoints;

import com.logistimo.AppFactory;
import com.logistimo.dao.JDOUtils;
import com.logistimo.media.entity.IMedia;
import com.logistimo.media.entity.Media;
import com.logistimo.services.blobstore.BlobKey;
import com.logistimo.services.blobstore.BlobstoreService;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.logger.XLog;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.InvalidServiceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Mohan Raja
 */
public class MediaEndPoint implements IMediaEndPoint {

  private static final XLog logger = XLog.getLog(MediaEndPoint.class);

  public IMedia insertMedia(IMedia media) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    InputStream is = null;
    try {
      if (media.getId() != null) {
        try {
          JDOUtils.getObjectById(IMedia.class, media.getId());
        } catch (JDOObjectNotFoundException e) {
          logger.info("Object already exists: ", e);
          throw new InvalidServiceException("Object already exists");
        }
      }
      if (StringUtils.isEmpty(media.getContent())) {
        throw new BadRequestException("content is a required field");
      }
      if (StringUtils.isEmpty(media.getDomainKey())) {
        throw new BadRequestException("domainKey is a required field");
      }
      BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
      is = new ByteArrayInputStream(Base64.decodeBase64(media.getContent().getBytes()));
      String
          blobKeyStr =
          blobstoreService
              .store(media.getDomainKey(), media.getMediaType().toString(), 0, is, "/media");
      media.setBlobKey(new BlobKey(blobKeyStr));
      media.setServingUrl(blobKeyStr);
      media.setUploadTime(new Date());
      pm.makePersistent(media);
      media = pm.detachCopy(media);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException ignored) {
          logger.warn("Exception while closing inputstream", ignored);
        }
      }
      pm.close();
    }
    return media;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<IMedia> getMedias(String domainKey) {
    Query q = null;
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      q = pm.newQuery(Media.class, "domainKey == '" + domainKey + "'");
      List<IMedia> result = (List<IMedia>) q.execute();
      return (List<IMedia>) pm.detachCopyAll(result);
    } finally {
      if (q != null) {
        try {
          q.closeAll();
        } catch (Exception ignored) {
          logger.warn("Exception while closing query", ignored);
        }
      }
      if (pm != null) {
        pm.close();
      }
    }
  }

  @Override
  public void removeMedia(Long id) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Media media;
    try {
      media = pm.getObjectById(Media.class, id);
      BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
      blobstoreService.remove(media.getServingUrl());
      pm.deletePersistent(media);
    } catch (Exception e) {
      throw new ServiceException("Error while deleting media", e);
    } finally {
      pm.close();
    }
  }
}
