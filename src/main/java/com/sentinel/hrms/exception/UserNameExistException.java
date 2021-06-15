package com.sentinel.hrms.exception;

public class UserNameExistException extends Exception {
    public UserNameExistException(String message) {
        super(message);
    }
}
