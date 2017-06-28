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

package com.logistimo;

import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.SourceConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.services.DuplicationException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;

/**
 * Created by charan on 26/10/16.
 */
public class LgTestCase {

  @BeforeSuite
  public static void setup() throws ServiceException {
  }

  @AfterSuite
  public static void close() throws ServiceException {
  }

  protected IDomain getDomainId(String name) throws ServiceException {
    return Services.getService(DomainsServiceImpl.class).getDomainByName(name);
  }

  protected IMaterial getMaterialId(Long domainId, String name) throws ServiceException {
    return Services.getService(MaterialCatalogServiceImpl.class)
        .getMaterialByName(domainId, name);
  }

  protected IKiosk getKiosk(Long domainId, String name) throws ServiceException {
    return Services.getService(EntitiesServiceImpl.class).getKioskByName(domainId, name);
  }

  /**
   * Delete domain config object from cache.
   *
   * @param domainId - domain id
   */
  protected void resetDomainConfig(Long domainId) {
    AppFactory.get().getMemcacheService().delete(IConfig.CONFIG_PREFIX + domainId);
  }

  protected void setDomainConfig(Long domainId, DomainConfig dc) {
    AppFactory.get().getMemcacheService().put(IConfig.CONFIG_PREFIX + domainId, dc);
  }

  protected IUserAccount getUser(String userId) throws ServiceException,
      ObjectNotFoundException {
    return Services.getService(UsersServiceImpl.class).getUserAccount(userId);
  }

  /**
   *
   * @throws ServiceException
   */
  protected void resetStock(Long kioskId, Long materialId, Long domainId, BigDecimal quantity,
                            String userId)
      throws ServiceException, DuplicationException {
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
    transaction.setDomainId(domainId);
    transaction.setKioskId(kioskId);
    transaction.setMaterialId(materialId);
    transaction.setType(ITransaction.TYPE_PHYSICALCOUNT);
    transaction.setQuantity(quantity);
    transaction.setSourceUserId(userId);
    transaction.setSrc(SourceConstants.WEB);
    IMaterial
        material =
        Services.getService(MaterialCatalogServiceImpl.class).getMaterial(materialId);
    if (material.isBatchEnabled()) {
      transaction.setBatchId("UNITTB_123");
      Calendar cal = Calendar.getInstance();
      cal.set(cal.get(Calendar.YEAR) + 2, Calendar.DECEMBER, 31);
      transaction.setBatchExpiry(cal.getTime());
      cal.add(Calendar.YEAR, -3);
      transaction.setBatchManufacturedDate(cal.getTime());
      transaction.setBatchManufacturer("UNITB_MFR");
      transaction.setReason("UNIT_TESTS");
    }
    ims.updateInventoryTransactions(domainId, Collections.singletonList(transaction), true);

  }


}
