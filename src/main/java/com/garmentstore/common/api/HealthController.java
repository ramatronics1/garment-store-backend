package com.garmentstore.common.api;
import com.garmentstore.common.response.ApiResponse; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/v1/health") public class HealthController { @GetMapping public ApiResponse<Map<String,String>> health(){return ApiResponse.success("Application is running",Map.of("status","UP"));}}
