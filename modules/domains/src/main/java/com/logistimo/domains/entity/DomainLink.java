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

package com.logistimo.domains.entity;

import com.logistimo.utils.NumberUtil;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class DomainLink implements IDomainLink {

  @PrimaryKey
  @Persistent
  private String key; // dId.linkType.ldId
  @Persistent
  private Long dId;
  @Persistent
  private String ndnm; // normalized domain name (all lower case; used for sorting)
  @Persistent
  private Long ldId; // linked domain Id
  @Persistent
  private String nldnm; // normalized linked domain name (all lower case; used for sorting)
  @Persistent
  private Integer ty = new Integer(TYPE_CHILD); // ldId is child of dId
  @Persistent
  private Date t; // relationship created on time

  public DomainLink() {
  }

  @Override
  public IDomainLink init(Long domainId, String domainName, Long linkedDomainId,
                          String linkedDomainName, int type, Date d) {
    setDomainId(domainId, domainName);
    setLinkedDomainId(linkedDomainId, linkedDomainName);
    ty = type;
    t = d;
    return this;
  }

  @Override
  public IDomainLink loadReverseLink(IDomainLink iLink) {
    DomainLink link = (DomainLink) iLink;
    dId = link.ldId;
    ndnm = link.nldnm;
    ldId = link.dId;
    nldnm = link.ndnm;
    ty = (link.ty == TYPE_CHILD ? TYPE_PARENT : TYPE_CHILD);
    t = link.t;
    createKey();
    return this;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String createKey() {
    if (dId == null || ldId == null) {
      return null;
    }
    key = dId + "." + ty + "." + ldId;
    return key;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long domainId, String domainName) {
    dId = domainId;
    if (domainName != null) {
      ndnm = domainName.toLowerCase();
    }
  }

  @Override
  public Long getLinkedDomainId() {
    return ldId;
  }

  @Override
  public void setLinkedDomainId(Long linkedDomainId, String linkedDomainName) {
    ldId = linkedDomainId;
    if (linkedDomainName != null) {
      nldnm = linkedDomainName.toLowerCase();
    }
  }

  @Override
  public int getType() {
    return NumberUtil.getIntegerValue(ty);
  }

  @Override
  public void setType(int type) {
    ty = new Integer(type);
  }

  @Override
  public Date getCreatedOn() {
    return t;
  }

  @Override
  public void setCreatedOn(Date createdOn) {
    t = createdOn;
  }
}
