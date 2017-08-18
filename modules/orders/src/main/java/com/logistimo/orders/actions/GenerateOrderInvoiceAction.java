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
import com.logistimo.orders.models.PDFResponseModel;
import com.logistimo.orders.utils.InvoiceUtils;
import com.logistimo.orders.utils.JasperClient;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by nitisha.khandelwal on 27/07/17.
 */

@Component
public class GenerateOrderInvoiceAction {

  private static final XLog xLogger = XLog.getLog(GenerateOrderInvoiceAction.class);

  private static final String PREFIX = "Invoice-";

  private final InvoiceUtils invoiceUtils;
  private final JasperClient jasperClient;

  @Autowired
  public GenerateOrderInvoiceAction(InvoiceUtils invoiceUtils, JasperClient jasperClient) {
    this.jasperClient = jasperClient;
    this.invoiceUtils = invoiceUtils;
  }

  public PDFResponseModel invoke(IOrder order, SecureUserDetails user)
      throws ServiceException, IOException, ValidationException {

    if (!invoiceUtils.hasAccessToOrder(user, order)) {
      xLogger.warn("User {0} does not have access to order id - {1}", user.getUsername(),
          order.getOrderId());
      throw new InvalidDataException("User does not have access to domain");
    }

    try {
      String fileName = PREFIX + order.getOrderId() + InvoiceUtils.DASH +
          invoiceUtils.getDateSuffix(user) + InvoiceUtils.PDF_EXTENSION;
      return jasperClient.generatePDF(fileName, getTemplate(user.getCurrentDomainId()),
          InvoiceUtils.UPLOADS, invoiceUtils.getInvoiceItems(order, null),
          invoiceUtils.getParameters(user, order, null));
    } catch (Exception e) {
      xLogger.severe("Failed to generate invoice for Order - {0}", order.getOrderId(), e);
      throw new ServiceException(e);
    }
  }

  private String getTemplate(Long domainId) {
    String template = DomainConfig.getInstance(domainId).getOrdersConfig().getInvoiceTemplate();
    return template != null ? template : "logistimo_invoice.jrxml";
  }
}
