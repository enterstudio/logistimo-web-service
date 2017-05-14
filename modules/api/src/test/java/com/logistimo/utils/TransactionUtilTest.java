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

package com.logistimo.utils;

import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.bulkuploads.MnlTransactionUtil;
import com.logistimo.dao.DaoFactory;

import org.junit.Before;
import org.junit.Test;

import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.mnltransactions.entity.IMnlTransaction;
import com.logistimo.mnltransactions.entity.MnlTransaction;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.LocalDateUtil;


import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by charan on 24/10/16.
 */
public class TransactionUtilTest {

  @Before
  public void setup() throws ServiceException {
  }

  @Test
  public void testParseUploadMnlTransactions() throws ServiceException {

    IMnlTransaction mnlTrans = new MnlTransaction();
    mnlTrans.setKioskId(1l);
    mnlTrans.setMaterialId(1l);
    mnlTrans.setDomainId(2l);
    Calendar cal = Calendar.getInstance();
    cal.set(2016, Calendar.JANUARY, 1);
    cal = LocalDateUtil.resetTimeFields(cal);
    mnlTrans.setReportingPeriod(cal.getTime());
    mnlTrans.setOpeningStock(BigDecimal.ONE);
    mnlTrans.setIssueQuantity(BigDecimal.ONE);
    mnlTrans.setReceiptQuantity(BigDecimal.ONE);
    mnlTrans.setStockoutDuration(10);
    mnlTrans.setUserId("charan");

    String line = "";
    long offset = 0;
    int rowNumber = 1;
    MnlTransactionUtil.InventoryEventRow
        invEventRow =
        MnlTransactionUtil.getInvEventRow(mnlTrans, line, offset, rowNumber);

    List<MnlTransactionUtil.InventoryEventRow>
        invEventRowList =
        Collections.singletonList(invEventRow);
    MnlTransactionUtil.saveInventoryEventRowList(invEventRowList,
        BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA);

    InventoryManagementServiceImpl
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    IInvntryDao invDao = new InvntryDao();
    List<IInvntryEvntLog>
        logs =
        invDao.getInvEventLogs(mnlTrans.getKioskId(), mnlTrans.getMaterialId(),
            mnlTrans.getReportingPeriod(), null, null);
    boolean found = false;
    for (IInvntryEvntLog log : logs) {
      if (log.getStartDate().equals(mnlTrans.getReportingPeriod())) {
        found = true;
        break;
      }
    }
    assertTrue("Inventory log not found", found);

  }


}
