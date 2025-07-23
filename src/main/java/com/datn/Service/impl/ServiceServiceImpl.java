package com.datn.Service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datn.Service.ServiceService;
import com.datn.dao.ServiceDAO;
import com.datn.model.ServiceEntity;
import com.datn.utils.ParamService;

@Service
public class ServiceServiceImpl implements ServiceService {

    @Autowired
    private ServiceDAO dao;

    @Autowired
    private ParamService param;

    @Override
    public List<ServiceEntity> findAll() {
        return dao.findAll();
    }

    @Override
    public ServiceEntity findByID(long id) {
        Optional<ServiceEntity> optional = dao.findById(id);
        return optional.orElse(null);
    }

    @Override
    public ServiceEntity create(ServiceEntity entity, MultipartFile image1, MultipartFile image2,
            MultipartFile image3) {
        // if (entity.getId() != null && dao.existsById(entity.getId())) {
        // throw new IllegalArgumentException("ID dịch vụ đã tồn tại.");
        // }
        if (dao.existsById(entity.getId())) {
            throw new IllegalArgumentException("ID dịch vụ đã tồn tại!");
        }

        if (image1 != null && !image1.isEmpty()) {
            entity.setImageUrl(
                    param.save(image1, "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
        }

        if (image2 != null && !image2.isEmpty()) {
            entity.setImageUrl2(
                    param.save(image2, "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
        }

        if (image3 != null && !image3.isEmpty()) {
            entity.setImageUrl3(
                    param.save(image3, "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
        }

        return dao.save(entity);
    }

    @Override
    public void update(ServiceEntity entity, MultipartFile image1, MultipartFile image2, MultipartFile image3,
            String[] oldImages) {
        if (!dao.existsById(entity.getId())) {
            throw new IllegalArgumentException("Không tìm thấy dịch vụ để cập nhật.");
        }

        // Xử lý ảnh 1
        if (image1 != null && !image1.isEmpty()) {
            entity.setImageUrl(
                    param.save(image1, "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
        } else {
            entity.setImageUrl(oldImages.length > 0 ? oldImages[0] : null);
        }

        // Xử lý ảnh 2
        if (image2 != null && !image2.isEmpty()) {
            entity.setImageUrl2(
                    param.save(image2, "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
        } else {
            entity.setImageUrl2(oldImages.length > 1 ? oldImages[1] : null);
        }

        // Xử lý ảnh 3
        if (image3 != null && !image3.isEmpty()) {
            entity.setImageUrl3(
                    param.save(image3, "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
        } else {
            entity.setImageUrl3(oldImages.length > 2 ? oldImages[2] : null);
        }

        dao.save(entity);
    }

    @Override
    public void deleteById(long id) {
        if (!dao.existsById(id)) {
            throw new IllegalArgumentException("Dịch vụ không tồn tại.");
        }
        dao.deleteById(id);
    }

    @Override
    public boolean existsById(long id) {
        return dao.existsById(id);
    }

    @Override
    public Page<ServiceEntity> findByAllService(Pageable pageable) {
        return dao.findAll(pageable);
    }

    @Override
    public Page<ServiceEntity> searchByName(String name, Pageable pageable) {
        return dao.searchByName(name, pageable);
    }

    @Override
    public Page<ServiceEntity> findAvailableServices(Pageable pageable) {
        return dao.findByAvailableTrue(pageable);
    }

    @Override
    public List<ServiceEntity> findAllAvailable() {
        return dao.findByAvailableTrue();
    }

}
