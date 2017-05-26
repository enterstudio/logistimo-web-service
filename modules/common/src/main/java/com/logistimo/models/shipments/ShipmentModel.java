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

package com.logistimo.models.shipments;



import com.logistimo.shipments.ShipmentStatus;

import java.util.List;

/**
 * Created by Mohan Raja on 29/09/16
 */
public class ShipmentModel {
  public Long orderId;
  public Long customerId;
  public Long vendorId;
  public List<ShipmentItemModel> items;
  public ShipmentStatus status;
  public String transporter;
  public String trackingId;
  public String reason;
  public String cdrsn;
  /**
   * Expected arrival date
   */
  public String ead;
  public List<String> tags;
  public Double latitude;
  public Double longitude;
  public Double geoAccuracy;
  public String geoError;
  /**
   * Shipment Id
   */
  public String sId;
  /**
   * Number or items
   */
  public int noi;
  /**
   * Created by user full name
   */
  public String createdBy;
  /**
   * Created by user Id
   */
  public String userID;
  /**
   * Created on
   */
  public String cOn;
  /**
   * Source domain Id
   */
  public Long sdid;
  /**
   * Updated by user full name
   */
  public String upBy;
  /**
   * Updated by user id
   */
  public String upId;
  /**
   * Updated on
   */
  public String upOn;

  // other fields
  /**
   * Actual fulfilment date
   */
  public String afd;
  /**
   * Shipment Status to be changed
   */
  public String changeStatus;
  public String sdname;
  public String customerAdd;
  public String vendorAdd;
  public String customerName;
  public String vendorName;
  public String statusCode;
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
   * Package size
   */
  public String ps;
  /**
   * Order type
   */
  public Integer oty;
  /**
   * Display text for expected arrival date
   */
  public String eadLabel;
  /**
   * Order update time
   */
  public String orderUpdatedAt;

}
