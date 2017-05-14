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

import com.logistimo.services.utils.ConfigUtil;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

import com.logistimo.logger.XLog;

import java.io.IOException;

/**
 * Created by charan on 22/01/15.
 */
public class MemcachedService implements MemcacheService {

  private static final XLog LOGGER = XLog.getLog(MemcachedService.class);
  MemcachedClient memcachedClient;
  private int expiry;

  public MemcachedService() {
    MemcachedClientBuilder
        builder =
        new XMemcachedClientBuilder(
            AddrUtil.getAddresses(ConfigUtil.get("memcached.url", "localhost:11211")));
//        builder.setConnectionPoolSize(5);
    try {
      memcachedClient = builder.build();
    } catch (IOException e) {
      LOGGER.severe("Couldn't initialize memcached client", e);
    }
    expiry = ConfigUtil.getInt("cache.expiry", 84400);
  }

  @Override
  public Object get(String cacheKey) {
    try {
      return memcachedClient.get(cacheKey);
    } catch (Exception e) {
      LOGGER.warn("Failed to get key from cache {0}", cacheKey, e);
    }
    return null;
  }

  @Override
  public void put(String cacheKey, Object obj) {
    put(cacheKey, obj, expiry);
  }

  @Override
  public void put(String cacheKey, Object obj, int expiry) {
    try {
      memcachedClient.set(cacheKey, expiry, obj);
    } catch (Exception e) {
      LOGGER.warn("Failed to put key in cache {0}", cacheKey, e);
    }
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
    try {
      return memcachedClient.delete(key);
    } catch (Exception e) {
      LOGGER.warn("Failed to delete key from cache {0}", key, e);
    }
    return false;
  }

  @Override
  public boolean deleteMulti(String... keys) {
    for (String key : keys) {
      delete(key);
    }
    return true;
  }

  @Override
  public void deleteByPattern(String key) {
    throw new UnsupportedOperationException("Dont know how to delete pattern based");
  }

  public void close() {
    if (memcachedClient != null) {
      try {
        memcachedClient.shutdown();
      } catch (IOException e) {
        LOGGER.warn("Error trying to shutdown memcached", e);
        if (!memcachedClient.isShutdown()) {
          try {
            memcachedClient.shutdown();
          } catch (IOException e1) {
            LOGGER.warn("Exception while trying to shutdown memcachedClient", e1);
          }
        }
      }
    }
  }
}
