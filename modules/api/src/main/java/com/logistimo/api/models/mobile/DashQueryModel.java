package com.logistimo.api.models.mobile;

/**
 * Created by kumargaurav on 22/06/17.
 */
public class DashQueryModel {

  public String country;
  public String state;
  public String district;
  public String incetags; // store tags inclusive
  public String exetags; // store tags inclusive
  public String mtags; // material tags
  public String mnm; //material name
  public String loc; //location
  public Integer p; //inventory dashboard period
  public String date; //date
  public Long domainId; //domain id
  public String groupby; //group by
  public String tp; //temp. dashboard period
  public String aty; //asset type
  public String locty; //location type
  public String timezone; // timezone

  public DashQueryModel() {
  }

  public DashQueryModel(String country, String state, String district, String incetags,
                        String exetags, String mtags, String mnm, String loc, Integer p,
                        String date, Long domainId, String groupby) {
    this.country = country;
    this.state = state;
    this.district = district;
    this.incetags = incetags;
    this.exetags = exetags;
    this.mtags = mtags;
    this.mnm = mnm;
    this.loc = loc;
    this.p = p;
    this.date = date;
    this.domainId = domainId;
    this.groupby = groupby;
  }

  public DashQueryModel(String country, String state, String district, String excludeETags, String loc, String tPeriod, Long domainId, String assetType, String level) {
    this.country = country;
    this.state = state;
    this.district = district;
    this.exetags = excludeETags;
    this.loc = loc;
    this.tp = tPeriod;
    this.domainId = domainId;
    this.aty = assetType;
    this.locty = level;
  }

}

