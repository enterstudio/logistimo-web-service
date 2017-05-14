package com.logistimo.auth;

/**
 * Created by charan on 09/03/17.
 */
public class SecurityConstants {
  public static final String ROLE_DOMAINOWNER = "ROLE_do";
  // User roles
  // NOTE: ROLE_ is a prefix to each role value, given Spring Security expects such a prefix for role values
  public static final String ROLE_KIOSKOWNER = "ROLE_ko";
  public static final String ROLE_SERVICEMANAGER = "ROLE_sm";
  public static final String ROLE_SUPERUSER = "ROLE_su";
  // All roles
  public static final String[] ROLES = {ROLE_KIOSKOWNER, ROLE_SERVICEMANAGER, ROLE_DOMAINOWNER,
      ROLE_SUPERUSER};
}
