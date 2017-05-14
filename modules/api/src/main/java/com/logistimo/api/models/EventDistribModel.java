package com.logistimo.api.models;

import java.io.Serializable;

/**
 * Created by charan on 03/11/15.
 */
public class EventDistribModel implements Serializable {
  /**
   * Out of stock count
   */
  public double oosp = 0.0d;
  /**
   * Under stock percentage
   */
  public double usp = 0.0d;
  /**
   * Over stock
   */
  public double osp = 0.0d;
  /**
   * Normal percentage
   */
  public double np = 0.0d;
  /**
   * Out of stock
   */
  public Long oos = 0l;
  /**
   * Over stock
   */
  public Long os = 0l;
  /**
   * Under stock
   */
  public Long us = 0l;
  /**
   * Normal count
   */
  public Long n = 0l;
  /**
   * Total count
   */
  public Long c = 0l;
  /**
   * Total Quantity
   */
  public Long q = 0l;

  /**
   * Invntry min
   */
  public Long min = 0l;

  /**
   * Invntry max
   */
  public Long max = 0l;

  /**
   * Invntry updated formatted timestamp
   */
  public String t;
}
