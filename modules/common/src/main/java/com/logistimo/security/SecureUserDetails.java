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
package com.logistimo.security;

import java.io.Serializable;
import java.util.Locale;

/**
 * Represents an authenticated user's details
 *
 * @author arun
 */
public class SecureUserDetails implements Serializable {

  private static final long serialVersionUID = 1L;

  private String role = null;
  private String userId = null;
  private boolean isEnabled = true;
  private Long domainId = null;
  private Locale locale = null;
  private String timezone = null;
  private Long currentDomainId;

  public Long getCurrentDomainId() {
    return currentDomainId;
  }

  public void setCurrentDomainId(Long currentDomainId) {
    this.currentDomainId = currentDomainId;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getUsername() {
    return userId;
  }

  public void setUsername(String userId) {
    this.userId = userId;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  // SG specific info. - not required for Spring
  public Long getDomainId() {
    return domainId;
  }

  public void setDomainId(Long domainId) {
    this.domainId = domainId;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  @Override
  public String toString() {
    return "SecureUserDetails{" +
        "role='" + role + '\'' +
        ", userId='" + userId + '\'' +
        ", isEnabled=" + isEnabled +
        ", domainId=" + domainId +
        ", locale=" + locale +
        ", timezone='" + timezone + '\'' +
        ", currentDomainId=" + currentDomainId +
        '}';
  }
}
