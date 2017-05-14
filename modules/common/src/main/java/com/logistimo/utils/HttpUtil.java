/**
 *
 */
package com.logistimo.utils;

import com.logistimo.constants.Constants;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.services.utils.SSLUtilities;

import com.logistimo.logger.XLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility to work with network connections, e.g. HTTP
 *
 * @author Arun
 */
public class HttpUtil {

  // Constants
  public static final String POST = "POST";
  public static final String GET = "GET";
  // Logger
  private static final XLog xLogger = XLog.getLog(HttpUtil.class);

  // HTTP GET
  public static String get(String url, Map<String, String> params,
                           Map<String, String> reqProperties)
      throws MalformedURLException, IOException {
    return connect(GET, url, params, reqProperties);
  }

  public static String get(String url, Map<String, String> reqProperties)
      throws MalformedURLException, IOException {
    return connect(GET, url, null, reqProperties);
  }

  public static String getMulti(String url, Map<String, List<String>> params,
                                Map<String, String> reqProperties, byte[] payload,
                                String contentType)
      throws IOException {

    return connectMulti(GET, url, params, reqProperties, payload, contentType);
  }

  // HTTP POST
  public static String post(String url, Map<String, String> params,
                            Map<String, String> reqProperties)
      throws IOException {
    return connect(POST, url, params, reqProperties);
  }

  public static String postMulti(String url, Map<String, List<String>> params,
                                 Map<String, String> reqProperties, byte[] payload,
                                 String contentType)
      throws IOException {
    return connectMulti(POST, url, params, reqProperties, payload, contentType);
  }

  public static String connectMulti(String method, String url, Map<String, List<String>> params,
                                    Map<String, String> requestProperties, byte[] payload,
                                    String contentType)
      throws IOException {
    xLogger.fine("Entered httpConnect");
    String returnVal = "";
    // Get the query string
    String queryString = "";
    long startTime = System.currentTimeMillis();
    boolean isSuccess = false;
    try {
      if (params != null) {
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
          String param = it.next();
          List<String> values = params.get(param);
          if (values != null) {
            for (String value : values) {
              if (queryString.length() > 0) {
                queryString += "&";
              }
              queryString += param + "=" + URLEncoder.encode(value, "UTF-8");
            }
          }
        }
      }
      ///else if ( url.indexOf( '?' ) > 0 ) {
      ///	queryString = url.substring( url.indexOf( '?' ) + 1, url.length() );
      ///}
      xLogger.fine("HttpUtil.connect: URL = {0}, query string = {1}", url, queryString);
      SSLUtilities.trustAllHostnames();
      SSLUtilities.trustAllHttpsCertificates();
      // Open the URL connection
      URL urlObj = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
      connection.setRequestMethod(method);
      connection.setConnectTimeout(10000); // 10 seconds max. in GAE (default is 5 seconds)

      // Set the properties, if present
      if (requestProperties != null) {
        Iterator<String> it = requestProperties.keySet().iterator();
        while (it.hasNext()) {
          String prop = it.next();
          String val = requestProperties.get(prop);
          if (val != null) {
            connection.setRequestProperty(prop, val);
          }
        }
      }
      connection.setRequestProperty(Constants.X_APP_ENGINE_TASK_NAME, Constants.INTERNAL_TASK);
      // Get the output stream for writing param data
      if (!queryString.isEmpty()) {
        connection.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(queryString);
        out.close();
      } else if (payload != null) {
        if (contentType != null) {
          connection.setRequestProperty("Content-Type", contentType);
        }
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        os.write(payload);
        os.close();
      }
      // Read returned value
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String decodedString = null;
      int i = 0;
      while ((decodedString = in.readLine()) != null) {
        if (i > 0) {
          returnVal += "\n";
        }
        returnVal += decodedString;
        ++i;
      }
      in.close();
      isSuccess = true;
    } catch (UnsupportedEncodingException e) {
      xLogger.warn("UnsupportedCodingException: {0}", e.getMessage());
    } finally {
      xLogger.info("CMulti: URL = {0}, query string = {1}, timetaken = {2}, success = {3}", url,
          queryString, System.currentTimeMillis() - startTime, isSuccess);
    }
    xLogger.fine("Exiting httpConnect");
    return returnVal;
  }

  // Do a HTTP get or post
  public static String connect(String method, String url, Map<String, String> params,
                               Map<String, String> requestProperties)
      throws MalformedURLException, IOException {
    xLogger.fine("Entered httpConnect");
    String returnVal = "";
    long startTime = System.currentTimeMillis();
    // Get the query string
    String queryString = "";
    boolean isSuccess = false;
    try {
      if (params != null) {
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
          String param = it.next();
          String value = params.get(param);
          if (value != null) {
            if (queryString.length() > 0) {
              queryString += "&";
            }
            queryString += param + "=" + URLEncoder.encode(value, "UTF-8");
          }
        }
      }
      ///else if ( url.indexOf( '?' ) > 0 ) {
      ///	queryString = url.substring( url.indexOf( '?' ) + 1, url.length() );
      ///}
      xLogger.fine("HttpUtil.connect: URL = {0}, query string = {1}", url, queryString);
      if (!ConfigUtil.getBoolean("gae.deployment", false)) {
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
      }
      // Open the URL connection
      URL urlObj = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
      connection.setRequestMethod(method);
      connection.setConnectTimeout(15000); // 10 seconds max. in GAE (default is 5 seconds)
      // Set the properties, if present
      if (requestProperties != null) {
        Iterator<String> it = requestProperties.keySet().iterator();
        while (it.hasNext()) {
          String prop = it.next();
          String val = requestProperties.get(prop);
          if (val != null) {
            connection.setRequestProperty(prop, val);
          }
        }
      }
      // Get the output stream for writing param data
      if (!queryString.isEmpty()) {
        connection.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(queryString);
        out.close();
      }
      // Read returned value
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String decodedString = null;
      int i = 0;
      while ((decodedString = in.readLine()) != null) {
        if (i > 0) {
          returnVal += "\n";
        }
        returnVal += decodedString;
        ++i;
      }
      in.close();
      isSuccess = true;
    } catch (UnsupportedEncodingException e) {
      xLogger.warn("UnsupportedCodingException: {0}", e.getMessage());
    } finally {
      xLogger.info("C: URL = {0}, query string = {1}, timetaken = {2}, success = {3}", url,
          queryString, System.currentTimeMillis() - startTime, isSuccess);
    }
    xLogger.fine("Exiting httpConnect");
    return returnVal;
  }

  // Get the URL base given a servlet
  public static String getUrlBase(HttpServletRequest request) {
    if ((request.getServerPort() == 80) || (request.getServerPort() == 443)) {
      return request.getScheme() + "://" + request.getServerName();
    } else {
      return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
  }
}
