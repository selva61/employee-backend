package com.sentinel.hrms.service;

import com.sentinel.hrms.exception.EmailExistException;
import com.sentinel.hrms.exception.UserNameExistException;
import com.sentinel.hrms.exception.UserNotFoundException;
import com.sentinel.hrms.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    User register(String firstName, String lastName,String username, String email, String password) throws UserNotFoundException, UserNameExistException, EmailExistException;

    List<User> getUsers();

    User findUserByUserName(String username);

    User findUserByEmail(String email);

    User addNewUser(String firstName, String lastName, String userName, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage);

    User updateUser(String currentUserName, String newFirstName, String newLastName, String newUserName, String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage);

    void deleteUser(long id);

    void resetPassword(String userName,String newPassword);

    User updateProfilePicture(String userName,MultipartFile newProfileImage);

}
