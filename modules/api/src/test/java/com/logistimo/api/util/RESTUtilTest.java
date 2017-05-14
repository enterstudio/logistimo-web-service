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

package com.logistimo.api.util;

import com.logistimo.config.models.InventoryConfig;
import com.logistimo.config.models.MatStatusConfig;
import com.logistimo.proto.JsonTagsZ;

import org.junit.Test;

import java.util.Hashtable;

import static org.junit.Assert.*;

/**
 * Created by yuvaraj on 12/04/17.
 */
public class RESTUtilTest {

  @Test
  public void testGetMaterialStatus() throws Exception {

    InventoryConfig ic = new InventoryConfig();
    MatStatusConfig mc = new MatStatusConfig();
    mc.setDf("hello,hello,,");
    ic.setMatStatusConfigByType("i",mc);
    Hashtable<String, Hashtable<String, String>>
        nestedHashTable =
        RESTUtil.getMaterialStatus(ic);
    Hashtable<String, String> values = nestedHashTable.get("i");
    String uniqueMStatVal = values.get(JsonTagsZ.ALL);
    assertNotNull(uniqueMStatVal);
    assertEquals("Status does not match", "hello",uniqueMStatVal);

  }
}