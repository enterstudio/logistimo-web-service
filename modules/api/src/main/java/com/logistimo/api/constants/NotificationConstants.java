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

package com.logistimo.api.constants;

/**
 * Created by naveensnair on 25/04/15.
 */
public class NotificationConstants {
  public static String[]
      orderKeys =
      {"com.logistimo.orders.entity.Order:100", "com.logistimo.orders.entity.Order:6",
          "com.logistimo.orders.entity.Order:1", "com.logistimo.orders.entity.Order:5",
          "com.logistimo.orders.entity.Order:2",
          "com.logistimo.orders.entity.Order:101", "com.logistimo.orders.entity.Order:302",
          "com.logistimo.orders.entity.Order:150"};
    public static String[]
      shipmentKeys =
            {"com.logistimo.shipments.entity.Shipment:1", "com.logistimo.shipments.entity.Shipment:2",
                    "com.logistimo.shipments.entity.Shipment:101", "com.logistimo.shipments.entity.Shipment:150"};
    public static String[]
      inventoryKeys =
      {"com.logistimo.inventory.entity.InvntryBatch:5", "com.logistimo.inventory.entity.Transaction:6",
          "com.logistimo.inventory.entity.Invntry:200", "com.logistimo.inventory.entity.Transaction:203",
          "com.logistimo.inventory.entity.Transaction:204",
          "com.logistimo.inventory.entity.Transaction:205",
          "com.logistimo.inventory.entity.Transaction:209",
          "com.logistimo.inventory.entity.Transaction:206", "com.logistimo.inventory.entity.Invntry:208",
          "com.logistimo.inventory.entity.Transaction:207",
          "com.logistimo.inventory.entity.Invntry:201", "com.logistimo.inventory.entity.Invntry:202"};
  public static String[]
      setupKeys =
      {"com.logistimo.entities.entity.Kiosk:1", "com.logistimo.entities.entity.Kiosk:2",
          "com.logistimo.entities.entity.Kiosk:3", "com.logistimo.inventory.entity.Invntry:1",
          "com.logistimo.inventory.entity.Invntry:2",
          "com.logistimo.inventory.entity.Invntry:3",
          "com.logistimo.materials.entity.Material:1", "com.logistimo.materials.entity.Material:2",
          "com.logistimo.materials.entity.Material:3",
          "com.logistimo.users.entity.UserAccount:6", "com.logistimo.users.entity.UserAccount:1",
          "com.logistimo.users.entity.UserAccount:2",
          "com.logistimo.users.entity.UserAccount:3", "com.logistimo.users.entity.UserAccount:5",
          "com.logistimo.users.entity.UserAccount:400",
          "com.logistimo.assets.entity.Asset:1", "com.logistimo.assets.entity.Asset:2",
          "com.logistimo.assets.entity.Asset:3", "com.logistimo.assets.entity.AssetRelation:1",
          "com.logistimo.assets.entity.AssetRelation:3",};
  public static String[]
      temperatureKeys =
      {"com.logistimo.assets.entity.AssetStatus:500", "com.logistimo.assets.entity.AssetStatus:501",
          "com.logistimo.assets.entity.AssetStatus:502", "com.logistimo.assets.entity.AssetStatus:6",
          "com.logistimo.assets.entity.AssetStatus:504",
          "com.logistimo.assets.entity.AssetStatus:505",
          "com.logistimo.assets.entity.AssetStatus:506",
          "com.logistimo.assets.entity.AssetStatus:507",
          "com.logistimo.assets.entity.AssetStatus:101"};
  public static String[]
      assetAlarmKeys =
      {"com.logistimo.assets.entity.AssetStatus:508", "com.logistimo.assets.entity.AssetStatus:509",
          "com.logistimo.assets.entity.AssetStatus:510",
          "com.logistimo.assets.entity.AssetStatus:511",
          "com.logistimo.assets.entity.AssetStatus:512",
          "com.logistimo.assets.entity.AssetStatus:513",
          "com.logistimo.assets.entity.AssetStatus:514",
          "com.logistimo.assets.entity.AssetStatus:515",
          "com.logistimo.assets.entity.AssetStatus:516",
          "com.logistimo.assets.entity.AssetStatus:517",
          "com.logistimo.assets.entity.AssetStatus:518",
          "com.logistimo.assets.entity.AssetStatus:102"};
  public static String[] accountKeys = {"com.logistimo.accounting.entity.Account:300"};
}
