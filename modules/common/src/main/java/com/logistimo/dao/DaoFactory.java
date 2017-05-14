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
