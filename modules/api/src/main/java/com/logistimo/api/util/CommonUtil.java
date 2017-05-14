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
