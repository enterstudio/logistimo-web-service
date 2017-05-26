/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

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
