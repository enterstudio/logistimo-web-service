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

package com.logistimo.api.models;

import java.util.Date;

/**
 * Created by naveensnair on 30/06/17.
 */
public class OrderApprovalModel {
  /**
   * Current status
   */
  private String st;
  /**
   * Approval requested time
   */
  private Date ct;
  /**
   * Approval status updated time
   */
  private Date ut;
  /**
   * Reqeuster detail
   */
  private UserModel req;
  /**
   * Hours left to expire
   */
  private Long ext;
  /**
   * Total approvals size for the order
   */
  private Integer ars;



  public String getStatus() {
    return st;
  }

  public void setStatus(String status) {
    this.st = status;
  }

  public Date getCt() {
    return ct;
  }

  public void setCt(Date ct) {
    this.ct = ct;
  }

  public Date getUt() {
    return ut;
  }

  public void setUt(Date ut) {
    this.ut = ut;
  }

  public UserModel getReq() {
    return req;
  }

  public void setReq(UserModel req) {
    this.req = req;
  }

  public Long getExpiryTime() {
    return ext;
  }

  public void setExpiryTime(Long expiryTime) {
    this.ext = expiryTime;
  }

  public Integer getArs() {
    return ars;
  }

  public void setArs(Integer ars) {
    this.ars = ars;
  }
}
