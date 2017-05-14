package com.logistimo.utils;

import com.logistimo.logger.XLog;

import java.util.List;

public class ParamChecker {
  public static <T> T notNull(T obj, String name) {
    if (obj == null) {
      throw new IllegalArgumentException(name + " cannot be null.");
    }

    return obj;
  }

  public static String notEmpty(String value, String name) {
    notNull(value, name).toString();

    if (value.length() == 0) {
      throw new IllegalArgumentException(name + " cannot be empty.");
    }

    return value;
  }

  public static <T> T[] notNullElements(T[] array, String name) {
    notNull(array, name);

    for (int i = 0; i < array.length; i++) {
      notNull(array[i], XLog.format("list [{0}] element [{1}]", name, i));
    }

    return array;
  }

  public static <T> List<T> notNullElements(List<T> list, String name) {
    notNull(list, name);

    for (int i = 0; i < list.size(); i++) {
      notNull(list.get(i), XLog.format("list [{0}] element [{1}]", name, i));
    }

    return list;
  }
}
