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

package com.logistimo.auth.service;

import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.users.entity.IUserToken;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.io.IOException;
import java.util.InputMismatchException;


/**
 * Created by naveensnair on 04/11/15.
 */
public interface AuthenticationService extends Service {

  String JWTKEY = "jwt.key";


  IUserToken generateUserToken(String userId) throws ServiceException, ObjectNotFoundException;

  String authenticateToken(String token, Integer accessInitiator)
      throws UnauthorizedException, ServiceException, ObjectNotFoundException;

  Boolean clearUserTokens(String userId);

  /**
   * Update users session with new session Id. This also clears user tokens generated for mobile.
   * sessionId can be null, Send null when being logged in via mobile.
   */
  void updateUserSession(String userId, String sessionId);

  String getUserToken(String userId);

  String resetPassword(String userId, int mode, String otp, String src,
                              String au)
      throws ServiceException, ObjectNotFoundException, MessageHandlingException, IOException,
      InputMismatchException;

  String generateOTP(String userId, int mode, String src, Long domainId, String hostUri)
      throws MessageHandlingException, IOException, ServiceException, ObjectNotFoundException,
      InvalidDataException;

  String createJWT(String userid, long ttlMillis);

  String decryptJWT(String token);

  String setNewPassword(String token, String newPassword, String confirmPassword)
      throws Exception;

  String generatePassword(String id);
}
