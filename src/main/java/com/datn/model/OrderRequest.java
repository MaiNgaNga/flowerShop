package com.datn.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
@Data
public class OrderRequest {
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
    private String sdt;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 5, message = "Địa chỉ phải có ít nhất 5 ký tự")
    private String address;
}
