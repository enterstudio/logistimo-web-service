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
