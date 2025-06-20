package com.datn.Service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.Service.ColorService;
import com.datn.dao.ColorDAO;
import com.datn.model.Color;

@Service
public class ColorServiceImpl implements ColorService {
    @Autowired
    ColorDAO dao;

    @Override
    public List<Color> findAll() {
        return dao.findAll();
    }

    // @Override
    // public Color findByID(int id) {
    // return dao.findAllById(id);
    // }

}
