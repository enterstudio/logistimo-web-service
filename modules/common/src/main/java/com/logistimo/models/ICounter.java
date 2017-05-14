package com.logistimo.models;

import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * @author Mohan Raja
 */
public interface ICounter {

  ICounter init(Long domainId, Map<String, Object> keys);

  ICounter init(Long domainId, String name);

  int getCount();

  void increment(int amount);

  void increment(int amount, PersistenceManager pm);

  boolean delete(PersistenceManager pm);

  boolean delete();

}
