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

import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.entity.IOrder;

import com.logistimo.services.impl.PMF;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Represents orders data that can be visualized using Google Visualization
 *
 * @author Arun
 */
public class OrdersData extends ReportData {

  // Column IDs
  public static final String ID_ORDERID = "oid";
  public static final String ID_KIOSKID = "kid";
  public static final String ID_KIOSK = "kiosk";
  public static final String ID_QUANTITY = "quantity";
  public static final String ID_TOTALPRICE = "totalprice";
  public static final String ID_TAX = "tax";
  public static final String ID_LOCATION = "location";
  public static final String ID_TIMECREATED = "timecreated";
  public static final String ID_TIMEUPDATED = "timeupdated";
  public static final String ID_USERID = "uid";
  public static final String ID_MESSAGE = "message";
  public static final String ID_STATUS = "status";
  public static final String ID_USER = "user";
  public static final String ID_MOBILE = "mobile";
  public static final String ID_VENDORNAME = "vendorname";
  public static final String ID_VENDORID = "vendorid";

  // Column names
  public static final String NAME_KIOSKID = "KioskId";
  public static final String NAME_USERID = "UserId";
  public static final String NAME_VENDORID = "VendorId";

  // Logger
  private static final XLog xLogger = XLog.getLog(OrdersData.class);

  /**
   */
  public OrdersData(Date from, Date until, Map<String, Object> filters, Locale locale,
                    String timezone, List<IOrder> orders, String cursor) {
    super(from, until, filters, locale, timezone, orders, cursor);
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
    PersistenceManager
        pm =
        PMF.get()
            .getPersistenceManager(); // PM to get kiosk object (using this instead of AccountsService for performance reasons, given we don't need related objects)
    DataTable data = new DataTable();
    try {
      // Init. data table
      // Add columns
      data.addColumn(
          new ColumnDescription(ID_ORDERID, ValueType.TEXT, messages.getString("order")));
      data.addColumn(
          new ColumnDescription(ID_QUANTITY, ValueType.NUMBER, messages.getString("items")));
      data.addColumn(
          new ColumnDescription(ID_TOTALPRICE, ValueType.NUMBER, messages.getString("price")));
      data.addColumn(
          new ColumnDescription(ID_STATUS, ValueType.TEXT, messages.getString("status")));
      data.addColumn(new ColumnDescription(ID_KIOSK, ValueType.TEXT, messages.getString("kiosk")));
      data.addColumn(
          new ColumnDescription(ID_LOCATION, ValueType.TEXT, messages.getString("village")));
      data.addColumn(new ColumnDescription(ID_TIMECREATED, ValueType.TEXT,
          messages.getString("createdon"))); // earlier "Placed"
      data.addColumn(new ColumnDescription(ID_TIMEUPDATED, ValueType.TEXT,
          messages.getString("lastupdated"))); // earlier "Updated"
      data.addColumn(new ColumnDescription(ID_TAX, ValueType.TEXT, messages.getString("tax")));
      data.addColumn(
          new ColumnDescription(ID_MESSAGE, ValueType.TEXT, messages.getString("message")));
      data.addColumn(new ColumnDescription(ID_USER, ValueType.TEXT, messages.getString("user")));
      data.addColumn(new ColumnDescription(ID_MOBILE, ValueType.TEXT,
          messages.getString("user.mobile"))); // earlier "Mobile"
      data.addColumn(new ColumnDescription(ID_USERID, ValueType.TEXT, NAME_USERID));
      data.addColumn(new ColumnDescription(ID_KIOSKID, ValueType.TEXT, NAME_KIOSKID));
      data.addColumn(new ColumnDescription(ID_VENDORID, ValueType.TEXT, NAME_VENDORID));
      data.addColumn(
          new ColumnDescription(ID_VENDORNAME, ValueType.TEXT, messages.getString("vendor")));

      // Add rows to data table
      for (IOrder o : (Iterable<IOrder>) results) {
        Long kioskId = o.getKioskId();
        try {
          IKiosk
              k =
              JDOUtils.getObjectById(IKiosk.class, kioskId,
                  pm); /// as.getKiosk( kioskId ); NOTE: we don't need kiosk users & poolgroups
          TableRow tr = new TableRow();
          tr.addCell(new TableCell(o.getOrderId().toString())); // order Id
          TableCell qcell = new TableCell(o.size());
          qcell.setCustomProperty("style", "text-align:left");
          tr.addCell(qcell); // number of items
          String currency = o.getCurrency();
          if (currency == null) {
            currency = "";
          }
          TableCell pcell = new TableCell(o.getTotalPrice().doubleValue());
          pcell.setFormattedValue(currency + " " + o.getFormattedPrice());
          pcell.setCustomProperty("style", "text-align:left");
          tr.addCell(pcell); // price
          tr.addCell(new TableCell(OrderUtils.getStatusDisplay(o.getStatus(), locale))); // status
          tr.addCell(new TableCell(k.getName() + ", " + k.getCity())); // kiosk name
          tr.addCell(new TableCell(k.getCity() + ", " + k.getState())); // city
          tr.addCell(new TableCell(
              LocalDateUtil.format(o.getCreatedOn(), locale, timezone))); // time created
          if (o.getUpdatedOn() != null) {
            tr.addCell(new TableCell(
                LocalDateUtil.format(o.getUpdatedOn(), locale, timezone))); // time updated
          } else {
            tr.addCell(new TableCell(""));
          }
          tr.addCell(new TableCell(String.valueOf(o.getTax()))); // tax
//                    String message = o.getMessage();
          String message = null;
          if (message == null) {
            message = "";
          }
          tr.addCell(new TableCell(message)); // message
          // Get user object
          String userName = "";
          String mobile = "";
                    /* NO longer sending user info. to optimize performance (if user data is needed, change the way we get kiosk above - use AccountsService instead of pm)
                                        UserAccount u = k.getUser();
					if ( u != null ) {
						userName = u.getFullName();
						mobile = u.getMobilePhoneNumber();
					}
					*/
          tr.addCell(new TableCell(
              userName)); // user (NOTE: adding empty data - keeping placeholder for future need)
          tr.addCell(new TableCell(
              mobile)); // mobile (NOTE: adding empty data - keeping placeholder for future need)
          tr.addCell(new TableCell(o.getUserId())); // user Id
          tr.addCell(new TableCell(kioskId.toString())); // kiosk Id
          // Add vendor details, if any
          Long vendorId = o.getServicingKiosk();
          String vendorIdStr = "", vendorName = "";
          if (vendorId != null) {
            try {
              IKiosk
                  vendor =
                  JDOUtils.getObjectById(IKiosk.class, vendorId,
                      pm); /// earlier: as.getKiosk( vendorId );
              vendorIdStr = vendorId.toString();
              vendorName = vendor.getName() + ", " + vendor.getCity();
            } catch (Exception e) {
              xLogger
                  .warn("{0} when getting vendor data for order {1}: {2}", e.getClass().getName(),
                      o.getOrderId(), e.getMessage());
            }
          }
          tr.addCell(new TableCell(vendorIdStr)); // vendor Id
          tr.addCell(new TableCell(vendorName)); // vendor name
          // Add row
          data.addRow(tr);
        } catch (Exception e) {
          xLogger.warn("{0} when trying to get kiosk {1} and create a new row for order {2}: {3}",
              e.getClass().getName(), kioskId, o.getOrderId(), e);
        }
      } // end while
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting generateDataTable");
    return data;
  }

}
