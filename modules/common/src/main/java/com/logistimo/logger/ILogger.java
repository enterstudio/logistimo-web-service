package com.logistimo.logger;

/**
 * Created by charan on 04/03/15.
 */
public interface ILogger {

  void fine(String msgTemplate, Object... params);

  void finer(String msgTemplate, Object... params);

  void finest(String msgTemplate, Object... params);

  void info(String msgTemplate, Object... params);

  void warn(String msgTemplate, Object... params);

  void severe(String msgTemplate, Object... params);
}
