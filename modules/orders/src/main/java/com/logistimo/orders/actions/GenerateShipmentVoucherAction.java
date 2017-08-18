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

package com.logistimo.orders.actions;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.ValidationException;
import com.logistimo.logger.XLog;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.InvoiceResponseModel;
import com.logistimo.orders.service.impl.InvoiceItem;
import com.logistimo.orders.utils.InvoiceUtils;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.storage.StorageUtil;
import com.logistimo.shipments.entity.IShipment;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by nitisha.khandelwal on 27/07/17.
 */

@Component
public class GenerateShipmentVoucherAction {

  private static final XLog xLogger = XLog.getLog(GenerateShipmentVoucherAction.class);
  private static final String PREFIX = "Shipment-";

  private final InvoiceUtils invoiceUtils;
  private final StorageUtil storageUtil;

  @Autowired
  public GenerateShipmentVoucherAction(InvoiceUtils invoiceUtils, StorageUtil storageUtil) {
    this.invoiceUtils = invoiceUtils;
    this.storageUtil = storageUtil;
  }

  public InvoiceResponseModel invoke(IOrder order, IShipment shipment, SecureUserDetails user)
      throws ServiceException, IOException, ValidationException {

    if (!invoiceUtils.hasAccessToOrder(user, order)) {
      xLogger.warn("User {0} does not have access to order id - {1}", user.getUsername(),
          order.getOrderId());
      throw new InvalidDataException("User does not have access to domain");
    }

    JasperPrint jasperPrint;
    InputStream inputStream = null;

    try {

      Map<String, Object> parameters = invoiceUtils.getParameters(user, order, shipment);
      List<InvoiceItem> invoiceItems = invoiceUtils.getInvoiceItems(order, shipment);

      JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(invoiceItems);
      String fileName = PREFIX + shipment.getShipmentId() + InvoiceUtils.DASH +
          invoiceUtils.getDateSuffix(user) + InvoiceUtils.PDF_EXTENSION;
      inputStream = storageUtil.getInputStream(InvoiceUtils.UPLOADS,
          getTemplate(user.getCurrentDomainId()));
      jasperPrint = JasperFillManager.fillReport(JasperCompileManager
          .compileReport(inputStream), parameters, beanColDataSource);
      byte[] bytes = JasperExportManager.exportReportToPdf(jasperPrint);
      return new InvoiceResponseModel(fileName, bytes);
    } catch (Exception e) {
      xLogger.severe("Failed to generate shipment voucher for Shipment {0}",
          shipment.getShipmentId(), e);
      throw new ServiceException(e);
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  private String getTemplate(Long domainId) {
    String template = DomainConfig.getInstance(domainId).getOrdersConfig().getShipmentTemplate();
    return template != null ? template : "logistimo_shipment.jrxml";
  }

}
