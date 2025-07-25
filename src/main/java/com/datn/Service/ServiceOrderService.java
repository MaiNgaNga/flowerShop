package com.datn.Service;

import com.datn.model.ServiceOrder;

import java.util.List;
import java.util.Optional;

public interface ServiceOrderService {
    ServiceOrder save(ServiceOrder order);

    Optional<ServiceOrder> findById(Long id);

    Optional<ServiceOrder> findByRequestId(Long requestId);

    List<ServiceOrder> findAll();

    void deleteById(Long id);
}
