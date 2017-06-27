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


import com.google.gson.JsonObject;

public class OrderMinimumResponseModel {

  //Order Id
  private Long tid;

  //Number of Items
  private Integer q;

  //customer Kiosk Id
  private Long kid;

  //customer kiosk name
  private String knm;

  //Customer kiosk city
  private String kcty;

  //Vendor kiosk id
  private Long vid;

  //Vendor kiosk name
  private String vnm;

  //Vendor kiosk city
  private String vcty;

  //Order creation time
  private String t;

  //Order status
  private String ost;

  //Updated time
  private String ut;

  //Order approval Details
  private JsonObject apprvl;

  //Created by user name
  private String cbn;

  //updated by user id
  private String ubid;

  //updated by user name
  private String ubn;

  //Comma separated tags
  private String tg;

  //Created by user id
  private String cbid;

  public JsonObject getApprvl() {
    return apprvl;
  }

  public void setApprvl(JsonObject apprvl) {
    this.apprvl = apprvl;
  }

  public String getCbid() {
    return cbid;
  }

  public void setCbid(String cbid) {
    this.cbid = cbid;
  }

  public String getCbn() {
    return cbn;
  }

  public void setCbn(String cbn) {
    this.cbn = cbn;
  }

  public String getUbid() {
    return ubid;
  }

  public void setUbid(String ubid) {
    this.ubid = ubid;
  }

  public String getUbn() {
    return ubn;
  }

  public void setUbn(String ubn) {
    this.ubn = ubn;
  }

  public Long getVid() {
    return vid;
  }

  public void setVid(Long vid) {
    this.vid = vid;
  }

  public String getVnm() {
    return vnm;
  }

  public void setVnm(String vnm) {
    this.vnm = vnm;
  }

  public String getVcty() {
    return vcty;
  }

  public void setVcty(String vcty) {
    this.vcty = vcty;
  }

  public String getT() {
    return t;
  }

  public void setT(String t) {
    this.t = t;
  }

  public String getOst() {
    return ost;
  }

  public void setOst(String ost) {
    this.ost = ost;
  }

  public String getUt() {
    return ut;
  }

  public void setUt(String ut) {
    this.ut = ut;
  }

  public String getTg() {
    return tg;
  }

  public void setTg(String tg) {
    this.tg = tg;
  }

  public Long getKid() {
    return kid;
  }

  public void setKid(Long kid) {
    this.kid = kid;
  }

  public String getKnm() {
    return knm;
  }

  public void setKnm(String knm) {
    this.knm = knm;
  }

  public String getKcty() {
    return kcty;
  }

  public void setKcty(String kcty) {
    this.kcty = kcty;
  }

  //Methods for get/set orderId
  public Long getTid() {
    return tid;
  }

  public void setTid(Long tid) {
    this.tid = tid;
  }

  public Integer getQ() {
    return q;
  }

  public void setQ(Integer q) {
    this.q = q;
  }
}
