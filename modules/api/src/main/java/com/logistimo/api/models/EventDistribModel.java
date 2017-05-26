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

package com.logistimo.api.models;

import java.io.Serializable;

/**
 * Created by charan on 03/11/15.
 */
public class EventDistribModel implements Serializable {
  /**
   * Out of stock count
   */
  public double oosp = 0.0d;
  /**
   * Under stock percentage
   */
  public double usp = 0.0d;
  /**
   * Over stock
   */
  public double osp = 0.0d;
  /**
   * Normal percentage
   */
  public double np = 0.0d;
  /**
   * Out of stock
   */
  public Long oos = 0l;
  /**
   * Over stock
   */
  public Long os = 0l;
  /**
   * Under stock
   */
  public Long us = 0l;
  /**
   * Normal count
   */
  public Long n = 0l;
  /**
   * Total count
   */
  public Long c = 0l;
  /**
   * Total Quantity
   */
  public Long q = 0l;

  /**
   * Invntry min
   */
  public Long min = 0l;

  /**
   * Invntry max
   */
  public Long max = 0l;

  /**
   * Invntry updated formatted timestamp
   */
  public String t;
}
