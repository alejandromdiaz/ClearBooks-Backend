package com.alemediaz.clearbooksbackend.repositories;

import com.alemediaz.clearbooksbackend.models.Invoice;
import com.alemediaz.clearbooksbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUserIdOrderByInvoiceDateDesc(Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.userId = ?1 ORDER BY i.invoiceNumber DESC")
    List<Invoice> findTopByUserIdOrderByInvoiceNumberDesc(Long userId);
    
    boolean existsByInvoiceNumber(String invoiceNumber);
}
