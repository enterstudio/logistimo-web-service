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

import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.reports.generators.ReportingDataException;

import com.logistimo.logger.XLog;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by charan on 06/03/17.
 */
public class ReportDataExportHandler implements IExportHandler {

  private static final XLog xLogger = XLog.getLog(ReportDataExportHandler.class);


  ReportData reportData;

  public ReportDataExportHandler(ReportData reportData){
    this.reportData = reportData;
  }

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    xLogger.fine("Entering getCSVHeader");
    String header = "";
    try {
      DataTable data = reportData.generateDataTable(null);
      List<ColumnDescription> colDescs = data.getColumnDescriptions();
      if (colDescs == null) {
        return header;
      }
      Iterator<ColumnDescription> it = colDescs.iterator();
      int i = 0;
      while (it.hasNext()) {
        ColumnDescription colDesc = it.next();
        if (i != 0) {
          header += ",";
        }
        header += colDesc.getLabel();
        ++i;
      }
    } catch (Exception e) {
      xLogger.severe(
          "{0} when getting CSV header for report: {1}", e.getClass().getName(), e.getMessage());
    }

    xLogger.fine("Exiting getCSVHeader. header: {0}", header);
    return header;
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    xLogger.fine("Entering toCSV");
    String csv = "";
    if (reportData.getResults() == null || reportData.getResults().isEmpty()) {
      return null;
    }

    try {
      DataTable data = reportData.generateDataTable(null);
      int rows = data.getNumberOfRows();
      int cols = data.getNumberOfColumns();
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          if (j != 0) {
            csv += ",";
          }
          csv += data.getValue(i, j);
        }
        csv += "\n";
      }
    } catch (ReportingDataException e) {
      xLogger.severe("{0} when generating CSV: {1}", e.getClass().getName(), e.getMessage());
      return null;
    }
    xLogger.fine("Exiting toCSV");
    return csv;
  }
}
