package com.logistimo.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by charan on 22/06/17.
 */
public class UserContactModel {

  @SerializedName("user_id")
  private String userId;

  private String name;

  private String phone;

  private String email;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
