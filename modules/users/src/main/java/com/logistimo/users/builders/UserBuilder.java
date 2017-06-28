package com.logistimo.users.builders;

import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.models.UserContactModel;
import com.logistimo.users.service.UsersService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charan on 26/06/17.
 */
@Component
public class UserBuilder {

  @Autowired
  private UsersService usersService;

  public UserContactModel buildUserContactModel(String userId) throws ObjectNotFoundException {
    return buildUserContactModel(userId, new UserContactModel());
  }

  public UserContactModel buildUserContactModel(String userId, UserContactModel model)
      throws ObjectNotFoundException {
    model.setUserId(userId);
    IUserAccount account = usersService.getUserAccount(userId);
    model.setEmail(account.getEmail());
    model.setName(account.getFullName());
    model.setPhone(account.getMobilePhoneNumber());
    return model;
  }
}
