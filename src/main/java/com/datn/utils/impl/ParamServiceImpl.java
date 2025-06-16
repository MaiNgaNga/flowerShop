package com.datn.utils.impl;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.datn.utils.ParamService;

@Service
public class ParamServiceImpl implements ParamService {
    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;

    @Override
    public String getString(String name, String defaultValue) {
        String value = request.getParameter(name);
        return (value != null) ? value : defaultValue;
    }

    @Override
    public int getInt(String name, int defaultValue) {
        try {
            return Integer.parseInt(request.getParameter(name));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;

        }
    }

    @Override
    public double getDouble(String name, double defaultValue) {
        try {
            return Double.parseDouble(request.getParameter(name));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        String param = request.getParameter(name);
        if (param == null) {
            return defaultValue;
        }
        return param.equalsIgnoreCase("true") || param.equals(1);
    }

    @Override
    public Date getDate(String name, String pattern) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            return formatter.parse(value);

        } catch (ParseException e) {
            throw new RuntimeException("Định dạng ngày không hợp lệ");
        }
    }

    @Override
    public File save(MultipartFile file, String path) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File destination = new File(directory, file.getOriginalFilename());
            file.transferTo(destination);
            return destination;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu file: " + e.getMessage());
        }
    }

}
