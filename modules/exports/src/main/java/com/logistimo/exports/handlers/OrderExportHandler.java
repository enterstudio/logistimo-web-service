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

package com.logistimo.exports.handlers;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.FieldsConfig;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.entity.DemandItem;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.apache.commons.lang.StringEscapeUtils;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.utils.GeoUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class OrderExportHandler implements IExportHandler {

  private static final XLog xLogger = XLog.getLog(OrderExportHandler.class);

  IOrder order;

  public OrderExportHandler(IOrder order){
    this.order = order;
  }

  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    FieldsConfig fc = null;
    if (dc != null) {
      fc = dc.getOrderFields();
    }

    try {
      // Get services
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      UsersService usersService = Services.getService(UsersServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      IKiosk c = as.getKiosk(order.getKioskId(), false);
      IKiosk v = null;
      try {
        v = as.getKiosk(order.getServicingKiosk(), false);
      } catch (ServiceException se) {
        xLogger.warn(
            "ServiceException ({0}) when getting kiosk for skId {1} while getting csv for order {2}: {3}",
            se.getClass().getName(), order.getServicingKiosk(), order.getIdString(), se.getMessage());
      }

      StringBuilder csv = new StringBuilder();
      StringBuilder orderDetSb = getOrderDetailsSb(locale, timezone, c, v);
      StringBuilder accSb = getAccountingSb(dc);
      StringBuilder orPrSb = getOrderPricingSb(dc);
      StringBuilder timeSb = getTimeSb(dc);
      StringBuilder locationSb = getLocationSb(dc, c, locale);
      StringBuilder tagSb = getTagSb(dc);
      StringBuilder crUpSb = getCreatedUpdatedSb(usersService, timezone);

      // Iterate over items and get the itemSb
      StringBuilder itemSb = new StringBuilder();
      if (order.getItems() != null && !order.getItems().isEmpty()) {
        Iterator<DemandItem> it = (Iterator<DemandItem>) order.getItems().iterator();
        while (it.hasNext()) {
          IDemandItem item = it.next();
          itemSb = getItemSb(mcs, dc, item);
          csv.append(orderDetSb).append(CharacterConstants.COMMA)
              .append(itemSb).append(CharacterConstants.COMMA)
              .append(orPrSb != null ? orPrSb.toString() + CharacterConstants.COMMA
                  : CharacterConstants.EMPTY)
              .append(accSb != null ? accSb.toString() + CharacterConstants.COMMA
                  : CharacterConstants.EMPTY)
              .append(timeSb).append(CharacterConstants.COMMA)
              .append(locationSb).append(CharacterConstants.COMMA);
          List<String> materialTags = item.getTags(TagUtil.TYPE_MATERIAL);
          csv.append((materialTags != null && !materialTags.isEmpty() ? StringEscapeUtils
              .escapeCsv(StringUtil.getCSV(materialTags)) : CharacterConstants.EMPTY))
              .append(CharacterConstants.COMMA)
              .append(tagSb)
              .append(CharacterConstants.COMMA)
              .append(crUpSb);
          if (it.hasNext()) {
            csv.append(CharacterConstants.NEWLINE);
          }
        }
      } else {
        itemSb.append(",,,,");
        if (!dc.isDisableOrdersPricing()) {
          itemSb.append(CharacterConstants.COMMA)
              .append(order.getCurrency() != null ? StringEscapeUtils.escapeCsv(order.getCurrency()) : CharacterConstants.EMPTY);
          itemSb.append(",,,");
        }

        csv.append(orderDetSb).append(CharacterConstants.COMMA)
            .append(itemSb).append(CharacterConstants.COMMA)
            .append(orPrSb != null ? orPrSb.toString() + CharacterConstants.COMMA
                : CharacterConstants.EMPTY)
            .append(accSb != null ? accSb.toString() + CharacterConstants.COMMA
                : CharacterConstants.EMPTY)
            .append(timeSb).append(CharacterConstants.COMMA)
            .append(locationSb).append(CharacterConstants.COMMA)
            .append(CharacterConstants.COMMA) // For material tags
            .append(tagSb).append(CharacterConstants.COMMA).append(crUpSb);
      }

			/*// Add custom fields, if any
                        if ( fc != null && !fc.isEmpty() ) {
				// Get the field value map
				Map<String,String> values = getFields();
				Iterator<String> fields = fc.keys();
				while ( fields.hasNext() ) {
					String field = fields.next();
					FieldsConfig.Field f = fc.getField( field );
					if ( values != null ) {
						String val = values.get( f.getId() );
						if ( val == null )
							val = "";
						orderStr += "," + val;
					}
				}
			}*/

      return csv.toString();
    } catch (Exception e) {
      xLogger.warn("{0} when getting CSV for order {1}: {2}", e.getClass().getName(), order.getIdString(),
          e.getMessage());
      return null;
    }
  }

  private StringBuilder getOrderDetailsSb(Locale locale, String timezone, IKiosk c, IKiosk v) {

    String oid = String.valueOf(order.getIdString());
    String status = OrderUtils.getStatusDisplay(order.getStatus(), locale);

    String vName = null, vCustomId = null;

    if (order.getServicingKiosk() != null) {
      if (v != null) {
        // Suppose skId is present, but the corresponding kiosk is removed from the system, v will be null
        vName = v.getName();
        vCustomId = v.getCustomId();
      } else {
        vName = Constants.UNKNOWN;
      }
    }

    StringBuilder orderDetailsSb = new StringBuilder();
    orderDetailsSb.append(oid).append(CharacterConstants.COMMA)
        .append(order.getReferenceID() != null ? StringEscapeUtils.escapeCsv(order.getReferenceID()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(status).append(CharacterConstants.COMMA)
        .append(order.getKioskId()).append(CharacterConstants.COMMA)
        .append(c != null && c.getCustomId() != null ? StringEscapeUtils.escapeCsv(c.getCustomId())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(c != null && c.getName() != null ? StringEscapeUtils.escapeCsv(c.getName())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(order.getServicingKiosk() != null ? order.getServicingKiosk() : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            vCustomId != null ? StringEscapeUtils.escapeCsv(vCustomId) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(vName != null ? StringEscapeUtils.escapeCsv(vName) : CharacterConstants.EMPTY);

    return orderDetailsSb;
  }

  private StringBuilder getItemSb(MaterialCatalogService mcs, DomainConfig dc, IDemandItem item)
      throws ServiceException {
    StringBuilder itemSb = new StringBuilder();

    IMaterial m = mcs.getMaterial(item.getMaterialId());
    itemSb.append(m.getMaterialId() != null ? m.getMaterialId() : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(m.getCustomId() != null ? StringEscapeUtils.escapeCsv(m.getCustomId())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(StringEscapeUtils.escapeCsv(m.getName())).append(CharacterConstants.COMMA)
        .append(item.getReason() != null ? StringEscapeUtils.escapeCsv(item.getReason())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(BigUtil.getFormattedValue(item.getQuantity()));

    if (!dc.isDisableOrdersPricing()) {
      itemSb.append(CharacterConstants.COMMA)
          .append(item.getCurrency() != null ? StringEscapeUtils.escapeCsv(item.getCurrency())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(item.getFormattedPrice()).append(CharacterConstants.COMMA)
          .append(item.getDiscount()).append(CharacterConstants.COMMA)
          .append(item.computeTotalPrice(true));
    }
    return itemSb;
  }

  private StringBuilder getOrderPricingSb(DomainConfig dc) {
    StringBuilder orPrSb = null;

    if (!dc.isDisableOrdersPricing()) {
      orPrSb = new StringBuilder();
      orPrSb.append(order.getFormattedPrice()).append(CharacterConstants.COMMA)
          .append(order.getTax());
    }
    return orPrSb;
  }

  private StringBuilder getAccountingSb(DomainConfig dc) {
    StringBuilder accSb = null;
    if (dc.isAccountingEnabled() && !dc.isDisableOrdersPricing()) {
      accSb = new StringBuilder();
      //accSb.append(Order.getFormattedPrice(getPaid())).append(CharacterConstants.COMMA)
      accSb.append(order.getPaymentOption() != null ? order.getPaymentOption() : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(order.getPaid() != null ? BigUtil.getFormattedValue(order.getPaid()) : 0).append(CharacterConstants.COMMA)
          .append(order.getPaidStatus() != null ? StringEscapeUtils.escapeCsv(order.getPaidStatus()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(order.getPaymentHistory() != null ? StringEscapeUtils.escapeCsv(order.getPaymentHistory()) : CharacterConstants.EMPTY);
    }
    return accSb;
  }

  StringBuilder getTimeSb(DomainConfig dc) {
    StringBuilder timeSb = new StringBuilder();
    timeSb.append(String.format("%.2f", order.getProcessingTimeInHours()))
        .append(CharacterConstants.COMMA)
        .append(String.format("%.2f",order.getDeliveryLeadTimeInHours()));
    return timeSb;
  }

  StringBuilder getLocationSb(DomainConfig dc, IKiosk c, Locale locale) {
    StringBuilder locationSb = new StringBuilder();
    locationSb.append(c.getCountry() != null ? StringEscapeUtils.escapeCsv(c.getCountry())
        : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(c.getState() != null ? StringEscapeUtils.escapeCsv(c.getState())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(c.getDistrict() != null ? StringEscapeUtils.escapeCsv(c.getDistrict())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(c.getTaluk() != null ? StringEscapeUtils.escapeCsv(c.getTaluk())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(c.getCity() != null ? StringEscapeUtils.escapeCsv(c.getCity())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(c.getStreet() != null ? StringEscapeUtils.escapeCsv(c.getStreet())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(c.getPinCode() != null ? StringEscapeUtils.escapeCsv(c.getPinCode())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(order.getLatitude() != null ? order.getLatitude() : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(order.getLongitude() != null ? order.getLongitude() : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(order.getGeoAccuracy() != null ? NumberUtil.getDoubleValue(order.getGeoAccuracy()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(order.getGeoErrorCode() != null ? StringEscapeUtils.escapeCsv(GeoUtil.getGeoErrorMessage(order.getGeoErrorCode(), locale))
            : CharacterConstants.EMPTY);
    return locationSb;
  }

  private StringBuilder getTagSb(DomainConfig dc) {
    StringBuilder tagSb = new StringBuilder();
    List<String> ktgs = order.getTags(TagUtil.TYPE_ENTITY);
    List<String> otgs = order.getTags(TagUtil.TYPE_ORDER);
    tagSb.append(
        ktgs != null && !ktgs.isEmpty() ? StringEscapeUtils.escapeCsv(StringUtil.getCSV(ktgs))
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(otgs != null && !otgs.isEmpty() ? StringEscapeUtils
            .escapeCsv(StringUtil.getCSV(otgs)) : CharacterConstants.EMPTY);

    return tagSb;
  }

  private StringBuilder getCreatedUpdatedSb(UsersService as, String timezone) {
    String cbFullName = null, cbCustomId = null;
    if (order.getUserId() != null) {
      try {
        IUserAccount u = as.getUserAccount(order.getUserId());
        cbFullName = u.getFullName();
        cbCustomId = u.getCustomId();
      } catch (ObjectNotFoundException e) {
        xLogger.warn("ObjectNotFoundException ({0}) when getting CSV for order: {1}",
            order.getUserId(), order.getIdString());
        cbFullName = Constants.UNKNOWN;
      } catch (ServiceException se) {
        // ignore
      }
    }
    String subFullName = null, subCustomId = null;
    if (order.getUpdatedBy() != null) {
      try {
        IUserAccount u = as.getUserAccount(order.getUpdatedBy());
        subFullName = u.getFullName();
        subCustomId = u.getCustomId();
      } catch (ObjectNotFoundException e) {
        xLogger.warn("ObjectNotFoundException ({0}) when getting CSV for order: {1}",
            order.getUserId(), order.getIdString());
        subFullName = Constants.UNKNOWN;
      } catch (ServiceException se) {
        // ignore
      }
    }

    StringBuilder crUpSb = new StringBuilder();

    crUpSb.append(order.getUserId() != null ? order.getUserId() : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            cbCustomId != null ? StringEscapeUtils.escapeCsv(cbCustomId) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(
            cbFullName != null ? StringEscapeUtils.escapeCsv(cbFullName) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(
            order.getCreatedOn() != null ? LocalDateUtil.formatCustom(order.getCreatedOn(), Constants.DATETIME_CSV_FORMAT, timezone)
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            order.getUpdatedOn() != null ? LocalDateUtil.formatCustom(order.getUpdatedOn(), Constants.DATETIME_CSV_FORMAT, timezone)
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(order.getUpdatedBy() != null ? StringEscapeUtils.escapeCsv(order.getUpdatedBy()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(subCustomId != null ? StringEscapeUtils.escapeCsv(subCustomId)
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(subFullName != null ? StringEscapeUtils.escapeCsv(subFullName)
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            order.getStatusUpdatedOn() != null ? LocalDateUtil.formatCustom(order.getStatusUpdatedOn(), Constants.DATETIME_CSV_FORMAT, timezone)
                : CharacterConstants.EMPTY);

    return crUpSb;
  }

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    FieldsConfig fc = null;
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", locale);
    StringBuilder headerSb = new StringBuilder();
    headerSb.append(messages.getString("order")).append(CharacterConstants.COMMA)
        .append(messages.getString("referenceid")).append(CharacterConstants.COMMA)
        .append(messages.getString("status")).append(CharacterConstants.COMMA)
        .append(messages.getString("customer")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.customer")).append(CharacterConstants.COMMA)
        .append(messages.getString("customer")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("name.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("vendor")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.vendor")).append(CharacterConstants.COMMA)
        .append(messages.getString("vendor")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("name.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("item")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.material")).append(CharacterConstants.COMMA)
        .append(messages.getString("item")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("name.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("order.reasons")).append(CharacterConstants.COMMA)
        .append(messages.getString("quantity")).append(CharacterConstants.COMMA);
    if (!dc.isDisableOrdersPricing()) {
      headerSb.append(messages.getString("currency")).append(CharacterConstants.COMMA)
          .append(messages.getString("item")).append(CharacterConstants.SPACE)
          .append(jsMessages.getString("price.lower")).append(CharacterConstants.COMMA)
          .append(jsMessages.getString("discount.upper")).append(CharacterConstants.SPACE)
          .append(CharacterConstants.O_BRACKET)
          .append(CharacterConstants.PERCENT).append(CharacterConstants.C_BRACKET)
          .append(CharacterConstants.COMMA)
          .append(messages.getString("amount")).append(CharacterConstants.COMMA)
          .append(messages.getString("order")).append(CharacterConstants.SPACE)
          .append(jsMessages.getString("price.lower")).append(CharacterConstants.COMMA)
          .append(messages.getString("tax")).append(CharacterConstants.SPACE)
          .append(CharacterConstants.O_BRACKET).append(CharacterConstants.PERCENT)
          .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA);
      if (dc.isAccountingEnabled()) {
        headerSb.append(messages.getString("payment.paymentoption"))
            .append(CharacterConstants.COMMA)
            .append(messages.getString("payment.paidsofar")).append(CharacterConstants.COMMA)
            .append(jsMessages.getString("paid.upper")).append(CharacterConstants.SPACE)
            .append(jsMessages.getString("status.lower")).append(CharacterConstants.COMMA)
            .append(messages.getString("payment")).append(CharacterConstants.SPACE)
            .append(jsMessages.getString("history.lower")).append(CharacterConstants.COMMA);
      }
    }
    headerSb.append(messages.getString("order.processingtime")).append(CharacterConstants.SPACE)
        .append(CharacterConstants.O_BRACKET)
        .append(messages.getString("hours")).append(CharacterConstants.C_BRACKET)
        .append(CharacterConstants.COMMA)
        .append(messages.getString("order.deliveryleadtime")).append(CharacterConstants.SPACE)
        .append(CharacterConstants.O_BRACKET)
        .append(messages.getString("hours")).append(CharacterConstants.C_BRACKET)
        .append(CharacterConstants.COMMA)
        .append(messages.getString("country")).append(CharacterConstants.COMMA)
        .append(messages.getString("state")).append(CharacterConstants.COMMA)
        .append(messages.getString("district")).append(CharacterConstants.COMMA)
        .append(messages.getString("taluk")).append(CharacterConstants.COMMA)
        .append(messages.getString("village")).append(CharacterConstants.COMMA)
        .append(messages.getString("streetaddress")).append(CharacterConstants.COMMA)
        .append(messages.getString("zipcode")).append(CharacterConstants.COMMA)
        .append(messages.getString("latitude")).append(CharacterConstants.COMMA)
        .append(messages.getString("longitude")).append(CharacterConstants.COMMA)
        .append(messages.getString("accuracy")).append(CharacterConstants.SPACE)
        .append(CharacterConstants.O_BRACKET)
        .append(messages.getString("meters")).append(CharacterConstants.C_BRACKET)
        .append(CharacterConstants.COMMA)
        .append(jsMessages.getString("gps")).append(CharacterConstants.SPACE)
        .append(messages.getString("errors.small")).append(CharacterConstants.COMMA)
        .append(messages.getString("material")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("tags.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("kiosk")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("tags.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("order")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("tags.lower")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("createdby")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("createdby")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("customid.lower")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("createdby")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("fullname.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("createdon")).append(CharacterConstants.COMMA)
        .append(messages.getString("updatedon")).append(CharacterConstants.COMMA)
        .append(messages.getString("status")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedby.lower"))
        .append(CharacterConstants.SPACE).append(jsMessages.getString("id"))
        .append(CharacterConstants.COMMA)
        .append(messages.getString("status")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedby.lower"))
        .append(CharacterConstants.SPACE).append(jsMessages.getString("customid.lower"))
        .append(CharacterConstants.COMMA)
        .append(messages.getString("status")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedby.lower"))
        .append(CharacterConstants.SPACE).append(jsMessages.getString("fullname.lower"))
        .append(CharacterConstants.COMMA)
        .append(messages.getString("status")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedon.lower"));

				/*if ( fc != null && !fc.isEmpty() ) {
                                        Iterator<String> fields = fc.keys();
					while ( fields.hasNext() ) {
						String field = fields.next();
						FieldsConfig.Field f = fc.getField( field );
						header += "," + f.name;
					}
				}*/
    return headerSb.toString();
  }
}
