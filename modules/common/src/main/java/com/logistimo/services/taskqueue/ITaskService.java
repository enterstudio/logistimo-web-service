package com.logistimo.services.taskqueue;

import com.logistimo.exception.TaskSchedulingException;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * Created by charan on 03/03/15.
 */
public interface ITaskService {

  // HTTP method types
  int METHOD_GET = 0;
  int METHOD_POST = 1;
  // Queue names
  String QUEUE_DEFAULT = "default";
  String QUEUE_DATASIMULATOR = "data-simulator";
  String QUEUE_SLICER = "slicer"; // used for transaction slicing for reporting
  String QUEUE_MESSAGE = "message"; // used for processing incoming messages (e.g. SMS)
  String QUEUE_OPTIMZER = "optimizer"; // used for inventory optimization (e.g. consumption rate)
  String QUEUE_EXPORTER = "exporter"; // used for exporting data (and storing in blobstore)
  String
      QUEUE_DOMAINS =
      "domains";
  // used for adding objects across domains or, more generally, super-domains tasks

  long schedule(String queueName, String url, Map<String, String> params,
                Map<String, String> headers, int methodType)
      throws TaskSchedulingException;

  long schedule(String queueName, String url, Map<String, String> params,
                Map<String, String> headers, int methodType, long domainId, String userName,
                String taskName)
      throws TaskSchedulingException;

  long schedule(String queueName, String url, Map<String, String> params,
                List<String> multiValueParams, Map<String, String> headers, int methodType,
                long etaMillis, long domainId, String userName, String taskName, String jsonData)
      throws TaskSchedulingException;

  long schedule(String queueName, String url, Map<String, String> params,
                List<String> multiValueParams, Map<String, String> headers, int methodType,
                long etaMillis, long domainId, String userName, String taskName)
      throws TaskSchedulingException;

  // Schedule to start at etaMillis (absolute time)
  long schedule(String queueName, String url, Map<String, String> params,
                Map<String, String> headers, int methodType, long etaMillis)
      throws TaskSchedulingException;

  // Schedule to start at etaMillis (absolute time)
  long schedule(String queueName, String url, Map<String, String> params,
                Map<String, String> headers, int methodType, long etaMillis, long domainId,
                String userName, String taskName)
      throws TaskSchedulingException;

  long schedule(String queueName, String url, Map<String, String> params,
                List<String> multiValueParams, Map<String, String> headers, int methodType,
                long etaMillis)
      throws TaskSchedulingException;

  long schedule(String queueName, String url, Map<String, String> params, int methodType)
      throws TaskSchedulingException;

  long schedule(String queueName, String url, String jsonData)
      throws TaskSchedulingException;

  // Method that returns a Map of params as required by TaskScheduler.schedule from a query string in the format
  // param-name1=param-value&param-name2=param-value2...
  Map<String, String> getParamsFromQueryString(String queryString);

  /**
   * Initialise the context which will initialize the configured camel routes
   */
  void initContext();

  /**
   * This method will return spring context configured in task service
   *
   * @return spring context
   */
  ApplicationContext getContext();
}
