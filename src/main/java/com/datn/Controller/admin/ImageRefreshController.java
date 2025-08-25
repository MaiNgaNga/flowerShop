package com.datn.Controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageRefreshController {

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshImages() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Trigger một timestamp mới để force refresh
            long timestamp = System.currentTimeMillis();
            response.put("success", true);
            response.put("timestamp", timestamp);
            response.put("message", "Images refreshed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error refreshing images: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}