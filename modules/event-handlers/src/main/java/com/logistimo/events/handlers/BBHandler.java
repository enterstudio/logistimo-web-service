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

/**
 *
 */
package com.logistimo.events.handlers;

import com.ibm.icu.util.Calendar;
import com.logistimo.AppFactory;
import com.logistimo.dao.JDOUtils;
import com.logistimo.events.models.ObjectData;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.entity.IBBoard;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.generators.EventGenerator;
import com.logistimo.events.generators.EventGeneratorFactory;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.impl.PMF;
import com.logistimo.constants.Constants;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.QueryUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Handler for the bulletin board
 *
 * @author Arun
 */
public class BBHandler {

  // Logging
  private static final XLog xLogger = XLog.getLog(BBHandler.class);

  private static ITaskService taskService = AppFactory.get()
      .getTaskService();

  // Add to the bulletin board
  public static Long add(IBBoard bboard) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Add this object to this domain and parent domains (superdomains)
      IBBoard
          bb =
          (IBBoard) DomainsUtil.addToDomain(bboard, bboard.getDomainId(),
              pm); /// earlier: BBoard bb = pm.makePersistent( bboard );
      return bb.getKey();
    } catch (Exception e) {
      xLogger.severe("Error in persisting bulletin board:", e);
    } finally {
      pm.close();
    }
    return null;
  }

  public static Long add(String message, IEvent event, ObjectData od) {
    IBBoard bb = JDOUtils.createInstance(IBBoard.class);
    bb.setDomainId(event.getDomainId());
    bb.setEventId(event.getId());
    bb.setMessage(message);
    bb.setTimestamp(new Date());
    bb.setType(event.getObjectType());
    bb.setEventKey(event.getKey());
    bb.setUserId(Constants.SYSTEM_ID);
    if (od != null) {
      bb.setKioskId(od.kioskId);
      bb.setCity(od.city);
      bb.setDistrict(od.district);
      bb.setState(od.state);
      bb.setTags(od.tags);
    }
    // Add
    return add(bb);
  }

  // Get BB items for the given duration
  @SuppressWarnings("unchecked")
  public static Results getItems(Long domainId, Date start, PageParams pageParams) {
    xLogger.fine("Entered getItems");
    List<IBBoard> items = null;
    String cursor = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(JDOUtils.getImplClass(IBBoard.class));
    String filter = "dId.contains(dIdParam)";
    String declaration = "Long dIdParam";
    String importStr = null;
    if (start != null) {
      filter += "&& t > startParam";
      declaration += ", Date startParam";
      importStr = " import java.util.Date;";
    }
    q.setFilter(filter);
    q.declareParameters(declaration);
    if (importStr != null) {
      q.declareImports(importStr);
    }
    q.setOrdering("t desc");
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      if (start != null) {
        items =
            (List<IBBoard>) q
                .execute(domainId, LocalDateUtil.getOffsetDate(start, -1, Calendar.MILLISECOND));
      } else {
        items = (List<IBBoard>) q.execute(domainId);
      }
      if (items != null) {
        items.size();
        cursor = QueryUtil.getCursor(items);
        items = (List<IBBoard>) pm.detachCopyAll(items);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    xLogger.fine("Exiting getItems");
    return new Results(items, cursor);
  }

  // Get valid items, wherein the corresponding events are valid
  // NOTE: INACTIVE & EXPIRED events should not be repeated on the board - so such handling is done here as well
  public static void removeInvalidItems(List<IBBoard> items) {
    xLogger.fine("Entered removeInvalidItems");
    if (items == null || items.isEmpty()) {
      return;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<Long> itemsToDelete = new ArrayList<Long>();
    List<String> similarItems = new ArrayList<String>(); // List of eventId.objectType.objectId
    Iterator<IBBoard> it = items.iterator(); // NOTE: this list is in descending order of time
    try {

      while (it.hasNext()) {
        IBBoard bb = it.next();
        Long bbKey = bb.getKey();
        // Get the corresponding event, if available
        Long eventKey = bb.getEventKey();
        if (eventKey == null) {
          continue;
        }
        // Get the corresponding event
        EventGenerator eg = null;
        try {
          IEvent event = JDOUtils.getObjectById(IEvent.class, eventKey, pm);
          String objectType = event.getObjectType();
          String objectId = event.getObjectId();
          Long domainId = event.getDomainId();
          // Remove an item, if represents the same event on the same object (keep the most recent one)
          String itemHandle = event.getId() + "." + objectType;
          if (objectId != null) {
            itemHandle += "." + objectId;
          }
          if (similarItems.contains(itemHandle)) {
            // Remove from BB and list
            if (!itemsToDelete.contains(bbKey)) {
              itemsToDelete.add(bbKey);
              it.remove();
              continue;
            }
          } else {
            similarItems.add(itemHandle);
          }
          // Get the event generator
          eg = EventGeneratorFactory.getEventGenerator(domainId, objectType);
          // Check if this event is still valid
          if (!eg.isEventValid(event, pm)) {
            // Remove from BB and list
            itemsToDelete.add(bbKey);
            it.remove();
          }
        } catch (JDOObjectNotFoundException e) {
          // Remove this element
          itemsToDelete.add(bbKey);
          it.remove();
        }
      }
    } finally {
      pm.close();
    }
    // Remove BBoard items, if needed
    if (!itemsToDelete.isEmpty()) {
      // Get CSV list of item IDs
      String itemIdsCSV = "";
      Iterator<Long> itIds = itemsToDelete.iterator();
      while (itIds.hasNext()) {
        if (!itemIdsCSV.isEmpty()) {
          itemIdsCSV += ",";
        }
        itemIdsCSV += itIds.next();
      }
      // Schedule task to remove items
      Map<String, String> params = new HashMap<String, String>();
      params.put("action", "rm");
      params.put("itemids", itemIdsCSV);
      try {
        taskService.schedule(taskService.QUEUE_MESSAGE, "/task/bboardmgr", params,
            taskService.METHOD_POST);
      } catch (Exception e) {
        xLogger
            .severe("{0} when scheduling task to delete items from BB: {1}", e.getClass().getName(),
                e.getMessage());
      }
    }
    xLogger.fine("Exiting removeInvalidItems");
  }

  // Remove the items with specified criteria from the board
  public static void remove(List<Long> bbItemIds) {
    AppFactory.get().getDaoUtil().removeBBItems(bbItemIds);
  }
}
