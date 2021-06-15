package com.sentinel.hrms.service;

import com.sentinel.hrms.exception.EmailExistException;
import com.sentinel.hrms.exception.UserNameExistException;
import com.sentinel.hrms.exception.UserNotFoundException;
import com.sentinel.hrms.model.User;
import com.sentinel.hrms.model.UserPrincipal;
import com.sentinel.hrms.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static com.sentinel.hrms.util.UserConstant.*;
import static com.sentinel.hrms.enumeration.Role.*;

@Service
@Transactional
@Qualifier("userDetailsService")
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Inside loadUserByUsername");
        User user = userRepository.findUserByUserName(username);
        if(user == null){
            log.error(NO_USER_FOUND_BY_USERNAME +username);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME+ username);
        }else{
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            log.info(FOUND_USER_BY_USERNAME + username);
            return userPrincipal;
        }
    }
    @Override
    public User findUserByUserName(String username) {
        return userRepository.findUserByUserName(username);
    }
    @Override
    public User register(String firstName, String lastName, String username, String email, String password) throws UserNotFoundException, UserNameExistException, EmailExistException {
        validateNewUsernameAndEmail(EMPTY, username, email);
        User user = new User();
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUserName(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
       String encodedPassword = encodePassword(password);
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImgUrl(getTemporaryProfileImageUrl());
        userRepository.save(user);
        log.info("New user password: " + password);
        return user;
    }

    private void validateNewUsernameAndEmail(String currentUsername,String newUsername, String newEmail) throws UserNotFoundException, UserNameExistException, EmailExistException {
        User userByNewUsername = findUserByUserName(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);
        if(StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findUserByUserName(currentUsername);
            if(currentUser == null) {
                throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
            }
            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UserNameExistException(USERNAME_ALREADY_EXISTS);
            }
            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
        } else {
            if(userByNewUsername != null) {
                throw new UserNameExistException(USERNAME_ALREADY_EXISTS);
            }
            if(userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
        }
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }



    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }


    private String generateUserId() {
        return RandomStringUtils.randomNumeric(7);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String getTemporaryProfileImageUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH).toUriString();
    }
}
