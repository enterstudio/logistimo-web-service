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

package com.logistimo.accounting.service;

import com.logistimo.accounting.entity.IAccount;
import com.logistimo.accounting.models.CreditData;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.math.BigDecimal;

/**
 * Created by charan on 05/03/17.
 */
public interface IAccountingService extends Service {

  // Update an account with payable or receivable (pass 0 to avoid any increments)
  void updateAccount(Long domainId, Long vendorId, Long customerId, int year, BigDecimal payable,
                     BigDecimal paid) throws ServiceException;

  // Get an account, given a vendor, customer and year
  IAccount getAccount(Long vendorId, Long customerId, int year)
      throws ObjectNotFoundException, ServiceException;

  CreditData getCreditData(Long customerId, Long vendorId, DomainConfig dc)
      throws ServiceException;

  // Accounting functions
  void persistAccount(IAccount account) throws ServiceException;

  // Get the list of accounts given parameters - if vendorId is null, all of customer Id accounts are obtained and vice-versa; year of -1 is ignored, i.e. year is not used
  Results getAccounts(Long vendorId, Long customerId, int year, String orderBy,
                      PageParams pageParams) throws ServiceException;
}
