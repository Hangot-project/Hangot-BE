package com.hanyang.dataportal.core.component;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.hanyang.dataportal.core.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class ApiResponseBuilder {

    public void buildErrorResponse(HttpServletResponse response,HttpStatus httpStatus,String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding("UTF-8");

        String json = JsonMapper.builder().build().writeValueAsString(ApiResponse.fail(message));

        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
    }

}
