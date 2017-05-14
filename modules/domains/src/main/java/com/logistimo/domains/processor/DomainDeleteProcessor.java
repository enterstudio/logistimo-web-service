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

package com.logistimo.domains.processor;

import com.logistimo.domains.IMultiDomain;
import com.logistimo.domains.utils.DomainDeleter;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.constants.MethodNameConstants;
import com.logistimo.logger.XLog;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;

/**
 * The {@code DomainDeleteProcessor} class deletes domain from data store based on
 * results it receives and update respective counter if necessary.
 *
 * @author Mohan Raja
 * @see DomainDeleter
 */
public class DomainDeleteProcessor implements Processor {
  private static final XLog xLogger = XLog.getLog(DomainDeleteProcessor.class);

  /**
   * Process each {@code results} and deletes/updates it and update/remove
   * counter of all its corresponding domain.
   *
   * @param domainId domain id to be removed from results, or delete all {@code results} if it is equal
   *                 to {@code sourceDomainId} in {@code results}
   * @param results  data which need to be updated or deleted
   * @param dummy    unused variable.
   * @param pm       persistent manager used for updating the data store
   * @return null
   */
  @Override
  public String process(Long domainId, Results results, String dummy, PersistenceManager pm)
      throws ProcessingException {
    if (results == null) {
      xLogger.fine("result is null");
      return null;
    }
    List list = results.getResults();
    if (list == null || list.isEmpty()) {
      return null;
    }
    Object o = list.get(0);
    if (o instanceof IMultiDomain) {
      try {
        Long
            sourceDomainId =
            (Long) o.getClass().getMethod(MethodNameConstants.GET_DOMAIN_ID).invoke(o);
        if (sourceDomainId.equals(domainId)) {
          pm.deletePersistentAll(list);
        } else if (o.getClass().getSimpleName().equals("Material")) {
          removeDomains(list, Collections.singletonList(domainId));
        } else {
          List<Long> niDomains = DomainsUtil.extractNonIntersectingDomainIds(domainId, o);
          removeDomains(list, niDomains);
        }
      } catch (Exception e) {
        xLogger.warn("Error while deleting the domain {1}:", domainId, e);
      }
    } else {
      pm.deletePersistentAll(list);
    }
    return null;
  }

  /**
   * Remove given {@code delDomainIds} from {@code list}'s domain ids
   *
   * @param list         multi domain object
   * @param delDomainIds domain ids to be removed from {@code list}
   */
  @SuppressWarnings("unchecked")
  private void removeDomains(List list, List<Long> delDomainIds)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    for (Object l : list) {
      List<Long>
          domainIds =
          (List<Long>) l.getClass().getMethod(MethodNameConstants.GET_DOMAIN_IDS).invoke(l);
      domainIds.removeAll(delDomainIds);
    }
  }

  @Override
  public String getQueueName() {
    return null;
  }
}
