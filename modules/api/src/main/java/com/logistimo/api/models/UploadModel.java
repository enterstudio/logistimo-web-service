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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohan raja on 08/01/15
 */
public class UploadModel {
  public String mp; // Master Page
  public boolean iut; // Is User Type
  public String tnm; // Type Name
  public String ty; // Type

  public boolean iu; // Is Uploaded
  public boolean ipc; // Is Previous Job Complete
  public int js; // Job Status
  public String jsnm; // Job Status Name
  public String id; // Uploaded Id
  public String fnm; // File Name
  public String utm; // Upload Time

  public String uid; // user ID (of user who uploaded)
  public String unm; // user name (of user who uploaded)

  public boolean ierr; // Is Error
  public int errcnt; // Error Count

  public List<Error> errs; //Errors

  public void addError(long off, String onm, List<String> msgs, String ln) {
    if (errs == null) {
      errs = new ArrayList<Error>();
    }
    Error error = new Error();
    error.off = off;
    error.onm = onm;
    error.msgs = msgs;
    error.ln = ln;
    errs.add(error);
  }

  public class Error {
    public long off; // Offset
    public String onm; // Operation Name
    public List<String> msgs; // Error Messages
    public String ln; // CSV Line
  }
}
