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

package com.logistimo.customreports.utils;

import com.logistimo.AppFactory;
import com.logistimo.customreports.CustomReportsExportMgr;
import com.logistimo.services.storage.StorageUtil;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.logistimo.constants.Constants;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class SpreadsheetUtil {
  public static final String CSV_SEPARATOR = "\n";
  private static final XLog xLogger = XLog.getLog(SpreadsheetUtil.class);
  private static StorageUtil _storageUtil = AppFactory.get().getStorageUtil();

  static {
    ZipSecureFile.setMinInflateRatio(ConfigUtil.getDouble("zip.inflate.ratio", 0.01D));
  }

  /**
   * This method clears the one or more sheets in the template. The template is read from the Blob store and is the one uploaded during the configurtion of custom reports.
   * It uses Apache POI apis to create a workbook from the existing report template. After clearing the specified sheets, it writes the workbook to the output stream. (Here google cloud storage file)
   */
  public static String clearSheets(String templateName, String fileName, String fileExtension,
                                   Map<String, String> typeSheetNameMap, InputStream inputStream,
                                   Locale locale, String timezone) throws IOException {
    xLogger.fine("Entering clearSheets");
    boolean clearedSheet = true;
    if (typeSheetNameMap != null && !typeSheetNameMap.isEmpty()) {
      OutputStream os = null;
      // Create a workbook from the bis
      try {
        // Create a workbook from the blob input stream
        Workbook
            templateWb =
            WorkbookFactory.create(inputStream); // From the bytes downloaded from the blobstore.
        if (templateWb == null) {
          xLogger.severe("Failed to create templateWb");
          return null;
        }
        xLogger.fine("Successfully created templateWb");
        Set<String> sheetNameKeys = typeSheetNameMap.keySet(); // Get the set of sheet name keys
        xLogger.fine("sheetNameKeys: {0}", sheetNameKeys);
        if (sheetNameKeys != null && !sheetNameKeys.isEmpty()) {
          Iterator<String>
              sheetNameKeysIter =
              sheetNameKeys.iterator(); // Iterate through the sheet name keys
          while (sheetNameKeysIter.hasNext()) {
            String sheetNameKey = sheetNameKeysIter.next();
            // Important: Bug fix. Earlier sheetName was set to the sheetNameKey itself and it failed in reports where sheet name was different from the type.
            String sheetName = typeSheetNameMap.get(sheetNameKey);
            if (sheetName != null && !sheetName.isEmpty()) {
              Sheet
                  sheet =
                  templateWb.getSheet(sheetName); // Get the sheet specified by the sheet name
              // If the sheet is not null, clear it.
              if (sheet != null) {
                xLogger.fine("Clearing sheet {0}", sheetName);
                // Clear the contents in the sheet
                for (int index = sheet.getLastRowNum(); index >= sheet.getFirstRowNum(); index--) {
                  Row r = sheet.getRow(index);
                  if (r
                      != null) // Important fix for avoiding the NullPointerException when getRow returns null.
                  {
                    sheet.removeRow(r);
                  } else {
                    xLogger.warn("Null row. No rows to delete.");
                  }
                }
                xLogger.fine("Cleared sheet {0}", sheetName);
              } else {
                xLogger.severe("Sheet by name {0} not found in template {1}. Failed to clear sheet",
                    sheetName, templateName);
                clearedSheet = false;
                fileName = null; // So that null is returned.
                break;
              }
            }
          }
        }
        // Only if the sheet is cleared, write to the templateWb. Otherwise, close the streams
        if (clearedSheet) {
          // Now write the templateWb as bytes to a ByteArrayOutputStream and return.
          xLogger.fine("Now creating os");
          // The google cloud storage  file name should be templateName + time stamp at which the file was created.
          // fileName = LocalDateUtil.getNameWithDate( templateName, new Date(), locale, timezone) + fileExtension;
          fileName += fileExtension;
          // Get the output stream object to write to the gooogle cloud storage file.
          os =
              _storageUtil
                  .getOutputStream(CustomReportsExportMgr.CUSTOMREPORTS_BUCKETNAME, fileName,
                      false);
          // Write the template workbook to the outputstream.
          templateWb.write(os);
          xLogger.fine("Wrote templateWb to os");
        }
      } catch (Exception e) {
        xLogger
            .severe("Exception {0} while clearing sheets in the report template: {1}, Message: {2}",
                e.getClass().getName(), templateName, e.getMessage());
      } finally {
        if (inputStream != null) {
          inputStream.close();
        }
        if (os != null) {
          os.close();
        }
      }
      xLogger.fine("Exiting clearSheets");
      return fileName;
    } else {
      xLogger.severe("Could not clear sheets because typeSheetNameMap is null");
      return null;
    }
  }

  /**
   * This method adds the data specified in dataCsv to the specified sheet in the specified file.
   */
  public static void addDataToSheet(String fileName, String sheetName, List<String> dataCsvList)
      throws IOException {
    xLogger.fine(
        "Entering addDataToSheet. fileName: {0}, sheetName: {1}, dataCsvList: {2}, dataCsvList.size: {3}",
        fileName, sheetName, dataCsvList.toString(), dataCsvList.size());
    if (sheetName == null || sheetName.isEmpty()) {
      xLogger.severe("Cannot add data to a sheet whose name is null or empty");
      return;
    }

    InputStream is = null;
    OutputStream os = null;
    try {
      // Get the cleaned template bytes from GCS
      is = _storageUtil.getInputStream(CustomReportsExportMgr.CUSTOMREPORTS_BUCKETNAME, fileName);
      if (is == null) {
        xLogger.severe("Failed to create Input stream for {0}", fileName);
        return;
      }
      // Create a workbook from the bytes
      Workbook
          templateWb =
          new SXSSFWorkbook(new XSSFWorkbook(is), 100, false,
              true);/*WorkbookFactory.create( is );*/ // From the bytes downloaded from the google cloud storage, form the Workbook
      if (templateWb != null) {
        CreationHelper createHelper = templateWb.getCreationHelper();
        if (createHelper != null) {
          // Get the sheet specified by sheetName and paste the dataCsv into it.
          Sheet sheet = templateWb.getSheet(sheetName);
          if (sheet != null) {
            // Append the dataCsv from the dataCsvList into that sheet.
            if (dataCsvList != null && !dataCsvList.isEmpty() || dataCsvList.size() != 0) {
              Iterator<String> dataCsvIter = dataCsvList.iterator();
              // Create a row with index lastRowNum
              int lastRowNum = sheet.getLastRowNum();
              if (lastRowNum != 0) {
                lastRowNum++;
              }
              CellStyle cs = templateWb.createCellStyle();
              cs.setDataFormat(
                  createHelper.createDataFormat().getFormat(Constants.DATETIME_CSV_FORMAT));
              while (dataCsvIter.hasNext()) {
                String rowDataCsv = dataCsvIter.next();
                String[] rowDataArray = StringUtil.getCSVTokens(rowDataCsv);
                List<String> rowDataList = StringUtil.getList(rowDataArray);
                Row row = sheet.createRow(lastRowNum);
                if (rowDataList != null && !rowDataList.isEmpty() && rowDataList.size() != 0) {
                  // Iterate through the rowDataList
                  Iterator<String> rowDataListIter = rowDataList.iterator();
                  int columnNum = 0;
                  while (rowDataListIter.hasNext()) {
                    Cell cell = row.createCell(columnNum);
                    String value = rowDataListIter.next();
                    if (StringUtil.isStringLong(value)) {
                      // Long values may be ids. In order to retain them as Long, we need to do this check.
                      cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                      try {
                        cell.setCellValue(Long.parseLong(value));
                      } catch (NumberFormatException nfe) {
                        xLogger.warn(
                            "{0} when setting cell value to Long. Message: {1}. Cell value: {2}",
                            nfe.getClass().getName(), nfe.getMessage(), value);
                      }
                    } else if (StringUtil.isStringDouble(value)) {
                      // Float and double values can be set as double values, with decimal.
                      cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                      try {
                        cell.setCellValue(Double.parseDouble(value));
                      } catch (NumberFormatException nfe) {
                        xLogger.warn(
                            "{0} when setting cell value to Double. Message: {1}. Cell value: {2}",
                            nfe.getClass().getName(), nfe.getMessage(), value);
                      }
                    } else if (StringUtil.isStringDate(value, Constants.DATETIME_CSV_FORMAT)) {
                      // Create a new cell style.
                      cell.setCellStyle(cs);
                      cell.setCellValue(
                          StringUtil.parseDateString(value, Constants.DATETIME_CSV_FORMAT));
                    } else if (StringUtil.isStringBoolean(value)) {
                      cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
                      cell.setCellValue(Boolean.parseBoolean(value));
                    } else {
                      cell.setCellValue(value);
                    }
                    columnNum++;
                  }
                } // End if rowDataList != null
                lastRowNum++;
                dataCsvIter.remove();
              } // End while iterating through dataList
            } // End if dataList != null
          } // End if sheet != null
          else {
            xLogger.severe("Sheet by name: {0} not found in file {1}. Data not added.", sheetName,
                fileName);
          }

          // Write to file
          xLogger.fine("Now creating baos");
          os =
              _storageUtil
                  .getOutputStream(CustomReportsExportMgr.CUSTOMREPORTS_BUCKETNAME, fileName,
                      false);
          templateWb.write(os); // Write the workbook to OutputStream
          templateWb.setForceFormulaRecalculation(true);
          xLogger.fine("Wrote templateWb to baos");
        } // End if createHelper != null
      } // End if templateWb != null
    } catch (Exception e) {
      xLogger.severe("{0} while adding data to sheet {1} in template {2}. Message: {3}",
          e.getClass().getName(), sheetName, fileName, e.getMessage(), e);
    } finally {
      if (is != null) {
        is.close();
      }
      if (os != null) {
        os.close();
      }
    }
    xLogger.fine("Exiting addDataToSheet");
  }

  /* This function evaluates the formulas in all the sheets of the workbook. It should be called
   * in the finalize method so that all the data is available before evaluating formulas.
   */
  public static void evaluateFormulas(String fileName) throws IOException {
    xLogger.fine("Entering evaluateFormulas. fileName: {0}", fileName);
    // Create a InoutStream from the bytes in the cloud storage.
    // Create a template workbook
    // Evaluate the formulas
    // Save the workbook.
    if (fileName == null || fileName.isEmpty()) {
      xLogger.severe("Cannot evaluate formulas in a null or empty file");
      return;
    }

    InputStream is = null;
    OutputStream os = null;
    // Create a workbook from the bytes
    try {
      // Get the template bytes from GCS ( Note: By now the data has been added to the appropriate sheet/s)
      is = _storageUtil.getInputStream(CustomReportsExportMgr.CUSTOMREPORTS_BUCKETNAME, fileName);
      if (is == null) {
        xLogger.severe("Failed to create Input stream for {0}", fileName);
        return;
      }
      Workbook
          templateWb =
          WorkbookFactory.create(
              is); // From the bytes downloaded from the google cloud storage, form the Workbook
      if (templateWb != null) {
        CreationHelper createHelper = templateWb.getCreationHelper();
        xLogger.fine("Created createHelper. {0}", createHelper);
        if (createHelper != null) {
          FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
          xLogger.fine("Created evaluator. {0}", evaluator);
          if (evaluator != null) {
            evaluator.evaluateAll();
            xLogger.fine("After evaluator.evaluateAll");
            templateWb.setForceFormulaRecalculation(
                true); // Added this line because some formula cells were not getting updated even after calling evaluateAll
            xLogger.fine("After templateWb.setForceFormulaRecalculation");
            // Write to file
            xLogger.fine("Now creating baos");
            os =
                _storageUtil
                    .getOutputStream(CustomReportsExportMgr.CUSTOMREPORTS_BUCKETNAME, fileName,
                        false);
            xLogger.fine("os: {0}", os);
            templateWb.write(os); // Write the workbook to OutputStream
            xLogger.fine("Wrote templateWb to baos");
          } // end if evaluator != null
        } // end if createHelper != null
      } // end if templateWb != null
    } catch (Exception e) {
      xLogger.severe("{0} while evaluating formulas in the file {1}. Message: {2}",
          e.getClass().getName(), fileName, e.getMessage(), e);
    } finally {
      if (is != null) {
        is.close();
      }
      if (os != null) {
        os.close();
      }
    }
    xLogger.fine("Exiting evaluateFormulas");
  }

}
