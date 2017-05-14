package com.logistimo.auth.service;

import com.logistimo.exception.UnauthorizedException;
import com.logistimo.users.entity.IUserToken;
import com.sun.media.sound.InvalidDataException;

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
