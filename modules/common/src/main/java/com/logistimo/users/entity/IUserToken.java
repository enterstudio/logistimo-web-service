package com.logistimo.users.entity;

import java.util.Date;

/**
 * Created by naveensnair on 03/11/15.
 */
public interface IUserToken {

  String getUserId();

  void setUserId(String userId);

  String getToken();

  void setToken(String token);

  Date getExpires();

  void setExpires(Date expires);

  Long getDomainId();

  void setDomainId(Long domainId);

  String getRawToken();

  void setRawToken(String rawToken);
}
