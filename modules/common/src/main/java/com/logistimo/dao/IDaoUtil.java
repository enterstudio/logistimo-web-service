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

package com.logistimo.dao;

import com.logistimo.domains.IMultiDomain;
import com.logistimo.services.ServiceException;

import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by Mohan Raja on 03/06/15.
 */
public interface IDaoUtil {

  // Remove the items with specified criteria from the board
  void removeBBItems(List<Long> bbItemIds);

  @SuppressWarnings({"unchecked", "rawtypes"})
  void updateTags(List<Long> domainIds, List<String> oldTags, List<String> newTags,
                  String type, Long entityId, PersistenceManager pm)
      throws ServiceException;

  Object createKeyFromString(String encodedString);

  List<String> getUploadedMessages(String uploadedKey);

  void deleteUploadedMessage(String uploadedKey);


  void setCursorExtensions(String cursorStr, Query query);

  String getCursor(List results);

  IMultiDomain getKioskDomains(Long kioskId) throws ServiceException;
}
