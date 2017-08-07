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

package com.logistimo.logger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Created by charan on 04/03/15.
 */
public class LogiLogger implements ILogger {

  private Logger xLogger;

  public LogiLogger(String name) {
    xLogger = Logger.getLogger(name);
  }


  @Override
  public void fine(String msgTemplate, Object... params) {
    log(Level.DEBUG, msgTemplate, params);
  }

  @Override
  public void finer(String msgTemplate, Object... params) {
    log(Level.DEBUG, msgTemplate, params);
  }

  @Override
  public void finest(String msgTemplate, Object... params) {
    log(Level.DEBUG, msgTemplate, params);
  }

  @Override
  public void info(String msgTemplate, Object... params) {
    log(Level.INFO, msgTemplate, params);
  }

  @Override
  public void warn(String msgTemplate, Object... params) {
    log(Level.WARN, msgTemplate, params);
  }

  @Override
  public void severe(String msgTemplate, Object... params) {
    log(Level.ERROR, msgTemplate, params);
  }

  private void log(Level level, String msgTemplate, Object... params) {
    if (xLogger.isEnabledFor(level)) {
      Throwable ex = XLog.getCause(params);
      String msg = XLog.format(msgTemplate, params);
      if (ex == null) {
        xLogger.log(level, msg);
      } else {
        if (level.equals(Level.WARN)) {
          xLogger.warn(msg, ex);
        } else {
          xLogger.error(msg, ex);
        }
      }
    }
  }

}
