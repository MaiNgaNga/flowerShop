package com.datn.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "service_request_drafts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết 1-1 với ServiceRequest
    @OneToOne
    @JoinColumn(name = "request_id", unique = true)
    private ServiceRequest request;

    // Thông tin tạm thời từ lần liên hệ
    @Column(name = "quoted_price")
    private BigDecimal quotedPrice;

    @Column(length = 100, columnDefinition = "NVARCHAR(100)")
    private String district;

    @Column(name = "address_detail", length = 255, columnDefinition = "NVARCHAR(255)")
    private String addressDetail;

    @Column(name = "execution_time")
    private LocalDate executionTime;

    @Column(length = 1000, columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // Thời gian tạo/cập nhật
    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "updated_at")
    private LocalDate updatedAt = LocalDate.now();
}