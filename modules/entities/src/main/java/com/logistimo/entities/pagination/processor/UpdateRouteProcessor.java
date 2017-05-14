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

package com.logistimo.entities.pagination.processor;

import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.entities.entity.IUserToKiosk;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.jdo.PersistenceManager;

public class UpdateRouteProcessor implements Processor {
  private static final XLog xLogger = XLog.getLog(UpdateRouteProcessor.class);

  public String process(Long domainId, Results results, String routeQueryString,
                        PersistenceManager pm) throws ProcessingException {
    xLogger.fine("Entering process");
    try {
      if (results == null) {
        xLogger.fine("results is null");
        return routeQueryString;
      }
      // Get results
      @SuppressWarnings("rawtypes")
      List list = results.getResults();
      if (list == null || list.isEmpty()) {
        return routeQueryString;
      }
      xLogger.fine("LIST: {0}, routeQueryString: {1}", list, routeQueryString);
      // Get the first item in the results list and check it's instance
      boolean isUserToKiosk = list.get(0) instanceof IUserToKiosk;

      // Get prevOutput which is the routeQueryString ( in the format, tag=<tagname>&routecsv=<comma separated list of routed kids>&noroutecsv=<comma separated list of kids that are not in a route and whose route index should be set to default> )
      if (routeQueryString == null || routeQueryString.isEmpty()) {
        return null;
      }

      // Create a RouteData object from the routeQueryString
      RouteData routeData = new RouteData(routeQueryString);

      // Print routeData
      xLogger.fine("Printing routeData: tag: {0}, routecsv: {1}, noroutecsv: {2}", routeData.tag,
          routeData.routecsv, routeData.noroutecsv);
      String routeTag = routeData.tag;
      String routeCSV = routeData.routecsv;
      String norouteCSV = routeData.noroutecsv;

      // Obtain a list of routed kids (Long)
      List<Long> routedKids = null;
      if (routeCSV != null && !routeCSV.isEmpty()) {
        List<String> strRoutedKids = StringUtil.getList(routeCSV);
        Iterator<String> strRoutedKidsIter = strRoutedKids.iterator();
        routedKids = new ArrayList<Long>();
        while (strRoutedKidsIter.hasNext()) {
          Long kid = Long.valueOf(strRoutedKidsIter.next());
          routedKids.add(kid);
        }
      }
      // Obtain a list of unrouted Kids(Long), called norouteKids.
      List<Long> noRouteKids = null;
      if (norouteCSV != null && !norouteCSV.isEmpty()) {
        List<String> strNoRouteKids = StringUtil.getList(norouteCSV);
        Iterator<String> strNoRouteKidsIter = strNoRouteKids.iterator();
        noRouteKids = new ArrayList<Long>();
        while (strNoRouteKidsIter.hasNext()) {
          Long kid = Long.valueOf(strNoRouteKidsIter.next());
          noRouteKids.add(kid);
        }
      }

      if (isUserToKiosk) {
        // UpdateRoute
        // Parse the list of UserToKiosk objects
        // Obtain kid for each element
        // Obtain index of the kid from the kids list and set the rank.
        @SuppressWarnings("unchecked")
        Iterator<IUserToKiosk> listIter = list.iterator();
        while (listIter.hasNext()) {
          IUserToKiosk userToKiosk = listIter.next();
          Long kid = userToKiosk.getKioskId();
          String exisitingTag = userToKiosk.getTag();
          if (kid != null) {
            if (routedKids != null && !routedKids.isEmpty()) {
              // Get the rank from the routedKids list
              int rank = routedKids.indexOf(kid);
              // Without tags before and after
              if (exisitingTag == null && routeTag == null) {
                if (rank != -1) {
                  userToKiosk.setRouteIndex(rank);
                } else {
                  userToKiosk.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
                }
              }

              // Without exisitingTag before now adding it to a tag
              if (exisitingTag == null && routeTag != null && rank != -1) {
                userToKiosk.setTag(routeTag);
                userToKiosk.setRouteIndex(rank);
              }

              // exisitingTag != null and routeTag == null
              // This case can get executed if tags are removed in the configuration and the user tries to
              // route the entities.
              if (exisitingTag != null && routeTag == null) {
                if (rank != -1) {
                  userToKiosk.setRouteIndex(rank);
                  userToKiosk.setTag(routeTag);
                } else {
                  // Set the tag to null and route index to default route index
                  userToKiosk.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
                  userToKiosk.setTag(routeTag);
                }
              }
              if (exisitingTag != null && routeTag != null) {
                if (rank != -1) {
                  if (exisitingTag.equals(routeTag)) {
                    userToKiosk.setRouteIndex(rank);
                  } else {
                    userToKiosk.setRouteIndex(rank);
                    userToKiosk.setTag(routeTag);
                  }
                } else {
                  if (exisitingTag.equals(routeTag)) {
                    userToKiosk.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
                    // set tag to null
                    // userToKiosk.setTag( null ); // Move from Tag list to master list
                  } else {
                    xLogger.fine("Leave it as it is");
                  }
                }
              }
            } else {
              xLogger.fine("tag: {0} , routecsv: {1}, tag is present, routecsv is null or empty.",
                  routeTag, routeCSV);
              // If the existingTag matches with the routeTag set it's route index to default route index and routedKids is null
              if (exisitingTag == null || exisitingTag.isEmpty() || routeTag == null || routeTag
                  .isEmpty() || exisitingTag.equals(routeTag)) {
                // Set the routeIndex to default
                userToKiosk.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
              }
            }
            // If noRouteKids is not null or not empty, set the UserToKiosk object tag to null and route index to default.
            if (noRouteKids != null && !noRouteKids.isEmpty()) {
              // Check if kid is present in norouteKids.
              // If yes, then set the route index to default route index
              // Get the rank from the routedKids list
              int notRouted = noRouteKids.indexOf(kid);
              if (notRouted != -1) {
                userToKiosk.setTag(null);
                userToKiosk.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
              }
            }
            pm.makePersistent(userToKiosk);
          }
        }
      } else {
        // UpdateRoute for KioskLink objects
        // UpdateRoute
        // Parse the list of KioskLink objects
        // Obtain linked kiosk Id for each element
        // Obtain index of the kid from the kids list and set the rank.
        @SuppressWarnings("unchecked")
        Iterator<IKioskLink> listIter = list.iterator();
        while (listIter.hasNext()) {
          IKioskLink kioskLink = listIter.next();
          Long lkid = kioskLink.getLinkedKioskId();
          String exisitingTag = kioskLink.getRouteTag();
          if (lkid != null) {
            if (routedKids != null && !routedKids.isEmpty()) {
              // Get the rank from the routedKids list
              int rank = routedKids.indexOf(lkid);
              // Without tags before and after
              if (exisitingTag == null && routeTag == null) {
                if (rank != -1) {
                  kioskLink.setRouteIndex(rank);
                } else {
                  kioskLink.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
                }
              }

              // Without existingTag before now adding it to a tag
              if (exisitingTag == null && routeTag != null && rank != -1) {
                kioskLink.setRouteTag(routeTag);
                kioskLink.setRouteIndex(rank);
              }

              // existingTag != null and routeTag == null
              // This case can get executed if tags are removed in the configuration and the user tries to
              // route the entities.
              if (exisitingTag != null && routeTag == null) {
                if (rank != -1) {
                  kioskLink.setRouteIndex(rank);
                  kioskLink.setRouteTag(routeTag);
                } else {
                  // Set the tag to null and route index to default route index
                  kioskLink.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
                  kioskLink.setRouteTag(routeTag);
                }
              }
              if (exisitingTag != null && routeTag != null) {
                if (rank != -1) {
                  if (exisitingTag.equals(routeTag)) {
                    kioskLink.setRouteIndex(rank);
                  } else {
                    kioskLink.setRouteIndex(rank);
                    kioskLink.setRouteTag(routeTag);
                  }
                } else {
                  if (exisitingTag.equals(routeTag)) {
                    kioskLink.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
                    // set tag to null
                    // userToKiosk.setTag( null ); // Move from Tag list to master list
                  } else {
                    xLogger.fine("Leave it as it is");
                  }
                }
              }
            } else {
              xLogger.fine("tag: {0} , routecsv: {1}, tag is present, routecsv is null or empty.",
                  routeTag, routeCSV);
              // If the existingTag matches with the routeTag set it's route index to default route index and routedKids is null
              if (exisitingTag == null || exisitingTag.isEmpty() || routeTag == null || routeTag
                  .isEmpty() || exisitingTag.equals(routeTag)) {
                // Set the routeIndex to default
                kioskLink.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
              }
            }
            // If noRouteKids is not null or not empty, set the UserToKiosk object tag to null and route index to default.
            if (noRouteKids != null && !noRouteKids.isEmpty()) {
              // Check if kid is present in norouteKids.
              // If yes, then set the route index to default route index
              // Get the rank from the routedKids list
              int notRouted = noRouteKids.indexOf(lkid);
              if (notRouted != -1) {
                kioskLink.setRouteTag(null);
                kioskLink.setRouteIndex(IUserToKiosk.DEFAULT_ROUTE_INDEX);
              }
            }
            pm.makePersistent(kioskLink);
          }
        }

				/* OLD_CODE
                                @SuppressWarnings("unchecked")
				Iterator<KioskLink> listIter = list.iterator();
				while ( listIter.hasNext() ) {
					KioskLink kioskLink = listIter.next();
					Long kid = kioskLink.getLinkedKioskId();
					if ( kid != null ) {
						int rank = routedKids.indexOf( kid );
						// If the kid is not in the list, then do not set the rank
						if ( rank != -1 ) {
							kioskLink.setRouteIndex( rank );
						} else {
							kioskLink.setRouteIndex( KioskLink.DEFAULT_ROUTE_INDEX ); // Reset the routeIndex to the highest Integer value. If this is not done, previous rank is retained and ordering will be incorrect.
						}
					}
				}*/
      }
    } catch (Exception e) {
      xLogger.severe(
          "{0} when updating route for managed entities or linked kiosks in domain {1}: {2}",
          e.getClass().getName(), domainId, e.getMessage());
    }
    xLogger.fine("Exiting process");
    return routeQueryString;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_DEFAULT;
  }

  private static class RouteData {
    public String tag = null;
    public String routecsv = null;
    public String noroutecsv = null;

    RouteData(String routeQueryString) {
      // Parse the query String here.
      if (routeQueryString != null && !routeQueryString.isEmpty()) {
        // Initialize the member variables
        // Tokenize the string using &
        StringTokenizer st = new StringTokenizer(routeQueryString, "&");
        while (st.hasMoreTokens()) {
          String token = st.nextToken();
          xLogger.fine("token: " + token);
          String[] tokenNameValue = getTokenNameValue(token);
          if (tokenNameValue.length == 2) {
            if (tokenNameValue[0].equals("tag")) {
              this.tag = tokenNameValue[1];
            }
            if (tokenNameValue[0].equals("routecsv")) {
              this.routecsv = tokenNameValue[1];
            }
            if (tokenNameValue[0].equals("noroutecsv")) {
              this.noroutecsv = tokenNameValue[1];
            }
          }
        }
      }
      xLogger.fine("Printing inside RouteData: tag: {0}, routecsv: {1}, noroutecsv: {2}", tag,
          routecsv, noroutecsv);
    }

    String[] getTokenNameValue(String token) {
      if (token == null || token.isEmpty()) {
        return null;
      }
      String[] tokenNameValue = token.split("=");
      return tokenNameValue;
    }
  }
}
