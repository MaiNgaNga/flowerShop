package com.datn.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
    @Column(name = "id", unique = true, nullable = false, columnDefinition = "VARCHAR(20)")
    private String id;

    @Column(name = "title", columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "discount_type") // e.g., "percent" or "amount"
    private String discountType;

    @Column(name = "discount_value")
    private Double discountValue;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "start_date")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "end_date")
    private LocalDate endDate;


    private Boolean status; // true = active, false = inactive

    @Column(name = "created_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

   @Column(name = "use_count")
   private Integer useCount;

    // @OneToMany(mappedBy = "promotion")
    // private List<Order> orders;

    
}
