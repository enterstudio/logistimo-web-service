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

/**
 *
 */
package com.logistimo.materials.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.logistimo.config.models.EventsConfig;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;

import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Material implements IMaterial {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long materialId;
  @Persistent(table = "MATERIAL_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain ID
  @Persistent
  private String vertical;
  @Persistent
  private String name;
  @Persistent
  @Column(length = 400)
  private String description;

  @NotPersistent
  private List<String> oldtags;

  @Persistent(table = "MATERIAL_TAGS", defaultFetchGroup = "true")
  @Join(column = "materialId")
  @Element(column = "id")
  private List<Tag> tgs; // list of material tags

  @Persistent
  private String idType;
  @Persistent
  private String idValue;
  @Persistent
  private Boolean seasonal;
  @Persistent
  private String imagePath;
  @Persistent
  private Date timeStamp; // creation date
  @Persistent
  private Date lastUpdated;
  @Persistent
  private String cb;
  @Persistent
  private String ub;
  @Persistent
  private BigDecimal retailPrice = BigDecimal.ZERO; // MSRP
  @Persistent
  private BigDecimal
      salePrice;
  // price at which product is sold; used Float object, given this is an upgrade, and datastore returns a null for all older objects without this property
  @Persistent
  private BigDecimal retailerPrice; // price at which it is given to retailer (with discount)
  @Persistent
  private String currency = Constants.CURRENCY_DEFAULT;
  @Persistent
  private String uName; // unique name code - for sorting purposes
  @Persistent
  private String scode; // short-code (say, 0, 1, 2, ...) for sending over SMS
  @Persistent
  private String sname; // short-name for sending over SMS
  @Persistent
  @Column(jdbcType = "VARCHAR", length = 400)
  private String info; // Info. on this material to be shown on mobile as well
  @Persistent
  private Boolean dispInfo = true; // display information or not on mobile
  @Persistent
  private String dty; // data type, e.g. Binary
  @Persistent
  private Boolean bm; // enable batch management
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Boolean mbm; // enable batch management on mobile device
  @Persistent
  private String cId; // Custom ID
  @Persistent
  private Boolean tmp; // enable temperature monitoring
  @Persistent
  private Float tmin; // min. temp.
  @Persistent
  private Float tmax; // max temp.

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;

  // Get formatted price, formatted to two decimal places
  public static String getFormattedPrice(float price) {
    return String.format("%.2f", price);
  }

  public static String getIdTypeDisplay(String idType) {
    String idTypeDisplay = "";
    if (IDTYPE_UPC.equals(idType)) {
      idTypeDisplay = "UPC";
    } else if (IDTYPE_ISBN.equals(idType)) {
      idTypeDisplay = "ISBN";
    } else if (IDTYPE_NONSTANDARD.equals(idType)) {
      idTypeDisplay = "Non-standard";
    }
    return idTypeDisplay;
  }

  /**
   * @return the materialId
   */
  @Override
  public Long getMaterialId() {
    return materialId;
  }

  /**
   * @param materialId the materialId to set
   */
  @Override
  public void setMaterialId(Long materialId) {
    this.materialId = materialId;
  }

  public Long getDomainId() {
    return sdId;
  }

  public void setDomainId(Long domainId) {
    sdId = domainId;
  }

  public List<Long> getDomainIds() {
    return dId;
  }

  public void setDomainIds(List<Long> domainIds) {
    this.dId.clear();
    this.dId.addAll(domainIds);
  }

  public void addDomainIds(List<Long> domainIds) {
    if (domainIds == null || domainIds.isEmpty()) {
      return;
    }
    if (this.dId == null) {
      this.dId = new ArrayList<>();
    }
    for (Long dId : domainIds) {
      if (!this.dId.contains(dId)) {
        this.dId.add(dId);
      }
    }
  }

  public void removeDomainId(Long domainId) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.remove(domainId);
    }
  }

  public void removeDomainIds(List<Long> domainIds) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.removeAll(domainIds);
    }
  }

  /**
   * @return the verticalOrIndustry
   */
  @Override
  public String getVertical() {
    return vertical;
  }

  /**
   * @param vertical the verticalOrIndustry to set
   */
  @Override
  public void setVertical(String vertical) {
    this.vertical = vertical;
  }

  /**
   * @return the name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  @Override
  public void setName(String name) {
    this.name = name;
    if (name != null) {
      this.uName = name.toLowerCase();
    } else {
      this.uName = null;
    }
  }

  @Override
  public String getTruncatedName(int numChars) {
    if (name != null && name.length() > numChars) {
      return name.substring(0, numChars) + "...";
    }
    return name;
  }

  /**
   * @return the description
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the tags
   */
  @Override
  public List<String> getTags() {
    if (oldtags == null) {
      oldtags = TagUtil.getList(tgs);
    }
    return oldtags;
  }

  /**
   * @param tags the tags to set
   */
  @Override
  public void setTags(List<String> tags) {
    this.oldtags = tags;
  }

  @Override
  public boolean hasTag(String tag) {
    getTags();
    return oldtags != null && oldtags.contains(tag);
  }

  /**
   * @return the identifierType
   */
  @Override
  public String getIdentifierType() {
    return idType;
  }

  /**
   * @param identifierType the identifierType to set
   */
  @Override
  public void setIdentifierType(String identifierType) {
    this.idType = identifierType;
  }

  /**
   * @return the identifierValue
   */
  @Override
  public String getIdentifierValue() {
    return idValue;
  }

  /**
   * @param identifierValue the identifierValue to set
   */
  @Override
  public void setIdentifierValue(String identifierValue) {
    this.idValue = identifierValue;
  }

  /**
   * @return the isSeasonal
   */
  @Override
  public boolean isSeasonal() {
    return seasonal;
  }

  /**
   * @param seasonal the isSeasonal to set
   */
  @Override
  public void setSeasonal(boolean seasonal) {
    this.seasonal = seasonal;
  }

  /**
   * @return the imagePath
   */
  @Override
  public String getImagePath() {
    return imagePath;
  }

  /**
   * @param imagePath the imagePath to set
   */
  @Override
  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

  /**
   * @return the timeStamp
   */
  @Override
  public Date getTimeStamp() {
    return timeStamp;
  }

  /**
   * @param timeStamp the timeStamp to set
   */
  @Override
  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  @Override
  public Date getLastUpdated() {
    return lastUpdated;
  }

  @Override
  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public BigDecimal getMSRP() {
    return retailPrice;
  }

  @Override
  public void setMSRP(BigDecimal retailPrice) {
    this.retailPrice = retailPrice;
  }

  @Override
  public BigDecimal getSalePrice() {
    if (salePrice != null) {
      return salePrice;
    }

    return BigDecimal.ZERO;
  }

  @Override
  public void setSalePrice(BigDecimal salePrice) {
    this.salePrice = salePrice;
  }

  @Override
  public BigDecimal getRetailerPrice() {
    if (retailerPrice != null) {
      return retailerPrice;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void setRetailerPrice(BigDecimal retailerPrice) {
    this.retailerPrice = retailerPrice;
  }

  @Override
  public String getCurrency() {
    return currency;
  }

  @Override
  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Override
  public String getUniqueName() {
    return uName;
  }

  @Override
  public String getShortCode() {
    return scode;
  }

  @Override
  public void setShortCode(String scode) {
    this.scode = scode;
  }

  @Override
  public String getShortName() {
    return sname;
  }

  @Override
  public void setShortName(String sname) {
    this.sname = sname;
  }

  @Override
  public String getInfo() {
    return info;
  }

  @Override
  public void setInfo(String info) {
    this.info = info;
  }

  @Override
  public String getType() {
    return dty;
  }

  @Override
  public void setType(String ty) {
    this.dty = ty;
  }

  @Override
  public boolean isBinaryValued() {
    return TYPE_BINARY.equals(dty);
  }

  @Override
  public boolean displayInfo() {
    if (dispInfo != null) {
      return dispInfo.booleanValue();
    }
    return true;
  }

  @Override
  public void setInfoDisplay(boolean display) {
    this.dispInfo = display;
  }

  @Override
  public boolean isBatchEnabled() {
    return bm != null && bm.booleanValue();
  }

  @Override
  public void setBatchEnabled(boolean batchEnabled) {
    bm = batchEnabled;
  }

  @Override
  public boolean isBatchEnabledOnMobile() {
    return mbm != null && mbm.booleanValue();
  }

  @Override
  public void setBatchEnabledOnMobile(boolean batchEnabled) {
    mbm = batchEnabled;
  }

  @Override
  public String getCustomId() {
    return cId;
  }

  @Override
  public void setCustomId(String customId) {
    this.cId = customId;
  }

  @Override
  public boolean isTemperatureSensitive() {
    return tmp != null && tmp.booleanValue();
  }

  @Override
  public void setTemperatureSensitive(boolean isSensitive) {
    tmp = isSensitive;
  }

  @Override
  public float getTemperatureMin() {
    return NumberUtil.getFloatValue(tmin);
  }

  @Override
  public void setTemperatureMin(float min) {
    tmin = new Float(min);
  }

  @Override
  public float getTemperatureMax() {
    return NumberUtil.getFloatValue(tmax);
  }

  @Override
  public void setTemperatureMax(float max) {
    tmax = new Float(max);
  }

  public String getCreatedBy() {
    return cb;
  }

  public void setCreatedBy(String cb) {
    this.cb = cb;
  }

  public String getLastUpdatedBy() {
    return ub;
  }

  public void setLastUpdatedBy(String lub) {
    this.ub = lub;
  }




  @Override
  public String toJsonStr() {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    String jsonStr = gson.toJson(this);
    return jsonStr;
  }

  public List<? extends ITag> getTgs() {
    return tgs;
  }

  @Override
  public void setTgs(List<? extends ITag> tgs) {
    if (this.tgs != null) {
      this.tgs.clear();
      if (oldtags != null) {
        this.tgs.addAll((Collection<Tag>) tgs);
      }
    } else {
      this.tgs = (List<Tag>) tgs;
    }
    this.oldtags = null;
  }

  public Date getArchivedAt() {
    return arcAt;
  }

  public void setArchivedAt(Date archivedAt) {
    arcAt = archivedAt;
  }

  public String getArchivedBy() {
    return arcBy;
  }

  public void setArchivedBy(String archivedBy) {
    arcBy = archivedBy;
  }

}
