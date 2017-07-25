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

package com.logistimo.proto;

public class MobileApprovalResponse {


  //Approval Id
  private String apprid;

  //Approval status
  private String st;

  //Approval status change time in milliseconds
  private Long t;

  //List of approval user id
  private String arrpvr;

  //List of approver user name
  private String arrpvrn;

  //Requester user id
  private String reqr;

  //requester username
  private String reqrn;

  //expiry time in milliseconds
  private Long expt;

  //requested time in milliseconds
  private Long reqrt;

  //Active approval type
  private String actappr;

  public String getApprid() {
    return apprid;
  }

  public void setApprid(String apprid) {
    this.apprid = apprid;
  }

  public String getSt() {
    return st;
  }

  public void setSt(String st) {
    this.st = st;
  }

  public Long getT() {
    return t;
  }

  public void setT(Long t) {
    this.t = t;
  }

  public String getArrpvr() {
    return arrpvr;
  }

  public void setArrpvr(String arrpvr) {
    this.arrpvr = arrpvr;
  }

  public String getArrpvrn() {
    return arrpvrn;
  }

  public void setArrpvrn(String arrpvrn) {
    this.arrpvrn = arrpvrn;
  }

  public String getReqr() {
    return reqr;
  }

  public void setReqr(String reqr) {
    this.reqr = reqr;
  }

  public String getReqrn() {
    return reqrn;
  }

  public void setReqrn(String reqrn) {
    this.reqrn = reqrn;
  }

  public Long getExpt() {
    return expt;
  }

  public String getActappr() {return actappr;}

  public void setActappr(String actappr) {this.actappr = actappr;}

  public void setExpt(Long expt) {
    this.expt = expt;
  }

  public Long getReqrt() {return reqrt;}

  public void setReqrt(Long reqrt) {this.reqrt = reqrt;}

}
