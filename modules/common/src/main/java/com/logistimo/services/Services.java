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

package com.logistimo.services;

import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;
import com.logistimo.services.utils.ConfigUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Services {
  private static final XLog xLogger = XLog.getLog(Services.class);

  private static Map<Class, Object> _factory = new HashMap<>();

  public static <T extends Service> T getService(String beanName) throws ServiceException {
    return getService(beanName, new Locale(Constants.LANG_DEFAULT, ""));
  }


  public static <T extends Service> T getService(Class<T> klass) throws ServiceException {
    return getService(klass, new Locale(Constants.LANG_DEFAULT, ""));
  }

  /**
   * Get service implementation object. This refers the beans.properties to identify the
   * implementation class.
   * @param beanName - bean name as defined
   * @param locale - locale of the user
   * @param <T> - Type of the service class
   * @return - Implementation of the service as defined in beans.properties
   * @throws ServiceException
   */
  public static <T extends Service> T getService(String beanName, Locale locale)
      throws ServiceException {
    String className = ConfigUtil.get(beanName);
    if(className == null ){
      throw new ServiceException("Service class not defined in beans.properties for "+ beanName);
    }
    try {
      Class<T> clazz = (Class<T>) Class.forName(className);
      return getService(clazz, locale);
    } catch (ClassNotFoundException e) {
      throw new ServiceException("Service class not found in beans.properties for "+ beanName);
    }
  }

  public static <T extends Service> T getService(Class<T> clazz, Locale locale)
      throws ServiceException {
    if (locale == null) {
      locale = new Locale(Constants.LANG_DEFAULT, "");
    }
    T instance = (T) _factory.get(clazz.getName()+locale.getDisplayName());
    if (instance == null) {
      synchronized (clazz.getName()) {
        try {
          instance = clazz.newInstance();
          instance.loadResources(locale);
          _factory.put(clazz, clazz.getName()+locale.toString());
        } catch (InstantiationException e) {
          xLogger.severe("Error Instantiating Service class {0}", clazz, e);
          throw new ServiceException(e);
        } catch (IllegalAccessException e) {
          xLogger.severe("Error Accessing Service class {0}", clazz, e);
          throw new ServiceException(e);
        }
      }
    }
    return instance;
  }


  @SuppressWarnings({"unchecked", "rawtypes"})
  public void destroy() throws ServiceException {
    Collection<Object> services = _factory.values();
    for (Object svc : services) {
      ((Service)svc).destroy();
    }
  }

}
