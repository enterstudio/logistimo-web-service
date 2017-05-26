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

package com.logistimo.api.migrators;

import com.logistimo.users.dao.IUserDao;
import com.logistimo.users.dao.UserDao;
import com.logistimo.users.entity.IUserAccount;

import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.logger.XLog;

import java.util.List;

import javax.jdo.PersistenceManager;

/**
 * Created by vani on 02/02/17.
 */
public class UserDomainIdsMigrator {
  private static final XLog xlogger = XLog.getLog(UserDomainIdsMigrator.class);
  private IUserDao accountDao = new UserDao();

  /**
   * Migrate the domain ids in which a user will be visible for existing users
   */
  public void migrateUserDomainIds() throws ServiceException {
    // Get users from the USERACCOUNT table, 100 at a time and migrate them until there are no more users.
    int offset = 0;
    int size = 100;
    List<IUserAccount> users = accountDao.getUsers(offset, size);
    while (users != null && !users.isEmpty()) {
      migrateUsers(users);
      offset += size;
      users = accountDao.getUsers(offset, size);
    }
    xlogger.warn("No more users to migrate");
  }


  /**
   * Migrate the domain ids in which the user will be visible into the USERACCOUNT_DOMAINS table.
   */
  private void migrateUsers(List<IUserAccount> users) throws ServiceException {
    if (users == null || users.isEmpty()) {
      xlogger.warn("No users to migrate");
      return;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      for (IUserAccount user : users) {
        migrateUser(user, pm);
      }
    } catch (ServiceException e) {
      throw e;
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  /**
   * Set the parent domain ids for a user
   */
  private void migrateUser(IUserAccount user, PersistenceManager pm) throws ServiceException {
    if (user == null) {
      return;
    }
    DomainsUtil.addToDomain(user, user.getDomainId(), pm, false);
  }
}
