package com.logistimo.api.models;

/**
 * Created by mohan raja.
 */
public class UserMessageModel {
  public String type;
  public String template;
  public String text;
  public String pushURL;
  public String appType; // Android smart phone app 'android' or Java Feature phone app 'j2me'
  public String userIds;

  public int sno;
  public String sto; //Sent To
  public String tm; //Time
  public String sta; //Status
  public String sby; //Sent By
  public String evtp;
}
