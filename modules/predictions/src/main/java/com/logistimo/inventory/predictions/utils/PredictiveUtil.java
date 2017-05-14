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

package com.logistimo.inventory.predictions.utils;

import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util for calculation predicted number of days based on available orders for a given inventory.
 *
 * @author Mohan Raja
 */
public class PredictiveUtil {

  private static final int PREDICTIVE_RT = ConfigUtil.getInt("predictive.response.time", 2);
  private static final int PREDICTIVE_LT = ConfigUtil.getInt("predictive.lead.time", 5);
  private static final int PREDICTIVE_BLT = ConfigUtil.getInt("predictive.buffer.lead.time", 3);
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

  /**
   * Fetches all demand items for given inventory with status {@code IOrder.COMPLETED and IOrder.CONFIRMED}
   *
   * @param inv Inventory
   * @return Map [date - DemandItem ]
   */
  private static Map<String, IDemandItem> getDemandItems(IInvntry inv)
      throws ServiceException, ObjectNotFoundException, ParseException {
    OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
    List<String> status = Arrays.asList(IOrder.COMPLETED, IOrder.CONFIRMED);
    List<IDemandItem>
        demandItems =
        oms.getDemandItemByStatus(inv.getKioskId(), inv.getMaterialId(), status);
    Map<String, IDemandItem> demandMap = new HashMap<>();
    if (demandItems != null) {
      for (IDemandItem dm : demandItems) {
        if (dm.getStatus().equals(IOrder.COMPLETED) || dm.getStatus().equals(IOrder.CONFIRMED)) {
          demandMap.put(getStatusUpdate(dm), dm);
        }
      }
    }
    return demandMap;
  }

  /**
   * Fetches all demand item for given inventory with expected quantity to receive by date
   *
   * @param inv Inventory
   * @return Map [ Date - Order Quantity ]
   */
  public static Map<String, BigDecimal> getOrderStkPredictions(IInvntry inv)
      throws ServiceException, ObjectNotFoundException, ParseException {
    Map<String, IDemandItem> demandItems = getDemandItems(inv);
    Map<String, BigDecimal> orders = new HashMap<>(demandItems.size());
    for (String s : demandItems.keySet()) {
      Calendar resDate = getPredictiveDate(demandItems.get(s).getStatus(), s);
      orders.put(sdf.format(resDate.getTime()), demandItems.get(s).getQuantity());
    }
    return orders;
  }

  /**
   * Gets the last status updated date if available, else returns created date.
   *
   * @param dm Demand Item
   * @return status update date
   */
  private static String getStatusUpdate(IDemandItem dm)
      throws ServiceException, ObjectNotFoundException {
    OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
    IOrder order = oms.getOrder(dm.getOrderId());
    return sdf.format(
        order.getStatusUpdatedOn() != null ? order.getStatusUpdatedOn() : order.getCreatedOn());
  }

  /**
   * Checks whether given date is valid to consider for predicting days of stock.
   * Returns true when days between current date and {@code resDate} is >= {stkAvailableDays}
   * It also considers buffer days {@code PREDICTIVE_BLT}, expected delay in receiving order excluding lead time.
   *
   * @param resDate          Date to be validated
   * @param stkAvailableDays Days of stock available currently
   * @param cr               Consumption Rate
   * @return boolean
   */
  private static boolean isDateValid(Calendar resDate, BigDecimal stkAvailableDays, BigDecimal cr) {
    if (BigUtil.greaterThanZero(cr)) {
      int
          dayDiff =
          LocalDateUtil.daysBetweenDates(LocalDateUtil.resetTimeFields(new GregorianCalendar()),
              LocalDateUtil.resetTimeFields(resDate));
      return dayDiff >= -PREDICTIVE_BLT && BigUtil
          .greaterThanEquals(stkAvailableDays.setScale(0, BigDecimal.ROUND_HALF_UP), dayDiff);
    }
    return false;
  }

  /**
   * Given status and its last updated date, returns date when it gets delivered to customer
   *
   * @param status          Either {@code IOrder.CONFIRMED or IOrder.COMPLETED}
   * @param statusUpdatedOn Last status updated date
   * @return Date
   */
  private static Calendar getPredictiveDate(String status, String statusUpdatedOn)
      throws ParseException {
    int time = PREDICTIVE_LT;
    if (IOrder.CONFIRMED.equals(status)) {
      time += PREDICTIVE_RT;
    }
    Calendar resDate = new GregorianCalendar();
    resDate.setTime(sdf.parse(statusUpdatedOn));
    resDate.add(Calendar.DAY_OF_MONTH, time);
    return resDate;
  }


}
