package com.logistimo.proto.utils;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author kkalyan
 */
public class LinkedHashtable extends Hashtable {

  Vector list;

  public LinkedHashtable() {
    list = new Vector();
  }

  public Object put(String key, Object o) {
    if (!list.contains(key)) {
      list.addElement(key);
    }
    return super.put(key, o);
  }

  public Object putFirst(String key, Object o) {
    if (list.contains(key)) {
      list.removeElement(key);
    }
    list.insertElementAt(key, 0);
    return super.put(key, o);
  }

  public Object remove(String key) {
    for (int i = 0; i < list.size(); i++) {
      String elem = (String) list.elementAt(i);
      if (elem.equals(key)) {
        list.removeElementAt(i);
        break;
      }
    }
    return super.remove(key);
  }

  public Vector getKeys() {
    return list;
  }

  public void clear() {
    super.clear();
    list.removeAllElements();
  }
}
