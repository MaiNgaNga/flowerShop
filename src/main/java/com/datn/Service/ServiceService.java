package com.datn.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.datn.model.ServiceEntity;

public interface ServiceService {
    List<ServiceEntity> findAll();

    ServiceEntity findByID(long id);

    ServiceEntity create(ServiceEntity entity, MultipartFile image1, MultipartFile image2, MultipartFile image3);

    void update(ServiceEntity entity, MultipartFile image1, MultipartFile image2, MultipartFile image3,
            String[] oldImages);

    void deleteById(long id);

    boolean existsById(long id);

    Page<ServiceEntity> findByAllService(Pageable pageable);

    Page<ServiceEntity> searchByName(String name, Pageable pageable);
}