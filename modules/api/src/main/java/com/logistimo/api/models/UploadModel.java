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
