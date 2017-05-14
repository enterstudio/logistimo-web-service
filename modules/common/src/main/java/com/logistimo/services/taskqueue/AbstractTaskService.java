package com.logistimo.services.taskqueue;

import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by charan on 03/03/15.
 */
public abstract class AbstractTaskService implements ITaskService {

  // Logger
  private final XLog xLogger = XLog.getLog(AbstractTaskService.class);

  /**
   * Schedule a task for execution using GAE task scheduler.
   *
   * @param queueName  The name of queue to schedule in.
   * @param url        The URL that represents the task
   * @param methodType The HTTP method type
   */
  @Override
  public long schedule(String queueName, String url, Map<String, String> params,
                       Map<String, String> headers, int methodType)
      throws TaskSchedulingException {
    return schedule(queueName, url, params, headers, methodType, -1);
  }

  @Override
  public long schedule(String queueName, String url, Map<String, String> params,
                       Map<String, String> headers, int methodType, long domainId, String userName,
                       String taskName)
      throws TaskSchedulingException {
    return schedule(queueName, url, params, null, headers, methodType, -1, domainId, userName,
        taskName, null);
  }


  // Schedule to start at etaMillis (absolute time)
  @Override
  public long schedule(String queueName, String url, Map<String, String> params,
                       Map<String, String> headers, int methodType, long etaMillis)
      throws TaskSchedulingException {
    return schedule(queueName, url, params, null, headers, methodType, etaMillis);
  }


  // Schedule to start at etaMillis (absolute time)
  @Override
  public long schedule(String queueName, String url, Map<String, String> params,
                       Map<String, String> headers, int methodType, long etaMillis, long domainId,
                       String userName, String taskName)
      throws TaskSchedulingException {
    return schedule(queueName, url, params, null, headers, methodType, etaMillis, domainId,
        userName, taskName, null);
  }


  @Override
  public long schedule(String queueName, String url, Map<String, String> params,
                       List<String> multiValueParams, Map<String, String> headers, int methodType,
                       long etaMillis)
      throws TaskSchedulingException {
    return schedule(queueName, url, params, multiValueParams, headers, methodType, etaMillis, -1,
        null, null, null);
  }

  @Override
  public long schedule(String queueName, String url, Map<String, String> params, int methodType)
      throws TaskSchedulingException {
    return schedule(queueName, url, params, null, methodType);
  }

  // Method that returns a Map of params as required by TaskScheduler.schedule from a query string in the format
  // param-name1=param-value&param-name2=param-value2...
  @Override
  public Map<String, String> getParamsFromQueryString(String queryString) {
    xLogger.fine("Entering getParamsFromQueryString");
    Map<String, String> params = null;
    if (queryString == null || queryString.isEmpty()) {
      return null;
    }
    params = new HashMap<String, String>();
    // Split the queryString based on &
    String[] paramValueArray = queryString.split("&");
    for (int i = 0; i < paramValueArray.length; i++) {
      String[] paramValuePair = paramValueArray[i].split("=");
      String paramName = paramValuePair[0];
      String paramValue = "";
      if (paramValuePair.length > 1) {
        paramValue = paramValuePair[1];
      }
      // Update filter map
      params.put(paramName, paramValue);
    }
    xLogger.fine("Exiting getParamsFromQueryString");
    return params;
  }

  public long schedule(String queueName, String url, String jsonData)
      throws TaskSchedulingException {
    return schedule(queueName, url, null, null, null, ITaskService.METHOD_POST, -1, -2, null, null,
        jsonData);
    //schedule( String queueName, String url, Map<String,String> params, List<String> multiValueParams, Map<String,String> headers, int methodType, long etaMillis, long domainId, String userName, String taskName, String jsonData )
  }

  public long schedule(String queueName, String url, Map<String, String> params,
                       List<String> multiValueParams, Map<String, String> headers, int methodType,
                       long etaMillis, long domainId, String userName, String taskName)
      throws TaskSchedulingException {
    return schedule(queueName, url, params, multiValueParams, headers, methodType, etaMillis,
        domainId, userName, taskName, null);
  }
}
