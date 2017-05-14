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
