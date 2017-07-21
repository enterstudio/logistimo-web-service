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

package com.logistimo.api.models.mobile;

import java.io.Serializable;

/**
 * Created by kumargaurav on 13/04/17.
 */
public class MobileInvDashboardDetailModel implements Serializable {

  protected static final long serialVersionUID = 1L;
  public Long tsc;//total store count
  public Long asc;//active store count
  public Long tc;//total inventory count
  public Long nc;//normal inventory count
  public Long gmc = 0l;//inventory count greater than max
  public Long lmnc = 0l;//inventory count Less than min
  public Long soc = 0l;//inventory count stock out
  public String lcnm;//location name
  public String lcid;//location id
  public Long did;//domain id
  public String dnm;//domain name
  public Long eId;//store id
  public String enm;//store name
  public Long mId;//material id
  public String mnm;//material name


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MobileInvDashboardDetailModel that = (MobileInvDashboardDetailModel) o;

    if (lcnm != null ? !lcnm.equals(that.lcnm) : that.lcnm != null) {
      return false;
    }
    if (eId != null ? !eId.equals(that.eId) : that.eId != null) {
      return false;
    }
    if (enm != null ? !enm.equals(that.enm) : that.enm != null) {
      return false;
    }
    if (mId != null ? !mId.equals(that.mId) : that.mId != null) {
      return false;
    }
    return mnm != null ? mnm.equals(that.mnm) : that.mnm == null;

  }

  @Override
  public int hashCode() {
    int result = lcnm != null ? lcnm.hashCode() : 0;
    result = 31 * result + (eId != null ? eId.hashCode() : 0);
    result = 31 * result + (enm != null ? enm.hashCode() : 0);
    result = 31 * result + (mId != null ? mId.hashCode() : 0);
    result = 31 * result + (mnm != null ? mnm.hashCode() : 0);
    return result;
  }

  /**
   * Second instance will be merged with first
   */
  public MobileInvDashboardDetailModel merge(MobileInvDashboardDetailModel other) {

    assert (this.equals(other));
    if (other.soc > 0) {
      this.soc = other.soc;
    }
    if (other.lmnc > 0) {
      this.lmnc = other.lmnc;
    }
    if (other.gmc > 0) {
      this.gmc = other.gmc;
    }
    return this;
  }

}
