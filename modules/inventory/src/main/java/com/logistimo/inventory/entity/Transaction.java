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
package com.logistimo.inventory.entity;

import com.logistimo.config.models.EventsConfig;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.materials.entity.Material;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;
import com.logistimo.users.entity.UserAccount;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.services.Resources;

import com.logistimo.services.impl.PMF;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.constants.SourceConstants;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
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
 *         NOTE: Attribute names are kept small to optimize storage on data store.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true", cacheable = "false")
public class Transaction implements ITransaction {

  private static final XLog xLogger = XLog.getLog(XLog.class);
  // user Id of user who is the source or originater of this transaction (e.g. one who ordered)
  @Persistent
  String
      duId;
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key;
  @Persistent(table = "TRANSACTION_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain ID
  @Persistent
  private String type; // transaction type (e.g. issue, receipt, phys. stock count, order)
  @Persistent
  private Long mId; // material Id
  @Persistent
  private Long kId; // kiosk Id
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private BigDecimal q = BigDecimal.ZERO; // quantity
  @Persistent
  @Column(jdbcType = "TIMESTAMP")
  private Date t; // timestamp of transaction
  @Persistent
  private String
      uId;
  // user Id of the user who is the destination or target of this transaction (e.g. one who received) [added 11/08/2010]
  @Persistent
  private String bid; // batch id/number
  @Persistent
  private Date bexp; // batch expiry date
  @Persistent
  private String bmfnm; // batch manufacturer name
  @Persistent
  private Date bmfdt; // batch manufactured date
  @Persistent
  private Long lkId; // linked kiosk id (customer for issues or vendor for receipts)
  @Persistent
  private String
      tid;
  // Tracking-id, such as OID, typically used when inventory transactions are automatically posted along with an order
  @Persistent
  private Long
      ltid;
  // linked transaction ID (say, a issue/receipt transaction is linked to its corresponding transfer transaction)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private BigDecimal sdf = BigDecimal.ZERO; // stock quantity difference (based on a stock-count)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private BigDecimal cs; // closing stock (overall)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private BigDecimal csb; // closing stock for batch, if batch data present
  @Persistent
  private Double lt; // latitude
  @Persistent
  private Double ln; // longitude
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Double gacc; // geo-accuracy in meters, if lat/lng were given
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String gerr; // geo-error code, in case of an error
  /**
   * Reason code
   */
  @Persistent
  private String rs; // reason code
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Integer ast; // aggregation status

  @Persistent(table = "TRANSACTION_MTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> mtgs;

  @NotPersistent
  private List<String>
      oldmtgs;
  // list of material tags, if present (used mainly for queries involving kioskId and tags)

  @Persistent(table = "TRANSACTION_KTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> ktgs;

  @NotPersistent
  private List<String> oldktgs; // list of kiosk tags (for queries)

  @Persistent
  private BigDecimal os; // opening stock
  @Persistent
  private BigDecimal osb; // opening stock by batch, if batch data present

  @NotPersistent
  private String
      msg;
  // used to carry any error messages, if needed (typically used to temporarily store inventory transaction update errors)

  @NotPersistent
  private String
      msgCode;
  // used to carry any error code, if needed (typically used to temporarily store inventory transaction update errors)

  @NotPersistent
  private boolean
      useCustomTimestamp =
      false;
  // if set, then use timestamp set by application, rather than system assigned (say, used in kiosk simulator)

  @Persistent
  private String tot; // Linked object type

  @Persistent
  private Integer src = SourceConstants.MOBILE; // Source of transaction; default set for mobile

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;

  @Persistent // actual transaction date
  private Date atd;


  @Persistent
  private Boolean eatd;

  /**
   * Material Status
   */
  @Persistent
  private String mst;
  /**
   * Altitude of the transaction
   */
  @Persistent
  private Double alt;

  @NotPersistent
  private String
      eoqrsn;
  // Only used by Rest API while updating order quantity. Mapped to sdrsn in DemandItem

  public static String getDisplayName(String transType, Locale locale) {
    return getDisplayName(transType, DomainConfig.TRANSNAMING_DEFAULT, locale);
  }

  public static String getDisplayName(String transType, String transNaming, Locale locale) {
    String name = "";
    // Get the resource bundle
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    if (messages == null) {
      return "";
    }
    if (TYPE_ISSUE.equals(transType)) {
      name =
          DomainConfig.TRANSNAMING_ISSUESRECEIPTS.equals(transNaming) ? messages
              .getString("transactions.issue") : messages.getString("transactions.sale");
    } else if (TYPE_RECEIPT.equals(transType)) {
      name =
          DomainConfig.TRANSNAMING_ISSUESRECEIPTS.equals(transNaming) ? messages
              .getString("transactions.receipt") : messages.getString("transactions.purchase");
    } else if (TYPE_PHYSICALCOUNT.equals(transType)) {
      name = messages.getString("transactions.stockcount");
    } else if (TYPE_ORDER.equals(transType)) {
      name = messages.getString("transactions.order");
    } else if (TYPE_REORDER.equals(transType)) {
      name = messages.getString("transactions.reorder");
    } else if (TYPE_WASTAGE.equals(transType)) {
      name = messages.getString("transactions.wastage");
    } else if (TYPE_RETURN.equals(transType)) {
      name = messages.getString("transactions.return");
    } else if (TYPE_TRANSFER.equals(transType)) {
      name = messages.getString("transactions.transfer");
    }
    return name;
  }

  public Transaction clone() {
    Transaction t = null;
    try {
      t = (Transaction) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return t;
  }

  public Long getKey() {
    return key;
  }

  public void setKey(Long key) {
    this.key = key;
  }

  @Override
  public String getKeyString() {
    return String.valueOf(key);
  }

  @Override
  public Long getMaterialId() {
    return mId;
  }

  @Override
  public void setMaterialId(Long materialId) {
    this.mId = materialId;
  }

  @Override
  public Integer getSrc() {
    return src;
  }

  @Override
  public void setSrc(Integer src) {
    this.src = src;
  }

  @Override
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kioskId) {
    this.kId = kioskId;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date timeStamp) {
    this.t = timeStamp;
  }

  @Override
  public String getSourceUserId() {
    return uId;
  }

  @Override
  public void setSourceUserId(String suId) {
    this.uId = suId;
  }

  @Override
  public String getDestinationUserId() {
    return duId;
  }

  @Override
  public void setDestinationUserId(String duId) {
    this.duId = duId;
  }

  @Override
  public BigDecimal getQuantity() {
    return BigUtil.getZeroIfNull(q);
  }

  @Override
  public void setQuantity(BigDecimal quantity) {
    this.q = quantity;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String inventoryTransactionType) {
    this.type = inventoryTransactionType;
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
    this.dId = domainIds;
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

  @Override
  public String getMessage() {
    return this.msg;
  }

  @Override
  public void setMessage(String message) {
    this.msg = message;
  }

  @Override
  public String getMsgCode() {
    return msgCode;
  }

  @Override
  public void setMsgCode(String msgCode) {
    this.msgCode = msgCode;
  }

  @Override
  public String getBatchId() {
    return bid;
  }

  @Override
  public void setBatchId(String bid) {
    this.bid = bid != null ? bid.toUpperCase() : bid;
  }

  @Override
  public boolean hasBatch() {
    return bid != null && !bid.isEmpty();
  }

  @Override
  public Date getBatchExpiry() {
    return bexp;
  }

  @Override
  public void setBatchExpiry(Date batchExpiry) {
    bexp = batchExpiry;
  }

  @Override
  public String getBatchManufacturer() {
    return bmfnm;
  }

  @Override
  public void setBatchManufacturer(String manufacturerName) {
    bmfnm = manufacturerName;
  }

  @Override
  public Date getBatchManufacturedDate() {
    return bmfdt;
  }

  @Override
  public void setBatchManufacturedDate(Date manufacturedDate) {
    bmfdt = manufacturedDate;
  }

  @Override
  public Long getLinkedKioskId() {
    return lkId;
  }

  @Override
  public void setLinkedKioskId(Long lkId) {
    this.lkId = lkId;
  }

  @Override
  public String getTrackingId() {
    return tid;
  }

  @Override
  public void setTrackingId(String tid) {
    this.tid = tid;
  }

  public Long getLinkedTransactionId() {
    return ltid;
  }

  public void setLinkedTransactionId(Long linkedTransId) {
    ltid = linkedTransId;
  }

  @Override
  public BigDecimal getStockDifference() {
    return BigUtil.getZeroIfNull(sdf);
  }

  @Override
  public void setStockDifference(BigDecimal sdf) {
    this.sdf = sdf;
  }

  @Override
  public BigDecimal getClosingStock() {
    return BigUtil.getZeroIfNull(cs);
  }

  @Override
  public void setClosingStock(BigDecimal closingStock) {
    cs = closingStock;
  }

  @Override
  public BigDecimal getClosingStockByBatch() {
    return BigUtil.getZeroIfNull(csb);
  }

  @Override
  public void setClosingStockByBatch(BigDecimal closingStockByBatch) {
    csb = closingStockByBatch;
  }

  @Override
  public double getLatitude() {
    return NumberUtil.getDoubleValue(lt);
  }

  @Override
  public void setLatitude(double latitude) {
    this.lt = new Double(latitude);
  }

  @Override
  public double getLongitude() {
    return NumberUtil.getDoubleValue(ln);
  }

  @Override
  public void setLongitude(double longitude) {
    this.ln = new Double(longitude);
  }

  @Override
  public boolean hasGeo() {
    return getLatitude() != 0 || getLongitude() != 0;
  }

  @Override
  public double getGeoAccuracy() {
    return NumberUtil.getDoubleValue(gacc);
  }

  @Override
  public void setGeoAccuracy(double geoAccuracyMeters) {
    gacc = new Double(geoAccuracyMeters);
  }

  @Override
  public String getGeoErrorCode() {
    return gerr;
  }

  @Override
  public void setGeoErrorCode(String errorCode) {
    gerr = errorCode;
  }

  @Override
  public String getReason() {
    return rs;
  }

  @Override
  public void setReason(String rs) {
    this.rs = rs;
  }

  @Override
  public String getEditOrderQtyReason() {
    return eoqrsn;
  }

  @Override
  public void setEditOrderQtyRsn(String eoqrsn) {
    this.eoqrsn = eoqrsn;
  }

  @Override
  public List<String> getTags(String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      if (oldmtgs == null) {
        oldmtgs = TagUtil.getList(mtgs);
      }
      return oldmtgs;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      if (oldktgs == null) {
        oldktgs = TagUtil.getList(ktgs);
      }
      return oldktgs;
    } else {
      return null;
    }
  }

  @Override
  public void setTags(List<String> tags, String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      oldmtgs = tags;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      oldktgs = tags;
    }
  }

  @Override
  public void setTgs(List<? extends ITag> tags, String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      if (this.mtgs != null) {
        this.mtgs.clear();
        if (tags != null) {
          this.mtgs.addAll((List<Tag>) tags);
        }
      } else {
        this.mtgs = (List<Tag>) tags;
      }
      this.oldmtgs = null;

    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      if (this.ktgs != null) {
        this.ktgs.clear();
        if (tags != null) {
          this.ktgs.addAll((List<Tag>) tags);
        }
      } else {
        this.ktgs = (List<Tag>) tags;
      }
      this.oldktgs = null;
    }
  }

  @Override
  public BigDecimal getOpeningStock() {
    return BigUtil.getZeroIfNull(os);
  }

  @Override
  public void setOpeningStock(BigDecimal openingStock) {
    os = openingStock;
  }

  public boolean hasOpeningStock() {
    return os != null;
  }

  @Override
  public BigDecimal getOpeningStockByBatch() {
    return BigUtil.getZeroIfNull(osb);
  }

  @Override
  public void setOpeningStockByBatch(BigDecimal openingStockByBatch) {
    osb = openingStockByBatch;
  }

  public boolean useCustomTimestamp() {
    return useCustomTimestamp;
  }

  public void setUseCustomTimestamp(boolean useCustomTimestamp) {
    this.useCustomTimestamp = useCustomTimestamp;
  }

  @Override
  public Long getTransactionId() {
    return key;
  }

  @Override
  public void setTransactionId(Long trnId) {

  }

  @Override
  public String getTrackingObjectType() {
    return tot;
  }

  @Override
  public void setTrackingObjectType(String tot) {
    this.tot = tot;
  }

  // Get the fingerprint of this transaction
  @Override
  public byte[] fingerprint() {
    try {
      String transTxt = getDomainId() + kId + mId + type + q + uId;
      if (rs != null) {
        transTxt += rs;
      }
      if (mst != null) {
        transTxt += mst;
      }
      if (lkId != null) {
        transTxt += lkId;
      }
      if (bid != null) {
        transTxt += bid;
      }
      if (tid != null) {
        transTxt += tid;
      }
      return transTxt.getBytes();
    } catch (Exception e) {
      xLogger.warn(
          "{0} when computing checksum for transaction mid-kid {1}-{2} in domain {3} of type {4} and quantity {5} from user {6}: {7}",
          e.getClass().getName(), mId, kId, getDomainId(), type, q, uId, e.getMessage());
      return null;
    }
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

  @Override
  public String getMaterialStatus() {
    return mst;
  }

  @Override
  public void setMaterialStatus(String materialStatus) {
    this.mst = materialStatus;
  }

  @Override
  public double getAltitude() {
    return NumberUtil.getDoubleValue(alt);
  }

  @Override
  public void setAltitude(double altitude) {
    this.alt = new Double(altitude);
  }

  @Override
  public Date getAtd() {
    return atd;
  }

  @Override
  public void setAtd(Date atd) {
    this.atd = atd;
  }

  @Override
  public Boolean getEatd() {
    return eatd;
  }

  @Override
  public void setEatd(Boolean eatd) {
    this.eatd = eatd;
  }

}
