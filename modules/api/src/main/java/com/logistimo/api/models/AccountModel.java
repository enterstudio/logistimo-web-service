package com.logistimo.api.models;

/**
 * Created by mohan raja on 03/12/14
 */
public class AccountModel {
  public int[] years; // All Years
  public int curyear; // Current Year

  public int sno; // Serial No
  public String name; // Customer/Vendor Name
  public String npay; // Normalised Payable
  public String bal; // Balance = Credit Limit - Payable
  public String cur; // Currency
  public String add; // address
}
