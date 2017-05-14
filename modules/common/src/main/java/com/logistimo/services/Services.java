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
