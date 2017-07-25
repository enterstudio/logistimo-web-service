package com.logistimo.exception;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;

/**
 * Created by charan on 22/07/17.
 */
public interface ExceptionWithCodes {

  default String getLocalisedMessage(Locale locale) {
    if (StringUtils.isNotBlank(getCode())) {
      return ExceptionUtils.constructMessage(getCode(), locale, getArguments());
    }
    return getLocalizedMessage();
  }

  String getLocalizedMessage();

  Object[] getArguments();

  String getCode();
}
