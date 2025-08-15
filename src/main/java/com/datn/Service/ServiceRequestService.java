package com.datn.Service;

import com.datn.model.ServiceRequest;
import com.datn.model.enums.ServiceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ServiceRequestService {

    ServiceRequest save(ServiceRequest request);

    ServiceRequest findById(Long id);

    List<ServiceRequest> findAll();
    
    Page<ServiceRequest> findAll(Pageable pageable);
    
    Page<ServiceRequest> findByFilters(ServiceRequestStatus status, String keyword, Pageable pageable);
    
    Page<ServiceRequest> findByFilters(ServiceRequestStatus status, String keyword, String month, Pageable pageable);
    
    Page<ServiceRequest> findByFiltersExcludeConfirmed(ServiceRequestStatus status, String keyword, Pageable pageable);

    void delete(Long id);

}
