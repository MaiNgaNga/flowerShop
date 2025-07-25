package com.datn.Service;

import com.datn.model.ServiceRequest;
import java.util.List;

public interface ServiceRequestService {

    ServiceRequest save(ServiceRequest request);

    ServiceRequest findById(Long id);

    List<ServiceRequest> findAll();

    void delete(Long id);

}
