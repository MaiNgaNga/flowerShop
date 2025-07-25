package com.datn.dao;

import com.datn.model.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceOrderDAO extends JpaRepository<ServiceOrder, Long> {
    Optional<ServiceOrder> findByRequestId(Long requestId);
}
