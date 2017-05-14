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
  @SuppressWarnings("unused")
  private String password = null; // encoded password. DEPRECATED
  private String userId = null;
  private boolean isEnabled = true;
  private Long domainId = null;
  private Locale locale = null;
  private String timezone = null;

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
}
