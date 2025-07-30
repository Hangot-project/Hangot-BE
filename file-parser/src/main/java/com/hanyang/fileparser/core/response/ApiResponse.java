package com.hanyang.fileparser.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.hanyang.fileparser.core.response.ResponseMessage.SUCCESS;


@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String msg;
    private T result;

    public ApiResponse(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, SUCCESS, data);
    }

    public static ApiResponse<?> fail(String message) {
        return new ApiResponse<>(false, message);
    }
}
