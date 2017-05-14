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

package com.logistimo.domains.entity;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by naveensnair on 08/09/15.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class DomainPermission implements IDomainPermission {

  @PrimaryKey
  @Persistent
  private Long dId; // Domain ID
  @Persistent
  private Boolean uv = true; //isUsersView
  @Persistent
  private Boolean ua = true; //isUsersAdd
  @Persistent
  private Boolean ue = true; //isUsersEdit
  @Persistent
  private Boolean ur = true; //isUsersRemove
  @Persistent
  private Boolean ev = true; //isEntityView
  @Persistent
  private Boolean ea = true; //isEntityAdd
  @Persistent
  private Boolean ee = true; //isEntityEdit
  @Persistent
  private Boolean er = true; //isEntityRemove
  @Persistent
  private Boolean egv = true; //isEntityGroupView
  @Persistent
  private Boolean ega = true; //isEntityGroupAdd
  @Persistent
  private Boolean ege = true; //isEntityGroupEdit
  @Persistent
  private Boolean egr = true; //isEntityGroupRemove
  @Persistent
  private Boolean erv = true; //isEntityRelationshipView
  @Persistent
  private Boolean era = true; //isEntityRelationshipAdd
  @Persistent
  private Boolean ere = true; //isEntityRelationshipEdit
  @Persistent
  private Boolean err = true; //isEntityRelationshipRemove
  @Persistent
  private Boolean iv = true; //isInventoryView
  @Persistent
  private Boolean ia = true; //isInventoryAdd
  @Persistent
  private Boolean ie = true; //isInventoryEdit
  @Persistent
  private Boolean ir = true; //isInventoryRemove
  @Persistent
  private Boolean mv = true; //isMaterialView
  @Persistent
  private Boolean ma = true; //isMaterialAdd
  @Persistent
  private Boolean me = true; //isMaterialEdit
  @Persistent
  private Boolean mr = true; //isMaterialRemove
  @Persistent
  private Boolean cc = true; //copyConfiguration
  @Persistent
  private Boolean cm = true; //copyMaterials
  @Persistent
  private Boolean cv = true; //configurationView
  @Persistent
  private Boolean ce = true; //configurationEdit

  @Persistent
  private Boolean ae = true;
  @Persistent
  private Boolean ar = true;
  @Persistent
  private Boolean aa = true;
  @Persistent
  private Boolean av = true;


  @Override
  public Long getdId() {
    return dId;
  }

  @Override
  public void setdId(Long dId) {
    this.dId = dId;
  }

  @Override
  public Boolean isUsersView() {
    return uv;
  }

  @Override
  public void setUsersView(Boolean iuv) {
    this.uv = iuv;
  }

  @Override
  public Boolean isUsersAdd() {
    return ua;
  }

  @Override
  public void setUsersAdd(Boolean iua) {
    this.ua = iua;
  }

  @Override
  public Boolean isUsersEdit() {
    return ue;
  }

  @Override
  public void setUsersEdit(Boolean iue) {
    this.ue = iue;
  }

  @Override
  public Boolean isUsersRemove() {
    return ur;
  }

  @Override
  public void setUsersRemove(Boolean iur) {
    this.ur = iur;
  }

  @Override
  public Boolean isEntityView() {
    return ev;
  }

  @Override
  public void setEntityView(Boolean iev) {
    this.ev = iev;
  }

  @Override
  public Boolean isEntityAdd() {
    return ea;
  }

  @Override
  public void setEntityAdd(Boolean iea) {
    this.ea = iea;
  }

  @Override
  public Boolean isEntityEdit() {
    return ee;
  }

  @Override
  public void setEntityEdit(Boolean iee) {
    this.ee = iee;
  }

  @Override
  public Boolean isEntityRemove() {
    return er;
  }

  @Override
  public void setEntityRemove(Boolean ier) {
    this.er = ier;
  }

  @Override
  public Boolean isEntityGroupView() {
    return egv;
  }

  @Override
  public void setEntityGroupView(Boolean iegv) {
    this.egv = iegv;
  }

  @Override
  public Boolean isEntityGroupAdd() {
    return ega;
  }

  @Override
  public void setEntityGroupAdd(Boolean iega) {
    this.ega = iega;
  }

  @Override
  public Boolean isEntityGroupEdit() {
    return ege;
  }

  @Override
  public void setEntityGroupEdit(Boolean iege) {
    this.ege = iege;
  }

  @Override
  public Boolean isEntityGroupRemove() {
    return egr;
  }

  @Override
  public void setEntityGroupRemove(Boolean iegr) {
    this.egr = iegr;
  }

  @Override
  public Boolean isEntityRelationshipView() {
    return erv;
  }

  @Override
  public void setEntityRelationshipView(Boolean erv) {
    this.erv = erv;
  }

  @Override
  public Boolean isEntityRelationshipAdd() {
    return era;
  }

  @Override
  public void setEntityRelationshipAdd(Boolean era) {
    this.era = era;
  }

  @Override
  public Boolean isEntityRelationshipEdit() {
    return ere;
  }

  @Override
  public void setEntityRelationshipEdit(Boolean ere) {
    this.ere = ere;
  }

  @Override
  public Boolean isEntityRelationshipRemove() {
    return err;
  }

  @Override
  public void setEntityRelationshipRemove(Boolean err) {
    this.err = err;
  }

  @Override
  public Boolean isInventoryView() {
    return iv;
  }

  @Override
  public void setInventoryView(Boolean iiv) {
    this.iv = iiv;
  }

  @Override
  public Boolean isInventoryAdd() {
    return ia;
  }

  @Override
  public void setInventoryAdd(Boolean iia) {
    this.ia = iia;
  }

  @Override
  public Boolean isInventoryEdit() {
    return ie;
  }

  @Override
  public void setInventoryEdit(Boolean iie) {
    this.ie = iie;
  }

  @Override
  public Boolean isInventoryRemove() {
    return ir;
  }

  @Override
  public void setInventoryRemove(Boolean iir) {
    this.ir = iir;
  }

  @Override
  public Boolean isMaterialView() {
    return mv;
  }

  @Override
  public void setMaterialView(Boolean imv) {
    this.mv = imv;
  }

  @Override
  public Boolean isMaterialAdd() {
    return ma;
  }

  @Override
  public void setMaterialAdd(Boolean ima) {
    this.ma = ima;
  }

  @Override
  public Boolean isMaterialEdit() {
    return me;
  }

  @Override
  public void setMaterialEdit(Boolean ime) {
    this.me = ime;
  }

  @Override
  public Boolean isMaterialRemove() {
    return mr;
  }

  @Override
  public void setMaterialRemove(Boolean imr) {
    this.mr = imr;
  }

  @Override
  public Boolean isCopyConfiguration() {
    return cc;
  }

  @Override
  public void setCopyConfiguration(Boolean cc) {
    this.cc = cc;
  }

  @Override
  public Boolean isCopyMaterials() {
    return cm;
  }

  @Override
  public void setCopyMaterials(Boolean cm) {
    this.cm = cm;
  }

  @Override
  public Boolean isConfigurationView() {
    return cv;
  }

  @Override
  public void setConfigurationView(Boolean cv) {
    this.cv = cv;
  }

  @Override
  public Boolean isConfigurationEdit() {
    return ce;
  }

  @Override
  public void setConfigurationEdit(Boolean ce) {
    this.ce = ce;
  }

  @Override
  public Boolean isAssetEdit() {
    return ae;
  }

  @Override
  public void setAssetEdit(Boolean ae) {
    this.ae = ae;
  }

  @Override
  public Boolean isAssetRemove() {
    return ar;
  }

  @Override
  public void setAssetRemove(Boolean ar) {
    this.ar = ar;
  }

  @Override
  public Boolean isAssetAdd() {
    return aa;
  }

  @Override
  public void setAssetAdd(Boolean aa) {
    this.aa = aa;
  }

  @Override
  public Boolean isAssetView() {
    return av;
  }

  @Override
  public void setAssetView(Boolean av) {
    this.av = av;
  }


}
