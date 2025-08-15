package com.datn.dao;

import com.datn.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceRequestDAO extends JpaRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {

}
