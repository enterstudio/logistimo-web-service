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

package com.logistimo.api.util;

import org.apache.commons.lang.StringUtils;
import com.logistimo.constants.CharacterConstants;

/**
 * @author Smriti
 */
public class CommonUtil {
  public static String getAddress(String city,String taluk, String district, String state) {
    StringBuilder address = new StringBuilder();
    if (StringUtils.isNotBlank(city)) {
      address.append(city).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);
    }
    if (StringUtils.isNotBlank(taluk)) {
      address.append(taluk).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);
    }
    if (StringUtils.isNotBlank(district)) {
      address.append(district).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);

    }
    if (StringUtils.isNotBlank(state)) {
      address.append(state).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);
    }
    address.setLength(address.length() - 2);
    return address.toString();
  }
}
