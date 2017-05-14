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

package com.logistimo.entity;

import com.logistimo.utils.NumberUtil;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Uploaded implements IUploaded {
  // Filename for the mobile app.
  private static final String APP_FILENAME = "logistimo";
  private static final String SEPARATOR = "_";

  @PrimaryKey
  @Persistent
  private String id; // typically, filename.version.locale
  @Persistent
  private String fn; // file name (has to be unique)
  @Persistent
  private String v; // version of the uploaded file (can be a number, time, etc.)
  @Persistent
  private String l; // locale
  @Persistent
  //@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private String desc; // optional description for the uploaded file
  @Persistent
  private String uid; // id of the user uploading the file
  @Persistent
  private Date t; // time at which the file is uploaded
  @Persistent
//	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private String blkey; // Blob key generated when the blob was uploaded
  @Persistent
  private Long dId; // domain Id
  @Persistent
  private String ty; // type of upload
  @Persistent
  private String jid; // Job ID, if present
  @Persistent
//	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private Integer st = new Integer(0); // status of upload (null or 0 implies 'pending')


  // The primary key is a combination of filename, version and locale
  public static String createKey(String filename, String version, String locale) {
    if (version != null && !version.isEmpty() && locale != null && !locale.isEmpty()) {
      return filename + Uploaded.SEPARATOR + version + Uploaded.SEPARATOR + locale;
    }
    return filename;
  }


  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String key) {
    id = key;
  }

  @Override
  public String getFileName() {
    return fn;
  }

  @Override
  public void setFileName(String fileName) {
    fn = fileName;
  }

  @Override
  public String getVersion() {
    return v;
  }

  @Override
  public void setVersion(String version) {
    v = version;
  }

  @Override
  public String getLocale() {
    return l;
  }

  @Override
  public void setLocale(String locale) {
    l = locale;
  }

  @Override
  public String getDescription() {
    return desc;
  }

  @Override
  public void setDescription(String description) {
    desc = description;
  }

  @Override
  public String getUserId() {
    return uid;
  }

  @Override
  public void setUserId(String userId) {
    uid = userId;
  }

  @Override
  public Date getTimeStamp() {
    return t;
  }

  @Override
  public void setTimeStamp(Date timeStamp) {
    t = timeStamp;
  }

  @Override
  public String getBlobKey() {
    return blkey;
  }

  @Override
  public void setBlobKey(String blobKey) {
    blkey = blobKey;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long dId) {
    this.dId = dId;
  }

  @Override
  public String getType() {
    return ty;
  }

  @Override
  public void setType(String ty) {
    this.ty = ty;
  }

  @Override
  public String getJobId() {
    return jid;
  }

  @Override
  public void setJobId(String jid) {
    this.jid = jid;
  }

  @Override
  public int getJobStatus() {
    return NumberUtil.getIntegerValue(st);
  }

  @Override
  public void setJobStatus(int status) {
    st = new Integer(status);
  }

}
