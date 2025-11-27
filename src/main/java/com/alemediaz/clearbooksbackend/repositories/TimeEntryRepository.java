package com.alemediaz.clearbooksbackend.repositories;

import com.alemediaz.clearbooksbackend.models.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByUserIdOrderByStartTimeDesc(Long userId);

    Optional<TimeEntry> findByUserIdAndIsRunningTrue(Long userId);

    @Query("SELECT SUM(t.durationSeconds) FROM TimeEntry t WHERE t.userId = ?1")
    Long getTotalSecondsByUserId(Long userId);

    @Query("SELECT t FROM TimeEntry t WHERE t.userId = ?1 AND t.startTime BETWEEN ?2 AND ?3 ORDER BY t.startTime DESC")
    List<TimeEntry> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
