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
package com.logistimo.reports.generators;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.users.entity.IUserAccount;

import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represents demand-board data that can be visualized using Google Visualization
 *
 * @author Arun
 */
public class DemandBoardData extends ReportData {

  // Column IDs
  public static final String ID_ORDERID = "oid";
  public static final String ID_MATERIALID = "mid";
  public static final String ID_KIOSKID = "kid";
  public static final String ID_MATERIAL = "material";
  public static final String ID_KIOSK = "kiosk";
  public static final String ID_QUANTITY = "quantity";
  public static final String ID_UNITPRICE = "unitprice";
  public static final String ID_CURRENCY = "currency";
  public static final String ID_CITY = "city";
  public static final String ID_STATE = "state";
  public static final String ID_TIME = "time";
  public static final String ID_USERID = "uid";
  public static final String ID_MESSAGE = "message";
  public static final String ID_STATUS = "status";
  public static final String ID_ITEMID = "iid";
  public static final String ID_USER = "user";
  public static final String ID_MOBILE = "mobile";

  // Column names
  public static final String NAME_ORDERID = "Order Id";
  public static final String NAME_MATERIALID = "MaterialId";
  public static final String NAME_KIOSKID = "KioskId";
  public static final String NAME_USERID = "UserId";
  public static final String NAME_ITEMID = "ItemId";

  // Logger
  private static final XLog xLogger = XLog.getLog(DemandBoardData.class);

  /**
   */
  public DemandBoardData(Date from, Date until, Map<String, Object> filters, Locale locale,
                         String timezone, List<IDemandItem> items, String cursor) {
    super(from, until, filters, locale, timezone, items, cursor);
  }

  /* (non-Javadoc)
   * @see org.lggi.samaanguru.reports.ReportData#generateDataTable(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public DataTable generateDataTable(String reportSubType) throws ReportingDataException {
    xLogger.fine("Entered generateDataTable");
    if (results == null || results.isEmpty()) {
      throw new ReportingDataException(messages.getString("nodataavailable"));
    }
    DataTable data = new DataTable();
    try {
      // Init. services
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      // Init. data table
      // Add columns
      data.addColumn(new ColumnDescription(ID_ORDERID, ValueType.TEXT, NAME_ORDERID));
      data.addColumn(new ColumnDescription(ID_MATERIALID, ValueType.TEXT, NAME_MATERIALID));
      data.addColumn(new ColumnDescription(ID_KIOSKID, ValueType.TEXT, NAME_KIOSKID));
      data.addColumn(
          new ColumnDescription(ID_UNITPRICE, ValueType.NUMBER, messages.getString("price")));
      data.addColumn(
          new ColumnDescription(ID_CURRENCY, ValueType.TEXT, messages.getString("currency")));
      data.addColumn(
          new ColumnDescription(ID_MATERIAL, ValueType.TEXT, messages.getString("material")));
      data.addColumn(
          new ColumnDescription(ID_QUANTITY, ValueType.NUMBER, messages.getString("quantity")));
      data.addColumn(new ColumnDescription(ID_KIOSK, ValueType.TEXT, messages.getString("kiosk")));
      data.addColumn(new ColumnDescription(ID_CITY, ValueType.TEXT, messages.getString("village")));
      data.addColumn(new ColumnDescription(ID_STATE, ValueType.TEXT, messages.getString("state")));
      data.addColumn(new ColumnDescription(ID_TIME, ValueType.TEXT, messages.getString("time")));
      data.addColumn(new ColumnDescription(ID_USERID, ValueType.TEXT, NAME_USERID));
      data.addColumn(
          new ColumnDescription(ID_MESSAGE, ValueType.TEXT, messages.getString("message")));
      data.addColumn(
          new ColumnDescription(ID_STATUS, ValueType.TEXT, messages.getString("status")));
      data.addColumn(new ColumnDescription(ID_ITEMID, ValueType.TEXT, NAME_ITEMID));
      data.addColumn(new ColumnDescription(ID_USER, ValueType.TEXT, messages.getString("user")));
      data.addColumn(
          new ColumnDescription(ID_MOBILE, ValueType.TEXT, messages.getString("user.mobile")));
      // Check if only latest entries are to be added
      String latestFlag = null;
      if (filters != null) {
        latestFlag = (String) filters.get(ReportsConstants.FILTER_LATEST);
      }
      boolean latest = "true".equals(latestFlag);
      List<String> kioskMaterials = null;
      if (latest) {
        kioskMaterials = new ArrayList<String>();
      }
      // Add rows to data table
      Iterator<IDemandItem> it = results.iterator();
      while (it.hasNext()) {
        IDemandItem item = it.next();
        Long materialId = item.getMaterialId();
        Long kioskId = item.getKioskId();
        // Check if only latest entries are to be included
        if (latest) {
          String km = kioskId.toString() + materialId.toString();
          if (kioskMaterials.contains(km)) {
            continue; // the latest kiosk-material pair is already processed, go to the next item
          } else {
            kioskMaterials.add(km);
          }
        }
        // Create row
        IKiosk k = as.getKiosk(kioskId);
        TableRow tr = new TableRow();
        String orderIdStr = "";
        if (item.getOrderId() != null) {
          orderIdStr = item.getOrderId().toString();
        }
        tr.addCell(new TableCell(orderIdStr)); // order Id
        tr.addCell(new TableCell(materialId.toString())); // material Id
        tr.addCell(new TableCell(kioskId.toString())); // kiosk Id
        TableCell pcell = new TableCell(item.getUnitPrice().doubleValue());
        pcell.setFormattedValue(item.getFormattedPrice());
        tr.addCell(pcell); // price
        String currency = "";
        if (item.getCurrency() != null) {
          currency = item.getCurrency();
        }
        tr.addCell(new TableCell(currency)); // currency
        tr.addCell(new TableCell(mcs.getMaterial(materialId).getName())); // material name
        TableCell qcell = new TableCell(item.getQuantity().longValue());
        qcell.setCustomProperty("style", "text-align:left;");
        tr.addCell(qcell); // quantity
        tr.addCell(new TableCell(k.getName())); // kiosk name
        tr.addCell(new TableCell(k.getCity())); // city
        tr.addCell(new TableCell(k.getState())); // state
        tr.addCell(
            new TableCell(LocalDateUtil.format(item.getTimestamp(), locale, timezone))); // time
        tr.addCell(new TableCell(item.getUserId())); // user Id
        String message = item.getMessage();
        if (message == null) {
          message = "";
        }
        tr.addCell(new TableCell(message)); // message
        String status = item.getStatus();
        if (status == null) {
          status = "";
        }
        tr.addCell(new TableCell(status)); // status
        tr.addCell(new TableCell(item.getIdAsString())); // item id
        // Get user object
        IUserAccount u = k.getUser();
        String userName = "";
        String mobile = "";
        if (u != null) {
          userName = u.getFullName();
          mobile = u.getMobilePhoneNumber();
        }
        tr.addCell(new TableCell(userName));
        tr.addCell(new TableCell(mobile));
        // Add row
        data.addRow(tr);
      }
    } catch (ServiceException e) {
      throw new ReportingDataException(e.getMessage());
    } catch (TypeMismatchException e) {
      throw new ReportingDataException(e.getLocalizedMessage());
    }
    xLogger.fine("Exiting generateDataTable");
    return data;
  }

}
