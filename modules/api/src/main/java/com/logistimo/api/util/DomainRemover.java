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

package com.logistimo.api.util;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import com.logistimo.domains.utils.DomainsUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

/**
 * The {@code DomainRemover} class removes any domain other than source domain and
 * its parents from kiosk.
 *
 * @author Mohan Raja
 */
public class DomainRemover {

  /**
   * Validate the given domain id {@code did} and returns error message if any, or return 'success'.
   *
   * @param domainId Domain id to be removed.
   * @param kioskId  Kiosk id from which the domain need to be removed.
   */
  public static String validateDomainRemove(Long domainId, Long kioskId) throws ServiceException {
    EntitiesService as = new EntitiesServiceImpl();
    IKiosk kiosk = as.getKiosk(kioskId);
    Set<Long> kioskParents = DomainsUtil.getDomainParents(kiosk.getDomainId(), true);
    if (kioskParents.contains(domainId)) {
      return "Source domain or ancestors of source domain can't be removed from entity.";
    }
    return "success";
  }

  /**
   * Remove the given {@code domainId} from {@code kioskId} object and decrement counters.
   *
   * @param domainId Domain id to be removed.
   * @param kioskId  Kiosk id from which the domain need to be removed.
   */
  public static void removeDomain(Long domainId, Long kioskId)
      throws ServiceException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException {
    EntitiesService as = Services.getService(EntitiesServiceImpl.class);
    IKiosk kiosk = as.getKiosk(kioskId);
    List<Long> niDomainIds = DomainsUtil.extractNonIntersectingDomainIds(domainId, kiosk);
    List<Long> domainIds = kiosk.getDomainIds();
    domainIds.removeAll(niDomainIds);
  }

}
