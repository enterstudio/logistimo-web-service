package com.logistimo.models;

import java.util.List;

/**
 * Created by charan on 13/03/17.
 */
public class QueryParamsModel {
  public String query;
  public List<String> params;

  public QueryParamsModel(String query, List<String> params) {
    this.query = query;
    this.params = params;
  }
}
