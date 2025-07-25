package com.datn.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Entity
@Builder
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "NVARCHAR(55)", nullable = false)
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 55, message = "Tên sản phẩm tối đa 55 ký tự")
    private String name;

    @Column(columnDefinition = "NVARCHAR(255)")
    @Size(max = 255, message = "Mô tả sản phẩm tối đa 255 ký tự")
    private String description;

    @Positive(message = "Giá sản phẩm phải lớn hơn 0")
    private double price;

    @Column
    @PositiveOrZero(message = "Số lượng không được âm")
    private Integer quantity;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String image_url;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String image_url2;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String image_url3;

    @ManyToOne
    @JoinColumn(name = "Category_Id", referencedColumnName = "id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "color_Id", referencedColumnName = "id")
    private Color color;

    @ManyToOne
    @JoinColumn(name = "product_Category_Id", referencedColumnName = "id") // Tạo FK
    private ProductCategory productCategory;

    // Thêm các trường giảm giá
    @Min(value = 0, message = "Phần trăm giảm giá không được nhỏ hơn 0%")
    @Max(value = 100, message = "Phần trăm giảm giá không được lớn hơn 100%")
    @Column
    private Integer discountPercent;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column
    private LocalDate discountStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column
    private LocalDate discountEnd;

    @Column(nullable = false)
        private Boolean available = true; // true: còn hàng, false: hết hàng

    public double getPriceAfterDiscount() {
        if (discountPercent != null && discountPercent > 0) {
            LocalDate now = LocalDate.now();
            if ((discountStart == null || !now.isBefore(discountStart)) &&
                    (discountEnd == null || !now.isAfter(discountEnd))) {
                return price * (1 - discountPercent / 100.0);
            }
        }
        return price;
    }

}
