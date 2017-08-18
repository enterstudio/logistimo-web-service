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

import com.logistimo.orders.entity.DemandItem;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.service.impl.InvoiceItem;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by charan on 07/08/17.
 */
public class GenerateInvoiceTest {

  
  @Test
  public void testInvoice(){
    List<InvoiceItem> invoiceItems = new ArrayList<>();

    int count = 1;
    for (IDemandItem demandItem : getDemandItems()) {
      InvoiceItem invoiceItem = new InvoiceItem();
      invoiceItem.setItem(demandItem.getMaterialId().toString());
      invoiceItem.setQuantity(demandItem.getQuantity().toString());
      invoiceItem.setRecommended(demandItem.getRecommendedOrderQuantity().toString());
      invoiceItem.setRemarks("Blah");
        invoiceItem.setBatchId("AB/1234/56"+count);
        invoiceItem.setExpiry("11/03/2020");
        invoiceItem.setManufacturer("Serum");
        invoiceItem.setBatchQuantity(BigDecimal.TEN.toPlainString());
      invoiceItem.setSno(String.valueOf(count++));
      invoiceItems.add(invoiceItem);
    }

    JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(invoiceItems);

    try {

      Map<String, Object> hm = new HashMap<>();
      JasperPrint jasperPrint = JasperFillManager.fillReport(
          JasperCompileManager
              .compileReport(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                  "test_logistimo_invoice.jrxml")), hm, beanColDataSource);

      JasperExportManager.exportReportToPdfFile(jasperPrint, "/tmp/logistimo_invoice.pdf");


    } catch (Exception e) {
      e.printStackTrace();
    }


  }

  private List<IDemandItem> getDemandItems() {
    List<IDemandItem> items = new ArrayList<>(2);
    DemandItem demandItem = new DemandItem();
    demandItem.setMaterialId(1234l);
    demandItem.setQuantity(BigDecimal.valueOf(20));
    demandItem.setRecommendedOrderQuantity(BigDecimal.TEN);
    items.add(demandItem);

    DemandItem demandItem2 = new DemandItem();
    demandItem2.setMaterialId(2334l);
    demandItem2.setQuantity(BigDecimal.valueOf(20));
    demandItem2.setRecommendedOrderQuantity(BigDecimal.TEN);
    items.add(demandItem2);

    DemandItem demandItem3 = new DemandItem();
    demandItem3.setMaterialId(1234l);
    demandItem3.setQuantity(BigDecimal.valueOf(20));
    demandItem3.setRecommendedOrderQuantity(BigDecimal.TEN);
    items.add(demandItem3);

    return items;
  }

}
