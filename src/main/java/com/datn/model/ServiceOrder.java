package com.datn.model;

import com.datn.model.enums.ServiceOrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "service_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết đến yêu cầu dịch vụ gốc
    @OneToOne(optional = false)
    @JoinColumn(name = "request_id", unique = true)
    @NotNull(message = "Yêu cầu dịch vụ không được null")
    private ServiceRequest request;

    // Giá đã báo
    @Column(name = "quoted_price", nullable = false)
    @NotNull(message = "Giá báo không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal quotedPrice;

    // Quận/Huyện
    @Column(length = 100, columnDefinition = "NVARCHAR(100)")
    @NotBlank(message = "Địa chỉ quận/huyện không được để trống")
    @Size(max = 100, message = "Tên quận/huyện tối đa 100 ký tự")
    private String district;

    // Địa chỉ chi tiết
    @Column(name = "address_detail", length = 255, columnDefinition = "NVARCHAR(255)")
    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự")
    private String addressDetail;

    // Ngày thực hiện (không cần giờ)
    @Column(name = "execution_time", nullable = false)
    @NotNull(message = "Ngày thực hiện không được để trống")
    @Future(message = "Ngày thực hiện phải ở tương lai")
    private LocalDate executionTime;

    // Mô tả
    @Column(length = 1000, columnDefinition = "NVARCHAR(MAX)")
    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    // Thời gian xác nhận đơn
    @Column(name = "confirmed_at")
    private LocalDate confirmedAt = LocalDate.now();

    // Trạng thái đơn hàng
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private ServiceOrderStatus status = ServiceOrderStatus.UNPAID;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    // Có thể thêm paymentType hoặc user xác nhận nếu cần
}
