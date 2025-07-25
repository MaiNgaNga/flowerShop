package com.datn.Service.impl;

import com.datn.Service.ServiceOrderService;
import com.datn.dao.ServiceOrderDAO;
import com.datn.model.ServiceOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceOrderServiceImpl implements ServiceOrderService {

    @Autowired
    private ServiceOrderDAO serviceOrderDAO;

    @Override
    public ServiceOrder save(ServiceOrder order) {
        return serviceOrderDAO.save(order);
    }

    @Override
    public Optional<ServiceOrder> findById(Long id) {
        return serviceOrderDAO.findById(id);
    }

    @Override
    public Optional<ServiceOrder> findByRequestId(Long requestId) {
        return serviceOrderDAO.findByRequestId(requestId);
    }

    @Override
    public List<ServiceOrder> findAll() {
        return serviceOrderDAO.findAll();
    }

    @Override
    public void deleteById(Long id) {
        serviceOrderDAO.deleteById(id);
    }
}
