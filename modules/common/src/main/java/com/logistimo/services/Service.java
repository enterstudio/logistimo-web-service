package com.logistimo.services;

import java.util.Locale;

public interface Service {
  void init(Services services) throws ServiceException;

  void destroy() throws ServiceException;

  Class<? extends Service> getInterface();

  void loadResources(Locale locale);

  Locale getLocale();

  Service clone() throws CloneNotSupportedException; // clone this service object
}
