package com.datn.dao;

import com.datn.model.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ServiceOrderDAO extends JpaRepository<ServiceOrder, Long>, JpaSpecificationExecutor<ServiceOrder> {
    Optional<ServiceOrder> findByRequestId(Long requestId);
}
