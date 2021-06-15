package com.sentinel.hrms.service;

import com.sentinel.hrms.exception.EmailExistException;
import com.sentinel.hrms.exception.UserNameExistException;
import com.sentinel.hrms.exception.UserNotFoundException;
import com.sentinel.hrms.model.User;

import java.util.List;

public interface UserService {

    User register(String firstName, String lastName,String username, String email, String password) throws UserNotFoundException, UserNameExistException, EmailExistException;

    List<User> getUsers();

    User findUserByUserName(String username);

    User findUserByEmail(String email);

}
