package com.logistimo.domains;

/**
 * An object that goes across domains - typically, entity relationships
 *
 * @author arun
 */
public interface ICrossDomain extends IMultiDomain {
  Long getLinkedDomainId();

  Long getKioskDomainId();
}
