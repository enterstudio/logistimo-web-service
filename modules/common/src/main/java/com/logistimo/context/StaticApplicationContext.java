package com.logistimo.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by charan on 27/06/17.
 */
public class StaticApplicationContext implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    StaticApplicationContext.setStaticContext(applicationContext);
  }

  private static void setStaticContext(ApplicationContext applicationContext) {
    StaticApplicationContext.applicationContext = applicationContext;
  }

  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

}
