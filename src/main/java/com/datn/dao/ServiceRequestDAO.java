package com.datn.dao;

import com.datn.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestDAO extends JpaRepository<ServiceRequest, Long> {

}
