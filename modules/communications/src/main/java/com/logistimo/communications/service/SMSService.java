/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.communications.service;

import com.logistimo.AppFactory;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.GeneralConfig;
import com.logistimo.config.models.SMSConfig;
import com.logistimo.config.models.SMSConfig.ProviderConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.services.cache.MemcacheService;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.ServiceResponse;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.utils.HttpUtil;
import com.logistimo.utils.MessageUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Arun
 */
public class SMSService extends MessageService {

  // Direction
  public static final String INCOMING = "incoming";
  public static final String OUTGOING = "outgoing";

  // Parameters sent by a Gateway
  public static final String PARAM_USERID = "%uid%";
  public static final String PARAM_PASSWORD = "%pwd%";
  public static final String PARAM_SENDERID = "%sid%";
  public static final String PARAM_MOBILENO = "%mno%";
  public static final String PARAM_MESSAGE = "%msg%";
  public static final String PARAM_PORT = "%prt%";
  public static final String PARAM_JOBID = "%jid%";
  public static final String PARAM_DONETIME = "%dtm%";
  public static final String PARAM_STATUS = "%sts%";
  public static final String PARAM_URL = "%url%";
  public static final String PARAM_RECEIVEDON = "%ron%";

  public static final int MAX_ADDRESSES_DEFAULT = 100; // max. addresses per call
  public static final String SMS_PREFIX = "S_";
  public static final String SMS_DATE_FORMAT = "yyMMdd";
  public static final String SMS_MAX_USER_KEY = "-2";
  public static final String SMS_MAX_DOMAIN_KEY = "-3";
  public static final String SMS_DUPLICATE_KEY = "-4";
  public static final String SMS_VALID_KEY = "0";
  public static final String FORMATTED = "f";
  public static final String UNFORMATTED = "u";
  private static final int MAX_TRIALS = 3;
  // Gateway config.
  private SMSConfig.ProviderConfig provider = null;

  public SMSService(String countryCode, String direction) throws MessageHandlingException {
    xLogger.fine("SMSService: country-code: {0}, direction {1}", countryCode, direction);
    try {
      SMSConfig smsConfig = SMSConfig.getInstance();
      String providerId = smsConfig.getProviderId(countryCode, direction);
      xLogger.fine("provider ID: {0}", providerId);
      provider = smsConfig.getProviderConfig(providerId);
    } catch (ConfigurationException e) {
      throw new MessageHandlingException(e.getMessage());
    }
  }

  public SMSService(String providerId) throws MessageHandlingException {
    try {
      SMSConfig smsConfig = SMSConfig.getInstance();
      provider = smsConfig.getProviderConfig(providerId);
    } catch (ConfigurationException e) {
      throw new MessageHandlingException(e.getMessage());
    }
  }

  public static SMSService getInstance() throws MessageHandlingException {
    return getInstance(Constants.COUNTRY_DEFAULT);
  }

  // Get an instance of this service (by default, an "outgoing" service will be returned)
  public static SMSService getInstance(String countryCode) throws MessageHandlingException {
    return getInstance(countryCode, OUTGOING);
  }

  // Get an instance of this service, given a country code and a direction (incoming/outgoing)
  public static SMSService getInstance(String countryCode, String direction)
      throws MessageHandlingException {
    return new SMSService(countryCode, direction);
  }

  public static SMSService getInstanceByProvider(String providerId)
      throws MessageHandlingException {
    return new SMSService(providerId);
  }

  public String getProviderId() {
    return provider.getString(SMSConfig.ProviderConfig.PROVIDER_ID);
  }

  // Send a SMS message of the given type
  // An array of ServiceResponse is returned, one for each call
  protected ServiceResponse doSend(List<String> addresses, String message, int messageType,
                                   String wapUrl, String port, Long domainId)
      throws MessageHandlingException, IOException {
    xLogger.fine("Entered sendMessage");
    // Get credentials
    String userId = provider.getString(ProviderConfig.USER_ID);
    String password = provider.getString(ProviderConfig.PASSWORD);
    String senderId = provider.getString(ProviderConfig.SENDER_ID);
    // Get the relevant URL for sending
    String url;
    if (messageType == UNICODE) {
      url = provider.getString(ProviderConfig.UNICODE_URL);
    } else if (messageType == BINARY) {
      url = provider.getString(ProviderConfig.BINARYTOPORT_URL);
    } else if (messageType == WAPPUSH) {
      url = provider.getString(ProviderConfig.WAPPUSH_URL);
    } else {
      url = provider.getString(ProviderConfig.NORMAL_URL);
    }
    // Add appendix to message, if present
    String appendix = provider.getString(ProviderConfig.APPENDIX);
    if (appendix != null && !appendix.isEmpty()) {
      message += " " + appendix;
    }
    // Get the address formating regexes
    String replacePlus = provider.getString(ProviderConfig.REPLACE_PHONENUMBER_PLUS);
//		String replaceSpace = provider.getString( ProviderConfig.REPLACE_PHONENUMBER_SPACE );
    Map<String, Map<String, List<String>>>
        address =
        filterAddress(domainId, message, formatMobileNumbers(addresses, replacePlus), addresses);

    boolean isJobIdUnique = "yes".equals(provider.getString(ProviderConfig.PARAM_JIDUNIQUE));
    Map<String, List<String>> skippedResponse = new HashMap<>(3);
    if (address.containsKey(SMS_MAX_USER_KEY)) {
      skippedResponse.put(SMS_MAX_USER_KEY, address.get(SMS_MAX_USER_KEY).get(UNFORMATTED));
    }
    if (address.containsKey(SMS_MAX_DOMAIN_KEY)) {
      skippedResponse.put(SMS_MAX_DOMAIN_KEY, address.get(SMS_MAX_DOMAIN_KEY).get(UNFORMATTED));
    }
    if (address.containsKey(SMS_DUPLICATE_KEY)) {
      skippedResponse.put(SMS_DUPLICATE_KEY, address.get(SMS_DUPLICATE_KEY).get(UNFORMATTED));
    }
    ServiceResponse
        svcResp =
        new ServiceResponse(provider.getString(ProviderConfig.PROVIDER_ID), isJobIdUnique,
            skippedResponse);

    // Split addresses by max. limit on number of addresses
    List<List<String>> addressLists = splitByMaxLimit(address.get(SMS_VALID_KEY).get(FORMATTED));
    List<List<String>>
        unfAddressLists =
        splitByMaxLimit(address.get(SMS_VALID_KEY).get(UNFORMATTED));

    for (int i = 0; i < addressLists.size(); i++) {
      List<String> addressList = addressLists.get(i);
      // Get addresses in CSV format
      String addressesCSV = MessageUtil.getCSV(addressList);
      // Replace variables in the URL
      url =
          replaceVariables(url, userId, password, senderId, addressesCSV, messageType, message,
              wapUrl, port);
      // Invoke URL and get the response
      invokeService(url, unfAddressLists.get(i), svcResp);
      incrementCounters(domainId, message, addressList);
    }
    xLogger.fine("Exiting sendMessage");
    return svcResp;
  }

  /**
   * Enforces SMS daily limits by user and domain, along with de-duplication.
   *
   * @return <ResponseCode: Map< Formatted and Unformatted addresses > >
   */
  private Map<String, Map<String, List<String>>> filterAddress(Long domainId, String message,
                                                               List<String> addresses,
                                                               List<String> noFormatAddresses) {
    Map<String, Map<String, List<String>>> filteredAddress = new HashMap<>(3);
    filteredAddress.put(SMS_MAX_USER_KEY, new HashMap<>(getTemplate()));
    filteredAddress.put(SMS_MAX_DOMAIN_KEY, new HashMap<>(getTemplate()));
    filteredAddress.put(SMS_DUPLICATE_KEY, new HashMap<>(getTemplate()));
    filteredAddress.put(SMS_VALID_KEY, new HashMap<>(getTemplate()));
    try {
      SimpleDateFormat df = new SimpleDateFormat(SMS_DATE_FORMAT);
      String date = df.format(new Date());
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig c = cms.getConfiguration(IConfig.GENERALCONFIG);
      GeneralConfig config = new GeneralConfig(c.getConfig());
      MemcacheService cache = AppFactory.get().getMemcacheService();
      for (int i = 0; i < addresses.size(); i++) {
        String address = addresses.get(i);
        String noFormatAddress = noFormatAddresses.get(i);
        boolean found = false;
        Integer
            cnt =
            (Integer) cache.get(SMS_PREFIX + address + CharacterConstants.UNDERSCORE + date);
        if (cnt != null && cnt >= config.getSmsMaxCountUser()) {
          filteredAddress.get(SMS_MAX_USER_KEY).get(UNFORMATTED).add(noFormatAddress);
          found = true;
        }
        if (!found) {
          cnt = (Integer) cache.get(SMS_PREFIX + domainId + CharacterConstants.UNDERSCORE + date);
          if (cnt != null && cnt >= config.getSmsMaxCountDomain()) {
            filteredAddress.get(SMS_MAX_DOMAIN_KEY).get(UNFORMATTED).add(noFormatAddress);
            found = true;
          }
        }
        if (!found
            && cache.get(message.hashCode() + CharacterConstants.UNDERSCORE + address) != null) {
          filteredAddress.get(SMS_DUPLICATE_KEY).get(UNFORMATTED).add(noFormatAddress);
          found = true;
        }
        if (!found) {
          filteredAddress.get(SMS_VALID_KEY).get(FORMATTED).add(address);
          filteredAddress.get(SMS_VALID_KEY).get(UNFORMATTED).add(noFormatAddress);
        }
      }
    } catch (Exception e) {
      xLogger.warn("Error in limiting SMS and deduplication check allowing all to send", e);
      filteredAddress.clear();
      Map<String, List<String>> template = new HashMap<>(2);
      template.put(FORMATTED, addresses);
      template.put(UNFORMATTED, noFormatAddresses);
      filteredAddress.put(SMS_VALID_KEY, template);
    }
    return filteredAddress;
  }

  private Map<String, List<String>> getTemplate() {
    Map<String, List<String>> template = new HashMap<>(2);
    template.put(FORMATTED, new ArrayList<String>(1));
    template.put(UNFORMATTED, new ArrayList<String>(1));
    return template;
  }

  private void incrementCounters(Long domainId, String message, Iterable<String> addresses) {
    try {
      SimpleDateFormat df = new SimpleDateFormat(SMS_DATE_FORMAT);
      String date = df.format(new Date());
      MemcacheService cache = AppFactory.get().getMemcacheService();
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig c = cms.getConfiguration(IConfig.GENERALCONFIG);
      GeneralConfig config = new GeneralConfig(c.getConfig());
      String domainKey = SMS_PREFIX + domainId + CharacterConstants.UNDERSCORE + date;
      for (String address : addresses) {
        String userKey = SMS_PREFIX + address + CharacterConstants.UNDERSCORE + date;
        Integer cnt = (Integer) cache.get(userKey);
        cache.put(userKey, (cnt == null ? 0 : cnt) + 1);
        cnt = (Integer) cache.get(domainKey);
        cache.put(domainKey, (cnt == null ? 0 : cnt) + 1);
        cache.put(message.hashCode() + CharacterConstants.UNDERSCORE + address, true,
            config.getSmsDedupDuration() * 60); // Expiry in seconds
      }
    } catch (Exception e) {
      xLogger.warn("Error while incrementing SMS and deduplication counters. Skipping the counters",
          e);
    }
  }

  // Parse the date in the provider's format
  public Date parseDate(String dateString) throws ParseException {
    xLogger.fine("Entered parseDate: {0}", dateString);
    // Get the date format
    String dateFormat = provider.getString(ProviderConfig.FORMAT_DATETIME);
    if (dateFormat == null || dateFormat.isEmpty()) {
      return null;
    }
    SimpleDateFormat df = new SimpleDateFormat(dateFormat);
    // TODO: get the timezone of the provider via a parameter in the config and use that to get GMT time
    return df.parse(dateString);
  }

  // Specify the parameter mapping from standard to custom parameters - e.g. incoming SMS parameters
  public String getParameterName(String paramName) {
    return provider.getParameterName(paramName);
  }

  // Get the status description
  public String getStatusMessage(String statusCode, Locale locale) {
    if (statusCode == null || statusCode.isEmpty()) {
      statusCode = provider.getString(ProviderConfig.STATUS_CODE_DEFAULT);
      /// old: statusCode = "9"; // default code - message sent
    }
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String key = provider.getStatusResourceKey(statusCode);
    if (messages != null && key != null && !key.isEmpty()) {
      return messages.getString(key);
    }
    return "";
  }

  // Get the formatted address, if any
  public String getFormattedAddress(String address) {
    // Get the address formatting regex
    String replacePlus = provider.getString(ProviderConfig.REPLACE_PHONENUMBER_PLUS);
    //String replaceSpace = provider.getString( ProviderConfig.REPLACE_PHONENUMBER_SPACE );
    String rPlus;
    if (replacePlus != null) {
      rPlus = replacePlus;
    } else {
      rPlus = "";
    }
    return address.replace("+", rPlus).replaceAll(" ", "");
  }

  // Format our standard mobile phones nos. as required by the provider - e.g.. 91XXXXXXXXX
  // In our format, remove the + and space in the mobile phone number as required
  private List<String> formatMobileNumbers(Iterable<String> mobileNos, String replacePlus) {
    List<String> fnos = new ArrayList<>();
    for (String mobileNo : mobileNos) {
      fnos.add(getFormattedMobileNo(mobileNo, replacePlus));
    }
    xLogger.fine("Formatted mobile nos: {0}", fnos);
    return fnos;
  }

  // Get the formatted phone number, in provider specific format
  private String getFormattedMobileNo(String mobileNo, String replacePlus) {
    if (mobileNo == null || mobileNo.isEmpty()) {
      return null;
    }
    String rPlus;
    if (replacePlus != null) {
      rPlus = replacePlus;
    } else {
      rPlus = "";
    }
    return mobileNo.replace("+", rPlus).replaceAll(" ", "");
  }

  // Replace the variables in a given URL
  private String replaceVariables(String url, String userId, String password, String senderId,
                                  String addresses, int messageType, String message, String wapUrl,
                                  String port) {
    Map<String, String> rmap = new HashMap<>();
    rmap.put(PARAM_USERID, userId);
    rmap.put(PARAM_PASSWORD, password);
    rmap.put(PARAM_SENDERID, senderId);
    rmap.put(PARAM_MOBILENO, addresses);
    rmap.put(PARAM_MESSAGE, encode(message, messageType));
    if (wapUrl != null) {
      rmap.put(PARAM_URL, encode(wapUrl, NORMAL));
    }
    if (port != null) {
      rmap.put(PARAM_PORT, port);
    }
    return replace(url, rmap);
  }

  // Replace variables in a given string
  private String replace(String str, Map<String, String> replaceMap) {
    for (String var : replaceMap.keySet()) {
      str = str.replace(var, replaceMap.get(var));
    }
    return str;
  }

  // Encode a param. value
  private String encode(String str, int messageType) {
    try {
      if (messageType == UNICODE) {
        return new String(
            Hex.encodeHex(str.getBytes("UTF-16"))); /// encodeHexString( str.getBytes( "UTF-16" ) );
      } else {
        return URLEncoder.encode(str, "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      return str;
    }
  }

  // Invoke a HTTP based Service
  private void invokeService(String url, List<String> addresses, ServiceResponse svcResponse)
      throws IOException {
    xLogger.fine("Entered invokeService");
    for (int i = 1; i <= MAX_TRIALS; i++) {
      try {
        String response = HttpUtil.get(url, null, null);
        xLogger.fine("Response: {0}", response);
        updateServiceResponse(response, addresses, svcResponse);
        xLogger.info("SMS request successful for url: {0} response: {1}", url, response);
        break; // move out of the loop!
      } catch (IOException e) {
        xLogger.warn("IOException on trial {0} for url {3} will be trying {1} more times: {2}", i,
            (MAX_TRIALS - i), e.getMessage(), url);
        if (i == MAX_TRIALS) {
          throw new IOException(
              "Error when connection to SMS Gateway to send message. Please try again later.");
        }
      }
    }
    xLogger.fine("Exiting invokeService");
  }

  // Get the appropriate service response
  private void updateServiceResponse(String resp, List<String> addresses, ServiceResponse svcResp) {
    String jobId = getJobId(resp);
    xLogger.fine("JobId: {0}", jobId);
    if (jobId == null) {
      svcResp.setMethod(ServiceResponse.METHOD_MESSAGE);
    } else {
      svcResp.setMethod(ServiceResponse.METHOD_ID);
    }
    svcResp.addResponse(jobId, addresses);
  }

  // Get the jobId from a gateway response
  protected String getJobId(String resp) {
    xLogger.fine("Entered getJobId");
    if (StringUtils.isNotBlank(resp) || resp.trim().startsWith(Constants.ERROR)) {
      // Get the jobId regex, if any
      String[] regexes = provider.getStrings(ProviderConfig.REGEXES_JOBID);
      if (regexes == null || regexes.length == 0) {
        return resp;
      }
      for (String regex : regexes) {
        if (regex.isEmpty()) {
          return resp;
        }
        try {
          Pattern p = Pattern.compile(regex);
          Matcher m = p.matcher(resp);
          boolean result = m.find();
          xLogger.fine("regex: {0}, result: {1}, matched: {2}", regex, result, m.group());
          if (result) {
            return m.group();
          }
        } catch (Exception e) {
          return resp;
        }
      }
      xLogger.fine("Exiting getJobId");
    }
    return null;
  }

  // Split numbers into multiple sets of MAX_NUMBERS each, in case they exceed the max.
  private List<List<String>> splitByMaxLimit(List<String> list) {
    // Get the max. addresses per call
    Integer maxAddressesI = provider.getInteger(ProviderConfig.MAX_ADDRESSES);
    if (maxAddressesI == null) {
      maxAddressesI = MAX_ADDRESSES_DEFAULT;
    }
    int maxAddresses = maxAddressesI;
    // Get list size
    int size = list.size();
    List<List<String>> lists = new ArrayList<>(1);
    if (size <= maxAddresses) {
      lists.add(list);
      return lists;
    }
    // Split the list into multiples, each of max. size of MAX_NUMBERS
    Iterator<String> it = list.iterator();
    int i = 0;
    List<String> sublist = new ArrayList<>(maxAddresses);
    while (it.hasNext()) {
      String val = it.next();
      if (i < maxAddresses) {
        sublist.add(val);
        i++;
      } else {
        lists.add(sublist);
        int remaining = list.size() - (lists.size() * maxAddresses);
        sublist = new ArrayList<>(remaining > maxAddresses ? maxAddresses : remaining);
        i = 0;
      }
    }
    // Add the last list
    if (i != 0) {
      lists.add(sublist);
    }
    return lists;
  }
}
