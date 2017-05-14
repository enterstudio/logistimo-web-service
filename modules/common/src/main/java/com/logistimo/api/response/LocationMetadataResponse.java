package com.logistimo.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class LocationMetadataResponse extends BaseResponse {
  private static final long serialVersionUID = -6907245306079275238L;
  @SerializedName("data")
  public Map<String, Country> countries;
}
