package com.logistimo.proto;

import java.util.Vector;

public interface MessageBean {
  void fromMessageString(Vector messages) throws ProtocolException;

  Vector toMessageString() throws ProtocolException;
}
