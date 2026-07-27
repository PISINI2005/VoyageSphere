package com.cts.dto;

import com.cts.enums.UserStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
