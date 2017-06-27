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

import com.logistimo.reports.ReportsConstants;

/**
 * @author Arun
 *         Factory for creating report generators
 */
public class ReportDataGeneratorFactory implements IReportDataGeneratorFactory {

  /**
   * Create an instance of a report generator
   *
   * @param reportType The type of report generator as described in ReportData
   */
  @Override
  public ReportDataGenerator getInstance(String reportType) throws ReportingDataException {
    if (ReportsConstants.TYPE_TRANSACTION0.equals(reportType) || ReportsConstants.TYPE_TRANSACTION_RAW
        .equals(reportType)) {
      return new TransReportDataGenerator();
    } else if (ReportsConstants.TYPE_STOCK.equals(reportType)) {
      return new StockReportDataGenerator();
    } else if (ReportsConstants.TYPE_DEMANDBOARD.equals(reportType)) {
      return new DemandBoardDataGenerator();
    } else if (ReportsConstants.TYPE_ORDERS.equals(reportType)) {
      return new OrdersDataGenerator();
    } else if (ReportsConstants.TYPE_CONSUMPTION.equals(reportType) || ReportsConstants.TYPE_TRANSACTION
        .equals(reportType) ||
        ReportsConstants.TYPE_STOCKEVENTRESPONSETIME.equals(reportType)
        || ReportsConstants.TYPE_ORDERRESPONSETIMES.equals(reportType)) {
      return new com.logistimo.reports.generators.TransTrendsDataGenerator(reportType);
    } else if (ReportsConstants.TYPE_STOCKLEVEL.equals(reportType)) {
      return new StockLevelDataGenerator();
    } else if (ReportsConstants.TYPE_USERACTIVITY.equals(reportType) || ReportsConstants.TYPE_DOMAINACTIVITY
        .equals(reportType)) {
      return new ActivityDataGenerator();
    } else if (ReportsConstants.TYPE_STOCKEVENT.equals(reportType)) {
      return new StockEventDataGenerator();
    } else {
      throw new ReportingDataException("Invalid reportType: " + reportType);
    }
  }

}
