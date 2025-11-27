package com.alemediaz.clearbooksbackend.controllers;

import com.alemediaz.clearbooksbackend.dto.TimeEntryDTO;
import com.alemediaz.clearbooksbackend.models.User;
import com.alemediaz.clearbooksbackend.services.TimeEntryService;
import com.alemediaz.clearbooksbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/timer")
public class TimeEntryController {
    @Autowired
    private TimeEntryService timeEntryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<TimeEntryDTO>> getAllTimeEntries(Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(timeEntryService.getAllTimeEntriesByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeEntryDTO> getTimeEntry(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        return ResponseEntity.ok(timeEntryService.getTimeEntryById(id, user.getId()));
    }

    @GetMapping("/running")
    public ResponseEntity<?> getRunningTimer(Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        Optional<TimeEntryDTO> running = timeEntryService.getRunningTimer(user.getId());

        if (running.isPresent()) {
            return ResponseEntity.ok(running.get());
        } else {
            return ResponseEntity.ok(Map.of("running", false));
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startTimer(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            String description = request.get("description");
            TimeEntryDTO entry = timeEntryService.startTimer(description, user.getId());
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopTimer(Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            TimeEntryDTO entry = timeEntryService.stopTimer(user.getId());
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimeEntry(
            @PathVariable Long id,
            @RequestBody TimeEntryDTO dto,
            Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            TimeEntryDTO updated = timeEntryService.updateTimeEntry(id, dto, user.getId());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTimeEntry(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByVatNumber(authentication.getName());
            timeEntryService.deleteTimeEntry(id, user.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getTotalTime(Authentication authentication) {
        User user = userService.findByVatNumber(authentication.getName());
        Long totalSeconds = timeEntryService.getTotalSeconds(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("totalSeconds", totalSeconds);
        response.put("totalHours", totalSeconds / 3600.0);

        return ResponseEntity.ok(response);
    }
}
