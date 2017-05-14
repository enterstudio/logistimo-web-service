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

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.ZParams;
import redis.clients.util.Pool;
import redis.clients.util.SafeEncoder;

/**
 * Created by charan on 22/01/15.
 */
public class RedisMemcacheService implements MemcacheService {

  private static final byte[] NX = "NX".getBytes();
  private static final byte[] EX = "EX".getBytes();
  private static final byte[]
      DOUBLE_LOCK_SCRIPT =
      "if redis.call('get',KEYS[1]) == false then if redis.call('get',KEYS[2]) == false  then redis.call('setex',KEYS[1],30,ARGV[1]) redis.call('setex',KEYS[2],30,ARGV[2]) return 1 else return 0 end else return 0 end"
          .getBytes();
  private static final String DELETE_SCRIPT_IN_LUA = "local keys = redis.call('keys', '%s')" +
      "  for i,k in ipairs(keys) do" +
      "    local res = redis.call('del', k)" +
      "  end";
  private static final XLog LOGGER = XLog.getLog(RedisMemcacheService.class);
  private static byte[] MIN_INF = SafeEncoder.encode("-inf");
  private static byte[] MAX_INF = SafeEncoder.encode("+inf");
  private final int expiry;
  Pool<Jedis> pool = null;

  public RedisMemcacheService() {
    String[] sentinelsStr = ConfigUtil.getCSVArray("redis.sentinels");
    if (sentinelsStr != null && sentinelsStr.length > 0) {
      Set<String> sentinels = new LinkedHashSet<>();
      sentinels.addAll(Arrays.asList(sentinelsStr));
      pool =
          new JedisSentinelPool("mymaster", sentinels, new GenericObjectPoolConfig(), 5000, null,
              0);
    } else {
      pool =
          new JedisPool(new JedisPoolConfig(), ConfigUtil.get("redis.server", "localhost"),
              ConfigUtil.getInt("redis.server.port", 6379), 5000, null, 0);
    }
    expiry = ConfigUtil.getInt("cache.expiry", 84400);
  }

  @Override
  public Object get(String cacheKey) {
    Jedis jedis = null;
    Object value = null;
    try {
      jedis = pool.getResource();
      value = getObject(jedis.get(cacheKey.getBytes()));
      pool.returnResource(jedis);
    } catch (Exception e) {
      LOGGER.warn("Failed to get key from cache {0}", cacheKey, e);
      pool.returnBrokenResource(jedis);
    }
    return value;
  }

  @Override
  public void put(String cacheKey, Object obj) {
    put(cacheKey, obj, expiry);
  }

  @Override
  public void put(String cacheKey, Object obj, int expiry) {
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      jedis.setex(cacheKey.getBytes(), expiry, getBytes(obj));
      pool.returnResource(jedis);
    } catch (Exception e) {
      LOGGER.warn("Failed to put key in cache {0}", cacheKey, e);
      pool.returnBrokenResource(jedis);
    }
  }

  @Override
  public boolean putIfNotExist(String cacheKey, Object obj) {
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      String status = jedis.set(cacheKey.getBytes(), getBytes(obj), NX, EX, 30);
      pool.returnResource(jedis);
      return Constants.OK.equals(status);
    } catch (Exception e) {
      LOGGER.warn("Failed to put key in cache {0}", cacheKey, e);
      pool.returnBrokenResource(jedis);
    }
    return false;
  }

  private byte[] getBytes(Object obj) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream outputStream;
    outputStream = new ObjectOutputStream(bos);
    outputStream.writeObject(obj);
    return bos.toByteArray();

  }

  @Override
  public boolean putMultiIfNotExists(String cacheKey1, Object obj1, String cacheKey2, Object obj2) {
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      List<byte[]> keys = new ArrayList<>(2);
      keys.add(cacheKey1.getBytes());
      keys.add(cacheKey2.getBytes());
      List<byte[]> args = new ArrayList<>(2);
      args.add(getBytes(obj1));
      args.add(getBytes(obj2));
      Long response = (Long) jedis.eval(DOUBLE_LOCK_SCRIPT, keys, args);
      pool.returnResource(jedis);
      return response == 1;
    } catch (Exception e) {
      LOGGER.warn("Failed to put key in cache {0}", cacheKey1, e);
      pool.returnBrokenResource(jedis);
    }
    return false;
  }

  private Object getObject(byte[] bytes) throws IOException {
    Object o = null;
    if (bytes != null) {
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      ObjectInput in = null;

      try {
        in = new ObjectInputStream(bis);
        o = in.readObject();
      } catch (ClassNotFoundException e) {
        LOGGER.warn("Failed to convert object", e);
        throw new IOException(e);
      } catch (IOException e) {
        LOGGER.warn("Failed to convert object", e);
        throw new IOException(e);
      } finally {
        try {
          bis.close();
        } catch (IOException ex) {
          // ignore close exception
          LOGGER.warn("Exception while trying to close bis", ex);
        }
        try {
          if (in != null) {
            in.close();
          }
        } catch (IOException ex) {
          // ignore close exception
          LOGGER.warn("Exception while trying to close in", ex);
        }
      }
    }
    return o;
  }

  @Override
  public boolean delete(String key) {
    Jedis jedis = null;
    long retVal = 0;
    try {
      jedis = pool.getResource();
      retVal = jedis.del(key);
      pool.returnResource(jedis);
    } catch (Exception e) {
      LOGGER.warn("Failed to delete key from cache {0}", key, e);
      pool.returnBrokenResource(jedis);
    }
    return retVal >= 1;
  }


  @Override
  public void deleteByPattern(String pattern) {
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      jedis.eval(String.format(DELETE_SCRIPT_IN_LUA, pattern));
      pool.returnResource(jedis);
    } catch (Exception e) {
      LOGGER.warn("Failed to delete multiple pattern from cache {0}", pattern, e);
      pool.returnBrokenResource(jedis);
    }
  }

  @Override
  public boolean deleteMulti(String... keys) {
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      Transaction trans = jedis.multi();
      for (String key : keys) {
        trans.del(key.getBytes());
      }
      trans.exec();
      pool.returnResource(jedis);
    } catch (Exception e) {
      LOGGER.warn("Failed to delete key from cache {0}", keys, e);
      pool.returnBrokenResource(jedis);
      return false;
    }
    return true;
  }

  @Override
  public void close() {
    pool.close();
  }


  public void zadd(String key, long score, String member) {
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      jedis.zadd(key, score, member);
      pool.returnResource(jedis);
    } catch (Exception e) {
      LOGGER.warn("Failed to zadd key from cache {0}:{1}:{2}", key, score, member, e);
      pool.returnBrokenResource(jedis);
      throw e;
    }
  }

  public Set<String> getAndRemZRange(String key, long score) {
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      Transaction trans = jedis.multi();
      trans.zrangeByScore(key.getBytes(), MIN_INF, SafeEncoder.encode(String.valueOf(score)));
      trans.zremrangeByScore(key.getBytes(), MIN_INF, SafeEncoder.encode(String.valueOf(score)));
      List<Object> response = trans.exec();
      Set<byte[]> data = (Set<byte[]>) response.get(0);
      Set<String> members = new LinkedHashSet<>(data.size());
      for (byte[] d : data) {
        members.add(new String(d));
      }
      pool.returnResource(jedis);
      return members;
    } catch (Exception e) {
      LOGGER.warn("Failed to get zrem keys from cache {0}:{1}", key, score, e);
      pool.returnBrokenResource(jedis);
      throw e;
    }
  }

  public void moveZRange(String currentKey, String destKey) {
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      ZParams zParams = new ZParams();
      zParams.aggregate(ZParams.Aggregate.MIN);
      Long count = jedis.zunionstore(destKey, zParams, currentKey, destKey);
      Long removedCount = jedis.zremrangeByScore(currentKey.getBytes(), MIN_INF, MAX_INF);
      LOGGER.info("{0} tasks in default queue", count);
      LOGGER.info("Moved {0} tasks to default queue", removedCount);
      pool.returnResource(jedis);
    } catch (Exception e) {
      LOGGER.severe("Failed to get zunion keys from cache {0}:{1}", currentKey, destKey, e);
      pool.returnBrokenResource(jedis);
      throw e;
    }
  }
}
