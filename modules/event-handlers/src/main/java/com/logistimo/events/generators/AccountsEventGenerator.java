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

/**
 *
 */
package com.logistimo.events.generators;


import com.logistimo.AppFactory;
import com.logistimo.accounting.entity.IAccount;
import com.logistimo.accounting.service.impl.AccountingServiceImpl;
import com.logistimo.dao.JDOUtils;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.events.entity.IEvent;

import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;


/**
 * @author Arun
 */
public class AccountsEventGenerator extends EventGenerator {

  public AccountsEventGenerator(Long domainId) {
    super(domainId, JDOUtils.getImplClass(IAccount.class).getName());
  }

  // Is an event still valid?
  @Override
  public boolean isEventValid(IEvent event, PersistenceManager pm) {
    int eventId = event.getId();
    try {
      switch (eventId) {
        case IEvent.CREDIT_LIMIT_EXCEEDED: {
          try {
            DomainConfig dc = DomainConfig.getInstance(event.getDomainId());
            IAccount
                a =
                JDOUtils.getObjectById(IAccount.class,
                    AppFactory.get().getDaoUtil().createKeyFromString(event.getObjectId()), pm);
            return BigUtil.lesserThanEqualsZero(Services
                .getService(AccountingServiceImpl.class)
                .getCreditData(a.getCustomerId(), a.getVendorId(), dc).availabeCredit);
          } catch (JDOObjectNotFoundException e1) {
            return false;
          }
        }
      }
    } catch (Exception e) {
      xLogger.warn("{0} when retrieving {1} with key {2} in domain {3} for event {4}: {5}",
          e.getClass().getName(), event.getObjectType(), event.getObjectId(), event.getDomainId(),
          eventId, e);
    }
    return super.isEventValid(event, pm);
  }
}
