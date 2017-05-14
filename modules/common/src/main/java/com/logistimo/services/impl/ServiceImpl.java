/**
 *
 */
package com.logistimo.services.impl;

import com.logistimo.services.Resources;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Implement generic properties/methods required for services
 *
 * @author Arun
 */
public class ServiceImpl implements Service {

  private static final XLog xLogger = XLog.getLog(ServiceImpl.class);

  protected ResourceBundle backendMessages = null;
  protected ResourceBundle messages = null;

  public void loadResources(Locale locale) throws MissingResourceException {
    if (locale == null) {
      locale = new Locale(Constants.LANG_DEFAULT, "");
    }
    backendMessages = Resources.get().getBundle("BackendMessages", locale);
    messages = Resources.get().getBundle("Messages", locale);
  }

  public Locale getLocale() {
    if (backendMessages != null) {
      return backendMessages.getLocale();
    }
    return null;
  }

  public Service clone() throws CloneNotSupportedException {
    ServiceImpl as;
    try {
      as = this.getClass().newInstance();
      xLogger.fine("clone(): got class = {0}", as.getClass().getName());
      as.backendMessages = backendMessages;
      as.messages = messages;
      return (Service) as;
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void destroy() throws ServiceException {
    xLogger.fine("Entering destroy");
    // TODO Auto-generated method stub
    xLogger.fine("Exiting destroy");
  }

  public Class<? extends Service> getInterface() {
    xLogger.fine("Entering getInterface");
    xLogger.fine("Exiting getInterface");
    return this.getClass();
  }

  public void init(Services services) throws ServiceException {
    xLogger.fine("Entering init");
    // TODO Auto-generated method stub
    xLogger.fine("Exiting init");
  }
}
