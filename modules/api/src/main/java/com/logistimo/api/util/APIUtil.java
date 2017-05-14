package com.logistimo.api.util;

import com.logistimo.users.entity.IUserAccount;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by charan on 09/03/17.
 */
public class APIUtil {
  // Get the difference of two user lists: a - b
  public static List<IUserAccount> getDifference(List<IUserAccount> a, List<IUserAccount> b) {
    if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
      return a;
    }
    List<IUserAccount> c = new ArrayList<IUserAccount>();
    Iterator<IUserAccount> itA = a.iterator();
    while (itA.hasNext()) {
      IUserAccount uA = itA.next();
      String userId = uA.getUserId();
      Iterator<IUserAccount> itB = b.iterator();
      boolean isInB = false;
      while (itB.hasNext()) {
        IUserAccount u = itB.next();
        if (userId.equals(u.getUserId())) {
          isInB = true;
          break;
        }
      }
      if (!isInB) {
        c.add(uA);
      }
    }
    return c;
  }
}
