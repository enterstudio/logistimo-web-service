package com.logistimo.domains;

import java.util.List;

/**
 * An object that belongs one or more domains, with interfaces to add/remove the domain
 *
 * @author arun
 */
public interface IMultiDomain {
  /**
   * get the source domain Id
   */
  Long getDomainId();

  /**
   * set the source domain Id
   */
  void setDomainId(Long domainId);

  /**
   * get all the domains this object belongs to
   */
  List<Long> getDomainIds();

  /**
   * Resets all domain Ids in this object to this new list
   */
  void setDomainIds(List<Long> domainIds);

  /**
   * Make this object a part of the specified domains
   * Adds unique domain Ids to this object's existing domain Ids.
   */
  void addDomainIds(List<Long> domainIds);

  /**
   * remove this object from the specified domain
   */
  void removeDomainId(Long domainId);

  void removeDomainIds(List<Long> domainIds);
}
