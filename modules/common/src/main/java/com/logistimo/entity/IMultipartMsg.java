package com.logistimo.entity;

import java.util.Date;
import java.util.List;

/**
 * Created by charan on 20/05/15.
 */
public interface IMultipartMsg {

  String SEP = "|||";

  String getId();

  void setId(String id);

  int size();

  List<String> getMessages();

  void addMessage(String m);

  Date getTimestamp();

  void setTimestamp(Date t);

  String getCountry();

  void setCountry(String ccode);
}
