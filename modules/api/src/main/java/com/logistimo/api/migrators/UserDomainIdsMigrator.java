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
