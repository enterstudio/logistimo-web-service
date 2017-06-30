package com.logistimo.entity;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by yuvaraj on 22/03/17.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class LocationFailedJob implements ILocationFailedJob{


  @PrimaryKey
  @Persistent(customValueStrategy = "uuid")
  private String id;

  @Persistent
  private String type;

  @Persistent
  private String payload;

  @Persistent
  private Date createDate;

  @Persistent
  private boolean processFlag;

  @Override
  public String getId() {
    return id;
  }

 @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getPayLoad() {
    return payload;
  }

  @Override
  public void setPayLoad(String payload) {
    this.payload = payload;
  }

  @Override
  public Date getCreateDate() {
    return createDate;
  }

  @Override
  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  @Override
  public boolean getProcessFlag() {
    return processFlag;
  }

  @Override
  public void setProcessFlag(boolean processFlag) {
    this.processFlag = processFlag;
  }

}
