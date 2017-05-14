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

package com.logistimo.services.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created by charan on 24/09/14.
 */
public class LocalMemcacheService implements MemcacheService {
  private final Cache<String, Object> cache;

  public LocalMemcacheService() {

    this.cache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();
  }

  @Override
  public Object get(String cacheKey) {
    return cache.getIfPresent(cacheKey);
  }

  @Override
  public void put(String cacheKey, Object obj) {
    cache.put(cacheKey, obj);
  }

  @Override
  public void put(String cacheKey, Object obj, int expiry) {
    cache.put(cacheKey, obj);
  }

  @Override
  public boolean putIfNotExist(String cacheKey, Object obj) {
    if (get(cacheKey) == null) {
      put(cacheKey, obj);
      return true;
    }
    return false;
  }

  @Override
  public boolean putMultiIfNotExists(String cacheKey1, Object obj1, String cacheKey2, Object obj2) {
    if (get(cacheKey1) == null && get(cacheKey2) == null) {
      synchronized (this) {
        if (get(cacheKey1) == null && get(cacheKey2) == null) {
          put(cacheKey1, obj1);
          put(cacheKey2, obj2);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean delete(String key) {
    cache.invalidate(key);
    return true;
  }

  @Override
  public void deleteByPattern(String key) {
    throw new UnsupportedOperationException("Dont know how to delete pattern based");
  }

  @Override
  public boolean deleteMulti(String... keys) {
    for (String key : keys) {
      delete(key);
    }
    return true;
  }

  @Override
  public void close() {
    this.cache.cleanUp();
  }
}
