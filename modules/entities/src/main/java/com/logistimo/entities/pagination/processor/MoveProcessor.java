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

package com.logistimo.entities.pagination.processor;

import com.logistimo.assets.AssetUtil;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.domains.IMultiDomain;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.services.Services;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.constants.MethodNameConstants;
import com.logistimo.logger.XLog;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

/**
 * The {@code MoveProcessor} class updates data store based on
 * results it receives and update its respective counter across all its domains.
 *
 * @author Mohan Raja
 * @see com.logistimo.entities.utils.EntityMover
 */
public class MoveProcessor implements Processor {
  private static final XLog xLogger = XLog.getLog(MoveProcessor.class);

  private Set<Long> sourceDomainParents;
  private Set<Long> destDomainParents;

  /**
   * Process each {@code results} and moves it to {@code destDomainId} by updating domain id and
   * counter of all its corresponding domain.
   *
   * @param destDomainId destination domain id to which results need to me moved
   * @param results      data which need to be moved to {@code destDomainId}
   * @param dummy        unused variable.
   * @param pm           persistent manager used for updating the data store
   * @return null
   */
  @SuppressWarnings("unchecked")
  @Override
  public String process(Long destDomainId, Results results, String dummy, PersistenceManager pm) {
    if (results == null) {
      xLogger.fine("result is null");
      return null;
    }
    List list = results.getResults();
    if (list == null || list.isEmpty()) {
      return null;
    }
    Object o = list.get(0);
    try {
      Long
          sourceDomainId =
          (Long) o.getClass().getMethod(MethodNameConstants.GET_DOMAIN_ID).invoke(o);
      sourceDomainParents = DomainsUtil.getDomainParents(sourceDomainId, true);
      destDomainParents = DomainsUtil.getDomainParents(destDomainId, true);
      update(list, destDomainId, pm);
      if (o instanceof IAsset) {
        EntitiesService es = Services.getService(EntitiesServiceImpl.class);
        AssetUtil.updateAssetTags((List<IAsset>) list,
            es.getAssetTagsToRegister(((IAsset) o).getKioskId()));
      } else if(o instanceof IUserAccount) {
        UsersService us = Services.getService(UsersServiceImpl.class);
        us.moveAccessibleDomains(((IUserAccount) o).getUserId(), sourceDomainId, destDomainId);
      }
    } catch (Exception e) {
      xLogger.warn("Error while moving the entity to domain {1}:", destDomainId, e);
    }
    return null;
  }

  /**
   * Update source domain Id and objects domain id list if available. Domain id list won't
   * be available for user tables.
   *
   * @param list         data which needs to be updated.
   * @param destDomainId source domain id will be set to this value.
   */
  @SuppressWarnings("unchecked")
  private void update(List list, Long destDomainId, PersistenceManager pm)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    for (Object l : list) {
      l = pm.detachCopy(l);
      if (l instanceof IMultiDomain) {
        IMultiDomain mObject = (IMultiDomain) l;
        mObject.setDomainId(destDomainId);
        mObject.removeDomainIds(new ArrayList<>(sourceDomainParents));
        mObject.addDomainIds(new ArrayList<>(destDomainParents));
      } else {
        l.getClass().getMethod(MethodNameConstants.SET_DOMAIN_ID, destDomainId.getClass())
            .invoke(l, destDomainId);
      }
      pm.makePersistent(l);
    }
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_DEFAULT;
  }
}
