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

package com.logistimo.orders.utils;

import com.logistimo.logger.XLog;
import com.logistimo.orders.models.PDFResponseModel;
import com.logistimo.services.storage.StorageUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by nitisha.khandelwal on 18/08/17.
 */

@Component
public class JasperClient {

  private static final XLog xLogger = XLog.getLog(JasperClient.class);
  private final StorageUtil storageUtil;

  @Autowired
  public JasperClient(StorageUtil storageUtil) {
    this.storageUtil = storageUtil;
  }

  public PDFResponseModel generatePDF(String fileName, String template, String bucketName,
      Collection<?> items, Map<String, Object> parameters)
      throws ClassNotFoundException, JRException, IOException {

    JasperPrint jasperPrint;
    InputStream inputStream = null;

    JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(items);

    try {
      inputStream = storageUtil.getInputStream(bucketName, template);
      jasperPrint = JasperFillManager.fillReport(JasperCompileManager.compileReport(
          inputStream), parameters, beanColDataSource);
      byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
      return new PDFResponseModel(fileName, pdfBytes);
    } catch (ClassNotFoundException | JRException | IOException e) {
      xLogger.severe("Failed to generate PDF for file name - ", fileName, e);
      throw e;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

}
