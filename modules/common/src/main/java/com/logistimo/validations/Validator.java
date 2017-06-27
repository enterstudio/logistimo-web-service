package com.logistimo.validations;

import com.logistimo.exception.ValidationException;

/**
 * Created by charan on 22/06/17.
 */
public interface Validator {

  void validate() throws ValidationException;
}
