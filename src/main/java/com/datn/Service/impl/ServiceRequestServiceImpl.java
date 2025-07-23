package com.datn.Service.impl;

import com.datn.dao.ServiceRequestDAO;
import com.datn.model.ServiceRequest;
import com.datn.Service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceRequestServiceImpl implements ServiceRequestService {

    @Autowired
    private ServiceRequestDAO requestDAO;

    @Override
    public ServiceRequest save(ServiceRequest request) {
        return requestDAO.save(request);
    }

    @Override
    public ServiceRequest findById(Long id) {
        return requestDAO.findById(id).orElse(null);
    }

    @Override
    public List<ServiceRequest> findAll() {
        return requestDAO.findAll();
    }

    @Override
    public void delete(Long id) {
        requestDAO.deleteById(id);
    }

}
