package com.datn.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

@Data
public class OrderRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
    private String sdt;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 5, message = "Địa chỉ phải có ít nhất 5 ký tự")
    private String address;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Vui lòng chọn ngày giao hàng")
    @Future(message = "Ngày giao phải sau hôm nay")
    private Date deliveryDate;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}
