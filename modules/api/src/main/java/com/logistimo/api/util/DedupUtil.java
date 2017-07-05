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

import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.utils.ConfigUtil;

/**
 * @author Mohan Raja
 */
public class DedupUtil {

  private static final int
      CACHE_EXPIRY =
      ConfigUtil.getInt("cache.expiry.for.transaction.signature", 900);// 15 minutes

  public static final int PENDING = 0;
  public static final int SUCCESS = 1;
  public static final int FAILED = 2;

  private DedupUtil() {
  }

  // Set the signature and status in cache
  public static void setSignatureAndStatus(MemcacheService cache, String signature, int status) {
    if (signature != null && cache != null) {
      cache.put(signature, status, CACHE_EXPIRY);
    }
  }

  /**
   * Removes the specified signature from cache
   * @param cache
   * @param signature
   */
  public static void removeSignature(MemcacheService cache, String signature) {
    if (cache != null && signature != null) {
      cache.delete(signature);
    }
  }
}
