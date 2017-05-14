package com.logistimo.api.models.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naveensnair on 17/11/14.
 */
public class TagsConfigModel {
  public String[] mt;
  public String[] et;
  public String[] rt;
  public String[] ot;
  public String[] ut;
  public boolean emt;
  public boolean eet;
  public boolean eot;
  public String en;
  public boolean eut;
  public String createdBy;
  public String lastUpdated;
  public String fn;
  public List<ETagOrder> etr = new ArrayList<>(1);

  public static class ETagOrder {
    public String etg;
    public Integer rnk;
  }
}
