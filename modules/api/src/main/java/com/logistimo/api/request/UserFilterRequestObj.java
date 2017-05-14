package com.logistimo.api.request;

/**
 * Created by vani on 06/01/16.
 */
public class UserFilterRequestObj {
  public int offset;
  public int size;
  public String role;
  public String nName;
  public String mobilePhoneNumber;
  public String lastLoginFrom;
  public String lastLoginTo;
  public String v;
  public String
      isEnabled;
  // can be "" for all users, true for active users or false for disabled users
  public boolean neverLogged;
  public String tgs;
}
