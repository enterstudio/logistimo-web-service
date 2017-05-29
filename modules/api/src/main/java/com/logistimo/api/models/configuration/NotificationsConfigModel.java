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

package com.logistimo.api.models.configuration;

/**
 * Created by naveensnair on 01/12/14.
 */
public class NotificationsConfigModel {
  public String or; // orders-notification
  public String ship; // shipments-notification
  public String inv; // inventory-notification
  public String st; // setup-notification
  public String temp; // temperature-notification
  public String acc; // accounts-notifiction
  public String pr; // prefix-days
  public String os; // order-status
  public String id; // id [org.lggi.samaanguru.entity.Order:100]
  public String cot; // customer owner notification time interval
  public String vnt; // vendor notification time interval
  public String adt; // administrator notification time interval
  public String crt; // creator notification time interval
  public String ust; // user notification time interval
  public String aut;
  public String mt; // message template
  public String uid; // userid
  public String nid; // notification message id
  public boolean co; // enable customer owner notification
  public boolean vn; // enable vendor notification
  public boolean ad; // enable administrator notification
  public boolean cr; // enable creator notification
  public boolean usr; // enable user notification
  public boolean bb; // enable post in bulletin board
  public boolean au;
  public String domainName; //domainName
  public String tags;
  public boolean add;
  public String eet; // Exclude entity tags (comma separated values, similar to uid)
  public String emt; // Exclude material tags (comma separated values, similar to uid)
  public String eot; // Exlcude order tags (comma separated values, similar to uid)
  public String mst;
  public String ist;
  public String usrTgs; // user-tags
}
