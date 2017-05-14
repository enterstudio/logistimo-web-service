package com.logistimo.reports.dao;


import java.util.List;

/**
 * Created by charan on 09/03/17.
 */
public interface IReportsDao {

  /**
   * this method will get the results from cassandra where data is of Slice ( to make it work with the existing reports)
   */
  List getResults(String query);
}
