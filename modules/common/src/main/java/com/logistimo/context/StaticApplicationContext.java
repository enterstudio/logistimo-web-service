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

package com.logistimo.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by charan on 27/06/17.
 */
public class StaticApplicationContext implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  private static void setStaticContext(ApplicationContext applicationContext) {
    StaticApplicationContext.applicationContext = applicationContext;
  }

  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    StaticApplicationContext.setStaticContext(applicationContext);
  }

  public static <T> T getBean(Class<T> clazz) {
    return applicationContext.getBean(clazz);
  }
}
