package com.datn.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "wards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ward_id")
    private Long id;

    @NotBlank(message = "Tên xã/phường không được để trống")
    @Column(name = "ward_name", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String name;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;
}

