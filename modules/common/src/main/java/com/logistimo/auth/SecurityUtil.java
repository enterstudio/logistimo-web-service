package com.logistimo.auth;

/**
 * Created by charan on 09/03/17.
 */
public class SecurityUtil {
  // Compare roles to determine which has more privileges
  // Returns 0, if both roles have same privilege
  //		   1, if role1 has more privilege than role2
  //		  -1, if role1 has lesser privilege than role2
  public static int compareRoles(String role1, String role2) {
    if (role1.equals(role2)) {
      return 0;
    }
    if (SecurityConstants.ROLE_SUPERUSER.equals(role1)) {
      return 1;
    } else if (SecurityConstants.ROLE_DOMAINOWNER.equals(role1)) {
      if (SecurityConstants.ROLE_SUPERUSER.equals(role2)) {
        return -1;
      } else {
        return 1;
      }
    } else if (SecurityConstants.ROLE_SERVICEMANAGER.equals(role1)) {
      if (SecurityConstants.ROLE_KIOSKOWNER.equals(role2)) {
        return 1;
      } else {
        return -1;
      }
    } else if (SecurityConstants.ROLE_KIOSKOWNER.equals(role1)) {
      return -1;
    } else {
      return 0;
    }
  }
}
