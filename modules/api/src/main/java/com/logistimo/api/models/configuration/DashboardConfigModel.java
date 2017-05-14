package com.logistimo.api.models.configuration;

/**
 * Created by Mohan Raja on 02/04/15.
 */
public class DashboardConfigModel {
  public boolean edm; //Dashboard panel enabled to managers
  public boolean ape; //Activity panel enabled
  public boolean rpe; //Revenue panel enabled
  public boolean ope; //Order panel enabled
  public boolean ipe; //Inventory panel enabled
  public String[] dmtg; //Default Material tag
  public String dimtg; //Default Inventory Material tag
  public String detg; //Default Entity tag
  public String aper; //Default period for activity
  public String dtt; //Default Transaction type
  public boolean atdd; //Actual date of transaction disabled
  public String createdBy; // user who last updated config
  public String lastUpdated; // last updated time
  public String fn; // first name
  public String[] exet; //list of exclude entity tags
  /**
   * Hide activity dashboard and reports for list of user tags
   */
  public String[] dutg;
  /**
   * List of exclude temperature status
   */
  public String[] exts;
}
