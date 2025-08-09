package com.datn.Service.impl;

import com.datn.dao.ServiceRequestDAO;
import com.datn.model.ServiceRequest;
import com.datn.model.enums.ServiceRequestStatus;
import com.datn.Service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
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
    public Page<ServiceRequest> findAll(Pageable pageable) {
        return requestDAO.findAll(pageable);
    }
    
    @Override
    public Page<ServiceRequest> findByFilters(ServiceRequestStatus status, String keyword, Pageable pageable) {
        Specification<ServiceRequest> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Lọc theo trạng thái
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            // Lọc theo từ khóa (tên, số điện thoại)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")), likeKeyword);
                Predicate phonePredicate = criteriaBuilder.like(
                    root.get("phone"), "%" + keyword + "%");
                
                predicates.add(criteriaBuilder.or(namePredicate, phonePredicate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return requestDAO.findAll(spec, pageable);
    }
    
    @Override
    public Page<ServiceRequest> findByFilters(ServiceRequestStatus status, String keyword, String month, Pageable pageable) {
        Specification<ServiceRequest> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Lọc theo trạng thái
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            // Lọc theo từ khóa (tên, số điện thoại)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")), likeKeyword);
                Predicate phonePredicate = criteriaBuilder.like(
                    root.get("phone"), "%" + keyword + "%");
                
                predicates.add(criteriaBuilder.or(namePredicate, phonePredicate));
            }
            
            // Lọc theo tháng
            if (month != null && !month.trim().isEmpty()) {
                try {
                    LocalDate startOfMonth = LocalDate.parse(month + "-01");
                    LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
                    
                    predicates.add(criteriaBuilder.between(root.get("createdAt"), startOfMonth, endOfMonth));
                } catch (Exception e) {
                    // Ignore invalid month format
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return requestDAO.findAll(spec, pageable);
    }
    
    @Override
    public Page<ServiceRequest> findByFiltersExcludeConfirmed(ServiceRequestStatus status, String keyword, Pageable pageable) {
        Specification<ServiceRequest> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Luôn loại trừ CONFIRMED
            predicates.add(criteriaBuilder.notEqual(root.get("status"), ServiceRequestStatus.CONFIRMED));
            
            // Lọc theo trạng thái (nếu có)
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            // Lọc theo từ khóa (tên, số điện thoại)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")), likeKeyword);
                Predicate phonePredicate = criteriaBuilder.like(
                    root.get("phone"), "%" + keyword + "%");
                
                predicates.add(criteriaBuilder.or(namePredicate, phonePredicate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return requestDAO.findAll(spec, pageable);
    }

    @Override
    public void delete(Long id) {
        requestDAO.deleteById(id);
    }

}
