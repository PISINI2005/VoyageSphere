package com.cts.dto;

import com.cts.enums.ComplaintStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;

    // Officer's note explaining how the complaint was handled (optional).
    private String resolutionNote;
}
