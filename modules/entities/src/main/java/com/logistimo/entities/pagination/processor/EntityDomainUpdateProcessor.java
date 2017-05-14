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

import com.logistimo.assets.entity.IAsset;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.domains.IMultiDomain;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.services.Services;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

/**
 * The {@code EntityDomainUpdateProcessor} class updates entity domain ids and all its associated objects
 * based on results and update its respective counter across all the new domains.
 *
 * @author Mohan Raja
 */
public class EntityDomainUpdateProcessor implements Processor {
  private static final XLog xLogger = XLog.getLog(EntityDomainUpdateProcessor.class);

  @SuppressWarnings("unchecked")
  @Override
  public String process(Long isAdd, Results results, String sDomainIds, PersistenceManager pm)
      throws ProcessingException {
    if (results == null) {
      xLogger.fine("result is null");
      return null;
    }
    List list = results.getResults();
    if (list == null || list.isEmpty()) {
      return null;
    }
    boolean isAddDomain = isAdd == 1;

    Set<Long> updateDomainIds = new HashSet<>();
    String[] domains = sDomainIds.split(CharacterConstants.COLON);
    Set<Long> sourceDomainTree = null;
    if (!domains[0].equals("0")) {
      try {
        sourceDomainTree = DomainsUtil.getDomainParents(Long.valueOf(domains[0]), true);
      } catch (Exception e) {
        xLogger.severe("Error in getting ancestors list for domain {0} ",
            domains[0], e);
        return sDomainIds;
      }
    }
    for (String domainId : domains[1].split(CharacterConstants.COMMA)) {
      try {
        updateDomainIds.addAll(DomainsUtil.getDomainParents(Long.valueOf(domainId), true));
      } catch (Exception e) {
        xLogger.severe(
            "Error in constructing update domain ids list while reading domain {0} for object {1} ",
            domainId, list.get(0).getClass().getName(), e.getMessage(), e);
        return sDomainIds;
      }
    }
    if (sourceDomainTree != null) {
      updateDomainIds.removeAll(sourceDomainTree);
      IMultiDomain o = (IMultiDomain) list.get(0);
      List<Long> dIds = new ArrayList<>(o.getDomainIds());
      dIds.removeAll(sourceDomainTree);
      dIds.removeAll(updateDomainIds);
      for (Long dId : dIds) {
        try {
          updateDomainIds.removeAll(DomainsUtil.getDomainParents(dId, false));
        } catch (Exception e) {
          xLogger.severe(
              "Error in constructing update domain ids list while reading domain {0} for object {1} ",
              dId, list.get(0).getClass().getName(), e.getMessage(), e);
          return sDomainIds;
        }
      }
    }
    try {
      for (Object l : list) {
        IMultiDomain mObject = (IMultiDomain) l;
        mObject = pm.detachCopy(mObject);
        if (isAddDomain) {
          mObject.addDomainIds(new ArrayList<>(updateDomainIds));
        } else {
          mObject.removeDomainIds(new ArrayList<>(updateDomainIds));
        }
        pm.makePersistent(mObject);
      }

      if (list.get(0) instanceof IAsset) {
        Services.getService(EntitiesServiceImpl.class).registerOrUpdateDevices(list);
      }
    } catch (Exception e) {
      xLogger
          .warn("Error while updating domain ids to object {0}:", list.get(0).getClass().getName(),
              e);
    }
    return sDomainIds;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_DEFAULT;
  }
}
