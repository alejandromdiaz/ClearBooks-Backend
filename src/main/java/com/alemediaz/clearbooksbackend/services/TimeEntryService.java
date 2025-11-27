package com.alemediaz.clearbooksbackend.services;

import com.alemediaz.clearbooksbackend.dto.TimeEntryDTO;
import com.alemediaz.clearbooksbackend.models.TimeEntry;
import com.alemediaz.clearbooksbackend.repositories.TimeEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TimeEntryService {
    @Autowired
    private TimeEntryRepository timeEntryRepository;

    public List<TimeEntryDTO> getAllTimeEntriesByUserId(Long userId) {
        return timeEntryRepository.findByUserIdOrderByStartTimeDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TimeEntryDTO getTimeEntryById(Long id, Long userId) {
        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Time entry not found"));

        if (!entry.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return convertToDto(entry);
    }

    public Optional<TimeEntryDTO> getRunningTimer(Long userId) {
        return timeEntryRepository.findByUserIdAndIsRunningTrue(userId)
                .map(this::convertToDto);
    }

    @Transactional
    public TimeEntryDTO startTimer(String description, Long userId) {
        // Check if there's already a running timer
        Optional<TimeEntry> runningTimer = timeEntryRepository.findByUserIdAndIsRunningTrue(userId);
        if (runningTimer.isPresent()) {
            throw new RuntimeException("There is already a running timer. Please stop it first.");
        }

        TimeEntry entry = new TimeEntry();
        entry.setDescription(description);
        entry.setStartTime(LocalDateTime.now());
        entry.setIsRunning(true);
        entry.setUserId(userId);
        entry.setDurationSeconds(0L);

        TimeEntry saved = timeEntryRepository.save(entry);
        return convertToDto(saved);
    }

    @Transactional
    public TimeEntryDTO stopTimer(Long userId) {
        TimeEntry entry = timeEntryRepository.findByUserIdAndIsRunningTrue(userId)
                .orElseThrow(() -> new RuntimeException("No running timer found"));

        entry.setEndTime(LocalDateTime.now());
        entry.setIsRunning(false);

        // Calculate duration
        Duration duration = Duration.between(entry.getStartTime(), entry.getEndTime());
        entry.setDurationSeconds(duration.getSeconds());

        TimeEntry saved = timeEntryRepository.save(entry);
        return convertToDto(saved);
    }

    @Transactional
    public TimeEntryDTO updateTimeEntry(Long id, TimeEntryDTO dto, Long userId) {
        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Time entry not found"));

        if (!entry.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (entry.getIsRunning()) {
            throw new RuntimeException("Cannot edit a running timer");
        }

        entry.setDescription(dto.getDescription());
        entry.setStartTime(dto.getStartTime());
        entry.setEndTime(dto.getEndTime());

        // Recalculate duration
        if (entry.getEndTime() != null) {
            Duration duration = Duration.between(entry.getStartTime(), entry.getEndTime());
            entry.setDurationSeconds(duration.getSeconds());
        }

        TimeEntry saved = timeEntryRepository.save(entry);
        return convertToDto(saved);
    }

    public void deleteTimeEntry(Long id, Long userId) {
        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Time entry not found"));

        if (!entry.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (entry.getIsRunning()) {
            throw new RuntimeException("Cannot delete a running timer");
        }

        timeEntryRepository.delete(entry);
    }

    public Long getTotalSeconds(Long userId) {
        Long total = timeEntryRepository.getTotalSecondsByUserId(userId);
        return total != null ? total : 0L;
    }

    private TimeEntryDTO convertToDto(TimeEntry entry) {
        TimeEntryDTO dto = new TimeEntryDTO();
        dto.setId(entry.getId());
        dto.setDescription(entry.getDescription());
        dto.setStartTime(entry.getStartTime());
        dto.setEndTime(entry.getEndTime());
        dto.setIsRunning(entry.getIsRunning());

        // Calculate current duration for running timers
        if (entry.getIsRunning()) {
            Duration duration = Duration.between(entry.getStartTime(), LocalDateTime.now());
            dto.setDurationSeconds(duration.getSeconds());
        } else {
            dto.setDurationSeconds(entry.getDurationSeconds());
        }

        dto.setFormattedDuration(formatDuration(dto.getDurationSeconds()));

        return dto;
    }

    private String formatDuration(Long seconds) {
        if (seconds == null) return "00:00:00";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
