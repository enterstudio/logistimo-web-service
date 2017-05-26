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

package com.logistimo.utils;

import com.logistimo.AppFactory;
import com.logistimo.services.cache.MemcacheService;

import com.logistimo.constants.Constants;

import java.util.Arrays;

/**
 * @author Mohan Raja
 */
public class LockUtil {

  private static final int DEFAULT_RETRY_COUNT = 50;
  private static final int DEFAULT_WAIT_TIME_IN_MILLIS = 500;
  private static MemcacheService cache = AppFactory.get().getMemcacheService();

  public static boolean isLocked(LockStatus lockStatus) {
    return !lockStatus.equals(LockStatus.FAILED_TO_LOCK);
  }

  public static boolean shouldReleaseLock(LockStatus lockStatus) {
    return lockStatus.equals(LockStatus.NEW_LOCK);
  }

  public static LockStatus lock(String key, int retryCount) {
    return lock(key, retryCount, DEFAULT_WAIT_TIME_IN_MILLIS);
  }

  public static LockStatus lock(String key) {
    return lock(key, DEFAULT_RETRY_COUNT, DEFAULT_WAIT_TIME_IN_MILLIS);
  }

  public static LockStatus lock(String key, int retryCount, int retryDelayInMillis) {
    while (retryCount-- > 0) {
      if (ThreadLocalUtil.get().locks.contains(key)) {
        return LockStatus.ALREADY_LOCKED;
      }
      if (cache.putIfNotExist(key, Constants.EMPTY)) {
        ThreadLocalUtil.get().locks.add(key);
        return LockStatus.NEW_LOCK;
      }
      if (retryCount > 0) {
        try {
          Thread.sleep(retryDelayInMillis);
        } catch (InterruptedException ignored) {
        }
      }
    }
    return LockStatus.FAILED_TO_LOCK;
  }

  public static boolean release(String key) {
    ThreadLocalUtil.get().locks.remove(key);
    return cache.delete(key);
  }

  public static boolean release(String... keys) {
    ThreadLocalUtil.get().locks.removeAll(Arrays.asList(keys));
    return cache.deleteMulti(keys);
  }

  public static LockStatus doubleLock(String lockKey, String lockKey2, int retryCount,
                                      int retryDelayInMillis) {
    while (retryCount-- > 0) {
      if (ThreadLocalUtil.get().locks.contains(lockKey) && ThreadLocalUtil.get().locks
          .contains(lockKey2)) {
        return LockStatus.ALREADY_LOCKED;
      }
      if (cache.putMultiIfNotExists(lockKey, Constants.EMPTY, lockKey2, Constants.EMPTY)) {
        ThreadLocalUtil.get().locks.add(lockKey);
        ThreadLocalUtil.get().locks.add(lockKey2);
        return LockStatus.NEW_LOCK;
      }
      if (retryCount > 0) {
        try {
          Thread.sleep(retryDelayInMillis);
        } catch (InterruptedException ignored) {
        }
      }
    }
    return LockStatus.FAILED_TO_LOCK;
  }

  public enum LockStatus {
    NEW_LOCK, ALREADY_LOCKED, FAILED_TO_LOCK
  }
}
