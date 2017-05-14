package com.logistimo.api.models.configuration;

import com.logistimo.api.models.UserModel;

import java.util.List;

/**
 * Created by naveensnair on 24/12/14.
 */
public class CustomReportsConfigModel {
  public String tn; // template name
  public String fn; // file Name
  public String tk; // template key
  public String dsc; // description
  public String invsn; // inventory sheet name
  public String usn; // user sheet name
  public String ksn; // entity sheet name
  public String msn; // material sheet name
  public String osn; // order sheet name
  public String od; // number of days order data can be exported
  public String tsn; // transaction sheet name
  public String mtsn; // manualtransaction sheet name
  public String td; // number of days transaction data can be exported
  public String mtd; // number of days manual transaction data can be exported
  public String tcsn; // transaction count sheet name
  public String tct; // transaction count time
  public String tce; // transaction count event
  public String tcd; // number of days transaction count data can be exported
  public String itsn; // inventory trends sheet name
  public String itt; // inventory trends time
  public String ite; // inventory trends event
  public String itd; // number of days inventory trends data can be exported
  public String hsn; // historical sheet name
  public String hd; // number of days before report generation
  public String rgth; // report generation time in hours
  public String rgtw; // report generation time week day
  public String rgtm; // report generation time month day
  public List<UserModel> mn; // manager names
  public List<UserModel> an; // administrators names
  public List<UserModel> sn; // super users names
  public boolean ib; // include batch details
  public boolean ogf; // order generation frequency
  public boolean tgf; // transaction generation frequency
  public boolean mtgf; // manual transaction generation frequency
  public boolean tcrgf; // transaction count report generation frequency
  public boolean itrgf; // inventory trends report generation frequency
  public String rgt; // report generation time
  public String lut; // last updated time
  public String ct; // creation time
  public String origname; //original name for update
  public String createdBy; // user who last updated config
  public String lastUpdated; // last updated time
  public String fName; // full name
  public List<String> exusrs;
  public List<String> usrTgs;
}
