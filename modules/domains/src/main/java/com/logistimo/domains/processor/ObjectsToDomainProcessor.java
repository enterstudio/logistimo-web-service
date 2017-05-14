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

import com.google.gson.Gson;

import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.domains.ObjectsToDomainModel;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.services.Services;
import com.logistimo.logger.XLog;

import java.util.List;

import javax.jdo.PersistenceManager;

public class ObjectsToDomainProcessor implements Processor {

  private static final XLog xLogger = XLog.getLog(ObjectsToDomainProcessor.class);

  /**
   * Add/remove the results (keys-only) to/from domains
   * NOTE: The domainId input parameter is of the same domain as the results; the new domain is within the ObjectsToDomainModel
   */
  @SuppressWarnings("unchecked")
  @Override
  public String process(Long domainId, Results results, String jsonAddObjectsToDomainModel,
                        PersistenceManager pm) throws ProcessingException {
    xLogger.fine("Entered AddObjectsToDomainProcessor.process");
    if (results == null) {
      return jsonAddObjectsToDomainModel;
    }
    List<Object> objectIds = results.getResults();
    if (objectIds == null || objectIds.isEmpty()) {
      return jsonAddObjectsToDomainModel;
    }
    if (jsonAddObjectsToDomainModel == null) {
      xLogger.severe("AddObjectsToDomainModel input is null; domain: {0}", domainId);
      return jsonAddObjectsToDomainModel;
    }
    try {
      // Get the input object model
      ObjectsToDomainModel
          aotdm =
          new Gson().fromJson(jsonAddObjectsToDomainModel, ObjectsToDomainModel.class);
      List<Long> domainIds = aotdm.getDomainIds();
      // Check parameters
      if (aotdm.getClassName() == null || domainIds == null || domainIds.isEmpty()) {
        xLogger.severe("Class or domain ID not specified");
        return jsonAddObjectsToDomainModel;
      }
      // Get the domains service
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      Class<?> clazz = Class.forName(aotdm.getClassName());
      boolean add = aotdm.getAction() == ObjectsToDomainModel.ACTION_ADD;
      if (add) {
        ds.addObjectsToDomains(objectIds, clazz, domainIds);
      } else // remove
      {
        ds.removeObjectsFromDomains(objectIds, clazz, domainIds);
      }

    } catch (Exception e) {
      xLogger.severe(
          "{0} when trying to process adding of objects in domain {1} to another domain: {2}",
          e.getClass().getName(), domainId, e.getMessage());
    }
    xLogger.fine("Exiting AddObjectsToDomainProcessor.process");
    return jsonAddObjectsToDomainModel;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_DOMAINS;
  }

}
