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

package com.logistimo.api.builders;

import com.logistimo.dao.JDOUtils;
import com.logistimo.media.SupportedMediaTypes;
import com.logistimo.media.entity.IMedia;
import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.api.models.MediaModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mohan Raja
 */
public class MediaBuilder {

  public static final String
      rootPath =
      ConfigUtil.get("blobstore.upload.root.path", "/user/logistimoapp/uploads/");
  private static final String servingUrlLocal = ConfigUtil.get("media.servingurllocal",
      "http://localhost:50070/webhdfs/v1");
  private static boolean isLocal = ConfigUtil.getBoolean("local.environment", false);

  public IMedia constructMedia(MediaModel model) {
    if (model == null) {
      return null;
    }
    IMedia media = JDOUtils.createInstance(IMedia.class);
    media.setMediaType(SupportedMediaTypes.valueOf(model.mediaType));
    media.setDomainKey(model.domainKey);
    media.setContent(model.content.value);
    return media;
  }

  public List<MediaModel> constructMediaModelList(List<IMedia> mediaList) {
    if (mediaList == null) {
      return null;
    }
    List<MediaModel> modelList = new ArrayList<>(mediaList.size());
    for (IMedia media : mediaList) {
      MediaModel model = constructMediaModel(media);
      if (model != null) {
        modelList.add(model);
      }
    }
    return modelList;
  }

  public MediaModel constructMediaModel(IMedia media) {
    return constructMediaModel(media, null);
  }

  public MediaModel constructMediaModel(IMedia media, String field) {
    if (media == null) {
      return null;
    }
    MediaModel model = new MediaModel();
    model.id = media.getId();
    if (field == null) {
      model.content = new MediaModel.MediaText();
      model.content.value = media.getContent();
      model.domainKey = media.getDomainKey();
      model.mediaType = media.getMediaType().toString();
    }
    model.fn = field;
    if (isLocal) {
      model.servingUrl =
          servingUrlLocal + rootPath.substring(0, rootPath.length() - 1) +
              media.getServingUrl() + "?op=OPEN";
    } else {
      model.servingUrl =
          "/media" + rootPath.substring(0, rootPath.length() - 1) + media.getServingUrl();
    }
    return model;
  }
}
