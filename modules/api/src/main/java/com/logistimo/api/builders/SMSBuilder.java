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

package com.logistimo.api.builders;

import com.logistimo.dao.JDOUtils;

import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.logger.XLog;
import com.logistimo.api.constants.SMSConstants;
import com.logistimo.api.models.SMSModel;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public class SMSBuilder {

  private static final XLog xLogger = XLog.getLog(SMSBuilder.class);

  public SMSModel constructSMSModel(String message) throws ServiceException {
    SMSModel model = new SMSModel();
    List<String> fields = Arrays.asList(message.split(SMSConstants.FIELD_SEPARATOR));
    for (String field : fields) {
      String[] keyValue = field.split(SMSConstants.KEY_SEPARATOR);
      switch (keyValue[0]) {
        case SMSConstants.TOKEN:
          model.token = keyValue[1];
          break;
        case SMSConstants.TRANSACTION_TYPE:
          model.type = keyValue[1];
          break;
        case SMSConstants.INVENTORY:
          model.materials = new ArrayList<>();
          List<String>
              materials =
              Arrays.asList(keyValue[1].split(SMSConstants.MATERIAL_SEPARATOR));
          for (String material : materials) {
            String[] mat = material.split(CharacterConstants.COMMA);
            model.addMaterial(Long.parseLong(mat[0]), new BigDecimal(mat[1]),
                new BigDecimal(mat[2]));
          }
          break;
        case SMSConstants.PARTIAL_ID:
          model.partialId = keyValue[1];
          break;
        case SMSConstants.SAVE_TIMESTAMP:
          model.saveTS = new Date(Long.parseLong(keyValue[1]));
          break;
        case SMSConstants.ACTUAL_TIMESTAMP:
          model.actualTD = keyValue[1];
          break;
        case SMSConstants.USER_ID:
          model.userId = keyValue[1];
          break;
        case SMSConstants.KIOSK_ID:
          model.kioskId = Long.parseLong(keyValue[1]);
          break;
        case SMSConstants.DEST_KIOSK_ID:
          model.destKioskId = Long.parseLong(keyValue[1]);
          break;
        default:
          xLogger.warn("Unknown key,value found in SMS: " + Arrays.toString(keyValue));
      }
    }
    if (model.materials != null && model.kioskId != null) {
      updateMaterialDetails(model);
    }
    return model.isValid() ? model : null;
  }

  public void updateMaterialDetails(SMSModel model) throws ServiceException {
    for (SMSModel.SMSInv material : model.materials) {
      InventoryManagementService ims = new InventoryManagementServiceImpl();
      IInvntry invntry = ims.getInvntryByShortID(model.kioskId, material.id);
      if (invntry != null) {
        material.matId = invntry.getMaterialId();
        material.curStk = invntry.getStock();
      }

    }
  }

  public String constructSMS(SMSModel model, String failMessage) throws ServiceException {
    return constructSMS(model, null, failMessage);
  }

  public String constructSMS(SMSModel model, Map<Long, String> errorCodes) throws ServiceException {
    return constructSMS(model, errorCodes, null);
  }

  public String constructSMS(SMSModel model, Map<Long, String> errorCodes, String failMessage)
      throws ServiceException {
    updateMaterialDetails(model);
    StringBuilder sms = new StringBuilder();
    sms.append(SMSConstants.TRANSACTION_TYPE).append(SMSConstants.KEY_SEPARATOR).append(model.type);
    if (model.partialId != null) {
      sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.PARTIAL_ID)
          .append(SMSConstants.KEY_SEPARATOR).append(model.partialId);
    }
    sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.SAVE_TIMESTAMP)
        .append(SMSConstants.KEY_SEPARATOR).append(model.saveTS.getTime())
        .append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.KIOSK_ID)
        .append(SMSConstants.KEY_SEPARATOR).append(model.kioskId);
    if (failMessage == null) {
      StringBuilder successInventory = new StringBuilder();
      StringBuilder failInventory = new StringBuilder();
      for (SMSModel.SMSInv material : model.materials) {
        if (!errorCodes.containsKey(material.matId)) {
          if (successInventory.length() > 0) {
            successInventory.append(SMSConstants.MATERIAL_SEPARATOR);
          }
          successInventory.append(material.id).append(CharacterConstants.COMMA)
              .append(BigUtil.getFormattedValue(material.curStk));
        } else {
          if (failInventory.length() > 0) {
            failInventory.append(SMSConstants.MATERIAL_SEPARATOR);
          }
          failInventory.append(material.id).append(CharacterConstants.COMMA)
              .append(BigUtil
                  .getFormattedValue(material.curStk == null ? BigDecimal.ZERO : material.curStk))
              .append(CharacterConstants.COMMA)
              .append(
                  errorCodes.get(material.matId) == null ? "M004" : errorCodes.get(material.matId));
        }
      }
      sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.SUCCESS_INVENTORY)
          .append(SMSConstants.KEY_SEPARATOR).append(successInventory);
      sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.FAIL_INVENTORY)
          .append(SMSConstants.KEY_SEPARATOR).append(failInventory);
    } else {
      sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.FAIL_MESSAGE)
          .append(SMSConstants.KEY_SEPARATOR).append(failMessage);
    }
    return sms.toString();
  }

  public List<
      ITransaction> buildInventoryTransactions(SMSModel model) {
    List<ITransaction> transactions = new ArrayList<>(model.materials.size());
    Date now = new Date();
    boolean checkBatchMgmt = ITransaction.TYPE_TRANSFER.equals(model.type);
    MaterialCatalogServiceImpl mcs = null;
    try {
      if (checkBatchMgmt) {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        IKiosk kiosk = as.getKiosk(model.kioskId);
        IKiosk linkedKiosk = as.getKiosk(model.destKioskId);
        checkBatchMgmt =
            !kiosk.isBatchMgmtEnabled() && linkedKiosk != null && linkedKiosk.isBatchMgmtEnabled();
        mcs = Services.getService(MaterialCatalogServiceImpl.class, Locale.ENGLISH);
      }
    } catch (ServiceException e) {
      xLogger.warn("ServiceException while getting kiosk details. Exception: {0)", e);
    }
    ITransDao transDao = new TransDao();
    for (SMSModel.SMSInv material : model.materials) {
      ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
      transaction.setKioskId(model.kioskId);
      transaction.setMaterialId(material.matId);
      transaction.setType(model.type);
      transaction.setQuantity(material.quantity);
      transaction.setSourceUserId(model.userId);
      transaction.setTimestamp(now);
      transaction.setLinkedKioskId(model.destKioskId);
      if (model.actualTS != null) {
        transaction.setAtd(model.actualTS);
      }
      transaction.setSrc(SourceConstants.SMS);
      transDao.setKey(transaction);
      if (checkBatchMgmt && mcs != null) {
        try {
          IMaterial mat = mcs.getMaterial(material.matId);
          if (mat.isBatchEnabled()) {
            transaction.setMessage(
                "Transfer from batch disabled entity to batch enabled entity failed for Material"
                    + mat.getName());
            transaction.setMsgCode("M008");
          }
        } catch (ServiceException e) {
          xLogger.warn("ServiceException while getting material details. Exception: {0}", e);
        }
      }
      transactions.add(transaction);
    }
    return transactions;
  }
}
