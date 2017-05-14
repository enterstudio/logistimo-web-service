package com.logistimo.utils;

/**
 * Created by charan on 26/02/16.
 */
public class ThreadLocalUtil {

  // Thread local variable containing each thread's ID
  private static final ThreadLocal<ThreadContext> threadId =
      new ThreadLocal<ThreadContext>() {
        @Override
        protected ThreadContext initialValue() {
          return new ThreadContext();
        }
      };

  // Returns the current thread's unique ID, assigning it if necessary
  public static ThreadContext get() {
    return threadId.get();
  }

}
