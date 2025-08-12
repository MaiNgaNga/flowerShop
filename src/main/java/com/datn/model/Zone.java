package com.datn.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Long id;

    @NotBlank(message = "Tên zone không được để trống")
    @Column(name = "zone_name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Phí vận chuyển không được null")
    @Min(value = 0, message = "Phí vận chuyển phải >= 0")
    @Column(name = "ship_fee", nullable = false)
    private Double shipFee;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Ward> wards;
}

