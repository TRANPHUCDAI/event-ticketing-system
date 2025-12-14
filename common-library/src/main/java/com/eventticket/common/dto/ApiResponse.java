package com.eventticket.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
     private boolean success;
     private String message;
     private T data;
     private String errorCode;
     private long timestamp;

     public static <T> ApiResponse<T> ok(T data) {
          return ApiResponse.<T>builder()
                    .success(true)
                    .message("Success")
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
     }

     public static <T> ApiResponse<T> ok(T data, String message) {
          return ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
     }

     public static <T> ApiResponse<T> error(String message, String errorCode) {
          return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .errorCode(errorCode)
                    .timestamp(System.currentTimeMillis())
                    .build();
     }
}
