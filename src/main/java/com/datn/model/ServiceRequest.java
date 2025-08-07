package com.datn.model;

import com.datn.model.enums.ServiceRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, columnDefinition = "NVARCHAR(50)")
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 50, message = "Họ và tên tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\d]+$", message = "Họ và tên không được chứa số")
    private String fullName;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Email không được để trống")
    @Size(max = 50, message = "Email tối đa 50 ký tự")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(nullable = false, length = 11)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải gồm 10 hoặc 11 chữ số")
    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceRequestStatus status = ServiceRequestStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id")
    @NotNull(message = "Phải chọn dịch vụ")
    private ServiceEntity service;
}
