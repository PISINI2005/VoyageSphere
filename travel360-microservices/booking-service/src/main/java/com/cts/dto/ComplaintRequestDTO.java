package com.cts.dto;

import com.cts.enums.ComplaintTargetType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintRequestDTO {

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    // Optional: what the complaint is about. Must be supplied together (both or neither);
    // when present, the referenced record must exist and belong to the complainant.
    private ComplaintTargetType targetType;

    private Long targetId;
}
