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

import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.models.orders.DiscrepancyModel;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;
import com.logistimo.api.models.DiscrepancyUIModel;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by vani on 04/10/16.
 */
public class DiscrepancyBuilder {
  private static final XLog xLogger = XLog.getLog(DiscrepancyBuilder.class);

  public Results buildDiscrepancyModels(Results results, SecureUserDetails user)
      throws ServiceException, ObjectNotFoundException {
    List<DiscrepancyUIModel> discUIModels = null;
    List<DiscrepancyModel> discModels = null;
    EntitiesService as = Services.getService(EntitiesServiceImpl.class);
    if (results != null) {
      discModels = results.getResults();
      int count = results.getOffset() + 1;
      discUIModels = new ArrayList<>(discModels.size());
      for (DiscrepancyModel dModel : discModels) {
        DiscrepancyUIModel duiModel = build(dModel, as, user);
        duiModel.sno = count++;
        if (duiModel != null) {
          discUIModels.add(duiModel);
        }
      }
    }
    Results
        finalResults =
        new Results(discUIModels, results.getCursor(), results.getNumFound(), results.getOffset());
    return finalResults;
  }

  public DiscrepancyUIModel build(DiscrepancyModel dm, EntitiesService as, SecureUserDetails user) {
    Locale locale = user.getLocale();
    String timezone = user.getTimezone();
    IKiosk c, v;
    Map<Long, String> domainNames = new HashMap<>(1);
    try {
      c = as.getKiosk(dm.cId, false);
    } catch (Exception e) {
      xLogger.warn("Error while fetching Kiosk {0}", dm.cId);
      return null;
    }

    DiscrepancyUIModel duiModel = new DiscrepancyUIModel();
    duiModel.id = dm.id;
    duiModel.disc = dm.discTypes;
    if (duiModel.disc != null) {
      if (duiModel.disc.contains(IDemandItem.ORDERING_DISCREPANCY)) {
        duiModel.hasOd = true;
      }
      if (duiModel.disc.contains(IDemandItem.SHIPPING_DISCREPANCY)) {
        duiModel.hasSd = true;
      }
      if (duiModel.disc.contains(IDemandItem.FULFILLMENT_DISCREPANCY)) {
        duiModel.hasFd = true;
      }
    }
    duiModel.oid = dm.oId;
    if (dm.oct != null) {
      duiModel.oct = LocalDateUtil.format(dm.oct, locale, timezone);
    }
    duiModel.rid = dm.rId;
    duiModel.oty = dm.oty;
    try {
      duiModel.otyStr = getOrderTypeDisplay(dm.oty, locale);
    } catch (Exception e) {
      xLogger.severe("Failed to order type : {0}", dm.oty, e);
    }
    duiModel.mid = dm.mId;
    duiModel.mnm = dm.mnm;
    duiModel.roq = dm.roq;
    duiModel.oq = dm.oq;
    if (dm.oq != null && dm.roq != null) {
      duiModel.od = dm.oq.subtract(dm.roq);
    }
    duiModel.sq = dm.sq;
    if (dm.sq != null && dm.oq != null) {
      duiModel.sd = dm.sq.subtract(dm.oq);
    }

    duiModel.fq = dm.fq;
    if (dm.sq != null && dm.fq != null) {
      duiModel.fd = dm.fq.subtract(dm.sq);
    }

    duiModel.odRsn = dm.odrsn;
    // duiModel.odRsnStr = getOdRsnsDisplay(duiModel.odRsn, locale);
    duiModel.sdRsn = dm.sdrsn;
    // duiModel.sdRsnStr = getSdRsnsDisplay(duiModel.sdRsn, locale);
    duiModel.fdRsns = dm.fdrsns;
    duiModel.fdRsnsStr = getFdRsnsDisplay(duiModel.fdRsns, locale);
    duiModel.st = dm.st;
    duiModel.status = OrderUtils.getStatusDisplay(dm.st, locale);
    if (dm.stt != null) {
      duiModel.stt = LocalDateUtil.format(dm.stt, locale, timezone);
    }
    duiModel.cid = dm.cId;
    duiModel.cnm = dm.cnm;
    duiModel.cadd = c.getFormattedAddress();
    duiModel.vid = dm.vId;
    duiModel.vnm = dm.vnm;
    try {
      v = as.getKiosk(dm.vId, false);
      duiModel.vadd = v != null ? v.getFormattedAddress() : null;
    } catch (Exception e) {
      xLogger.warn("Ignoring Exception while getting vendor kiosk with id {0}", dm.vId);
    }

    duiModel.sdid = dm.sdid;
    String domainName = domainNames.get(dm.sdid);
    if (domainName == null) {
      IDomain domain = null;
      try {
        DomainsService ds = Services.getService(DomainsServiceImpl.class);
        domain = ds.getDomain(dm.sdid);
      } catch (Exception e) {
        xLogger.warn("Error while fetching Domain {0}", dm.sdid);
      }
      if (domain != null) {
        domainName = domain.getName();
      } else {
        domainName = Constants.EMPTY;
      }
      domainNames.put(dm.sdid, domainName);
    }
    duiModel.sdnm = domainName;
    return duiModel;
  }

  private String getFdRsnsDisplay(List<String> fdRsns, Locale locale) {
    ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", locale);
    if (jsMessages == null) {
      return "unknown";
    }
    if (fdRsns == null || fdRsns.isEmpty()) {
      return null;
    }
    boolean isBatch = fdRsns.get(0).substring(fdRsns.get(0).indexOf("||") + 2).contains("||");
    StringBuilder fdRsnsStr = new StringBuilder();
    fdRsnsStr.append("<table><tr>")
        .append("<th>").append(jsMessages.getString("shipment")).append("</th>");
    if (isBatch) {
      fdRsnsStr.append("<th>").append("Batch").append("</th>");
    }
    fdRsnsStr.append("<th>").append(jsMessages.getString("reasons")).append("</th></tr>");
    for (String fdRsn : fdRsns) {
      int index = fdRsn.indexOf("||");
      String sid = fdRsn.substring(0, index);
      String bid = null;
      if (isBatch) {
        int lastIndex = fdRsn.lastIndexOf("||");
        bid = fdRsn.substring(index + 2, lastIndex);
        index = lastIndex;
      }
      String rsn = fdRsn.substring(index + 2);
      fdRsnsStr.append("<tr><td><p align=\"left\">").append(sid).append("</p></td>");
      if (isBatch) {
        fdRsnsStr.append("<td><p align=\"left\" class=\"pr5\">").append(bid).append("</p></td>");
      }
      fdRsnsStr.append("<td><p align=\"left\">").append(rsn).append("</p></td></tr>");
    }
    fdRsnsStr.append("</table>");
    return fdRsnsStr.toString();
  }

  public static String getOrderTypeDisplay(int oType, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    if (messages == null) {
      return "unknown";
    }
    String name = "";
    if (oType == IOrder.TRANSFER) {
      name = messages.getString("transactions.transfer.upper");
    }
    return name;
  }

}
