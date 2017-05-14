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
import com.logistimo.config.utils.DomainConfigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.events.entity.Event;
import com.logistimo.events.entity.IEvent;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.utils.StringUtil;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class InvntryExportHandler implements IExportHandler {

    private static final XLog xLogger = XLog.getLog(InvntryExportHandler.class);

    IInvntry invntry;
    private IInvntryDao invDao = new InvntryDao();

    public InvntryExportHandler(IInvntry invntry) {
        this.invntry = invntry;
    }

    @Override
    public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
        xLogger.fine("Entering toCSV. locale: {0}, timezone: {1}", locale, timezone);
        try {
            // Get services
            EntitiesService as = Services.getService(EntitiesServiceImpl.class);
            UsersServiceImpl us = Services.getService(UsersServiceImpl.class);
            MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
            InventoryManagementService
                    ims =
                    Services.getService(InventoryManagementServiceImpl.class);

            IKiosk k = as.getKiosk(invntry.getKioskId(), false);
            IMaterial m = mcs.getMaterial(invntry.getMaterialId());
            String ubFullName = null, ubCustomId = null;
            if (invntry.getUpdatedBy() != null) {
                try {
                    IUserAccount ubUser = us.getUserAccount(invntry.getUpdatedBy());
                    ubFullName = ubUser.getFullName();
                    ubCustomId = ubUser.getCustomId();
                } catch (Exception e) {
                    ubFullName = Constants.UNKNOWN;
                }
            }

            List<String> ktgs = invntry.getTags(TagUtil.TYPE_ENTITY);
            List<String> mtgs = invntry.getTags(TagUtil.TYPE_MATERIAL);

            StringBuilder csv = new StringBuilder();
            csv.append(invntry.getKioskId()).append(CharacterConstants.COMMA)
                    .append(k.getCustomId() != null ? StringEscapeUtils.escapeCsv(k.getCustomId())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(StringEscapeUtils.escapeCsv(k.getName())).append(CharacterConstants.COMMA)
                    .append(invntry.getMaterialId()).append(CharacterConstants.COMMA)
                    .append(m.getCustomId() != null ? StringEscapeUtils.escapeCsv(m.getCustomId())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(StringEscapeUtils.escapeCsv(m.getName())).append(CharacterConstants.COMMA)
                    .append(k.getCountry() != null ? StringEscapeUtils.escapeCsv(k.getCountry())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(k.getState() != null ? StringEscapeUtils.escapeCsv(k.getState())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(k.getDistrict() != null ? StringEscapeUtils.escapeCsv(k.getDistrict())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(k.getTaluk() != null ? StringEscapeUtils.escapeCsv(k.getTaluk())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(k.getCity() != null ? StringEscapeUtils.escapeCsv(k.getCity())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(k.getStreet() != null ? StringEscapeUtils.escapeCsv(k.getStreet())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(k.getPinCode() != null ? StringEscapeUtils.escapeCsv(k.getPinCode())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(k.getLatitude()).append(CharacterConstants.COMMA)
                    .append(k.getLongitude()).append(CharacterConstants.COMMA)
                    .append(k.getGeoAccuracy()).append(CharacterConstants.COMMA)
                    .append(k.getGeoError() != null ? StringEscapeUtils.escapeCsv(k.getGeoError())
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(ktgs != null && !ktgs.isEmpty() ? StringEscapeUtils
                            .escapeCsv(StringUtil.getCSV(ktgs, CharacterConstants.SEMICOLON))
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(mtgs != null && !mtgs.isEmpty() ? StringEscapeUtils
                            .escapeCsv(StringUtil.getCSV(mtgs, CharacterConstants.SEMICOLON))
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getStock())).append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getReorderLevel())).append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getMaxStock())).append(CharacterConstants.COMMA);
            try {
                IInvntryEvntLog lastEventLog = invDao.getInvntryEvntLog(invntry);
                if (lastEventLog != null && invntry.getStockEvent() != IEvent.NORMAL) {
                    csv.append(
                            (lastEventLog != null && invntry.getStockEvent() != IEvent.NORMAL)
                                    ? Event.getEventName(invntry.getStockEvent(), locale)
                                    : CharacterConstants.EMPTY).append(CharacterConstants.COMMA);
                    csv.append((System.currentTimeMillis() - lastEventLog.getStartDate().getTime())
                            / LocalDateUtil.MILLISECS_PER_DAY).append(CharacterConstants.COMMA);
                } else {
                    csv.append(CharacterConstants.COMMA).append(CharacterConstants.COMMA);
                }
            } catch (Exception e) {
                xLogger.warn("Exception while getting abnormality type and duration", e);
            }
            if (!dc.getInventoryConfig().isMinMaxAbsolute()) {
                csv.append(BigUtil.getFormattedValue(invntry.getMinDuration())).append(
                        CharacterConstants.COMMA)
                        .append(BigUtil.getFormattedValue(invntry.getMaxDuration())).append(CharacterConstants.COMMA);
            }
            csv.append(BigUtil.getFormattedValue(invntry.getRetailerPrice())).append(CharacterConstants.COMMA)
                    .append(invntry.getInventoryModel() != null ?
                            StringEscapeUtils.escapeCsv(invntry.getInventoryModel()) : CharacterConstants.EMPTY)
                    .append(CharacterConstants.COMMA)
                    .append(NumberUtil.getFormattedValue(invntry.getServiceLevel())).append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(ims.getStockAvailabilityPeriod(invntry, dc)))
                    .append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getConsumptionRateDaily()))
                    .append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getConsumptionRateWeekly()))
                    .append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getConsumptionRateMonthly()))
                    .append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getRevPeriodDemand())).append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getSafetyStock())).append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getEconomicOrderQuantity()))
                    .append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getLeadTimeDemand())).append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getLeadTime())).append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getOrderPeriodicity())).append(CharacterConstants.COMMA)
                    .append(BigUtil.getFormattedValue(invntry.getStdevRevPeriodDemand()))
                    .append(CharacterConstants.COMMA)
                    .append(
                            invntry.getCreatedOn() != null ?
                                    LocalDateUtil.formatCustom(invntry.getCreatedOn(), Constants.DATETIME_CSV_FORMAT, timezone)
                                    : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(invntry.getUpdatedBy() != null ?
                            invntry.getUpdatedBy() : CharacterConstants.EMPTY).append(
                    CharacterConstants.COMMA)
                    .append(ubCustomId != null ? StringEscapeUtils.escapeCsv(ubCustomId)
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(ubFullName != null ? StringEscapeUtils.escapeCsv(ubFullName)
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(invntry.getTimestamp() != null ?
                            LocalDateUtil.formatCustom(invntry.getTimestamp(), Constants.DATETIME_CSV_FORMAT, timezone)
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(invntry.getReorderLevelUpdatedTime() != null ? LocalDateUtil
                            .formatCustom(invntry.getReorderLevelUpdatedTime(), Constants.DATETIME_CSV_FORMAT, timezone)
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(invntry.getMaxUpdatedTime() != null ? LocalDateUtil
                            .formatCustom(invntry.getMaxUpdatedTime(), Constants.DATETIME_CSV_FORMAT, timezone)
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(invntry.getRetailerPriceUpdatedTime() != null ? LocalDateUtil
                            .formatCustom(invntry.getRetailerPriceUpdatedTime(), Constants.DATETIME_CSV_FORMAT, timezone)
                            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(
                            invntry.getPSTimestamp() != null ? LocalDateUtil.formatCustom(invntry.getPSTimestamp(), Constants.DATETIME_CSV_FORMAT, timezone)
                                    : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                    .append(
                            invntry.getDQTimestamp() != null ?
                                    LocalDateUtil.formatCustom(invntry.getDQTimestamp(), Constants.DATETIME_CSV_FORMAT, timezone)
                                    : CharacterConstants.EMPTY).append(CharacterConstants.COMMA);
            if (dc.autoGI()) {
                csv.append(BigUtil.getFormattedValue(invntry.getAllocatedStock())).append(CharacterConstants.COMMA)
                        .append(BigUtil.getFormattedValue(invntry.getInTransitStock()));
            }
            return csv.toString();
        } catch (Exception e) {
            xLogger.warn("{0} when getting CSV for inventory {1}: {2}", e.getClass().getName(), invntry.getKey(),
                    e.getMessage());
            return null;
        }
    }

    public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
        xLogger.fine("Entering getCSVHeader. locale: {0}", locale);
        ResourceBundle messages = Resources.get().getBundle("Messages", locale);
        ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", locale);
        StringBuilder header = new StringBuilder();
        String mcrUnits = invntry.getMCRUnits(locale);
        String mmd = DomainConfigUtil.getMinMaxDuration(dc);
        header.append(messages.getString("kiosk")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
                .append(messages.getString("customid.entity")).append(CharacterConstants.COMMA)
                .append(messages.getString("kiosk.name")).append(CharacterConstants.COMMA)
                .append(messages.getString("material")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
                .append(messages.getString("customid.material")).append(CharacterConstants.COMMA)
                .append(messages.getString("material.name")).append(CharacterConstants.COMMA)
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
                .append(CharacterConstants.O_BRACKET).append(messages.getString("meters"))
                .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
                .append(jsMessages.getString("gps")).append(CharacterConstants.SPACE)
                .append(messages.getString("errors.small")).append(CharacterConstants.COMMA)
                .append(messages.getString("kiosk")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("tags.lower")).append(CharacterConstants.COMMA)
                .append(messages.getString("material")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("tags.lower")).append(CharacterConstants.COMMA)
                .append(messages.getString("material.stockonhand")).append(CharacterConstants.COMMA)
                .append(messages.getString("inventory.reorderlevel")).append(CharacterConstants.COMMA)
                .append(messages.getString("max")).append(CharacterConstants.COMMA)
                .append(jsMessages.getString("abnormality.type")).append(CharacterConstants.COMMA)
                .append(jsMessages.getString("abnormality.duration") + "-" + messages.getString("days")).append(CharacterConstants.COMMA);

        if (!mmd.isEmpty()) {
            header.append(messages.getString("inventory.reorderlevel")).append(mmd)
                    .append(CharacterConstants.COMMA)
                    .append(messages.getString("max")).append(mmd).append(CharacterConstants.COMMA);
        }

        header.append(messages.getString("material.retailerprice")).append(CharacterConstants.COMMA)
                .append(messages.getString("inventory.policy")).append(CharacterConstants.COMMA)
                .append(messages.getString("inventory.servicelevel")).append(CharacterConstants.COMMA)
                .append(mcrUnits).append(CharacterConstants.COMMA)
                .append(messages.getString("config.consumptionrates")).append(CharacterConstants.F_SLASH)
                .append(messages.getString("day")).append(CharacterConstants.COMMA)
                .append(messages.getString("config.consumptionrates")).append(CharacterConstants.F_SLASH)
                .append(messages.getString("week")).append(CharacterConstants.COMMA)
                .append(messages.getString("config.consumptionrates")).append(CharacterConstants.F_SLASH)
                .append(messages.getString("month")).append(CharacterConstants.COMMA)
                .append(messages.getString("demandforecast")).append(CharacterConstants.COMMA)
                .append(messages.getString("inventory.safetystock")).append(CharacterConstants.SPACE)
                .append(CharacterConstants.O_BRACKET).append(jsMessages.getString("computed.lower"))
                .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
                .append(messages.getString("order.optimalorderquantity")).append(CharacterConstants.COMMA)
                .append(messages.getString("order.leadtimedemand")).append(CharacterConstants.COMMA)
                .append(messages.getString("order.leadtime")).append(CharacterConstants.SPACE)
                .append(messages.getString("days")).append(CharacterConstants.COMMA)
                .append(messages.getString("order.periodicity")).append(CharacterConstants.SPACE)
                .append(messages.getString("days")).append(CharacterConstants.COMMA)
                .append(messages.getString("order.stddevleadtime")).append(CharacterConstants.COMMA)
                .append(messages.getString("createdon")).append(CharacterConstants.COMMA)
                .append(messages.getString("updatedby")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
                .append(messages.getString("updatedby")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("customid.lower")).append(CharacterConstants.COMMA)
                .append(messages.getString("updatedby")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("fullname.lower")).append(CharacterConstants.COMMA)
                .append(messages.getString("updatedon")).append(CharacterConstants.COMMA)
                .append(messages.getString("min")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
                .append(messages.getString("max")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
                .append(messages.getString("material.retailerprice")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
                .append(jsMessages.getString("pands")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
                .append(jsMessages.getString("dandq")).append(CharacterConstants.SPACE)
                .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA);
        if (dc.autoGI()) {
            header.append("Allocated stock").append(CharacterConstants.COMMA)
                    .append("In transit stock");
        }

        xLogger.fine("Exiting getCSVHeader");
        return header.toString();
    }


}
