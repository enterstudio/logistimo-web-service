package com.logistimo.api.models;

import java.util.List;
import java.util.Map;

/**
 * Created by mohan raja on 23/11/14
 */
public class TransactionDomainConfigModel {
  public Map<String, List<String>> reasons;
  public int noc; // No of customers
  public int nov; // No of vendors
  public boolean isMan; // Is Manager
  public List<TransactionModel> customers; // Customers
  public List<TransactionModel> vendors; // Vendors
  public List<TransactionModel> dest; // Destinations
  public boolean showCInv; // Show Customer Inventory
  public String atdi;
  public String atdr;
  public String atdp;
  public String atdw;
  public String atdt;
}
