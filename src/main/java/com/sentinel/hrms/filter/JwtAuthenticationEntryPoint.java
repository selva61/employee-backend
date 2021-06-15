package com.sentinel.hrms.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.hrms.model.HttpResponse;
import static com.sentinel.hrms.util.SecurityConstant.*;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.http.HttpStatus.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static org.springframework.util.MimeTypeUtils.*;


@Component
@Slf4j
public class JwtAuthenticationEntryPoint  extends Http403ForbiddenEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2) throws IOException {
        log.debug("Pre-authenticated entry point called. Rejecting access");
        HttpResponse httpResponse = new HttpResponse(FORBIDDEN.value(),FORBIDDEN,
                FORBIDDEN.getReasonPhrase().toUpperCase(), FORBIDDEN_MESSAGE);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(FORBIDDEN.value());
        OutputStream outputStream = response.getOutputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(outputStream,httpResponse);
        outputStream.flush();
    }

}
