package com.datn.Service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.datn.Service.ShipperService;
import com.datn.dao.ShipperDAO;
import com.datn.model.Shipper;

@Service
public class ShipperServiceImpl implements ShipperService {

    @Autowired
    private ShipperDAO shipperDAO;

    @Override
    public List<Shipper> findAll() {
        return shipperDAO.findAll();
    }

    @Override
    public Shipper findById(int id) {
        return shipperDAO.findById(id);
    }
        

    @Override
    public Shipper save(Shipper shipper) {
        return shipperDAO.save(shipper);
    }

    @Override
    public void deleteById(int id) {
        shipperDAO.deleteById(id);
    }

    @Override
    public boolean existsById(int id) {
        return shipperDAO.existsById(id);
    }

    @Override
    public Shipper update(Shipper shipper) {
        // TODO Auto-generated method stub
        return shipperDAO.save(shipper);
    }

    @Override
    public Page<Shipper> findByAllShippers(Pageable pageable) {
        return shipperDAO.findAllActive(pageable);
    }

    @Override
    public Page<Shipper> searchByName(String name, Pageable pageable) {
        return shipperDAO.searchByName(name, pageable);
    }

    @Override
    public Page<Shipper> searchByStatus(Boolean status, Pageable pageable) {
        return shipperDAO.findByStatus(status, pageable);
    }

    @Override
    public boolean existsByUserId(Integer userId) {
        return shipperDAO.existsByUserId(userId);
    }

    @Override
    public void deleteByUserId(int userId) {
        shipperDAO.deleteByUserId(userId);
    }

    @Override
    public Shipper findByUserId(Integer userId) {
        return shipperDAO.findByUserId(userId);
    }

   
    
}
