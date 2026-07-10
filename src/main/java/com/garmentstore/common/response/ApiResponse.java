package com.garmentstore.common.response;
import com.fasterxml.jackson.annotation.JsonInclude; import java.time.Instant;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success,String message,T data,ErrorDetails error,Instant timestamp){
  public static <T> ApiResponse<T> success(String message,T data){return new ApiResponse<>(true,message,data,null,Instant.now());}
  public static <T> ApiResponse<T> failure(String message,ErrorDetails error){return new ApiResponse<>(false,message,null,error,Instant.now());}
}
