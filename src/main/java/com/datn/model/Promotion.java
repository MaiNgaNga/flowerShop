package com.datn.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "Promotions")
@AllArgsConstructor
@NoArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @Column(name = "discount_type")
    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType; // "percent" hoặc "amount" - bạn nên kiểm tra thêm ở tầng controller/service

    @Column(name = "discount_value")
    @NotNull(message = "Giá trị giảm giá không được để trống")
    @Positive(message = "Giá trị giảm giá phải lớn hơn 0")
    private Double discountValue;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "start_date")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu phải là hôm nay hoặc tương lai")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "end_date")
    @NotNull(message = "Ngày kết thúc không được để trống")
    @FutureOrPresent(message = "Ngày kết thúc phải là hôm nay hoặc tương lai")
    private LocalDate endDate;

    @Column(name = "status")
    private Boolean status; // true = đang hoạt động, false = không hoạt động

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "use_count", nullable = false)
    @Min(value = 0, message = "Số lượt sử dụng phải >= 0")
    private Integer useCount = 0;

    
    @OneToMany(mappedBy = "promotion")
    private List<Order> orders;
}
