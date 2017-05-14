/**
 *
 */
package com.logistimo.api.models;

import java.util.Date;
import java.util.List;

/**
 * @author charan
 */
public class EntityGroupModel {

  public Long id;
  public String nm;
  public String ct; //city
  public String dt; //district
  public String st; //state
  public String str; //street
  public String cnt; //country
  public String tlk; //taluk
  public String uid; //owner id
  public String add; //address
  public String unm;
  public String dsc; //description
  public int num;
  public Date t;
  public int sno;
  public List<EntityModel> ent;

  public String creBy; //created by
  public String creByn;
  public String updBy; //Last updated by
  public String updByn;
  public String updOn;   // Last updated time
  public String creOn; //created on

  /**
   * Group Id
   */
  public Long gid;
}
