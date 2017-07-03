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

import java.util.List;

public class SMSTransactionModel {
  private Long sendTime;
  private Long actualTransactionDate;
  private String userId;
  private String partialId;
  private String token;
  private Long kioskId;
  private List<InventoryTransactions> inventoryTransactionsList;

  public List<InventoryTransactions> getInventoryTransactionsList() {
    return inventoryTransactionsList;
  }

  public void setInventoryTransactionsList(List<InventoryTransactions> inventoryTransactionsList) {
    this.inventoryTransactionsList = inventoryTransactionsList;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPartialId() {
    return partialId;
  }

  public void setPartialId(String partialId) {
    this.partialId = partialId;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Long getKioskId() {
    return kioskId;
  }

  public void setKioskId(Long kioskId) {
    this.kioskId = kioskId;
  }

  public Long getSendTime() {
    return sendTime;
  }

  public void setSendTime(Long sendTime) {
    this.sendTime = sendTime;
  }

  public Long getActualTransactionDate() {
    return actualTransactionDate;
  }

  public void setActualTransactionDate(Long actualTransactionDate) {
    this.actualTransactionDate = actualTransactionDate;
  }


}
