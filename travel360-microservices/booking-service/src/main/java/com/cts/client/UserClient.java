package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cts.config.FeignClientConfig;
import com.cts.dto.UserResponseDTO;

@FeignClient(name = "USER-SERVICE", configuration = FeignClientConfig.class, fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/api/v1/users/{userId}")
    UserResponseDTO getUser(@PathVariable Long userId);
}
