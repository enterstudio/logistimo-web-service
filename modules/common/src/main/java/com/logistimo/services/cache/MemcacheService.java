package com.logistimo.services.cache;

public interface MemcacheService {

  Object get(String cacheKey);

  /**
   * Set cache object, default expiry 24 hours
   */
  void put(String cacheKey, Object obj);

  /**
   * set cache obj for given expiry period
   *
   * @param expiry in seconds
   */
  void put(String cacheKey, Object obj, int expiry);

  boolean putIfNotExist(String cacheKey, Object obj);

  boolean putMultiIfNotExists(String cacheKey1, Object obj1, String cacheKey2, Object obj2);

  boolean delete(String key);

  void deleteByPattern(String key);

  boolean deleteMulti(String... keys);

  void close();
}
