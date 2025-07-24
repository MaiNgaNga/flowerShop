package com.datn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "shippers")
public class Shipper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quan hệ ManyToOne với bảng users (mỗi shipper tương ứng 1 user)
    @ManyToOne
    @JoinColumn(name = "user_ID", nullable = true)
    private User user;

    @Column(name = "totals_numberorder")
    private Integer totalsNumberOrder;


    // ràng buộc nvarchar(255)
    @Column(columnDefinition = "NVARCHAR(255)")
    private String vehicle;

  
    private String cccd;
  
   @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    private String historyOrder;

    Boolean status ; // Trạng thái shipper, mặc định là true (hoạt động)

    
}
