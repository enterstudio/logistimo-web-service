package com.logistimo.api.models;

/**
 * Created by naveensnair on 07/09/15.
 */
public class PasswordModel {
  public String mp; // masked phone number
  public String me; // masked email id
  public String uid; // userId
  public int mode; // mode sms or email
  public String otp; // otp received via sms
}
