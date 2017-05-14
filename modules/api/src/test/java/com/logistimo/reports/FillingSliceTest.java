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

package com.logistimo.reports;

import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.entity.slices.ReportsSlice;
import com.logistimo.reports.utils.ReportsUtil;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by mohansrinivas on 2/23/17.
 */
public class FillingSliceTest {

  @Test
  public void fillSliceWithDefaultMonthly(){
    List<ISlice> slices = ReportsUtil.fillMissingSlicesWithDefaultValues(getTestSlices(),
        ReportsConstants.FREQ_MONTHLY);
    assertEquals(4,slices.size());
    assertEquals(200,slices.get(0).getTotalCount());
    assertEquals(400,slices.get(slices.size()-1).getTotalCount());
    assertEquals(0,slices.get(slices.size()-2).getTotalCount());
  }

  @Test
  public void fillSliceWithDefaultDaily(){
    List<ISlice> slices = ReportsUtil.fillMissingSlicesWithDefaultValues(getTestSlices(),ReportsConstants.FREQ_DAILY);
    assertEquals(91,slices.size());
    assertEquals(200,slices.get(0).getTotalCount());
    assertEquals(400,slices.get(slices.size()-1).getTotalCount());
    assertEquals(0,slices.get(slices.size()-10).getTotalCount());
  }

  @Test
  public void fillSliceWithPrevValues(){
    List<? extends ISlice> testSlices = getTestSlices();
    int n  = testSlices.size();
    Date startDate = testSlices.get(n-1).getDate();
    Date endDate = testSlices.get(0).getDate();
    List<ISlice> slices = ReportsUtil.fillMissingSlices(getTestSlices(), startDate, endDate,ReportsConstants.FREQ_DAILY, false);
    assertEquals(91,slices.size());
    assertEquals(new BigDecimal(1000),slices.get(0).getStockQuantity());
    assertEquals(new BigDecimal(2000),slices.get(slices.size()-1).getStockQuantity());
    assertEquals(new BigDecimal(2000),slices.get(slices.size()-10).getStockQuantity());
  }

  public  List<? extends ISlice> getTestSlices(){
    List results = new ArrayList();
    ReportsSlice cs = new ReportsSlice();
    cs.cassandraRow.put("t","2017-04-01");
    cs.cassandraRow.put("uid","mohans");
    cs.cassandraRow.put("tc","200");
    cs.cassandraRow.put("sq","1000");
    cs.cassandraRow.put("did","1");
    results.add(cs);
    ReportsSlice cs1 = new ReportsSlice();
    cs1.cassandraRow.put("t","2017-01-01");
    cs1.cassandraRow.put("uid","mohan");
    cs1.cassandraRow.put("tc","400");
    cs1.cassandraRow.put("sq","2000");
    cs1.cassandraRow.put("did","1");
    results.add(cs1);

    return results;
  }
}