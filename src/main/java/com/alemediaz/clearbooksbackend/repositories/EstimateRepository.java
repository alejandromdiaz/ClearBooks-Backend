package com.alemediaz.clearbooksbackend.repositories;

import com.alemediaz.clearbooksbackend.models.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    List<Estimate> findByUserIdOrderByEstimateDateDesc(Long userId);

    @Query("SELECT e FROM Estimate e WHERE e.userId = ?1 ORDER BY e.estimateNumber DESC")
    List<Estimate> findTopByUserIdOrderByEstimateNumberDesc(Long userId);
}
