package com.alemediaz.clearbooksbackend.repositories;

import com.alemediaz.clearbooksbackend.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUserIdOrderByInvoiceDateDesc(Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.userId = :userId ORDER BY i.id DESC")
    List<Invoice> findTopByUserIdOrderByInvoiceNumberDesc(@Param("userId") Long userId);

    boolean existsByInvoiceNumber(String invoiceNumber);
}
