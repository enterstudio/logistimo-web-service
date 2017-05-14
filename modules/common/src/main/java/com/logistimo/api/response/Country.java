package com.logistimo.api.response;

import java.io.Serializable;
import java.util.Map;

public class Country implements Serializable {
  private static final long serialVersionUID = 509685909046871004L;
  public String name;
  public String countryPhoneCode;
  public Map<String, State> states;
}
