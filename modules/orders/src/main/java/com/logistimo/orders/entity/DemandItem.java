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

package com.logistimo.orders.entity;

import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;

import com.logistimo.orders.service.IDemandService;
import com.logistimo.services.Services;
import com.logistimo.orders.service.impl.DemandService;
import com.logistimo.utils.BigUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * @author Arun
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class DemandItem implements IDemandItem {

  private static final XLog xLogger = XLog.getLog(DemandItem.class);

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id; // id

  @Element(column = "oId")
  private Order order;

  @Persistent(table = "DEMANDITEM_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain Id
  @Persistent
  private Long kId; // kiosk id
  @Persistent
  private Long mId; // material id
  @Persistent
  private BigDecimal q = BigDecimal.ZERO;
  @Persistent
  private BigDecimal oq = BigDecimal.ZERO;
  @Persistent
  private BigDecimal roq = BigDecimal.ZERO;
  @Persistent
  private BigDecimal sq = BigDecimal.ZERO;
  @Persistent
  private BigDecimal fq = BigDecimal.ZERO;
  @Persistent
  private BigDecimal isq = BigDecimal.ZERO;
  @Persistent
  private BigDecimal dq = BigDecimal.ZERO;

  @Persistent
  private String st = Order.PENDING; // status
  @Persistent
  private Date t; // (last) updated time
  @Persistent
  private BigDecimal up; // unit price of item
  @Persistent
  private String cr; // currency
  @Persistent
  private String ms; // message
  @Persistent
  private String uId; // user Id who posted this demand item
  @Persistent
  private Long oid; // parent order
  @Persistent
  private BigDecimal dct; // discount (in %)
  @Persistent
  private BigDecimal tx; // tax rate for this item (in %)
  @Persistent
  private Integer ast; // aggregation status
  @Persistent(table = "DEMANDITEM_MTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> mtgs;

  @NotPersistent
  private List<String>
      oldmtgs;
  // list of material tgs, if present (used mainly for queries involving kioskId and tgs)

  @Persistent(table = "DEMANDITEM_KTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> ktgs;

  @NotPersistent
  private List<String> oldktgs; // list of kiosk tgs (for queries)
  // Batch metadata
  @NotPersistent
  private Set<DemandItemBatch> btchs; // a set of batches associated with this entity

  @NotPersistent
  private Long linkedKioskId; // used during data aggregation, as a dimension

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;
  @Persistent
  private String rsn;

  /**
   * Time to order - Time diff between order creation time to min event on inv created time.
   */
  @Persistent
  private Long tto;

  @Persistent
  private String sdrsn; // Shipped discrepancy reason

  @NotPersistent
  private BigDecimal aq;

  public Long getId() {
    return id;
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

  @Override
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

  @Override
  public void removeDomainId(Long domainId) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.remove(domainId);
    }
  }

  @Override
  public void removeDomainIds(List<Long> domainIds) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.removeAll(domainIds);
    }
  }

  @Override
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kId) {
    this.kId = kId;
  }

  @Override
  public Long getMaterialId() {
    return mId;
  }

  @Override
  public void setMaterialId(Long mId) {
    this.mId = mId;
  }

  @Override
  public BigDecimal getQuantity() {
    if (q != null) {
      return q;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void setQuantity(BigDecimal q) {
    if (this.q != null && BigUtil.notEquals(this.q, q)) {
      if (BigUtil.equalsZero(q)) {
        // this implies that a demand item is being cancelled - i.e. reset to 0
        this.st = IOrder.CANCELLED;
      } else {
        this.st = IOrder.CHANGED;
      }
    }
    this.q = q;
  }

  @Override
  public BigDecimal getOriginalQuantity() {
    return oq;
  }

  @Override
  public void setOriginalQuantity(BigDecimal quantity) {
    oq = quantity;
  }

  @Override
  public String getStatus() {
    return st;
  }

  @Override
  public void setStatus(String st) {
    this.st = st;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date t) {
    this.t = t;
  }

  @Override
  public BigDecimal getUnitPrice() {
    if (up != null) {
      return up;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void setUnitPrice(BigDecimal up) {
    this.up = up;
  }

  // Get formatted unit price, formatted to two decimal places
  @Override
  public String getFormattedPrice() {
    if (up == null) {
      return "0.00";
    }
    String fp = Order.getFormattedPrice(up); //String.format( "%.2f", up );
    return fp;
  }

  // Computes item price without any tax added to it
  @Override
  public BigDecimal computeTotalPrice(boolean includeTax) {
    BigDecimal tp = BigDecimal.ZERO;
    if (q != null && up != null) {
      tp = q.multiply(up);
      if (dct != null) {
        tp = tp.subtract(tp.multiply(dct.divide(new BigDecimal(100), RoundingMode.HALF_UP)));
      }
      // Check item level tax, if any
      BigDecimal tax = getTax();
      if (includeTax && BigUtil.notEqualsZero(tax)) {
        tp = tp.add(tp.multiply(tax.divide(new BigDecimal(100), RoundingMode.HALF_UP)));
      }
    }
    return tp;
  }

  @Override
  public String getCurrency() {
    return cr;
  }

  @Override
  public void setCurrency(String cr) {
    this.cr = cr;
  }

  @Override
  public String getMessage() {
    return ms;
  }

  @Override
  public void setMessage(String ms) {
    this.ms = ms;
  }

  @Override
  public String getUserId() {
    return uId;
  }

  @Override
  public void setUserId(String uId) {
    this.uId = uId;
  }

  @Override
  public Long getOrderId() {
    return oid;
  }

  public void setKey(Long oid) {
    this.oid = oid;
  }

  @Override
  public BigDecimal getDiscount() {
    if (dct == null) {
      return BigDecimal.ZERO;
    }
    return dct;
  }

  @Override
  public void setDiscount(BigDecimal dct) {
    this.dct = dct;
  }

  @Override
  public BigDecimal getTax() {
    return BigUtil.getZeroIfNull(tx);
  }

  @Override
  public void setTax(BigDecimal tax) {
    tx = tax;
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
          this.mtgs.addAll((Collection<Tag>) tags);
        }
      } else {
        this.mtgs = (List<Tag>) tags;
      }
      this.oldmtgs = null;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      if (ktgs != null) {
        this.ktgs.clear();
        if (tags != null) {
          this.ktgs.addAll((Collection<Tag>) tags);
        }
      } else {
        this.ktgs = (List<Tag>) tags;
      }
      this.oldktgs = null;
    }
  }

  @Override
  public Set<? extends IDemandItemBatch> getItemBatches() {
    return btchs;
  }

  @Override
  public void setItemBatches(Set<? extends IDemandItemBatch> itemBatches) {
    btchs = (Set<DemandItemBatch>) itemBatches;
  }

  @Override
  public void addBatch(IDemandItemBatch batch) {
    if (btchs == null) {
      btchs = new HashSet<DemandItemBatch>();
    }
    btchs.add((DemandItemBatch) batch);
  }

  @Override
  public BigDecimal getQuantityByBatches() {
    BigDecimal q = BigDecimal.ZERO;
    if (btchs == null || btchs.isEmpty()) {
      return q;
    }
    for (DemandItemBatch btch : btchs) {
      q = q.add(btch.getQuantity());
    }
    return q;
  }

  @Override
  public List<Map<String, Object>> getBatchesAsMap() {
    if (btchs == null || btchs.isEmpty()) {
      return null;
    }
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    Iterator<DemandItemBatch> it = btchs.iterator();
    while (it.hasNext()) {
      list.add(it.next().toMap());
    }
    // Sort by expiry date (asc)
    Collections.sort(list, new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        Date d1 = (Date) o1.get(IDemandItemBatch.EXPIRY);
        Date d2 = (Date) o2.get(IDemandItemBatch.EXPIRY);
        if (d1 == null && d2 != null) {
          return -1;
        } else if (d1 != null && d2 == null) {
          return 1;
        } else if (d1 == null && d2 == null) {
          return 0;
        } else {
          return d1.compareTo(d2);
        }
      }
    });
    return list;
  }

  @Override
  public Map<String, Object> toMap(String currency, Locale locale, String timezone,
                                   boolean forceIntegerQuantity) {
    Map<String, Object> map = new HashMap<>(1);
    try {
      IDemandService ds = Services.getService(DemandService.class);
      map = ds.getDemandItemAsMap(this.id, currency, locale, timezone, forceIntegerQuantity);
    } catch (Exception e) {
      xLogger.severe("Exception while getting demand item as map for id: {0}", this.id, e);
    }
    return map;
  }

  @Override
  public void setLinkedKioskId(Long linkedKioskId) {
    this.linkedKioskId = linkedKioskId;
  }

  @Override
  public String getIdAsString() {
    return String.valueOf(id);
  }

  @Override
  public void updateOId(IOrder o) {
    oid = ((Order) o).getId();
  }

  @Override
  public Date getArchivedAt() {
    return arcAt;
  }

  @Override
  public void setArchivedAt(Date archivedAt) {
    arcAt = archivedAt;
  }

  @Override
  public String getArchivedBy() {
    return arcBy;
  }

  @Override
  public void setArchivedBy(String archivedBy) {
    arcBy = archivedBy;
  }

  @Override
  public String getReason() {
    return rsn;
  }

  @Override
  public void setReason(String reason) {
    rsn = reason;
  }

  @Override
  public BigDecimal getRecommendedOrderQuantity() {
    return roq;
  }

  @Override
  public void setRecommendedOrderQuantity(BigDecimal roq) {
    this.roq = roq;
  }

  @Override
  public BigDecimal getShippedQuantity() {
    return BigUtil.getZeroIfNull(sq);
  }

  @Override
  public void setShippedQuantity(BigDecimal quantity) {
    sq = quantity;
  }

  @Override
  public BigDecimal getInShipmentQuantity() {
    return BigUtil.getZeroIfNull(isq);
  }

  @Override
  public void setInShipmentQuantity(BigDecimal quantity) {
    isq = quantity;
  }

  @Override
  public BigDecimal getDiscrepancyQuantity() {
    return BigUtil.getZeroIfNull(dq);
  }

  @Override
  public void setDiscrepancyQuantity(BigDecimal quantity) {
    dq = quantity;
  }

  @Override
  public BigDecimal getFulfilledQuantity() {
    return BigUtil.getZeroIfNull(fq);
  }

  @Override
  public void setFulfilledQuantity(BigDecimal quantity) {
    fq = quantity;
  }

  @Override
  public Long getTimeToOrder() {
    return tto;
  }

  @Override
  public void setTimeToOrder(Long timeToOrder) {
    this.tto = timeToOrder;
  }

  @Override
  public String getShippedDiscrepancyReason() {
    return sdrsn;
  }

  @Override
  public void setShippedDiscrepancyReason(String sdrsn) {
    this.sdrsn = sdrsn;
  }

  @Override
  public BigDecimal getAllocatedStock() {
    return aq;
  }

  @Override
  public void setAllocatedStock(BigDecimal quantity) {
    this.aq = quantity;
  }

}