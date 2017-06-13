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

package com.logistimo.inventory;

import com.logistimo.logger.XLog;

import org.springframework.util.StringUtils;

/**
 * Created by vani on 19/04/17.
 */
public class MobileTransactionsHandlerFactory {
  private static final XLog xLogger = XLog.getLog(MobileTransactionsHandlerFactory.class);
  public static int POLICY_1 = 1;
  public static MobileTransactionsHandler getInstance(int policyType) {
    String className = null;
    // Based on the type, get the enum
    if (policyType == POLICY_1) {
      className = MobileTransactionsHandlerPolicy.POLICY_1.getClassName();
    } else {
      xLogger.severe("Exception while getting class name for policy {0} while handling mobile transactions", policyType);
      return null;
    }
    try {
      Class<?> clazz = Class.forName(className);
      return (MobileTransactionsHandler) clazz.newInstance();
    } catch (Exception e) {
      xLogger.severe("Exception while getting instance of MobileTransactionsHandler, className: {0}", className, e);
    }
    return null;
  }
}
