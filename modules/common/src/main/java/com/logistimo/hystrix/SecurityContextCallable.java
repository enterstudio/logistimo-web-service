package com.logistimo.hystrix;

import com.logistimo.security.SecureUserDetails;
import com.logistimo.utils.ThreadLocalUtil;

import java.util.concurrent.Callable;

/**
 * Created by charan on 15/07/17.
 */
public class SecurityContextCallable<T> implements Callable<T> {
  private final Callable<T> actual;
  private final SecureUserDetails secureUserDetails;

  public SecurityContextCallable(Callable<T> actual) {
    this.actual = actual;
    // copy whatever state such as MDC
    this.secureUserDetails = ThreadLocalUtil.get().getSecureUserDetails();
  }

  @Override
  public T call() throws Exception {
    try {
      // set the state of this thread to that of its parent
      // this is where the MDC state and other ThreadLocal values can be set
      ThreadLocalUtil.get().setSecureUserDetails(secureUserDetails);
      // execute actual Callable with the state of the parent
      return actual.call();
    } finally {
      // restore this thread back to its original state
      ThreadLocalUtil.get().setSecureUserDetails(null);
    }
  }
}
