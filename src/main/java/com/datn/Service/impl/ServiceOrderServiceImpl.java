package com.datn.Service.impl;

import com.datn.Service.ServiceOrderService;
import com.datn.dao.ServiceOrderDAO;
import com.datn.model.ServiceOrder;
import com.datn.model.enums.ServiceOrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
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
    public Page<ServiceOrder> findAll(Pageable pageable) {
        return serviceOrderDAO.findAll(pageable);
    }
    
    @Override
    public Page<ServiceOrder> findByFilters(ServiceOrderStatus status, String keyword, String month, Pageable pageable) {
        Specification<ServiceOrder> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Lọc theo trạng thái đơn hàng
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            // Lọc theo từ khóa (tên khách hàng, email, số điện thoại, tên dịch vụ)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("request").get("fullName")), likeKeyword);
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("request").get("email")), likeKeyword);
                Predicate phonePredicate = criteriaBuilder.like(
                    root.get("request").get("phone"), "%" + keyword + "%");
                Predicate servicePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("request").get("service").get("name")), likeKeyword);
                
                predicates.add(criteriaBuilder.or(namePredicate, emailPredicate, phonePredicate, servicePredicate));
            }
            
            // Lọc theo tháng
            if (month != null && !month.trim().isEmpty()) {
                try {
                    LocalDate startOfMonth = LocalDate.parse(month + "-01");
                    LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
                    
                    predicates.add(criteriaBuilder.between(root.get("confirmedAt"), startOfMonth, endOfMonth));
                } catch (Exception e) {
                    // Ignore invalid month format
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return serviceOrderDAO.findAll(spec, pageable);
    }

    @Override
    public void deleteById(Long id) {
        serviceOrderDAO.deleteById(id);
    }
}
