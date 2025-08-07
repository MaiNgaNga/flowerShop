package com.datn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "services")
public class ServiceEntity {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;


    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Size(max = 100, message = "Tên dịch vụ tối đa 100 ký tự")
    @Column(columnDefinition = "NVARCHAR(100)", nullable = false)
    private String name;

    @Size(max = 1000, message = "Mô tả dịch vụ tối đa 1000 ký tự")
    @Column(columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String image_url;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String image_url2;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String image_url3;

    @Column(nullable = false)
    private boolean available = true;
}
