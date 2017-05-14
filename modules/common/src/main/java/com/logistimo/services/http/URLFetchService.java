package com.logistimo.services.http;


import java.net.URL;

/**
 * Created by charan on 16/03/15.
 */
public interface URLFetchService {
  HttpResponse post(URL urlObj, byte[] payload, String userName, String password, int timeout);

  HttpResponse get(URL urlObj, String userName, String password, int timeout);
}
