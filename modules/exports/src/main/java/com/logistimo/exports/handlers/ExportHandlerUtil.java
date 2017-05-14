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

package com.logistimo.exports.handlers;

import com.logistimo.assets.models.Temperature;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entity.IMessageLog;
import com.logistimo.events.entity.IEvent;
import com.logistimo.exports.BulkExportMgr;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.models.InvntrySnapshot;
import com.logistimo.inventory.models.InvntryWithBatchInfo;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.mnltransactions.entity.IMnlTransaction;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.DiscrepancyExportableModel;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.entity.slices.IMonthSlice;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.reports.models.DomainUsageStats;
import com.logistimo.services.Resources;
import com.logistimo.users.entity.IUserAccount;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class ExportHandlerUtil {

  public static IExportHandler getInstance(Object o){
    if(o instanceof IUserAccount){
      return new UserExportHandler((IUserAccount) o);
    }else if(o instanceof IKiosk){
      return new KioskExportHandler((IKiosk) o);
    }else if(o instanceof IMaterial){
      return new MaterialExportHandler((IMaterial) o);
    }else if(o instanceof IInvntry){
      return new InvntryExportHandler((IInvntry) o);
    }else if(o instanceof IOrder){
      return new OrderExportHandler((IOrder) o);
    }else if(o instanceof ReportData){
      return new ReportDataExportHandler((ReportData) o);
    }else if(o instanceof InvntryWithBatchInfo){
      return new InvntryWithBatchInfoExportHandler((InvntryWithBatchInfo) o);
    }else if(o instanceof IEvent){
      return new EventExportHandler((IEvent) o);
    }else if(o instanceof IMonthSlice){
      return new MonthSliceExportHandler((IMonthSlice) o);
    }else if(o instanceof ISlice){
      return new SliceExportHandler((ISlice) o);
    }else if(o instanceof ITransaction){
      return new TransactionExportHandler((ITransaction) o);
    }else if(o instanceof Temperature){
      return new TemperatureExportHandler((Temperature) o);
    }else if(o instanceof IMnlTransaction){
      return new MnlTransExportHandler((IMnlTransaction) o);
    } else if (o instanceof DiscrepancyExportableModel) {
      return new DiscrepancyExportHandler((DiscrepancyExportableModel) o);
    } else if (o instanceof IInvntryEvntLog) {
      return new InvntryEvntLogExportHandler((IInvntryEvntLog) o);
    } else if (o instanceof DomainUsageStats) {
      return new DomainUsageStatsExportHandler((DomainUsageStats) o);
    } else if (o instanceof InvntrySnapshot) {
      return new InventrySnapshotExportHandler((InvntrySnapshot) o);
    } else if (o instanceof IInvntryBatch) {
      return new InvntryBatchExportHandler((IInvntryBatch) o);
    } else if (o instanceof IMessageLog) {
      return new MessageLogExportHandler((IMessageLog) o);
    }

    throw new RuntimeException("Unidentified export object type encountered "+o.getClass().getName());
  }

  public static String getExportTypeDisplay(String type, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    if (type.equals(BulkExportMgr.TYPE_USERS)) {
      return messages.getString("users");
    } else if (type.equals(BulkExportMgr.TYPE_KIOSKS)) {
      return messages.getString("kiosks");
    } else if (type.equals(BulkExportMgr.TYPE_MATERIALS)) {
      return messages.getString("materials");
    } else if (type.equals(BulkExportMgr.TYPE_INVENTORY)) {
      return messages.getString("inventory");
    } else if (type.equals(BulkExportMgr.TYPE_TRANSACTIONS)) {
      return messages.getString("transactions");
    } else if (type.equals(BulkExportMgr.TYPE_ORDERS)) {
      return messages.getString("orders");
    } else if (type.equals(BulkExportMgr.TYPE_ABNORMALSTOCK) || type
        .equals(ReportsConstants.TYPE_STOCKEVENT)) {
      return messages.getString("abnormalstock");
    } else if (type.equals(BulkExportMgr.TYPE_EVENTS)) {
      return messages.getString("events");
    } else if (type.equals(BulkExportMgr.TYPE_BATCHEXPIRY)) {
      return messages.getString("batchexpiry");
    } else if (type.equals(BulkExportMgr.TYPE_INVENTORYBATCH)) {
      return messages.getString("inventorybatch");
    } else if (type.equals(BulkExportMgr.TYPE_USAGESTATISTICS)) {
      return messages.getString("usagestatistics");
    } else if (type.equals(BulkExportMgr.TYPE_MANUALTRANSACTIONS)) {
      return messages.getString("manualtransactions");
    } else if (type.equals(BulkExportMgr.TYPE_NOTIFICATIONS_STATUS)) {
      return messages.getString("notificationStatus");
    } else if (type.equals(ReportsConstants.TYPE_CONSUMPTION)) {
      return messages.getString("report.consumptiontrends");
    } else if (type.equals(ReportsConstants.TYPE_ORDERRESPONSETIMES)) {
      return messages.getString("report.orderresponsetimes");
    } else if (type.equals(ReportsConstants.TYPE_STOCKEVENTRESPONSETIME)) {
      return messages.getString("report.replenishmentresponsetimes");
    } else if (type.equals(ReportsConstants.TYPE_TRANSACTION)) {
      return messages.getString("report.transactioncounts");
    } else if (type.equals(ReportsConstants.TYPE_USERACTIVITY)) {
      return messages.getString("report.useractivity");
    } else if (type.equals(ReportsConstants.TYPE_POWERDATA)) {
      return messages.getString("asset.powerdata");
    } else if (type.equals(BulkExportMgr.TYPE_ASSETS)) {
      return messages.getString("assets");
    } else if (type.equals(BulkExportMgr.TYPE_DISCREPANCIES)) {
      return messages.getString("discrepancies");
    }
    return "";
  }
}
