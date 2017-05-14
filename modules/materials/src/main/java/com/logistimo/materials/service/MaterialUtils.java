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

package com.logistimo.materials.service;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.utils.MsgUtil;

import java.util.List;

/**
 * Created by vani on 18/03/17.
 */
public class MaterialUtils {
  /**
   * Converts a list of materials to a format - <material-name1>
   *                                          - <material-name2>
   * @param materials - List of materials
   * @return Material names separated by hyphen and newline
   */
  public static String getMaterialNamesString(List<IMaterial> materials) {
   if (materials == null || materials.isEmpty()) {
   return CharacterConstants.EMPTY;
   }
   StringBuilder materialNames = new StringBuilder();
   String
    prefix = MsgUtil.newLine() + CharacterConstants.HYPHEN + CharacterConstants.SPACE;
    for (IMaterial material : materials) {
      materialNames.append(prefix);
      materialNames.append(material.getName());
      }
    return materialNames.toString();
    }
}
