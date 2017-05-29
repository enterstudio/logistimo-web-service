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

package com.logistimo.dao;

import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by charan on 13/02/15.
 */
public class DaoFactory {

  private static final XLog xLogger = XLog.getLog(DaoFactory.class);

  private static final String DAO_IMPL_PKG = "com.logistimo.dao.impl.";

  private static Map<Class, Object> _factory = new HashMap<Class, Object>();


  private static Object getObject(Class clazz) throws DaoException {
    Object instance = _factory.get(clazz);
    if (instance == null) {
      synchronized (clazz.getName()) {
        String
            implClazzName =
            DAO_IMPL_PKG + clazz.getSimpleName().substring(1);
        try {
          instance = Class.forName(implClazzName).newInstance();
          _factory.put(clazz, instance);
        } catch (ClassNotFoundException e) {
          xLogger.severe("Error Loading DAO class {0}", implClazzName, e);
          throw new DaoException(e);
        } catch (InstantiationException e) {
          xLogger.severe("Error Instantiating DAO class {0}", implClazzName, e);
          throw new DaoException(e);
        } catch (IllegalAccessException e) {
          xLogger.severe("Error Accessing DAO class {0}", implClazzName, e);
          throw new DaoException(e);
        }
      }
    }
    return instance;
  }

}
