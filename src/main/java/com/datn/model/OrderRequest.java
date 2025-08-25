package com.datn.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

@Data
public class OrderRequest {

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 5, message = "Địa chỉ phải có ít nhất 5 ký tự")
    private String address;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Vui lòng chọn ngày giao hàng")
    @FutureOrPresent(message = "Ngày giao từ hôm nay trở đi")
    private LocalDate deliveryDate;

    @Temporal(TemporalType.TIME)
    @Column(name = "delivery_time")
    @NotNull(message = "Giờ nhận hàng không được để trống")
    private LocalTime deliveryTime;

    // Người nhận hàng (không để trống)
    @NotBlank(message = "Người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
    @Column(name = "receiver_name", columnDefinition = "NVARCHAR(100)")
    private String receiverName;

    // Số điện thoại người nhận (bắt buộc, định dạng hợp lệ)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại phải gồm 10 số và bắt đầu bằng 0")
    @Column(name = "receiver_phone", columnDefinition = "NVARCHAR(20)")
    private String receiverPhone;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}
