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

package com.logistimo.config.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by naveensnair on 12/05/17.
 */
public class ApprovalsConfig implements Serializable {

  public static final String PURCHASE_SALES_ORDER_APPROVAL = "psoa";
  public static final String ENTITY_TAGS = "et";
  public static final String PURCHASE_ORDER_APPROVAL = "poa";
  public static final String TRANSFER_ORDER_APPROVAL = "toa";
  public static final String SALES_ORDER_APPROVAL = "soa";
  public static final String SALES_ORDER_APPROVAL_CREATION = "soac";
  public static final String SALES_ORDER_APPROVAL_SHIPPING = "soas";
  public static final String PRIMARY_APPROVERS = "pa";
  public static final String SECONDARY_APPROVERS = "sa";
  public static final String PURCHASE_ORDER_APPROVAL_EXPIRY = "px";
  public static final String SALES_ORDER_APPROVAL_EXPIRY = "sx";
  public static final String TRANSFER_ORDER_APPROVAL_EXPIRY = "tx";
  private static final String ORDER = "order";
  private OrderConfig orderConfig;

  public ApprovalsConfig() {
    orderConfig = new OrderConfig();
  }

  public ApprovalsConfig(JSONObject json) {
    try {
      orderConfig = new OrderConfig(json.getJSONObject(ORDER));
    } catch (JSONException e) {
      orderConfig = new OrderConfig();
    }
  }

  public OrderConfig getOrderConfig() {
    return orderConfig;
  }

  public void setOrderConfig(OrderConfig orderConfig) {
    this.orderConfig = orderConfig;
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    try {
      JSONObject json = new JSONObject();
      if (orderConfig != null) {
        json.put(ORDER, orderConfig.toJSONObject());
      }
      return json;
    } catch (Exception e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public static class OrderConfig implements Serializable {

    private List<PurchaseSalesOrderConfig> psoa = new ArrayList<>(1);
    private List<String> pa = new ArrayList<>(1); //primary approvers
    private List<String> sa = new ArrayList<>(1); //secondary approvers
    private int px = 24; //purchase order approval expiry time
    private int sx = 24; //sales order approval expiry time
    private int tx = 24; //transfer order approval expiry time

    public OrderConfig() {

    }

    public OrderConfig(JSONObject jsonObject) {
      if (jsonObject != null && jsonObject.length() > 0) {
        try {
          JSONArray primaryArray = jsonObject.getJSONArray(PRIMARY_APPROVERS);
          List<String> primaryApprovers = new ArrayList<>();
          for (int i = 0; i < primaryArray.length(); i++) {
            primaryApprovers.add(primaryArray.get(i).toString());
          }
          pa = primaryApprovers;
        } catch (Exception e) {
          pa = Collections.emptyList();
        }

        try {
          JSONArray secondaryArray = jsonObject.getJSONArray(SECONDARY_APPROVERS);
          List<String> secondaryApprovers = new ArrayList<>();
          for (int i = 0; i < secondaryArray.length(); i++) {
            secondaryApprovers.add(secondaryArray.get(i).toString());
          }
          sa = secondaryApprovers;
        } catch (Exception e) {
          sa = Collections.emptyList();
        }

        if (jsonObject.get(PURCHASE_SALES_ORDER_APPROVAL) != null) {
          JSONArray jsonArray = jsonObject.getJSONArray(PURCHASE_SALES_ORDER_APPROVAL);
          List<PurchaseSalesOrderConfig> purchaseSalesOrderConfigs = new ArrayList<>();
          for (int i = 0; i < jsonArray.length(); i++) {
            PurchaseSalesOrderConfig ps = new PurchaseSalesOrderConfig(jsonArray.getJSONObject(i));
            purchaseSalesOrderConfigs.add(ps);
          }
          psoa = purchaseSalesOrderConfigs;
        }

        try {
          px = jsonObject.getInt(PURCHASE_ORDER_APPROVAL_EXPIRY);
        } catch (Exception e) {
          px = 24;
        }
        try {
          sx = jsonObject.getInt(SALES_ORDER_APPROVAL_EXPIRY);
        } catch (Exception e) {
          px = 24;
        }
        try {
          tx = jsonObject.getInt(TRANSFER_ORDER_APPROVAL_EXPIRY);
        } catch (Exception e) {
          px = 24;
        }

      }
    }

    public List<PurchaseSalesOrderConfig> getPurchaseSalesOrderApproval() {
      return psoa;
    }

    public void setPurchaseSalesOrderApproval(
        List<PurchaseSalesOrderConfig> psoa) {
      this.psoa = psoa;
    }

    public List<String> getPrimaryApprovers() {
      return pa;
    }

    public void setPrimaryApprovers(List<String> pa) {
      this.pa = pa;
    }

    public List<String> getSecondaryApprovers() {
      return sa;
    }

    public void setSecondaryApprovers(List<String> sa) {
      this.sa = sa;
    }

    public int getPurchaseOrderApprovalExpiry() {
      return px;
    }

    public void setPurchaseOrderApprovalExpiry(int px) {
      this.px = px;
    }

    public int getSalesOrderApprovalExpiry() {
      return sx;
    }

    public void setSalesOrderApprovalExpiry(int sx) {
      this.sx = sx;
    }

    public int getTransferOrderApprovalExpiry() {
      return tx;
    }

    public void setTransferOrderApprovalExpiry(int tx) {
      this.tx = tx;
    }

    public Integer getExpiry(Integer orderType) {
      Integer expiry = 24;
      switch (orderType) {
        case 0:
          expiry = getTransferOrderApprovalExpiry();
          break;
        case 1:
          expiry = getPurchaseOrderApprovalExpiry();
          break;
        case 2:
          expiry = getSalesOrderApprovalExpiry();
          break;
        default:
          break;
      }
      return expiry;
    }

    public boolean isApprover(String userId) {
      return (getPrimaryApprovers().contains(userId) || getSecondaryApprovers().contains(userId));
    }

    public boolean isPurchaseApprovalEnabled(List<String> entityTags) {
      return psoa.stream()
          .filter(config -> entityTags.stream()
              .anyMatch(tag -> config.getEntityTags().contains(tag)))
          .filter(PurchaseSalesOrderConfig::isPurchaseOrderApproval)
          .findFirst().isPresent();
    }

    public boolean isSaleApprovalEnabled(List<String> entityTags) {
      return psoa.stream()
          .filter(config -> entityTags.stream()
              .anyMatch(tag -> config.getEntityTags().contains(tag)))
          .filter(PurchaseSalesOrderConfig::isSalesOrderApproval)
          .findFirst().isPresent();
    }


    public boolean isTransferApprovalEnabled() {
      return !pa.isEmpty();
    }

    public JSONObject toJSONObject() {
      JSONObject json = new JSONObject();
      if (pa != null && pa.size() > 0) {
        json.put(PRIMARY_APPROVERS, pa);
      }
      if (sa != null && sa.size() > 0) {
        json.put(SECONDARY_APPROVERS, sa);
      }
      if (psoa != null && psoa.size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PurchaseSalesOrderConfig purchaseSalesOrderConfig : psoa) {
          JSONObject jsonObject = purchaseSalesOrderConfig.toJSONObject();
          jsonArray.put(jsonObject);
        }
        if (jsonArray.length() > 0) {
          json.put(PURCHASE_SALES_ORDER_APPROVAL, jsonArray);
        }
      }
      json.put(PURCHASE_ORDER_APPROVAL_EXPIRY, px);
      json.put(SALES_ORDER_APPROVAL_EXPIRY, sx);
      json.put(TRANSFER_ORDER_APPROVAL_EXPIRY, tx);

      return json;
    }

  }


  public static class PurchaseSalesOrderConfig implements Serializable {
    private List<String> et = new ArrayList<>(1); //entity tags
    private boolean poa; //purchase order approval
    private boolean soa; //purchase order approval
    private boolean soac; //sales order approval during creation
    private boolean soas; //sales order approval during shipping

    public PurchaseSalesOrderConfig() {

    }

    public PurchaseSalesOrderConfig(JSONObject jsonObject) {
      if (jsonObject != null && jsonObject.length() > 0) {
        try {
          JSONArray jsonArray = jsonObject.getJSONArray(ENTITY_TAGS);
          et = new ArrayList<>();
          for (int i = 0; i < jsonArray.length(); i++) {
            Object obj = jsonArray.get(i);
            et.add((String) obj);
          }

        } catch (JSONException e) {
          // do nothing
        }

        if (jsonObject.get(SALES_ORDER_APPROVAL) != null) {
          soa = jsonObject.getBoolean(SALES_ORDER_APPROVAL);
        }
        if (jsonObject.get(PURCHASE_ORDER_APPROVAL) != null) {
          poa = jsonObject.getBoolean(PURCHASE_ORDER_APPROVAL);
        }
        if (jsonObject.get(SALES_ORDER_APPROVAL_CREATION) != null) {
          soac = jsonObject.getBoolean(SALES_ORDER_APPROVAL_CREATION);
        }
        if (jsonObject.get(SALES_ORDER_APPROVAL_SHIPPING) != null) {
          soas = jsonObject.getBoolean(SALES_ORDER_APPROVAL_SHIPPING);
        }
      }
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(ENTITY_TAGS, et);
      json.put(PURCHASE_ORDER_APPROVAL, poa);
      json.put(SALES_ORDER_APPROVAL, soa);
      json.put(SALES_ORDER_APPROVAL_CREATION, soac);
      json.put(SALES_ORDER_APPROVAL_SHIPPING, soas);
      return json;
    }

    public List<String> getEntityTags() {
      return et;
    }

    public void setEntityTags(List<String> et) {
      this.et = et;
    }

    public boolean isPurchaseOrderApproval() {
      return poa;
    }

    public void setPurchaseOrderApproval(boolean poa) {
      this.poa = poa;
    }

    public boolean isSalesOrderApproval() {
      return soa;
    }

    public void setSalesOrderApproval(boolean soa) {
      this.soa = soa;
    }

  }
}
