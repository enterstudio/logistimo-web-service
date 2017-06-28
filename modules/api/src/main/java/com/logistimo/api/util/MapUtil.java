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
package com.logistimo.api.util;

import com.logistimo.dao.JDOUtils;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;

import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;

import com.logistimo.api.util.MapUtil.GeoData.Point;

import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

/**
 * @author Arun
 */
public class MapUtil {

  private static final XLog xLogger = XLog.getLog(MapUtil.class);

  /**
   * Get mappable kiosk data (JS array in String format), given a list of kiosks
   * Format: [ [<kioskid>, <kioskName>, <lat>, <long> ], ..., [<lat-north>,<lng-east>,<lat-south>,<lng-west>] ] // last array is metadata
   */
  public static GeoData getMappableKiosks(List<IKiosk> kiosks) {
    GeoData geoData = new GeoData();
    if (kiosks != null && kiosks.size() > 0) {
      // Init. bounding box coordinates (required for centering the map to this box)
      double latNorth = 0, lngEast = 0, latSouth = 0, lngWest = 0;
      boolean justStarted = true;
      for (IKiosk k : kiosks) {
        double lat = k.getLatitude();
        double lng = k.getLongitude();

        // If lat/lng are 0, then do not add
        if (lat == 0 && lng == 0) {
          continue;
        }

        if (justStarted) {
          latNorth = latSouth = lat;
          lngEast = lngWest = lng;
          justStarted = false;
        } else {
          // Get the north east, and south west corners of the bounding box of all points
          if (lat > latNorth) {
            latNorth = lat;
          } else if (lat < latSouth) {
            latSouth = lat;
          }
          if (lng > lngEast) {
            lngEast = lng;
          } else if (lng < lngWest) {
            lngWest = lng;
          }
        }
        // Get kiosk name (along with location)
        String title = k.getName() + ", " + k.getCity();
        // Update kiosk location in array
        geoData.points.add(new Point(title, lat, lng, k.getGeoAccuracy(), k, null, null, null));
        geoData.size++;
      } // end for
      // Add the metadata array at the end (in this case, the lat-long bounds)
      if (geoData.points.size() >= 2) {
        geoData.latLngBounds =
            "[" + latNorth + "," + lngEast + "," + latSouth + "," + lngWest + "]";
      }
    }
    return geoData;
  }

  /**
   * Get mappable kiosk data (JS array in String format), given a list of inventory
   * Format: [ [<kioskid>, <kioskName>, <lat>, <long>, <stock>, <safetyStock> ], ..., [<lat-north>,<lng-east>,<lat-south>,<lng-west>,<max-stock>] ]
   */
  public static GeoData getMappableKiosksByInventory(List<IInvntry> inventories,
                                                     List<IKiosk> includeKiosks) {
    GeoData geoData = new GeoData();
    if (inventories != null && inventories.size() > 0) {
      // Get the accounts service
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      // Init. max stock (required for scaling the markers)
      BigDecimal maxStock = BigDecimal.ZERO;
      // Init. bounding box coordinates (required for centering the map to this box)
      double latNorth = 0, lngEast = 0, latSouth = 0, lngWest = 0;
      boolean justStarted = true;
      for (IInvntry inv : inventories) {
        BigDecimal stockOnHand = inv.getStock();
        // Get the max stock until now
        if (BigUtil.greaterThan(stockOnHand, maxStock)) {
          maxStock = stockOnHand;
        }
        // Get kiosk
        try {
          IKiosk k = as.getKiosk(inv.getKioskId(), false);
          // Check if this kiosk is to be included or not (say, given permissions or other constraints)
          if (includeKiosks != null && !includeKiosks.contains(k)) {
            continue;
          }
          // Get coordinates
          double lat = k.getLatitude();
          double lng = k.getLongitude();

          // If lat/lng are 0, then do not add
          if (lat == 0 && lng == 0) {
            continue;
          }

          if (justStarted) {
            latNorth = latSouth = lat;
            lngEast = lngWest = lng;
            justStarted = false;
          } else {
            // Get the north east, and south west corners of the bounding box of all points
            if (lat > latNorth) {
              latNorth = lat;
            } else if (lat < latSouth) {
              latSouth = lat;
            }
            if (lng > lngEast) {
              lngEast = lng;
            } else if (lng < lngWest) {
              lngWest = lng;
            }
          }
          // Get title
          String title = k.getName() + ", " + k.getCity();
          geoData.points.add(
              new Point(title, lat, lng, k.getGeoAccuracy(), k, inv, inv.getKeyString(), null));
          geoData.size++;
        } catch (Exception e) {
          xLogger
              .warn("{0} when getting kiosk {1}: {2}", e.getClass().getName(), inv.getKioskId(),
                  e.getMessage());
        }
      } // end for
      geoData.maxValue = maxStock;
      // Add the metadata
      if (geoData.points.size() >= 2) {
        geoData.latLngBounds =
            "[" + latNorth + "," + lngEast + "," + latSouth + "," + lngWest + "]";
      }
    }
    return geoData;
  }


  // Get mappable points by inventory transactions or orders
  // NOTE: 'objects' can have instances of either Transaction or Order
  @SuppressWarnings("rawtypes")
  public static GeoData getMappablePointsByTransactions(List objects, Locale locale,
                                                        String timezone) {
    GeoData geoData = new GeoData();
    if (objects == null || objects.isEmpty()) {
      return geoData;
    }
    xLogger.fine("objects.size: " + objects.size());
    // Get the resource bundle
    ///ResourceBundle messages = Resources.get().getBundle( "Messages", locale );
    boolean isOrders = (objects.get(0) instanceof IOrder);
    // Init. bounding box coordinates (required for centering the map to this box)
    double latNorth = 0, lngEast = 0, latSouth = 0, lngWest = 0;
    boolean justStarted = true;
    Iterator it = objects.iterator();
    double lat = 0, lng = 0, accuracy = 0;
    Date transTimestamp = null;
    String transUid = null;
    String tid = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      while (it.hasNext()) {
        Object o = it.next();
        String title = "";
        if (isOrders) { // order
          IOrder order = ((IOrder) o);
          lat = NumberUtil.getDoubleValue(order.getLatitude());
          lng = NumberUtil.getDoubleValue(order.getLongitude());
          if (lat == 0 && lng == 0) {
            continue;
          }

          accuracy = NumberUtil.getDoubleValue(order.getGeoAccuracy());
          tid = order.getOrderId().toString();
          try {
            IKiosk customer = JDOUtils.getObjectById(IKiosk.class, order.getKioskId(), pm);
            IKiosk vendor = null;
            if (order.getServicingKiosk() != null) {
              vendor = JDOUtils.getObjectById(IKiosk.class, order.getServicingKiosk(), pm);
            }
            title =
                order.size() + " item(s) ordered by " + customer.getName() + ", " + customer
                    .getCity();
            if (vendor != null) {
              title += " from " + vendor.getName() + ", " + vendor.getCity();
            }
            title += " on " + LocalDateUtil.format(order.getCreatedOn(), locale, timezone);
            title += " [Order: " + order.getOrderId() + "]";
          } catch (Exception e) {
            xLogger.warn(
                "{0} when getting customer/vendor for order {1} during geo-data acquisition: {2}",
                e.getClass().getName(), order.getOrderId(), e.getMessage());
          }
        } else { // inventory transaction
          ITransaction trans = ((ITransaction) o);
          lat = trans.getLatitude();
          lng = trans.getLongitude();
          String thisTransUid = trans.getSourceUserId();
          Date thisTransTimestamp = trans.getTimestamp();

          // If no geocodes are available then continue.
          // Else if the user id associated with the transaction and time stamp are the same, then continue, given it must be part of the same transaction.
          if (lat == 0 && lng
              == 0) // If co-ordinates are 0, then no geocoding available for that transaction.
          {
            continue; // if same coordinates as previous transaction, then continue, given must be part of same update
          } else if (thisTransUid != null && !thisTransUid.isEmpty() && thisTransUid
              .equals(transUid)
              && thisTransTimestamp != null && thisTransTimestamp.equals(transTimestamp)) {
            continue;
          }
          transUid = thisTransUid;
          transTimestamp = thisTransTimestamp;

          // lat = thisLat;
          // lng = thisLng;
          accuracy = trans.getGeoAccuracy();
          tid = trans.getKeyString();
          try {
            IKiosk k = JDOUtils.getObjectById(IKiosk.class, trans.getKioskId(), pm);
            IKiosk lk = null;
            if (trans.getLinkedKioskId() != null) {
              lk = JDOUtils.getObjectById(IKiosk.class, trans.getLinkedKioskId(), pm);
            }
            String transType = trans.getType();
            title = TransactionUtil.getDisplayName(transType, locale);
            if (ITransaction.TYPE_ISSUE.equals(transType) || ITransaction.TYPE_TRANSFER
                .equals(transType)) {
              if (lk != null) {
                title += " to " + lk.getName() + ", " + lk.getCity();
              }
              title += " from";
            } else if (ITransaction.TYPE_RECEIPT.equals(transType)) {
              if (lk != null) {
                title += " from " + lk.getName() + ", " + lk.getCity();
              }
              title += " to";
            } else {
              title += " at";
            }
            title +=
                " " + k.getName() + ", " + k.getCity() + " on " + LocalDateUtil
                    .format(trans.getTimestamp(), locale, timezone);
          } catch (Exception e) {
            xLogger
                .warn("{0} when getting kiosk for ID {1} during trans. geo-data acquisition: {2}",
                    e.getClass().getName(), trans.getKioskId(), e.getMessage());
          }
        } // end if-else
        // If lat/lng are 0, then do not add
        if (lat == 0 && lng == 0) {
          continue;
        }
        if (justStarted) {
          latNorth = latSouth = lat;
          lngEast = lngWest = lng;
          justStarted = false;
        } else {
          // Get the north east, and south west corners of the bounding box of all points
          if (lat > latNorth) {
            latNorth = lat;
          } else if (lat < latSouth) {
            latSouth = lat;
          }
          if (lng > lngEast) {
            lngEast = lng;
          } else if (lng < lngWest) {
            lngWest = lng;
          }
        }
        geoData.points.add(new Point(title, lat, lng, accuracy, null, null, tid, null));
        geoData.size++;
      } // end while
    } finally {
      pm.close();
    }
    // Add the metadata array at the end (in this case, the lat-long bounds)
    if (geoData.points.size() >= 2) {
      geoData.latLngBounds = "[" + latNorth + "," + lngEast + "," + latSouth + "," + lngWest + "]";
    }
    return geoData;
  }

  // Expand the list of kiosks to include each kiosk's customers, if present
  @SuppressWarnings("unchecked")
  private static Results expandToCustomers(List<IKiosk> kiosks, PageParams pageParams) {
    List<IKiosk> exKiosks = new ArrayList<IKiosk>();
    String cursor = null;
    exKiosks.addAll(kiosks);
    Iterator<IKiosk> it = kiosks.iterator();
    while (it.hasNext()) {
      IKiosk k = it.next();
      // Get the customers
      try {
        Results
            results =
            Services.getService(EntitiesServiceImpl.class)
                .getLinkedKiosks(k.getKioskId(), IKioskLink.TYPE_CUSTOMER, null, pageParams);
        List<IKiosk> customers = results.getResults();
        cursor = results.getCursor();
        if (customers != null && !customers.isEmpty()) {
          Iterator<IKiosk> custIt = customers.iterator();
          while (custIt.hasNext()) {
            IKiosk c = custIt.next();
            if (!exKiosks.contains(c)) {
              exKiosks.add(c);
            }
          }
        }
      } catch (ServiceException e) {
        xLogger.warn("ServiceException when getting customers for kiosk {0}: {1}", k.getKioskId(),
            e.getMessage());
      }
    }
    return new Results(exKiosks, cursor);
  }

  // Compute the distance (in Km) between two geo-point (uses the Haversine formula for a spherical earth; gives approx. distances, but should be ok for smaller distances)
  public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
    double R = 6371D; // radius of earth in Km
    double dLat = Math.toRadians(lat2 - lat1); // delta lat
    double dLng = Math.toRadians(lng2 - lng1); // delta lng
    double dLatSin = Math.sin(dLat / 2);
    double dLngSin = Math.sin(dLng / 2);
    double a = dLatSin * dLatSin +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * dLngSin * dLngSin;
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double d = (R * c); // Km
    if (d < 0) {
      d = -1 * d;
    }
    return d;
  }

  /**
   * Get mappable kiosk data (JS array in String format), given a list of kiosks
   * Format: [ [<kioskid>, <kioskName>, <lat>, <long> ], ..., [<lat-north>,<lng-east>,<lat-south>,<lng-west>] ] // last array is metadata
   */
  public static GeoData getMappableKiosksByRoute(List<IKiosk> kiosks) {
    GeoData geoData = new GeoData();
    if (kiosks != null && kiosks.size() > 0) {
      // Init. bounding box coordinates (required for centering the map to this box)
      double latNorth = 0, lngEast = 0, latSouth = 0, lngWest = 0;
      boolean justStarted = true;
      for (IKiosk k : kiosks) {
        double lat = k.getLatitude();
        double lng = k.getLongitude();
        double accuracy = k.getGeoAccuracy();
        // If lat/lng are 0, then do not add
        if (lat == 0 && lng == 0) {
          continue;
        }

        if (justStarted) {
          latNorth = latSouth = lat;
          lngEast = lngWest = lng;
          justStarted = false;
        } else {
          // Get the north east, and south west corners of the bounding box of all points
          if (lat > latNorth) {
            latNorth = lat;
          } else if (lat < latSouth) {
            latSouth = lat;
          }
          if (lng > lngEast) {
            lngEast = lng;
          } else if (lng < lngWest) {
            lngWest = lng;
          }
        }
        // Get kiosk name (along with location)
        String title = k.getName() + "," + k.getCity();
        // Update kiosk location in array
        geoData.points.add(new Point(title, lat, lng, accuracy, k, null, null, null));
        geoData.size++;
      } // end for
      // Add the metadata array at the end (in this case, the lat-long bounds)
      if (geoData.points.size() >= 2) {
        geoData.latLngBounds =
            "[" + latNorth + "," + lngEast + "," + latSouth + "," + lngWest + "]";
      }
    }
    return geoData;
  }

  public static class GeoData {
    //public String geoCodes = "[]"; // JSON array of geo-codes (format specific to kiosks, materials or demand)
    public List<Point> points = new ArrayList<Point>();
    public String
        latLngBounds =
        "[]";
    // JSONArray of lat-lng bounds; format: [<lat-north>,<lng-east>,<lat-south>,<lng-west>]
    public BigDecimal
        maxValue =
        BigDecimal.ZERO;
    // maximum value amongst all values, if any (e.g. stock, demand quantity)
    public int size = 0; // number of geo-codes
    public String cursor = ""; // next cursor
    public boolean hasMoreResults = false;

    public String toJSONString() {
      String geoCodes = "[";
      Iterator<Point> it = points.iterator();
      while (it.hasNext()) {
        if (geoCodes.length() > 1) {
          geoCodes += ",";
        }
        geoCodes += it.next().toJSONString();
      }
      geoCodes += "]";
      return "{ \"geocodes\": " + geoCodes + ", \"size\": " + size + ", \"cursor\": \"" + cursor
          + "\", \"latlngbounds\": " + latLngBounds + ", \"maxvalue\": " + maxValue
          + ", \"hasMoreResults\": " + hasMoreResults + " }";
    }

    // Represents a given geodata point, esp. an entity
    public static class Point {
      // Entity attributes
      public Long kid = null; // kiosk/entity ID
      public String title = null;
      double lat = 0; // latitude
      double lng = 0; // longitude
      double accuracy = 0; // accuracy, if available
      int routeIndex;
      String routeTag = null;
      String
          tid =
          null;
      // transaction ID - order-ID or Transaction-ID, if point derived from an order
      // Inventory related attributes
      BigDecimal stockOnHand = null;
      BigDecimal safetyStock = null;
      BigDecimal reorderLevel = null;
      BigDecimal max = null;
      // Transaction/demand quantity
      BigDecimal quantity = null;

      public Point(String title, double lat, double lng, double accuracy, IKiosk k, IInvntry inv,
                   String transactionId, BigDecimal quantity) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.accuracy = accuracy;
        if (k != null) {
          this.kid = k.getKioskId();
          this.routeIndex = k.getRouteIndex();
          this.routeTag = k.getRouteTag();
        }
        this.tid = transactionId;
        if (inv != null) {
          stockOnHand = inv.getStock();
          safetyStock = inv.getSafetyStock();
          reorderLevel = inv.getReorderLevel();
          max = inv.getMaxStock();
        }
        if (quantity != null) {
          this.quantity = quantity;
        }
      }

      public String toJSONString() {
        String jsonStr = "{";
        jsonStr += "\"lat\":" + lat + ",";
        jsonStr += "\"lng\":" + lng + ",";
        jsonStr += "\"routeindex\":" + routeIndex;
        jsonStr += (title != null ? ",\"title\":\"" + title + "\"" : "");
        jsonStr += (kid != null ? ",\"kid\":\"" + kid + "\"" : "");
        jsonStr += (accuracy > 0 ? ",\"accuracy\":" + accuracy : "");
        jsonStr += (routeTag != null ? ",\"routetag\":\"" + routeTag + "\"" : "");
        jsonStr += (tid != null ? ",\"tid\":\"" + tid + "\"" : "");
        jsonStr += (stockOnHand != null ? ",\"stock\":" + stockOnHand : "");
        jsonStr += (safetyStock != null ? ",\"safetystock\":" + safetyStock : "");
        jsonStr += (reorderLevel != null ? ",\"reorderlevel\":" + reorderLevel : "");
        jsonStr += (max != null ? ",\"max\":" + max : "");
        jsonStr += (quantity != null ? ",\"quantity\":" + quantity : "");
        jsonStr += "}";
        return jsonStr;
      }
    }
  }

}
