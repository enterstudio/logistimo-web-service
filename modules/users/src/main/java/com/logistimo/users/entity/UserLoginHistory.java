/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.users.entity;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by mohansrinivas on 10/17/16.
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class UserLoginHistory implements IUserLoginHistory {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Integer id;

  @Persistent
  private String userId;

  @Persistent
  private Date t; //logged in time

  @Persistent
  private Integer lgSrc; // login source whether it is from mobile or desktop

  @Persistent
  private String ipAddr; // IP address of clients device

  @Persistent
  private String usrAgnt;

  @Persistent
  private String v;

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public void setUserId(String userId) {
    this.userId = userId;
  }

  @Override
  public Date getLoginTime() {
    return t;
  }

  @Override
  public void setLoginTime(Date login) {
    this.t = login;
  }

  @Override
  public Integer getLgSrc() {
    return lgSrc;
  }

  @Override
  public void setLgSrc(Integer lgSrc) {
    this.lgSrc = lgSrc;
  }

  @Override
  public String getIpAddr() {
    return ipAddr;
  }

  @Override
  public void setIpAddr(String ipAddr) {
    this.ipAddr = ipAddr;
  }

  @Override
  public String getUsrAgnt() {
    return usrAgnt;
  }

  @Override
  public void setUsrAgnt(String usrAgnt) {
    this.usrAgnt = usrAgnt;
  }

  @Override
  public String getVersion() {
    return v;
  }

  @Override
  public void setVersion(String v) {
    this.v = v;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof IUserLoginHistory && ((IUserLoginHistory) o).getUserId().equals(userId);
  }

}
