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
