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

package com.logistimo.api.servlets.mobile.builders;

import com.logistimo.pagination.Results;
import com.logistimo.services.Services;

import com.logistimo.activity.entity.IActivity;
import com.logistimo.activity.models.ActivityModel;
import com.logistimo.activity.service.ActivityService;
import com.logistimo.activity.service.impl.ActivityServiceImpl;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.proto.MobileConversationModel;
import com.logistimo.proto.MobileShipmentItemBatchModel;
import com.logistimo.proto.MobileShipmentItemModel;
import com.logistimo.proto.MobileShipmentModel;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.entity.IShipmentItem;
import com.logistimo.shipments.entity.IShipmentItemBatch;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by vani on 04/11/16.
 */
public class MobileShipmentBuilder {
  private static final XLog xLogger = XLog.getLog(MobileShipmentBuilder.class);

  List<MobileShipmentModel> buildMobileShipmentModels(Long orderId, Locale locale, String timezone,
                                                      boolean includeShipmentItems,
                                                      boolean includeBatchDetails) {
    if (orderId == null) {
      return null;
    }
    List<MobileShipmentModel> msmList = null;
    try {
      IShipmentService ss = Services.getService(ShipmentService.class);
      List<IShipment> shipments = ss.getShipmentsByOrderId(orderId);
      if (shipments != null && !shipments.isEmpty()) {
        msmList = new ArrayList<>(1);
        for (IShipment s : shipments) {
          ss.includeShipmentItems(s);
          MobileShipmentModel
              msm =
              buildMobileShipmentModel(s, locale, timezone, includeShipmentItems,
                  includeBatchDetails);
          if (msm != null) {
            msmList.add(msm);
          }
        }
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting shipments for the order {0}", orderId, e);
    }
    return msmList;
  }

  public MobileShipmentModel buildMobileShipmentModel(IShipment s, Locale locale, String timezone,
                                                      boolean includeShipmentItems,
                                                      boolean includeBatchDetails) {
    if (s == null) {
      return null;
    }
    MobileShipmentModel msm = new MobileShipmentModel();
    try {
      msm.sid = s.getShipmentId();
      msm.st = s.getStatus().toString();
      if (s.getUpdatedOn() != null) {
        msm.t = LocalDateUtil.format(s.getUpdatedOn(), locale, timezone);
      } else if (s.getCreatedOn() != null) {
        msm.t = LocalDateUtil.format(s.getCreatedOn(), locale, timezone);
      }
      try {
        ActivityService acs = Services.getService(ActivityServiceImpl.class);
        Results
            res =
            acs.getActivity(s.getShipmentId(), IActivity.TYPE.SHIPMENT.toString(), null, null, null,
                null, null);
        if (res != null) {
          List<ActivityModel> amList = res.getResults();
          if (amList != null && !amList.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_FORMAT);
            for (ActivityModel am : amList) {
              if (ShipmentStatus.SHIPPED.toString().equals(am.newValue)) {
                Date cd = sdf.parse(am.createDate);
                msm.ssht = LocalDateUtil.format(cd, locale, timezone);
                break;
              }
            }
          }
        }
      } catch (Exception e) {
        xLogger.warn("Exception while getting shipped time for the shipment with ID {0}",
            s.getShipmentId(), e);
      }
      try {
        UsersService as = Services.getService(UsersServiceImpl.class);
        if (s.getUpdatedBy() != null && !s.getUpdatedBy().isEmpty()) {
          msm.uid = s.getUpdatedBy();
          IUserAccount u = as.getUserAccount(s.getUpdatedBy());
          msm.n = u.getFullName();
        } else if (s.getCreatedBy() != null && !s.getCreatedBy().isEmpty()) {
          msm.uid = s.getCreatedBy();
          IUserAccount u = as.getUserAccount(s.getCreatedBy());
          msm.n = u.getFullName();
        }
      } catch (Exception e) {
        xLogger.warn("Exception while getting shipment created by user name for shipment ID {0}",
            s.getShipmentId());
      }
      msm.trsp = s.getTransporter();
      msm.trid = s.getTrackingId();
      msm.rsnps = s.getReason();
      msm.rsnco = s.getCancelledDiscrepancyReasons();
      msm.pksz = s.getPackageSize();
      if (s.getExpectedArrivalDate() != null) {
        msm.ead =
            LocalDateUtil.format(s.getExpectedArrivalDate(), locale, timezone);
      }
      if (s.getActualFulfilmentDate() != null) {
        msm.dar =
            LocalDateUtil
                .format(s.getActualFulfilmentDate(), locale, timezone);
      }
      if (includeShipmentItems) {
        List<MobileShipmentItemModel>
            msimList =
            buildMobileShipmentItemModelList((List<IShipmentItem>) s.getShipmentItems(),
                s.getServicingKiosk(), locale, timezone, includeBatchDetails);
        if (msimList != null && !msimList.isEmpty()) {
          msm.mt = msimList;
        }
      }
      // Conversations
      MobileConversationBuilder mcb = new MobileConversationBuilder();
      MobileConversationModel
          mcm =
          mcb.build(MobileConversationBuilder.CONVERSATION_OBJECT_TYPE_SHIPMENT, s.getShipmentId(),
              locale, timezone);
      if (mcm != null && mcm.cnt > 0) {
        msm.cmnts = mcm;
      }

    } catch (Exception e) {
      xLogger.warn("Exception while getting shipment for shipment id {0}", s.getShipmentId(), e);
    }
    return msm;
  }

  List<MobileShipmentItemModel> buildMobileShipmentItemModelList(List<IShipmentItem> shipmentItems,
                                                                 Long skid, Locale locale,
                                                                 String timezone,
                                                                 boolean includeBatchDetails) {
    if (shipmentItems == null || shipmentItems.isEmpty() || skid == null) {
      return null;
    }
    List<MobileShipmentItemModel> msimList = new ArrayList<>(1);
    for (IShipmentItem si : shipmentItems) {
      MobileShipmentItemModel
          msim =
          buildMobileShipmentItemModel(si, skid, locale, timezone, includeBatchDetails);
      if (msim != null) {
        msimList.add(msim);
      }
    }
    return msimList;
  }

  MobileShipmentItemModel buildMobileShipmentItemModel(IShipmentItem si, Long skid, Locale locale,
                                                       String timezone,
                                                       boolean includeBatchDetails) {
    if (si == null || skid == null) {
      return null;
    }
    MobileShipmentItemModel msim = new MobileShipmentItemModel();
    msim.mid = si.getMaterialId();
    try {
      IShipmentService ss = Services.getService(ShipmentService.class);
      BigDecimal
          alq =
          ss.getAllocatedQuantityForShipmentItem(si.getShipmentId(), skid, si.getMaterialId());
      if (alq != null) {
        msim.alq = alq;
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting allocated quantity for shipment item with id {0}",
          si.getShipmentItemId(), e);
    }
    msim.q = si.getQuantity();
    msim.flq = si.getFulfilledQuantity();
    msim.rsnpf = si.getFulfilledDiscrepancyReason();
    if (si.getUpdatedOn() != null) {
      msim.t = LocalDateUtil.format(si.getUpdatedOn(), locale, timezone);
    } else if (si.getCreatedOn() != null) {
      msim.t = LocalDateUtil.format(si.getCreatedOn(), locale, timezone);
    }
    msim.mst = si.getShippedMaterialStatus();
    msim.fmst = si.getFulfilledMaterialStatus();
    try {
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      IMaterial m = mcs.getMaterial(si.getMaterialId());
      msim.mnm = m.getName();
      String customMaterialId = m.getCustomId();
      if (customMaterialId != null && !customMaterialId.isEmpty()) {
        msim.cmid = customMaterialId;
      }
      if (m.isBatchEnabled() && includeBatchDetails) {
        List<MobileShipmentItemBatchModel>
            msibmList =
            buildMobileShipmentItemBatchModelList(
                (List<IShipmentItemBatch>) si.getShipmentItemBatch(), si.getShipmentId(), skid,
                locale, timezone);
        if (msibmList != null && !msibmList.isEmpty()) {
          msim.bt = msibmList;
        }
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting material for shipment item with id {0}",
          si.getShipmentItemId(), e);
    }
    return msim;
  }

  List<MobileShipmentItemBatchModel> buildMobileShipmentItemBatchModelList(
      List<IShipmentItemBatch> sibList, String sid, Long skid, Locale locale, String timezone) {
    if (sibList == null || sibList.isEmpty()) {
      return null;
    }
    List<MobileShipmentItemBatchModel> msibmList = new ArrayList<>(1);
    for (IShipmentItemBatch sib : sibList) {
      MobileShipmentItemBatchModel
          msibm =
          buildMobileShipmentItemBatchModel(sib, sid, skid, locale, timezone);
      if (msibm != null) {
        msibmList.add(msibm);
      }
    }
    return msibmList;
  }

  MobileShipmentItemBatchModel buildMobileShipmentItemBatchModel(IShipmentItemBatch sib, String sid,
                                                                 Long skid, Locale locale,
                                                                 String timezone) {
    if (sib == null || skid == null) {
      return null;
    }
    MobileShipmentItemBatchModel msibm = new MobileShipmentItemBatchModel();
    msibm.bid = sib.getBatchId();
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      // Get batch details from InvntryBatch since they will not be present in ShipmentItemBatch until the shipment is fulfilled.
      IInvntryBatch
          batch =
          ims.getInventoryBatch(skid, sib.getMaterialId(), sib.getBatchId(), null);
      if (batch != null) {
        if (batch.getBatchExpiry() != null) {
          msibm.bexp =
              LocalDateUtil.formatCustom(batch.getBatchExpiry(), Constants.DATE_FORMAT, timezone);
        }
        if (batch.getBatchManufacturer() != null && !batch.getBatchManufacturer().isEmpty()) {
          msibm.bmfnm = batch.getBatchManufacturer();
        }
        if (batch.getBatchManufacturedDate() != null) {
          msibm.bmfdt =
              LocalDateUtil
                  .formatCustom(batch.getBatchManufacturedDate(), Constants.DATE_FORMAT, timezone);
        }
      }

    } catch (Exception e) {
      xLogger.severe(
          "Exception while getting inventory allocation for the shipment item batch with id {0}",
          sib.getShipmentItemId(), e);
    }
    msibm.q = sib.getQuantity();
    msibm.flq = sib.getFulfilledQuantity();
    if (sib.getUpdatedOn() != null) {
      msibm.t = LocalDateUtil.format(sib.getUpdatedOn(), locale, timezone);
    } else if (sib.getCreatedOn() != null) {
      msibm.t = LocalDateUtil.format(sib.getCreatedOn(), locale, timezone);
    }
    if (sib.getShippedMaterialStatus() != null) {
      msibm.mst = sib.getShippedMaterialStatus();
    }

    if (sib.getFulfilledMaterialStatus() != null) {
      msibm.fmst = sib.getFulfilledMaterialStatus();
    }
    if (sib.getFulfilledDiscrepancyReason() != null) {
      msibm.rsnpf = sib.getFulfilledDiscrepancyReason();
    }
    // Get inventoryAllocation for batch
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      List<IInvAllocation>
          iAllocs =
          ims.getAllocationsByTypeId(skid, sib.getMaterialId(), IInvAllocation.Type.SHIPMENT, sid);

      if (iAllocs != null && !iAllocs.isEmpty()) {
        for (IInvAllocation iAlloc : iAllocs) {
          if (iAlloc.getBatchId() != null && !iAlloc.getBatchId().isEmpty() && iAlloc.getBatchId()
              .equals(sib.getBatchId())) {
            msibm.alq = iAlloc.getQuantity();
          }
        }
      }
    } catch (Exception e) {
      xLogger.warn(
          "Exception while getting inventory allocation for the shipment item batch with id {0}, skid: {1}",
          sib.getShipmentItemId(), skid, e);
    }
    return msibm;
  }
}
