package com.logistimo.auth.service;

import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;

/**
 * Created by charan on 08/03/17.
 */
public interface AuthorizationService {

  /**
   * Authorise a user to perform transaction based on role based transaction type capabilities
   *
   * @param transType transaction type
   * @return true if authorised, false otherwise
   */
  boolean authoriseTransactionAccess(String transType, Long domainId, String userId)
      throws ServiceException, ObjectNotFoundException;

  /**
   * Authorise a user to perform add,edit,remove Entities or Users, checks capability "Allow creation of entities" of user
   * also restricts Read-only and Asset-only users to perform these operations
   *
   * @param userId-userId of the user trying to perform these actions
   * @return true if authorised, false otherwise
   * @throws ObjectNotFoundException if user not found
   */
  boolean authoriseUpdateKiosk(String userId, Long domainId)
      throws ServiceException, ObjectNotFoundException;

}
