package com.logistimo.api.models.configuration;

import java.util.List;
import java.util.Map;

/**
 * Created by mohan raja.
 */
public class CapabilitiesConfigModel {
  public String[] cap; //capabilities
  public String[] tm; //transaction menu
  public String[] et; //entity types
  public boolean er; //edit routing time
  public boolean lr; //login reconnect
  public List<String> hi; //hide inventory
  public List<String> ho; //hide orders
  public boolean sv; //send vendors
  public boolean sc; //send customers
  public String gcs; //geo coding strategy
  public String ro; //role
  public String createdBy; //userId last saved configuration
  public String lastUpdated; //last updated time
  public String fn; //first name
  public boolean eshp; // Enable shipping orders on mobile
  public int atexp; // Authentication token expiry
  public boolean llr; // Local login required

  public Map<String, CapabilitiesConfigModel> roleConfig;

  public List<String> hii; //hide inventory material tags on issue
  public List<String> hir; //hide inventory material tags on receipt
  public List<String> hip; // hide inventory material tags on stock count
  public List<String> hiw; // hide inventory material tags on discards
  public List<String> hit; // hide inventory material tags on transfers

  public int mdri = 0; // Master data refresh interval
  public int iri = 0; // Inventory refresh interval
  public int aplui = 0; // Application log upload interval
  public int stwd = 0; // SMS transmission wait duration
}
