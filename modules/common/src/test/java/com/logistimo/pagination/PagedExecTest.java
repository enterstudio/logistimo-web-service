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

package com.logistimo.pagination;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.Query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by mohan on 23/08/17.
 */
public class PagedExecTest {

  Query query = mock(Query.class);

  @Before
  public void init() {
    doReturn(Collections.singletonList("Executed list")).when(query).executeWithArray(any());
    doReturn(Collections.singletonList("Executed map")).when(query).executeWithMap(any());
    doReturn(Collections.singletonList("Executed")).when(query).execute();
  }

  @Test
  public void testExecuteWithList() throws Exception {
    QueryParams qp =
        new QueryParams(null, Collections.singletonList("Execute"), QueryParams.QTYPE.SQL,
            Object.class);
    List val = PagedExec.execute(qp, query);
    assertTrue(val.size() == 1);
    assertEquals("Executed list", val.get(0));
  }

  @Test
  public void testExecuteWithMap() throws Exception {
    HashMap<String, Object> map = new HashMap<>();
    map.put("1", "1");
    QueryParams qp = new QueryParams("query", map);
    List val = PagedExec.execute(qp, query);
    assertTrue(val.size() == 1);
    assertEquals("Executed map", val.get(0));
  }

  @Test
  public void testExecute() throws Exception {
    QueryParams qp = new QueryParams("query", (Map<String, Object>) null);
    List val = PagedExec.execute(qp, query);
    assertTrue(val.size() == 1);
    assertEquals("Executed", val.get(0));
  }
}