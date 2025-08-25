package com.datn.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
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
    
    //nếu là online thì là người đặt, ofline thì là nhân viên
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id", nullable = false)
    private User user;
    // Người giao hàng
    @ManyToOne
    @JoinColumn(name = "shipperId")
    private User shipper;

    // mapping với comments
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> comments;
    
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Column
    private String sdt;

    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column
    private Double shipFee;

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
    private LocalDate deliveryDate;

     @Temporal(TemporalType.TIME)
    @Column(name = "delivery_time")
    private LocalTime deliveryTime;
    

    // Người nhận hàng
    @Column(name = "receiver_name", columnDefinition = "NVARCHAR(100)")
    private String receiverName;

    // Số điện thoại người nhận
    @Column(name = "receiver_phone", columnDefinition = "NVARCHAR(20)")
    private String receiverPhone;
    
    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "reason", columnDefinition = "NVARCHAR(500)")
    private String reason;

    @Column(name = "original_id")
    private Long originalId;

    @Column(name = "payment_method", columnDefinition = "NVARCHAR(50)")
    private String paymentMethod; // "COD" hoặc "PAYOS"

    @Column(name = "payment_status", columnDefinition = "NVARCHAR(50)")
    private String paymentStatus; // "PENDING", "PAID", "CANCELLED", "FAILED"

    @Column(name = "payment_url", columnDefinition = "NVARCHAR(500)")
    private String paymentUrl; // URL thanh toán PayOS

    @Column(name = "transaction_id", columnDefinition = "NVARCHAR(100)")
    private String transactionId; // ID giao dịch từ PayOS

}
