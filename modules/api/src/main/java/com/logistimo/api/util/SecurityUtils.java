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

package com.logistimo.api.util;

import com.logistimo.assets.AssetUtil;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.ServiceException;
import com.logistimo.constants.CharacterConstants;

import com.logistimo.logger.XLog;

import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

public class SecurityUtils {

  public static final String DOMAIN_HEADER = "d";
  private final static String HMAC_SHA1_ALGORITHM = "HmacSHA1";
  private static final XLog xLogger = XLog.getLog(SecurityUtils.class);


  public static Long getDomainId(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request
        .getSession());
    String userId = sUser.getUsername();
    return SessionMgr.getCurrentDomain(request.getSession(), userId);

  }

  public static SecureUserDetails getUserDetails(HttpServletRequest request) {
    return SecurityMgr.getUserDetails(request.getSession());
  }

  public static String getReqCookieUserDomain(HttpServletRequest req) {
    return req.getHeader(DOMAIN_HEADER);
  }

  public static Long getReqCookieDomain(HttpServletRequest req) {
    try {
      String reqUserDomain = getReqCookieUserDomain(req);
      if (reqUserDomain != null && reqUserDomain.contains(CharacterConstants.COLON)) {
        return Long.valueOf(
            reqUserDomain.substring(reqUserDomain.lastIndexOf(CharacterConstants.COLON) + 1));
      }
    } catch (Exception e) {
      xLogger.warn("Error while getting request domain ", e);
    }
    return null;
  }

  public static boolean verifyAssetServiceRequest(String signature, String data)
      throws ServiceException {
    String secretKey = AssetUtil.getSecretKeyFromLogistimo();
    if (StringUtils.isEmpty(secretKey)) {
      throw new ServiceException("Internal server error");
    }
    String hmac = hmac(secretKey, data);
    if (signature == null || signature.isEmpty()) {
      xLogger.severe("Invalid or null signature in request: {0}", signature);
      return false;
    }
    if (hmac == null || hmac.isEmpty()) {
      xLogger.severe("Signature created using secret key and data is invalid or null: {0}", hmac);
      return false;
    }
    return signature.equals(hmac);
  }

  private static String hmac(String secret, String data) throws IllegalArgumentException {
    xLogger.fine("Entering hmac");
    try {
      if (secret == null || secret.isEmpty() || data == null || data.isEmpty()) {
        return null;
      }
      SecretKeySpec signatureKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
      Mac m = Mac.getInstance(HMAC_SHA1_ALGORITHM);
      m.init(signatureKey);
      byte[] rawHmac = m.doFinal(data.getBytes());
      String result = new String(Base64.encodeBase64(rawHmac));
      return result;
    } catch (GeneralSecurityException e) {
      xLogger
          .severe("Unexpected error while creating hash: {0}. Exception: {1}", e.getMessage(), e);
      throw new IllegalArgumentException();
    }
  }
}
