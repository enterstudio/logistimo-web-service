package com.logistimo.api.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableResponse {
  public List<String> headings = new ArrayList<>();
  public Map<String, List<List<ReportDataModel>>> table;
  public List<String> metrics;
}