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

package com.logistimo.assets.models;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kaniyarasu on 24/11/15.
 */
public class AssetRelationModel {
  public List<AssetRelations> data;

  public AssetRelationModel() {
    data = new ArrayList<>(1);
  }

  public static class AssetRelations {
    public String dId;
    public String vId;
    public List<Relation> ras;

    public AssetRelations() {
      ras = new ArrayList<>(5);
    }

    public AssetRelations(String vId, String dId) {
      this.dId = dId;
      this.vId = vId;
      ras = new ArrayList<>(5);
    }

    public AssetRelations(String vId, String dId, List<Relation> ras) {
      this.dId = dId;
      this.vId = vId;
      this.ras = ras;
    }
  }

  public static class Relation {
    public String dId;
    public String vId;
    public String sId;
    public Integer typ;
    public Integer mpId;
    public JsonElement meta;
  }
}
