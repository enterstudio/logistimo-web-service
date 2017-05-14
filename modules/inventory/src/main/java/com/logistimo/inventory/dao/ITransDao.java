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

package com.logistimo.inventory.dao;

import com.logistimo.inventory.entity.ITransaction;

import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 03/03/15.
 */
public interface ITransDao {

  String getKeyAsString(ITransaction transaction);

  void setKey(ITransaction transaction);

  ITransaction getById(String id);

  ITransaction getById(String id, PersistenceManager persistenceManager);

  void linkTransactions(ITransaction destination, ITransaction source);

  Results getInventoryTransactions(Date sinceDate, Date untilDate, Long domainId, Long kioskId,
                                   Long materialId, List<String> transTypes,
                                   Long linkedKioskId, String kioskTag,
                                   String materialTag, List<Long> kioskIds, PageParams pageParams,
                                   String bid,
                                   boolean atd, String reason, List<String> excludeReasons)
      throws ServiceException;

  QueryParams buildTransactionsQuery(Date sinceDate, Date untilDate, Long domainId,
                                               Long kioskId,
                                               Long materialId, List<String> transTypes,
                                               Long linkedKioskId, String kioskTag,
                                               String materialTag, List<Long> kioskIds,
                                               String bid,
                                               boolean atd, String reason,
                                               List<String> excludeReasons);
}
