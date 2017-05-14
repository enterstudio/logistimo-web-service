/**
 *
 */
package com.logistimo.pagination.processor;

import com.logistimo.pagination.Results;

import javax.jdo.PersistenceManager;

/**
 * @author Arun
 */
public interface Processor {
  // Process the results, and optionally return an output Object, if any
  // NOTE: It is important that the output object, if returned, can be serialized to a String using the toString() method
  String process(Long domainId, Results results, String prevOutput, PersistenceManager pm)
      throws ProcessingException;

  String getQueueName();
}
