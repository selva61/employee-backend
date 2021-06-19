package com.sentinel.hrms.service;

import com.sentinel.hrms.enumeration.Role;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static com.sentinel.hrms.constants.FileConstant.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static com.sentinel.hrms.constants.UserConstant.*;
import static com.sentinel.hrms.enumeration.Role.*;

@Service
@Transactional
@Qualifier("userDetailsService")
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private LoginAttemptService loginAttemptService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Inside loadUserByUsername");
        User user = userRepository.findUserByUserName(username);
        if(user == null){
            log.error(NO_USER_FOUND_BY_USERNAME +username);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME+ username);
        }else{
            validateLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            log.info(FOUND_USER_BY_USERNAME + username);
            return userPrincipal;
        }
    }

    private void validateLoginAttempt(User user){
        if(user.isNotLocked()){
            if(loginAttemptService.hasExceededMaxAttmepts(user.getUserName())){
                user.setNotLocked(false);
            }else {
                user.setNotLocked(true);
            }
        }else{
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUserName());
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
        user.setPassword(encodePassword(password));
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImgUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        log.info("New user password: " + password);
        return user;
    }

    private User validateNewUsernameAndEmail(String currentUsername,String newUsername, String newEmail) throws UserNotFoundException, UserNameExistException, EmailExistException {
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
            return currentUser;
        } else {
            if(userByNewUsername != null) {
                throw new UserNameExistException(USERNAME_ALREADY_EXISTS);
            }
            if(userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return null;
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

    @Override
    public User addNewUser(String firstName, String lastName, String userName,String password, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UserNameExistException, EmailExistException, IOException {
        validateNewUsernameAndEmail(EMPTY,userName,email);
        User user = new User();
        user.setPassword(encodePassword(password));
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUserName(userName);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setActive(isActive);
        user.setNotLocked(isNotLocked);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImgUrl(getTemporaryProfileImageUrl(userName));
        userRepository.save(user);
        saveProfileImage(user,profileImage);
        return user;
    }

    @Override
    public User updateUser(String currentUserName, String newFirstName, String newLastName, String newUserName, String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UserNameExistException, EmailExistException, IOException {
        User currentUser = validateNewUsernameAndEmail(currentUserName,newUserName,newEmail);
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setUserName(newUserName);
        currentUser.setEmail(newEmail);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNotLocked);
        currentUser.setRole(getRoleEnumName(role).name());
        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
        userRepository.save(currentUser);
        saveProfileImage(currentUser,profileImage);
        return currentUser;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String userName, String newPassword) {
        User user = userRepository.findUserByUserName(userName);
        if(user == null){
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME);
        }
        user.setPassword(encodePassword(newPassword));
        userRepository.save(user);
    }

    @Override
    public User updateProfilePicture(String userName, MultipartFile newProfileImage) throws UserNotFoundException, UserNameExistException, EmailExistException, IOException {
        User user = validateNewUsernameAndEmail(userName,null,null);
        saveProfileImage(user,newProfileImage);
        return user;
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(7);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
        if(profileImage !=null){
            Path userFolder = Paths.get(USER_FOLDER + user.getUserName()).toAbsolutePath().normalize();
            if(!Files.exists(userFolder)){
                Files.createDirectories(userFolder);
                log.info(DIRECTORY_CREATED + userFolder);
            }
            Files.delete(Paths.get(userFolder + user.getUserName() + DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(userFolder + user.getUserName() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setProfileImgUrl(setProfileImageUrl(user.getUserName()));
            userRepository.save(user);
            log.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        }
    }

    private String setProfileImageUrl(String userName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + userName + FORWARD_SLASH
                + userName + DOT + JPG_EXTENSION).toUriString();
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private String getTemporaryProfileImageUrl(String userName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + userName).toUriString();
    }

}
