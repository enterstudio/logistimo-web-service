package com.logistimo.utils;


import com.logistimo.constants.CharacterConstants;
import com.logistimo.services.ServiceException;
import com.logistimo.exception.InvalidServiceException;

import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public class PropertyUtil {
  private static final XLog xLogger = XLog.getLog(PropertyUtil.class);

  /**
   * Parse the given string {@code relatedObjectsStr} and generate {@link java.util.Map} with table name as
   * key and field names as value.
   *
   * @param relatedObjectsStr value of config property to be parsed
   * @return a map with table name as key and field names as value.
   */
  public static Map<String, String[]> parseProperty(String relatedObjectsStr) throws ServiceException {
    xLogger.fine("Entering parseProperty");
    if (relatedObjectsStr == null || relatedObjectsStr.isEmpty()) {
      xLogger.warn("Error in reading property: {0}", relatedObjectsStr);
      throw new InvalidServiceException("Error in reading property :" + relatedObjectsStr);
    }
    String[] relObjectArr = relatedObjectsStr.split(CharacterConstants.COMMA);
    Map<String, String[]> map = new HashMap<>(relObjectArr.length);
    for (String aRelObjectArr : relObjectArr) {
      String[] relObject = aRelObjectArr.split(CharacterConstants.COLON);
      if (relObject[1].contains(CharacterConstants.AMPERSAND)) {
        map.put(relObject[0], relObject[1].split(CharacterConstants.AMPERSAND));
      } else {
        String[] fields = {relObject[1]};
        map.put(relObject[0], fields);
      }
    }
    xLogger.fine("Exiting parseProperty");
    return map;
  }
}
