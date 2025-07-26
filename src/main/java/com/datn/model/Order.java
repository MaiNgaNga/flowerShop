package com.datn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.Data;
import lombok.ToString;
import java.util.Date;
import java.util.List;

@Data
@ToString(exclude = { "orderDetails" }) 
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id", nullable = false)
    private User user;
    // Người giao hàng
    @ManyToOne
    @JoinColumn(name = "shipperId")
    private User shipper;

    @Temporal(TemporalType.DATE)
    private Date createDate;
    @Column
    private String sdt;

    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column
    private Double totalAmount;

    @Column(name = "order_type", columnDefinition = "NVARCHAR(50)")
    private String orderType;

    @Column(name = "status", columnDefinition = "NVARCHAR(255)")
    private String status;

    @Column(name = "order_code", columnDefinition = "NVARCHAR(50)")
    private String orderCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;
    
    @ManyToOne
    @JoinColumn(name = "promotion_id", referencedColumnName = "id")
    private Promotion promotion;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "delivery_date")
    private Date deliveryDate;

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;
}
