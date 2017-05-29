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

package com.logistimo.domains;

import java.util.List;

/**
 * An object that belongs one or more domains, with interfaces to add/remove the domain
 *
 * @author arun
 */
public interface IMultiDomain {
  /**
   * get the source domain Id
   */
  Long getDomainId();

  /**
   * set the source domain Id
   */
  void setDomainId(Long domainId);

  /**
   * get all the domains this object belongs to
   */
  List<Long> getDomainIds();

  /**
   * Resets all domain Ids in this object to this new list
   */
  void setDomainIds(List<Long> domainIds);

  /**
   * Make this object a part of the specified domains
   * Adds unique domain Ids to this object's existing domain Ids.
   */
  void addDomainIds(List<Long> domainIds);

  /**
   * remove this object from the specified domain
   */
  void removeDomainId(Long domainId);

  void removeDomainIds(List<Long> domainIds);
}
