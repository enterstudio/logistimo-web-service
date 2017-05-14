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
package com.logistimo.config.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author arun
 *
 *         Represents k-values used in inventory optimization
 */
public class KValue {
  @SuppressWarnings("serial")
  private static final Map<Float, Float> kvalues = new HashMap<Float, Float>() {{
    put(99F, 2.72F);
    put(98F, 2.45F);
    put(97F, 2.28F);
    put(96F, 2.15F);
    put(95F, 2.04F);
    put(94F, 1.95F);
    put(93F, 1.87F);
    put(92F, 1.79F);
    put(91F, 1.72F);
    put(90F, 1.66F);
    put(89F, 1.61F);
    put(88F, 1.55F);
    put(87F, 1.5F);
    put(86F, 1.45F);
    put(85F, 1.4F);
    put(84F, 1.35F);
    put(83F, 1.3F);
    put(82F, 1.26F);
    put(81F, 1.22F);
    put(80F, 1.17F);
    put(79F, 1.13F);
    put(78F, 1.09F);
    put(77F, 1.05F);
    put(76F, 1.01F);
    put(75F, 0.96F);
    put(74F, 0.92F);
    put(73F, 0.88F);
    put(72F, 0.84F);
    put(71F, 0.8F);
    put(70F, 0.75F);
    put(69F, 0.71F);
    put(68F, 0.66F);
    put(67F, 0.62F);
    put(66F, 0.56F);
    put(65F, 0.51F);
  }};

  public static float get(float svcLevel) {
    Float kvalue = kvalues.get(new Float(svcLevel));
    if (kvalue == null) {
      return 0;
    }
    return kvalue.floatValue();
  }

  public Iterator<Float> getServiceLevels() {
    return kvalues.keySet().iterator();
  }
}
