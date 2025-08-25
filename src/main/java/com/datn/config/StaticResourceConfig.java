package com.datn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.CacheControl;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Đường dẫn thực tế đến thư mục images
        String imagesPath = System.getProperty("user.dir") + "/src/main/resources/static/images/";
        File imagesDir = new File(imagesPath);
        
        // Tạo thư mục nếu chưa tồn tại
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        
        // Cấu hình resource handler cho images với đường dẫn file system thực tế
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagesPath)
                .addResourceLocations("classpath:/static/images/") // fallback
                .setCacheControl(CacheControl.noCache()
                        .noStore()
                        .mustRevalidate()
                        .maxAge(0, TimeUnit.SECONDS))
                .resourceChain(false);
        
        // Các static resources khác
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
                
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
    }
}