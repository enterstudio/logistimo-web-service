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

import java.util.Date;

/**
 * Created by Mohan Raja on 08/06/15.
 */
public interface IDomainLink {

  // Relationship types
  int TYPE_CHILD = 0; // ldId is child of dId
  int TYPE_PARENT = 1; // ldId is parent of dId

  IDomainLink init(Long domainId, String domainName, Long linkedDomainId, String linkedDomainName,
                   int type, Date d);

  IDomainLink loadReverseLink(IDomainLink link);

  String getKey();

  String createKey();

  Long getDomainId();

  void setDomainId(Long domainId, String domainName);

  Long getLinkedDomainId();

  void setLinkedDomainId(Long linkedDomainId, String linkedDomainName);

  int getType();

  void setType(int type);

  Date getCreatedOn();

  void setCreatedOn(Date createdOn);
}
