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

package com.logistimo.api.models.superdomains;

/**
 * Created by naveensnair on 09/09/15.
 */
public class DomainPermissionModel {
  public Long did;
  public Boolean uv; // userView
  public Boolean ua; // userAdd
  public Boolean ue; // userEdit
  public Boolean ur; // userRemove
  public Boolean ev; // entityView
  public Boolean ea; // entityAdd
  public Boolean ee; // entityEdit
  public Boolean er; // entityRemove
  public Boolean egv; // entityGroupView
  public Boolean ega; // entityGroupAdd
  public Boolean ege; // entityGroupEdit
  public Boolean egr; // entityGroupRemove
  public Boolean erv; // entityGroupView
  public Boolean era; // entityGroupAdd
  public Boolean ere; // entityGroupEdit
  public Boolean err; // entityGroupRemove
  public Boolean iv; // inventoryView
  public Boolean ia; // inventoryAdd
  public Boolean ie; // inventoryEdit
  public Boolean ir; // inventoryRemove
  public Boolean mv; // materialView
  public Boolean ma; // materialAdd
  public Boolean me; // materialEdit
  public Boolean mr; // materialRemove
  public Boolean cm; // copyMaterials
  public Boolean cc; // copyConfiguration
  public Boolean cv; // viewConfiguration
  public Boolean ce; // editConfiguration
  /**
   * Asset edit
   */
  public Boolean ae;
  /**
   * Asset remove
   */
  public Boolean ar;
  /**
   * Asset add
   */
  public Boolean aa;
  /**
   * Asset view
   */

  public Boolean av;

  /**
   * View only permission
   */

  public Boolean vp;
}
