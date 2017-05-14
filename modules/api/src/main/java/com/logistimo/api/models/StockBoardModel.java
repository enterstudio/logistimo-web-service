package com.logistimo.api.models;

/**
 * Created by naveensnair on 15/04/15.
 */
public class StockBoardModel {
  public int esb; // enable stock board
  public int itv; // items in view [should be between 0 and 10]
  public int rd; // refresh duration
  public int sci; // scroll interval
  public int hsi; // horizontal scroll interval
  public Long kid; // kiosk Id
  public String msg; // messages to show
  public boolean add;
}
