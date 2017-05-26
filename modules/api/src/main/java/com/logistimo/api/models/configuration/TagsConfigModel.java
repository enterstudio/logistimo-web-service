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

package com.logistimo.api.models.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naveensnair on 17/11/14.
 */
public class TagsConfigModel {
  public String[] mt;
  public String[] et;
  public String[] rt;
  public String[] ot;
  public String[] ut;
  public boolean emt;
  public boolean eet;
  public boolean eot;
  public String en;
  public boolean eut;
  public String createdBy;
  public String lastUpdated;
  public String fn;
  public List<ETagOrder> etr = new ArrayList<>(1);

  public static class ETagOrder {
    public String etg;
    public Integer rnk;
  }
}
