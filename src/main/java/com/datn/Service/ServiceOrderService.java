package com.datn.Service;

import com.datn.model.ServiceOrder;
import com.datn.model.enums.ServiceOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ServiceOrderService {
    ServiceOrder save(ServiceOrder order);

    Optional<ServiceOrder> findById(Long id);

    Optional<ServiceOrder> findByRequestId(Long requestId);

    List<ServiceOrder> findAll();
    
    Page<ServiceOrder> findAll(Pageable pageable);
    
    Page<ServiceOrder> findByFilters(ServiceOrderStatus status, String keyword, String month, Pageable pageable);

    void deleteById(Long id);
}
