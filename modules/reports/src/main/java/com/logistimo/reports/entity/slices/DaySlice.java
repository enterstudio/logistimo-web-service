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
package com.logistimo.reports.entity.slices;

import com.logistimo.utils.LocalDateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

/**
 * Represents a slices of daily aggregated transaction data (quantities and trans. counts) per object over a given dimension
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Cacheable("false")
public class DaySlice extends Slice implements IDaySlice {

  @Override
  public void setDate(Date d, String tz, String freq) {
    Calendar
        cal =
        tz != null ? GregorianCalendar.getInstance(TimeZone.getTimeZone(tz))
            : GregorianCalendar.getInstance();
    cal.setTime(d);
    this.d = LocalDateUtil.getGMTZeroTimeFromCal(cal);
  }
}
