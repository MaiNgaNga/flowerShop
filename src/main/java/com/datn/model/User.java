package com.datn.model;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Entity
@Builder
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer id;

    @NotBlank(message = "Tên không được để trống")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;
    
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "Số điện thoại không hợp lệ")
    private String sdt;


    @NotNull(message = "Vai trò không được để trống")
    private Integer role;

     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Shipper> shippers;

    @NotNull(message = "Trạng thái không được để trống")
    @Column(nullable = false)
    private Boolean status = true;


}
