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

import java.util.Date;
import java.util.Map;

/**
 * Created by vani on 27/10/15.
 */
public class JobStatusModel {
  public long id; // Job ID
  public String crby; // Created by
  public String crbyFn; // Full name of the user who created the job.
  public String ty; // Type
  public String sbty; // Sub type
  public String sbtyd; // Sub type display
  public int st; // Status
  public String std; // Status display
  public String rsn; // Reason
  public Date stt; // Start time
  public String sttStr; // Formatted start time
  public Date ut; // Last updated time
  public String utStr; // Formatted updated time
  public int nr; // Number of records completed
  public String on; // Output file name
  public String ofl; // Output file location
  public Map<String, String> rcp; // Map of recipient id versus full name
  public int sno; //Serial No
}
