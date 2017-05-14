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

package com.logistimo.media.entity;

import com.logistimo.media.SupportedMediaTypes;
import com.logistimo.services.blobstore.BlobKey;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by Mohan Raja on 13/08/15.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Media implements IMedia {
  public String domainKey;
  public String namespaceId;
  @NotPersistent
  public String content;
  public BlobKey blobKey;
  public SupportedMediaTypes mediaType;
  public String servingUrl;
  public Date uploadTime;
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  public Long getId() {
    return id;
  }

  @Override
  public String getDomainKey() {
    return domainKey;
  }

  @Override
  public void setDomainKey(String domainKey) {
    this.domainKey = domainKey;
  }

  @Override
  public String getNamespaceId() {
    return namespaceId;
  }

  @Override
  public void setNamespaceId(String namespaceId) {
    this.namespaceId = namespaceId;
  }

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public BlobKey getBlobKey() {
    return blobKey;
  }

  @Override
  public void setBlobKey(BlobKey blobKey) {
    this.blobKey = blobKey;
  }

  @Override
  public SupportedMediaTypes getMediaType() {
    return mediaType;
  }

  @Override
  public void setMediaType(SupportedMediaTypes mediaType) {
    this.mediaType = mediaType;
  }

  @Override
  public String getServingUrl() {
    return servingUrl;
  }

  @Override
  public void setServingUrl(String servingUrl) {
    this.servingUrl = servingUrl;
  }

  @Override
  public Date getUploadTime() {
    return uploadTime;
  }

  @Override
  public void setUploadTime(Date uploadTime) {
    this.uploadTime = uploadTime;
  }
}