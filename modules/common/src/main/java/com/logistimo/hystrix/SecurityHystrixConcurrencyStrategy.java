package com.logistimo.hystrix;

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;

import java.util.concurrent.Callable;

/**
 * Created by charan on 15/07/17.
 */
public class SecurityHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {


  public <T> Callable<T> wrapCallable(Callable<T> callable) {
    return new SecurityContextCallable<>(callable);
  }


}
