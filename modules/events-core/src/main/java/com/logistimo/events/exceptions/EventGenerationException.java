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

package com.logistimo.events.exceptions;

import com.logistimo.exception.LogiException;

import java.util.Locale;

/**
 * Created by charan on 07/03/17.
 */
public class EventGenerationException extends LogiException {
  public EventGenerationException(String code, Locale locale, Object... arguments) {
    super(code, locale, arguments);
  }

  public EventGenerationException(String message) {
    super(message);
  }

  public EventGenerationException(String message, Throwable t) {
    super(message, t);
  }

  public EventGenerationException(String code, Throwable t, Object... arguments) {
    super(code, t, arguments);
  }

  public EventGenerationException(String code, Object... arguments) {
    super(code, arguments);
  }

  public EventGenerationException(Throwable t) {
    super(t);
  }
}
