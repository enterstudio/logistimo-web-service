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

package com.logistimo.exception;

import java.util.Locale;

/**
 * Created by charan on 22/06/17.
 */
public class SystemException extends RuntimeException implements ExceptionWithCodes {

  private final transient Object[] arguments;
  private final String code;

  public String getCode() {
    return code;
  }

  public SystemException(Exception e) {
    super(e);
    this.arguments = new Object[0];
    this.code = null;
  }

  public SystemException(Exception e, String code, Object... arguments) {
    super(e);
    this.code = code;
    this.arguments = arguments;
  }

  public SystemException(String message) {
    super(message);
    this.code = null;
    this.arguments = new Object[0];
  }

  public SystemException(String message, Throwable e) {
    super(message, e);
    this.code = null;
    this.arguments = new Object[0];
  }

  @Override
  public String getMessage() {
    return ExceptionUtils.constructMessage(code, Locale.getDefault(), arguments);
  }

  @Override
  public Object[] getArguments() {
    return this.arguments;
  }
}
