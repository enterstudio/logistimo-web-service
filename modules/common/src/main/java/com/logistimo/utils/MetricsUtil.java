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

package com.logistimo.utils;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;


/**
 * Created by charan on 09/02/16.
 */
public class MetricsUtil {

  private static final MetricRegistry _metrics = new MetricRegistry();
  static JmxReporter _jmxReporter;

  public static void startReporter() {
    _jmxReporter = JmxReporter.forRegistry(_metrics).build();
    _jmxReporter.start();
  }

  public static MetricRegistry getRegistry() {
    return _metrics;
  }

  public static Timer getTimer(Class clazz, String key) {
    return _metrics.timer(MetricRegistry.name(clazz, key));
  }

  public static Meter getMeter(Class clazz, String key) {
    Meter
        meter =
        _metrics.getMeters() != null ? _metrics.getMeters().get(MetricRegistry.name(clazz, key))
            : null;
    if (meter == null) {
      return _metrics.meter(MetricRegistry.name(clazz, key));
    }
    return meter;
  }
}
