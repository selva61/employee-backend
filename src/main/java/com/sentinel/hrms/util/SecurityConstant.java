package com.sentinel.hrms.util;

public class SecurityConstant {

    public static final long EXPIRATION_TIME = 2592_000_000L;// 30 Days in milliseconds
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String SENTINEL_LLC = "Sentinel, LLC";
    public static final String SENTINEL_HRMS = "Access Control Management";
    public static final String AUTHORITIES = "Authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to log in to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    public static final String OPTION_HTTP_METHOD= "OPTIONS";
    //public static final String[] PUBLIC_URLS = {"/user/login","/user/register", "/user/resetpassword/**", "/user/image/**"};
    public static final String[] PUBLIC_URLS = {"**"};
}
