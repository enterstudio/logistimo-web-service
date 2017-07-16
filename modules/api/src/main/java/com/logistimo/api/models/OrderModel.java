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

import com.logistimo.entities.entity.IApprovers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class OrderModel extends PermissionsModel {

  public Long id;
  public Integer size;
  /**
   * Currency
   */
  public String cur;
  public String price;
  /**
   * Status text
   */
  public String status;
  public String cdt;
  public String udt;
  public String msg;
  public BigDecimal tax;
  public String uid;
  public Long eid;
  public String vid;
  public String vnm = "";
  public String enm = "";
  /**
   *   Source of order creation
   */
  public Integer src;
  /**
   * Available Credit
   */
  public BigDecimal avc;

  /**
   * Available Credit Error
   */
  public String avcerr;

  /**
   * Latitude
   */
  public Double lt;
  /**
   * Longitude
   */
  public Double ln;
  public Double ac;
  /**
   * Entity Latitude
   */
  public double elt;
  /**
   * Entity Longitude
   */
  public double eln;
  /**
   * Price Statement
   */
  public String pst;
  /**
   * Order Package Size
   */
  public String pkgs;
  /**
   * Paid amount
   */
  public BigDecimal pd;
  /**
   * Payment Option
   */
  public String po;
  /**
   * Processing time
   */
  public String pt;
  /**
   * Delivery Lead time
   */
  public String dlt;

  /**
   * Order Total Price
   */

  public BigDecimal tp;

  /**
   * Items
   */
  public Set<DemandModel> its;

  /**
   * Status Code
   */
  public String st;

  /**
   * Transporter
   */
  public String trns;

  /**
   * Order Tags
   */
  public List<String> tgs;

  /**
   * Serial Number for display
   */
  public int sno;
  /**
   * Confirmed Fulfillment Time range
   */
  public String cft;
  /**
   * Expected Fulfillment Time range
   */
  public LinkedHashMap<String, String> eft;
  /**
   * User name
   */
  public String unm;
  /**
   * Updated by
   */
  public String uby;
  /**
   * Has Vendor access
   */
  public boolean hva;
  /**
   * Source domain id
   */
  public Long sdid;
  /**
   * Source domain name
   */
  public String sdname;
  /**
   * Customer address
   */
  public String eadd;
  /**
   * Vendor address
   */
  public String vadd;
  /**
   * Updated by
   */
  public String ubid;
  /**
   * Access to vendor
   */
  public Boolean atv = true;
  /**
   * Access to view vendor
   */
  public Boolean atvv = true;
  /**
   * Access to customer
   */
  public Boolean atc = true;
  /**
   * Access to view customer
   */
  public Boolean atvc = true;
  /**
   * Order type
   */
  public Integer ty;
  /**
   * Order reason
   */
  public String crsn;
  /**
   * Order type
   */
  public Integer oty;
  /**
   * Reference Id
   */
  public String rid;
  /**
   * Expected fulfillment date
   */
  public String efd;
  /**
   * Expected due date
   */
  public String edd;
  /**
   * allowCancel
   */
  public boolean alc;
  /**
   * Display text for expected due date
   */
  public String eddLabel;
  /**
   * Display text for expected fulfillment time
   */
  public String efdLabel;

  /**
   * Order update time
   */
  public String orderUpdatedAt;
  /**
   * domain ids
   */
  private List<Long> dids = new ArrayList<>(1);
  /**
   * Order visible to customer
   * @return
   */
  private boolean vtc;
  /**
   * Order visible to vendor
   * @return
   */
  private boolean vtv;
  /**
   * approval types
   */
  private List<OrderApprovalTypesModel> approvalTypesModels;
  /**
   * Is approver
   * @return
   */
  private OrderApproverModel approver;

  public List<Long> getDids() {
    return dids;
  }

  public void setDids(List<Long> dids) {
    this.dids = dids;
  }

  public boolean isVisibleToCustomer() {
    return vtc;
  }

  public void setVisibleToCustomer(boolean vtc) {
    this.vtc = vtc;
  }

  public boolean isVisibleToVendor() {
    return vtv;
  }

  public void setVisibleToVendor(boolean vtv) {
    this.vtv = vtv;
  }

  public List<OrderApprovalTypesModel> getApprovalTypesModels() {
    return approvalTypesModels;
  }

  public void setApprovalTypesModels(
      List<OrderApprovalTypesModel> approvalTypesModels) {
    this.approvalTypesModels = approvalTypesModels;
  }

  public OrderApproverModel getApprover() {
    return approver;
  }

  public void setApprover(OrderApproverModel approver) {
    this.approver = approver;
  }

}
