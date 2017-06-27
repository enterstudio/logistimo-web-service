package com.logistimo.entities.entity;

import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by naveensnair on 19/05/17.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Approvers implements IApprovers{

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  @Persistent
  private Long kid;
  @NotPersistent
  List<String> pa;
  @NotPersistent
  List<String> sa;
  @Persistent
  private String uid;
  @Persistent
  private Integer type;
  @Persistent
  private String otype;
  @Persistent
  private Date con;
  @Persistent
  private String cby;
  @Persistent
  private Date uon;
  @Persistent
  private String uby;
  @Persistent
  private Long sdid;

  public Long getId() {
    return id;
  }

  public Long getKioskId() {
    return kid;
  }

  public void setKioskId(Long kid) {
    this.kid = kid;
  }

  public String getUserId() {
    return uid;
  }

  public void setUserId(String uid) {
    this.uid = uid;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public String getOrderType() {
    return otype;
  }

  public void setOrderType(String orderType) {
    this.otype = orderType;
  }

  public Date getCreatedOn() {
    return con;
  }

  public void setCreatedOn(Date con) {
    this.con = con;
  }

  public String getCreatedBy() {
    return cby;
  }

  public void setCreatedBy(String cby) {
    this.cby = cby;
  }

  public Date getUpdatedOn() {
    return uon;
  }

  public void setUpdatedOn(Date uon) {
    this.uon = uon;
  }

  public String getUpdatedBy() {
    return uby;
  }

  public void setUpdatedBy(String uby) {
    this.uby = uby;
  }
  public List<String> getPrimaryApprovers() {
    return pa;
  }

  public void setPrimaryApprovers(List<String> pa) {
    this.pa = pa;
  }

  public List<String> getSecondaryApprovers() {
    return sa;
  }

  public void setSecondaryApprovers(List<String> sa) {
    this.sa = sa;
  }

  public Long getSourceDomainId() {
    return sdid;
  }

  public void setSourceDomainId(Long sdid) {
    this.sdid = sdid;
  }

}
