package com.datn.dao;

import com.datn.model.ServiceRequestDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRequestDraftDAO extends JpaRepository<ServiceRequestDraft, Long> {
    
    @Query("SELECT d FROM ServiceRequestDraft d WHERE d.request.id = :requestId")
    Optional<ServiceRequestDraft> findByRequestId(@Param("requestId") Long requestId);
    
    void deleteByRequestId(Long requestId);
}