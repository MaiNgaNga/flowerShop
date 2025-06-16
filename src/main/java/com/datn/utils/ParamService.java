package com.datn.utils;

import java.io.File;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

public interface ParamService {
    String getString(String name, String defaultValue);

    int getInt(String name, int defaultValue);

    double getDouble(String name, double defaultValue);

    boolean getBoolean(String name, boolean defaultValue);

    Date getDate(String name, String pattern);

    File save(MultipartFile file, String folder);
}
