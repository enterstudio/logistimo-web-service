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

package com.logistimo.service.impl;
import org.junit.Before;
import org.junit.Test;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.Constants;
import com.logistimo.events.entity.IEvent;
import com.logistimo.inventory.entity.IInvntry;

import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.Invntry;
import com.logistimo.inventory.entity.InvntryEvntLog;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by charan on 24/10/16.
 */
public class InventoryManagementServiceTest {
  @Before
  public void setup() throws ServiceException {
  }

  @Test
  public void adjustInventoryEvents() throws ServiceException {
    InventoryManagementServiceImpl
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    Long kioskId = 1l, materialId = 1l;
    IInvntry invntry = ims.getInventory(kioskId, materialId);
    InvntryEvntLog invntryEvntLog = new InvntryEvntLog(IEvent.STOCKOUT, (Invntry) invntry);
    Calendar cal = Calendar.getInstance();
    cal.set(2016, Calendar.JANUARY, 1);
    invntryEvntLog.setStartDate(cal.getTime());
    cal.set(Calendar.DAY_OF_MONTH, 30);
    invntryEvntLog.setEndDate(cal.getTime());
    IInvntryEvntLog createdInvEvntLog = ims.adjustInventoryEvents(invntryEvntLog);
    invntry = ims.getInventory(kioskId, materialId);
    Long actualInvEvntLog = invntry.getLastStockEvent();
    assertEquals("Event log id does not match", createdInvEvntLog.getKey(), actualInvEvntLog);
  }

  @Test
  public void getInventoryTest() throws ServiceException {
    InventoryManagementServiceImpl
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    Long domainId = 2l, materialId = 7l;
    List<Long> kioskIds = new ArrayList<Long>(1);
    kioskIds.add(119l);
    Results results1 = ims.getInventoryByMaterialDomain(materialId, null, kioskIds, null, domainId);
    Results results2 = ims.getInventory(domainId, null, kioskIds, null, materialId, null, 0,
        false, null, null,null);
    assertEquals("Results match", results1.getResults().size(), results2.getResults().size());
  }

  @Test
  public void allowEntityBatchManagementUpdateTest() throws ServiceException {
    InventoryManagementServiceImpl
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    Long kioskId = 118l;
    boolean changeAllowed = ims.validateEntityBatchManagementUpdate(kioskId);
    assertEquals("Results match", false, changeAllowed);
  }

  @Test
  public void allowMaterialBatchManagementUpdateTest() throws ServiceException {
    InventoryManagementServiceImpl
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    Long materialId = 7l;
    boolean changeAllowed = ims.validateMaterialBatchManagementUpdate(materialId);
    assertEquals("Results match", false, changeAllowed);
  }
}
