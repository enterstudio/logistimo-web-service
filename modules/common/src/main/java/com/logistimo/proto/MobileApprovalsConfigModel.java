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

import com.google.gson.annotations.Expose;

import java.util.Map;

/**
 * Created by vani on 28/06/17.
 */
public class MobileApprovalsConfigModel {
  /**
   * Approval configuration model for purchase/sales orders
   */
  @Expose
  public Map<String, MobilePurchaseSalesOrdersApprovalModel> ords;
  /**
   * Approval configuration model for transfers
   */
  @Expose
  public MobileTransfersApprovalModel trf;

  public static class MobileTransfersApprovalModel {
    /**
     * 1 if approval is enabled and 0 if approval is disabled
     */
    @Expose
    public Integer enb;
    /**
     * Approvers model
     */
    @Expose
    public MobileApproversModel apprvrs;
  }

  public static class MobileOrderApprovalModel {
    /**
     * 1 if approval is enabled and 0 if approval is disabled
     */
    @Expose
    public Integer enb;
  }
}
