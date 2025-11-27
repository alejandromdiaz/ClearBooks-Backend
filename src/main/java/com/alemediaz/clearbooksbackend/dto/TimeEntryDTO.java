package com.alemediaz.clearbooksbackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TimeEntryDTO {
    private Long id;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
    private Boolean isRunning;
    private String formattedDuration; // HH:MM:SS
}
