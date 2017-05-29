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

package com.logistimo.api.models.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naveensnair on 19/11/14.
 */
public class InventoryConfigModel {
  public String ri; // Reasons for issue
  public String rr; // Reasons for receipt
  public String rs; // Reasons for stock count
  public String rd; // Reasons for discard
  public String rt; // Reasons for transfer
  public boolean ivc; // inventory visibility customers
  public boolean etdx; // enable transaction data export
  public String et; //export times
  public String an; // export user ids
  public String co; // compute options [none/forecast/eoq]
  public String minhpccr; // minimum historical period consumption rate - automatic
  public String maxhpccr; // maximum historical period consumption rate - automatic
  public String aopfd; // average order period demand forecasts
  public String nopfd; // number of orders period demand forecasts
  public String im; // inventory model
  public String aopeoq; // average order period eoq
  public String nopeoq; // number of orders period eoq
  public String lt; // lead time
  public boolean eidb; // enable Inventory Dashboard
  public boolean emuidt; // enable manual upload inventory data and transaction
  public boolean euse; // enable upload per single entity
  public String createdBy; //Last configuration saved userid
  public String lastUpdated; //Last updated time
  public String fn; //first name

  public boolean cimt;//configure by material tag for issues
  public boolean crmt; // configure by material tag for receipts
  public boolean cdmt; //configure by material tag for discards
  public boolean csmt; //configure by material tag for stock counts
  public boolean ctmt; //configure by material tag for transfers

  public List<MTagReason> imt = new ArrayList<>(1); //material tag reasons for issues
  public List<MTagReason> rmt = new ArrayList<>(1); //material tag reasons for receipts
  public List<MTagReason> dmt = new ArrayList<>(1);  // material tag reasons for discards
  public List<MTagReason> smt = new ArrayList<>(1);  //material tag reasons for stock count
  public List<MTagReason> tmt = new ArrayList<>(1); // material tag reasons for transfers
  public String idf; //material status for issue
  public String iestm; // material status for issue of temperature sensitive materials
  public String rdf;  //material status for receipt
  public String restm;// material status for receipt of temperature sensitive materials
  public String pdf;//material status for stock count
  public String pestm;// material status for stock count of temperature sensitive materials
  public String wdf;//material status for discards
  public String westm;// material status for discards of temperature sensitive materials
  public String tdf;//material status for transfer
  public String testm;// material status for transfer of temperature sensitive materials
  public String catdi; // capture the actual date for transactions 0-optional,1-mandatory
  public String catdr;
  public String catdp;
  public String catdw;
  public String catdt;
  public List<String> enTgs; //Entity tags to be filtered in stock views.
  public List<String> usrTgs; // user tags for export transaction data
  public String crc; //Consumption rate computation - none/manual/automatic
  public boolean dispcr;//Show stock
  public String crfreq; // Frequency for computation
  public String mcrfreq; // Frequency for manual consumption rate
  public String dcrfreq; // consumption rate for display [daily, weekly, monthly]
  public boolean showpr; // show predictions
  public boolean ddf; // Display demand forecast
  public boolean dooq; // Display OOQ
  public int mmType; // Min Max type
  public String mmDur; // Min Max duration
  public String mmFreq; // Min Max frequency
  public boolean edis; // Exclude discards
  public List<String> ersns; // Exclude reasons
  public boolean ism; //Issue status mandatory
  public boolean rsm; //Receipt status mandatory
  public boolean psm; //Stock count status mandatory
  public boolean wsm; //Discard status mandatory
  public boolean tsm; //Transfer status mandatory
  public LeadTimeAvgConfigModel ltacm; // Lead time average config model

  public static class MTagReason {
    public String mtg;
    public String rsn;
  }

  public static class LeadTimeAvgConfigModel {
    public Integer mino; // Minimum number of orders to consider for computing leadtime average
    public Integer maxo; // Maximum number of orders to consider for computing leadtime average
    public Float maxop; // Maximum order periods to consider for computing leadtime average
    public boolean exopt; // Exclude order processing time while computing leadtime average
  }
}
