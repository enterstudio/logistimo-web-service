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

import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;

import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Arun
 */
public class StockReportData extends ReportData {

  // Sub-report types
  public static final String SUBTYPE_STOCKLEVEL = "slvl";
  public static final String SUBTYPE_UNSAFESTOCKLEVEL = "nsf";

  // Column IDs
  public static final String ID_LAT = "lat";
  public static final String ID_LONG = "long";
  public static final String ID_STOCK = "stock";
  public static final String ID_REORDERLEVEL = "reordlevel";
  public static final String ID_MAX = "max";
  public static final String ID_SAFESTOCK = "sftystock";
  public static final String ID_MATERIAL = "material";
  public static final String ID_LEADTIME = "ldtime";
  public static final String ID_LEADTIMEDEMAND = "ldtimedemand";
  public static final String ID_REVPERIODDEMAND = "revperioddemand";
  public static final String ID_STDDEV = "stddev";
  public static final String ID_ORDERPERIODICITY = "orderperiodicity";
  public static final String ID_LASTUPDATED = "lastupdated";

  // Custom properties
  public static final String PROP_STYLE = "style";
  public static final String STYLE_UNSAFESTOCK = "border: 2px solid #FFA500;";
  public static final String STYLE_STOCKOUT = "border: 2px solid #FF0000;";
  public static final String STYLE_EXCESSSTOCK = "border: 2px solid #FF00FF;";

  // Logger
  private static final XLog xLogger = XLog.getLog(StockReportData.class);

  // Attributes
  private boolean optimizationOn = false;

  public StockReportData(Date from, Date until, Map<String, Object> filters, Locale locale,
                         String timezone,
                         List<IInvntry> inventories, String cursor, boolean optimizationOn) {
    super(from, until, filters, locale, timezone, inventories, cursor);
    this.optimizationOn = optimizationOn;
  }

  /* (non-Javadoc)
   * @see org.lggi.samaanguru.reports.ReportData#generateDataTable(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public DataTable generateDataTable(String reportSubType) throws ReportingDataException {
    xLogger.fine("Entered StockReportData.generateDataTable");
    if (results == null || results.size() == 0) {
      throw new ReportingDataException("No Inventory data specified");
    }

    // Init. data table
    DataTable data = null;
    try {
      // Get the services
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);

      // Form the data table
      data = new DataTable();
      // Add the column descriptions
      ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();
      cd.add(new ColumnDescription(ID_MATERIAL, ValueType.TEXT, messages.getString("material")));
      cd.add(new ColumnDescription(ID_STOCK, ValueType.NUMBER, messages.getString("stock")));
      cd.add(new ColumnDescription(ID_REORDERLEVEL, ValueType.NUMBER,
          messages.getString("inventory.reorderlevel")));
      cd.add(new ColumnDescription(ID_MAX, ValueType.NUMBER, messages.getString("max")));
      if (optimizationOn) {
        cd.add(new ColumnDescription(ID_SAFESTOCK, ValueType.NUMBER,
            messages.getString("inventory.safetystock")));
        cd.add(new ColumnDescription(ID_ORDERPERIODICITY, ValueType.NUMBER,
            messages.getString("order.periodicity")));
        cd.add(new ColumnDescription(ID_LEADTIME, ValueType.NUMBER,
            messages.getString("order.leadtime")));
        cd.add(new ColumnDescription(ID_LEADTIMEDEMAND, ValueType.NUMBER,
            messages.getString("order.leadtimedemand")));
        cd.add(new ColumnDescription(ID_STDDEV, ValueType.NUMBER,
            messages.getString("inventory.stddev")));
      }
      cd.add(
          new ColumnDescription(ID_LASTUPDATED, ValueType.TEXT, messages.getString("lastupdated")));
      data.addColumns(cd);
      // Add the rows
      Iterator<IInvntry> it = this.results.iterator();
      while (it.hasNext()) {
        // Get next inventory
        IInvntry inv = it.next();
        BigDecimal stockOnHand = inv.getStock();
        BigDecimal safetyStock = inv.getSafetyStock();
        BigDecimal reorderLevel = inv.getReorderLevel();
        // Form the table row
        TableRow tr = new TableRow();
        // Material name
        tr.addCell(mcs.getMaterial(inv.getMaterialId()).getName());
        // Stock-on-hand
        TableCell cell = new TableCell(stockOnHand.longValue());
        // Set the cell's border color depending on stock
        if (BigUtil.equalsZero(stockOnHand)) // stockout
        {
          cell.setCustomProperty(PROP_STYLE, STYLE_STOCKOUT);
        } else if (inv.isStockUnsafe()) // unsafe stock
        {
          cell.setCustomProperty(PROP_STYLE, STYLE_UNSAFESTOCK);
        } else if (inv.isStockExcess()) {
          cell.setCustomProperty(PROP_STYLE, STYLE_EXCESSSTOCK);
        }
        tr.addCell(cell);
        // Re-order level
        tr.addCell(new TableCell(reorderLevel.longValue()));
        tr.addCell(new TableCell(inv.getMaxStock().longValue()));
        if (optimizationOn) {
          // Safety stock
          tr.addCell(new TableCell(safetyStock.longValue()));
          // Order periodicity
          tr.addCell(new TableCell(BigUtil.round2(inv.getOrderPeriodicity()).doubleValue()));
          // Lead time
          tr.addCell(new TableCell(BigUtil.round2(inv.getLeadTime()).doubleValue()));
          // Lead time demand
          tr.addCell(new TableCell(BigUtil.round2(inv.getLeadTimeDemand()).doubleValue()));
          // Rev. period demand
          tr.addCell(new TableCell(BigUtil.round2(inv.getRevPeriodDemand()).doubleValue()));
        }
        // Last updated time
        Date time = inv.getTimestamp();
        tr.addCell(new TableCell(LocalDateUtil.format(time, this.locale, this.timezone)));
        // Add row
        data.addRow(tr);
      }
    } catch (ServiceException e) {
      throw new ReportingDataException(e.getMessage());
    } catch (TypeMismatchException e) {
      throw new ReportingDataException(e.getLocalizedMessage());
    }

    xLogger.fine("Exiting StockReportData.generateDataTable");
    return data;
  }
}
