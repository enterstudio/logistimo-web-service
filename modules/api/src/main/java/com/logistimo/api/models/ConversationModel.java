package com.logistimo.api.models;

import java.util.Set;

/**
 * Created by kumargaurav on 04/10/16.
 */
public class ConversationModel {

  public String id;

  public String objectId;

  public String objectType;

  public Long domainId;

  public String userId;

  public String createDate;

  public String updateDate;

  public Set<String> tags;

  @Override
  public String toString() {

    StringBuilder tostr = new StringBuilder("ConversationModel{" +
        "objectId=" + objectId +
        ", objectType= " + objectType +
        ", domainId=" + domainId +
        ", userId=" + userId);

    if (null != tags && !tags.isEmpty()) {
      tostr.append(", tags=").append(tags);
    }
    tostr.append('}');
    return tostr.toString();
  }
}
