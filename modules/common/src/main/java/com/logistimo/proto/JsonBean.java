package com.logistimo.proto;


public interface JsonBean {
  void fromJSONString(String jsonString) throws ProtocolException;

  String toJSONString() throws ProtocolException;
}
