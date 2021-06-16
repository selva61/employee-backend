package com.sentinel.hrms.controller;

import com.sentinel.hrms.exception.EmailExistException;
import com.sentinel.hrms.exception.ExceptionHandling;
import com.sentinel.hrms.exception.UserNameExistException;
import com.sentinel.hrms.exception.UserNotFoundException;
import com.sentinel.hrms.model.HttpResponse;
import com.sentinel.hrms.model.User;
import com.sentinel.hrms.model.UserPrincipal;
import com.sentinel.hrms.service.UserService;
import com.sentinel.hrms.util.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.sentinel.hrms.constants.SecurityConstant.JWT_TOKEN_HEADER;
import static com.sentinel.hrms.constants.UserConstant.*;
import static com.sentinel.hrms.constants.FileConstant.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RestController
@RequestMapping(path = {"/","/user"})
public class UserController  extends ExceptionHandling {
    private AuthenticationManager authenticationManager;
    private UserService userService;
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(AuthenticationManager authenticationManager, UserService userService, JWTTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, EmailExistException, UserNameExistException {
        User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUserName(), user.getEmail(), user.getPassword());
        return new ResponseEntity<>(newUser, OK);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) throws UserNotFoundException, EmailExistException, UserNameExistException {
        authenticate(user.getUserName(), user.getPassword());
        User loginUser = userService.findUserByUserName(user.getUserName());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, OK);
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String userName,
                                           @RequestParam("password") String password,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isNotLocked") String isNotLocked,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, EmailExistException, IOException, UserNameExistException {
        User newUser = userService.addNewUser(firstName,lastName,userName,password,email,role,Boolean.parseBoolean(isNotLocked),Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(newUser, OK);
    }

    @PostMapping("/update")
    public ResponseEntity<User> updateUser(@RequestParam("currentUserName") String currentUserName,
                                           @RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String userName,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isNotLocked") String isNotLocked,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UserNotFoundException, EmailExistException, IOException, UserNameExistException {
        User updatedUser = userService.updateUser(currentUserName,firstName,lastName,userName,email,role,Boolean.parseBoolean(isNotLocked),Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(updatedUser, OK);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> findUser(@PathVariable("username") String username){
        User user = userService.findUserByUserName(username);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userService.getUsers();
        return new ResponseEntity<>(users, OK);
    }

    /*@GetMapping("/resetpassword/{username}{oldPassword}{newPassword}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("username") String userName, @PathVariable("oldPassword") String oldPassword, @PathVariable("password")  String newPassword){
        authenticate(userName, oldPassword);
        userService.resetPassword(userName,newPassword);
        return response(OK,USER_PASSWORD_RESET_SUCCESS + userName);
    }*/

    @PostMapping("/resetpassword")
    public ResponseEntity<HttpResponse> resetPassword(@RequestParam("username") String userName,
                                                      @RequestParam("oldPassword") String oldPassword,
                                                      @RequestParam("newPassword") String newPassword) {
        authenticate(userName, oldPassword);
        userService.resetPassword(userName,newPassword);
        return response(OK,USER_PASSWORD_RESET_SUCCESS + userName);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") long id){
        userService.deleteUser(id);
        return response(OK, USER_DELETE_SUCCESS);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(@RequestParam("username") String userName,@RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException, EmailExistException, IOException, UserNameExistException {
        User updatedUser = userService.updateProfilePicture(userName, profileImage);
        return new ResponseEntity<>(updatedUser, OK);
    }

    @GetMapping(path = "/image/{username}/{fileName}", produces = {IMAGE_JPEG_VALUE,IMAGE_PNG_VALUE})
    public byte[] getProfileImage(@PathVariable("username") String userName,@PathVariable("fileName") String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + userName + FORWARD_SLASH + fileName));
    }

    @GetMapping(path = "/image/profile/{username}", produces = {IMAGE_JPEG_VALUE,IMAGE_PNG_VALUE})
    public byte[] getTempProfileImage(@PathVariable("username") String userName) throws IOException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + userName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try(InputStream inputStream = url.openStream()){
            int bytesRead;
            byte[] chunk = new byte[1024];
            while((bytesRead = inputStream.read(chunk)) > 0){
                byteArrayOutputStream.write(chunk,0,bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }


    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(),httpStatus,httpStatus.getReasonPhrase().toUpperCase(),
                message.toUpperCase()),httpStatus);
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }


}

